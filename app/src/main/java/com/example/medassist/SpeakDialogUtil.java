package com.example.medassist;

import android.app.AlertDialog;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SpeakDialogUtil {

    // Show dialog and read full description in chunks
    public static void showAndSpeak(Context ctx, Medicine med, boolean includeFoundPrefix) {
        final TextToSpeech[] ttsHolder = {null};

        // Dialog UI (reuse dialog_medicine_result.xml)
        View v = View.inflate(ctx, R.layout.dialog_medicine_result, null);
        TextView tvName = v.findViewById(R.id.tvMedName);
        TextView tvDesc = v.findViewById(R.id.tvMedDesc);
        ImageView img = v.findViewById(R.id.imgMedicine);

        tvName.setText(safe(med.getName()));
        tvDesc.setText(safe(med.getDescription()));
        String imageUrl = med.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(ctx).load(imageUrl).into(img);
        }

        // TTS Initialization
        ttsHolder[0] = new TextToSpeech(ctx.getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                TextToSpeech tts = ttsHolder[0];
                if (tts != null) {
                    tts.setLanguage(Locale.ENGLISH);
                    tts.setSpeechRate(0.9f);

                    // ✅ Conditional intro (for scan vs list)
                    String intro = safe(med.getName());
                    if (includeFoundPrefix) intro = "I found " + intro;
                    if (!intro.isEmpty()) intro = intro + ". ";

                    tts.speak(intro, TextToSpeech.QUEUE_FLUSH, null, "intro");
                    for (String chunk : chunkText(safe(med.getDescription()), 3800)) {
                        tts.speak(chunk, TextToSpeech.QUEUE_ADD, null, "desc");
                    }
                    tts.speak(" Please consult a licensed doctor for dosage and safety.",
                            TextToSpeech.QUEUE_ADD, null, "safety");
                }
            }
        });

        new AlertDialog.Builder(ctx)
                .setView(v)
                .setOnDismissListener(d -> {
                    TextToSpeech tts = ttsHolder[0];
                    if (tts != null) {
                        tts.stop();
                        tts.shutdown();
                    }
                })
                .setPositiveButton("Close", (d, w) -> d.dismiss())
                .show();
    }

    // Helper methods
    private static String safe(String s) { return s == null ? "" : s; }

    private static List<String> chunkText(String text, int maxLen) {
        ArrayList<String> parts = new ArrayList<>();
        if (text == null) return parts;
        String t = text.trim();
        while (t.length() > maxLen) {
            int cut = t.lastIndexOf('.', maxLen);
            if (cut < 0) cut = t.lastIndexOf(' ', maxLen);
            if (cut < 0) cut = maxLen;
            parts.add(t.substring(0, Math.min(cut + 1, t.length())).trim());
            t = t.substring(Math.min(cut + 1, t.length())).trim();
        }
        if (!t.isEmpty()) parts.add(t);
        return parts;
    }
}
