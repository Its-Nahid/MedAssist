package com.example.medassist;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScanMedicineActivity extends AppCompatActivity {
    private static final String TAG = "ScanMed";

    private PreviewView previewView;
    private View scanWindow;
    private ImageButton btnShutter, btnClose;
    private MaterialButton btnUpload;
    private TextView tvTitle, tvSubtitle;

    private ImageCapture imageCapture;
    private Executor cameraExecutor;

    // alias/name(lowercase) -> docId
    private final Map<String, String> nameToDocId = new HashMap<>();
    private static final Map<String, String> DEFAULT_ALIASES = new HashMap<>();
    static {
        // Quick wins so "NAPA" works even if Firestore lacks aliases
        DEFAULT_ALIASES.put("napa", "Paracetamol");
        DEFAULT_ALIASES.put("acetaminophen", "Paracetamol");
    }

    private final ActivityResultLauncher<String> reqCamPerm =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) startCamera();
                else Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        previewView = findViewById(R.id.previewView);
        scanWindow = findViewById(R.id.scanWindow);
        btnShutter = findViewById(R.id.btnShutter);
        btnUpload  = findViewById(R.id.btnUpload);
        btnClose   = findViewById(R.id.btnClose);
        tvTitle    = findViewById(R.id.tvTitle);
        tvSubtitle = findViewById(R.id.tvSubtitle);

        cameraExecutor = Executors.newSingleThreadExecutor();

        FirebaseFirestore.getInstance().setFirestoreSettings(
                new FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true).build()
        );

        preloadMedicineNames();

        btnShutter.setOnClickListener(v -> captureFromPreview());
        if (btnClose != null) btnClose.setOnClickListener(v -> finish());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            reqCamPerm.launch(Manifest.permission.CAMERA);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                CameraSelector selector = CameraSelector.DEFAULT_BACK_CAMERA;
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, selector, preview, imageCapture);
            } catch (Exception e) {
                Log.e(TAG, "Camera start failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void preloadMedicineNames() {
        FirebaseFirestore.getInstance()
                .collection("medicines")
                .get()
                .addOnSuccessListener(snapshot -> {
                    Map<String, String> nameToIdByCanonical = new HashMap<>();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String id = doc.getId();
                        String displayName = doc.getString("name");
                        String canonical = (displayName != null && !displayName.isEmpty()) ? displayName : id;
                        String canonicalLower = canonical.toLowerCase(Locale.ROOT);

                        nameToDocId.put(canonicalLower, id);
                        nameToIdByCanonical.put(canonical, id);

                        Object aliases = doc.get("aliases");
                        if (aliases instanceof List<?>) {
                            for (Object a : (List<?>) aliases) {
                                if (a != null) nameToDocId.put(a.toString().toLowerCase(Locale.ROOT), id);
                            }
                        }
                    }

                    // Bind default aliases -> actual doc ids, by canonical
                    for (Map.Entry<String, String> e : DEFAULT_ALIASES.entrySet()) {
                        String alias = e.getKey();
                        String canonical = e.getValue();
                        for (Map.Entry<String,String> c : nameToIdByCanonical.entrySet()) {
                            if (c.getKey().equalsIgnoreCase(canonical)) {
                                nameToDocId.put(alias.toLowerCase(Locale.ROOT), c.getValue());
                                break;
                            }
                        }
                    }
                    Log.d(TAG, "Indexed " + nameToDocId.size() + " names/aliases");
                });
    }

    private void captureFromPreview() {
        tvSubtitle.setText("Reading text…");
        btnShutter.setEnabled(false);

        Bitmap frame = previewView.getBitmap();
        if (frame == null) { resetUi(); return; }
        Bitmap cropped = cropToView(frame, scanWindow, previewView);
        runOcr(cropped);
    }

    private void runOcr(Bitmap bitmap) {
        try {
            InputImage img = InputImage.fromBitmap(bitmap, 0);
            TextRecognition.getClient(new TextRecognizerOptions.Builder().build())
                    .process(img)
                    .addOnSuccessListener(result -> {
                        String text = result.getText();
                        bitmap.recycle();
                        onOcrText(text);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "OCR failed", e);
                        bitmap.recycle();
                        resetUi();
                        Toast.makeText(this, "Couldn't read text", Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            Log.e(TAG, "OCR exception", e);
            bitmap.recycle();
            resetUi();
        }
    }

    private void onOcrText(String ocrText) {
        String haystack = normalizeForSearch(ocrText == null ? "" : ocrText);
        Log.d(TAG, "OCR normalized: " + haystack);

        String[] tokens = haystack.split("\\s+");
        String matchedDocId = null;

        // 1) token-level exact/alias
        for (String t : tokens) {
            if (t.length() < 2) continue;
            String id = nameToDocId.get(t);
            if (id != null) { matchedDocId = id; break; }
        }

        // 2) word-boundary whole text
        if (matchedDocId == null) {
            int bestPos = Integer.MAX_VALUE;
            for (String key : nameToDocId.keySet()) {
                Matcher m = Pattern.compile("\\b" + Pattern.quote(key) + "\\b").matcher(haystack);
                if (m.find()) {
                    int pos = m.start();
                    if (pos < bestPos) { bestPos = pos; matchedDocId = nameToDocId.get(key); }
                }
            }
        }

        // 3) tiny fuzzy fallback (<=2)
        if (matchedDocId == null) {
            int bestDist = Integer.MAX_VALUE; String bestKey = null;
            for (String t : tokens) {
                for (String key : nameToDocId.keySet()) {
                    int d = editDistance(t, key);
                    if (d < bestDist) { bestDist = d; bestKey = key; }
                }
            }
            if (bestKey != null && bestDist <= 2) {
                matchedDocId = nameToDocId.get(bestKey);
            }
        }

        if (matchedDocId == null) {
            resetUi();
            new AlertDialog.Builder(this)
                    .setTitle("No medicine found")
                    .setMessage("I couldn’t identify a medicine name. Try a clearer label or different angle, then scan again.")
                    .setPositiveButton("OK", null).show();
            return;
        }

        FirebaseFirestore.getInstance().collection("medicines").document(matchedDocId)
                .get()
                .addOnSuccessListener(this::showResultAndSpeak)
                .addOnFailureListener(e -> {
                    resetUi();
                    Toast.makeText(this, "Failed to fetch medicine", Toast.LENGTH_SHORT).show();
                });
    }

    private void showResultAndSpeak(DocumentSnapshot doc) {
        resetUi();

        // Convert Firestore doc to your model
        Medicine med = toMedicine(doc);

        // Use shared dialog + TTS; keep "I found ..." for scanner
        SpeakDialogUtil.showAndSpeak(this, med, /* includeFoundPrefix = */ true);
    }

    private void resetUi() {
        if (btnShutter != null) btnShutter.setEnabled(true);
        if (tvSubtitle != null) tvSubtitle.setText("Tap to scan a medicine");
    }

    // --- Helpers ---

    private static String normalizeForSearch(String text) {
        String t = (text == null) ? "" : text.toLowerCase(Locale.ROOT);
        t = t.replaceAll("[^a-z0-9\\s]+", " ");
        t = t.replaceAll("\\s+", " ").trim();
        return t;
    }

    private static int editDistance(String a, String b) {
        int n = a.length(), m = b.length();
        int[][] dp = new int[n+1][m+1];
        for (int i=0; i<=n; i++) dp[i][0]=i;
        for (int j=0; j<=m; j++) dp[0][j]=j;
        for (int i=1; i<=n; i++) {
            char ca=a.charAt(i-1);
            for (int j=1; j<=m; j++) {
                int cost = (ca==b.charAt(j-1))?0:1;
                dp[i][j] = Math.min(Math.min(dp[i-1][j]+1, dp[i][j-1]+1), dp[i-1][j-1]+cost);
            }
        }
        return dp[n][m];
    }

    private static Bitmap cropToView(Bitmap source, View targetView, View container) {
        int[] cLoc = new int[2]; container.getLocationOnScreen(cLoc);
        int[] fLoc = new int[2]; targetView.getLocationOnScreen(fLoc);

        float left = fLoc[0] - cLoc[0];
        float top = fLoc[1] - cLoc[1];
        float right = left + targetView.getWidth();
        float bottom = top + targetView.getHeight();

        RectF viewRect = new RectF(0, 0, container.getWidth(), container.getHeight());
        RectF bmpRect = new RectF(0, 0, source.getWidth(), source.getHeight());

        Matrix m = new Matrix();
        m.setRectToRect(viewRect, bmpRect, Matrix.ScaleToFit.CENTER);
        float[] pts = new float[]{left, top, right, bottom};
        m.mapPoints(pts);

        int x = Math.max(0, Math.round(Math.min(pts[0], pts[2])));
        int y = Math.max(0, Math.round(Math.min(pts[1], pts[3])));
        int w = Math.min(source.getWidth() - x, Math.round(Math.abs(pts[2] - pts[0])));
        int h = Math.min(source.getHeight() - y, Math.round(Math.abs(pts[3] - pts[1])));

        if (w <= 0 || h <= 0) return source;
        try {
            return Bitmap.createBitmap(source, x, y, w, h);
        } catch (Exception e) {
            Log.e(TAG, "Failed to create cropped bitmap.", e);
            return source;
        }
    }

    private Medicine toMedicine(DocumentSnapshot doc) {
        Medicine m = new Medicine();
        m.setMedicineId(doc.getId());
        m.setName(doc.getString("name"));
        m.setDescription(doc.getString("description"));
        m.setShortUse(doc.getString("shortUse"));
        m.setSideEffects(doc.getString("sideEffects"));
        m.setPrecautions(doc.getString("precautions"));
        m.setImageUrl(doc.getString("imageUrl"));

        Object priceObject = doc.get("price");
        if (priceObject instanceof Number) {
            m.setPrice(String.valueOf(((Number) priceObject).longValue()));
        } else {
            m.setPrice((String) priceObject);
        }
        return m;
    }
}
