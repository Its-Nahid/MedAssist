package com.example.medassist;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    // User's API Key - THIS MUST BE REGENERATED
    private final String GEMINI_API_KEY = "AIzaSyAixddjqgBCppGSAM-KCovJZSFJuVU9GW8";

    private FrameLayout btnMic;
    private TextView tvUserMessage;
    private TextView tvBotMessage;
    private TextView tvListeningStatus;

    private SpeechRecognizer speechRecognizer;
    private TextToSpeech textToSpeech;
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Correctly map the views from the XML layout
        btnMic = findViewById(R.id.micContainer);
        tvUserMessage = findViewById(R.id.tvLiveQuote);
        tvBotMessage = findViewById(R.id.tvAssistant);
        tvListeningStatus = findViewById(R.id.tvListening);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        initializeTextToSpeech();
        initializeSpeechRecognizer();

        btnMic.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
            } else {
                startListening();
            }
        });
    }

    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Language not supported");
                }
            } else {
                Log.e(TAG, "TTS initialization failed");
            }
        });
    }

    private void initializeSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                tvListeningStatus.setText("Listening...");
                tvListeningStatus.setVisibility(View.VISIBLE);
                tvUserMessage.setVisibility(View.GONE);
                tvBotMessage.setVisibility(View.GONE);
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String query = matches.get(0);
                    tvUserMessage.setText(query);
                    tvUserMessage.setVisibility(View.VISIBLE);
                    tvListeningStatus.setText("Tap the mic to speak");
                    getResponseFromGemini(query);
                }
            }

            @Override
            public void onError(int error) {
                Log.e(TAG, "Speech recognition error: " + error);
                tvListeningStatus.setText("Tap the mic to speak");
            }

            // Other methods are not used but required
            @Override
            public void onBeginningOfSpeech() {}
            @Override
            public void onRmsChanged(float rmsdB) {}
            @Override
            public void onBufferReceived(byte[] buffer) {}
            @Override
            public void onEndOfSpeech() {}
            @Override
            public void onPartialResults(Bundle partialResults) {}
            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }

    private void startListening() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now");
        speechRecognizer.startListening(intent);
    }

    private void speak(CharSequence text) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    private void getResponseFromGemini(String query) {
        // Correct regional endpoint for asia-east1
        String url = "https://asia-east1-generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + GEMINI_API_KEY;
        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        JSONObject jsonBody = new JSONObject();
        try {
            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            content.put("role", "user"); // Explicitly set the role

            JSONArray parts = new JSONArray();
            JSONObject textPart = new JSONObject();
            textPart.put("text", query);
            parts.put(textPart);

            content.put("parts", parts);
            contents.put(content);
            jsonBody.put("contents", contents);
        } catch (JSONException e) {
            Log.e(TAG, "JSON Exception", e);
            runOnUiThread(() -> tvBotMessage.setText("Error creating request."));
            return;
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        tvBotMessage.setText("Thinking...");
        tvBotMessage.setVisibility(View.VISIBLE);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "OkHttp Error", e);
                runOnUiThread(() -> {
                    tvBotMessage.setText("Server error. Please check your connection or API key.");
                    speak("I encountered a server error. Please try again later.");
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    final String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    Log.e(TAG, "API Error: " + response.code() + " " + errorBody);
                    runOnUiThread(() -> {
                        tvBotMessage.setText("API Error: " + response.code());
                        speak("I encountered an API error.");
                    });
                    return;
                }

                try {
                    String responseBody = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseBody);

                    String botResponse = jsonResponse.getJSONArray("candidates")
                                                     .getJSONObject(0)
                                                     .getJSONObject("content")
                                                     .getJSONArray("parts")
                                                     .getJSONObject(0)
                                                     .getString("text");

                    runOnUiThread(() -> {
                        tvBotMessage.setText(botResponse);
                        speak(botResponse);
                    });
                } catch (JSONException | NullPointerException e) {
                    Log.e(TAG, "JSON Parsing Error", e);
                    runOnUiThread(() -> {
                        tvBotMessage.setText("Error parsing server response.");
                        speak("I had trouble understanding the server's response.");
                    });
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startListening();
            } else {
                Toast.makeText(this, "Microphone permission is required to use the voice assistant.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
