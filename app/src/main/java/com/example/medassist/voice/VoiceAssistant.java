package com.example.medassist.voice;

import android.app.Activity;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import com.example.medassist.data.Db;
import com.example.medassist.reminders.AlarmHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VoiceAssistant {
    private final Activity act; private final Db db;
    private TextToSpeech tts; private static final int REQ=2025;

    // Defaults
    private static final String DEFAULT_FREQUENCY = "Daily";
    private static final String DEFAULT_DOSAGE = "500 mg"; // New default when user omits dosage
    private static final int DEFAULT_STOCK = 1; // Keep existing default

    // Precompiled patterns (order matters: most specific first)
    private static final Pattern P_ADD_MEDICINE = Pattern.compile(
            "(?:add|set)(?: medicine)?\\s+" +                   // add / add medicine / set medicine
            "(?<name>[a-z0-9 .\\'-]+?)\\s*" +                   // medicine name (relaxed)
            "(?:(?<dosage>\\d+\\s*(?:mg|milligram|g|ml|pill(?:s)?|tablet(?:s)?))\\s*)?" + // optional dosage
            "(?:at|@)\\s+(?<times>.+?)" +                        // required times after 'at'
            "(?:\\s+(?:with|and)\\s+(?<stock>\\d+)\\s+stock)?\\s*$", // optional stock
            Pattern.CASE_INSENSITIVE);

    private static final Pattern P_CANCEL = Pattern.compile("(?:cancel|remove|delete).*(?:reminder\\s+)?for\\s+(?<name>.+)", Pattern.CASE_INSENSITIVE);

    private static final Pattern P_STOCK_QUERY = Pattern.compile( "(?:stock|how (?:much|many) stock).*(?:for|of)\\s+(?<name>.+)", Pattern.CASE_INSENSITIVE);

    private static final Pattern P_ADD_STOCK = Pattern.compile("(?:add|increase)\\s+(?<amount>\\d+)\\s+stock\\s+(?:to|for|of)\\s+(?<name>.+)", Pattern.CASE_INSENSITIVE);

    public VoiceAssistant(Activity a, Db db){
        this.act=a; this.db=db;
        tts = new TextToSpeech(a, s -> { if(s==TextToSpeech.SUCCESS) tts.setLanguage(Locale.getDefault()); });
    }

    public void start(){
        speak("How can I help you?");
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        act.startActivityForResult(i, REQ);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode!=REQ || data==null) return;
        ArrayList<String> res = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        if(res==null || res.isEmpty()) return;
        handle(res.get(0).toLowerCase(Locale.getDefault()));
    }

    private void handle(String cmd){
        // ADD MEDICINE (simpler: dosage & stock are optional; defaults kick in)
        Matcher addMed = P_ADD_MEDICINE.matcher(cmd);
        if(addMed.find()){
            String name = safeTrim(addMed.group("name"));
            String dosage = safeTrim(addMed.group("dosage"));
            if (isEmpty(dosage)) dosage = DEFAULT_DOSAGE; // <-- new default 500 mg

            String timesRaw = safeTrim(addMed.group("times"));
            String timesCsv = normalizeTimes(timesRaw);
            if (isEmpty(timesCsv)) { speak("Sorry, I couldn't understand the time."); return; }

            int stock = DEFAULT_STOCK;
            String stockStr = safeTrim(addMed.group("stock"));
            if(!isEmpty(stockStr)){
                try { stock = Integer.parseInt(stockStr); } catch (NumberFormatException ignored) { stock = DEFAULT_STOCK; }
            }

            long medId = db.addMedicine(name, dosage, DEFAULT_FREQUENCY, timesCsv, stock);
            for(String t : timesCsv.split(",")){
                int reqCode = (name + "|" + medId + "|" + t.trim()).hashCode(); // unique per reminder
                AlarmHelper.scheduleDaily(act, reqCode, (int)medId, name, t.trim());
            }
            speak("Added " + name + " at " + humanizeTimes(timesCsv) + ". Dosage " + dosage + ", stock " + stock + ".");
            return;
        }

        // CANCEL REMINDERS
        Matcher cancel = P_CANCEL.matcher(cmd);
        if(cancel.find()){
            String name = safeTrim(cancel.group("name"));
            int medId = db.findMedIdByName(name);
            if(medId!=-1){ db.deleteRemindersForMedicine(medId); speak("Canceled reminders for "+name); }
            else speak("I couldn't find " + name);
            return;
        }

        // STOCK QUERY
        Matcher q = P_STOCK_QUERY.matcher(cmd);
        if(q.find()){
            String name = safeTrim(q.group("name"));
            int medId = db.findMedIdByName(name);
            if(medId!=-1){ int s = db.getStock(medId); speak(name + " stock is " + s); } else speak("I couldn't find " + name);
            return;
        }

        // ADD STOCK
        Matcher mAddStock = P_ADD_STOCK.matcher(cmd);
        if(mAddStock.find()){
            int amount;
            try { amount = Integer.parseInt(safeTrim(mAddStock.group("amount"))); }
            catch (NumberFormatException e) { speak("Please say a number for stock."); return; }
            String name = safeTrim(mAddStock.group("name"));
            int medId = db.findMedIdByName(name);
            if(medId!=-1){ db.addStockLog(medId, amount); speak("Added "+amount+" to "+name); } else speak("I couldn't find " + name);
            return;
        }

        // BASIC HEALTH Q&A (generic, non-diagnostic)
        if(cmd.contains("dizzy") || cmd.contains("dizziness")){
            speak("If you're feeling dizzy, sit or lie down, hydrate, and avoid driving. If it persists or you faint, seek medical care.");
            return;
        }
        if(cmd.contains("how much water")){
            speak("General guidance is around two to three liters per day for healthy adults. Adjust for heat and activity.");
            return;
        }
        Matcher what = Pattern.compile("what is (.+) used for", Pattern.CASE_INSENSITIVE).matcher(cmd);
        if(what.find()){
            String med = what.group(1);
            speak(med + " is commonly used for pain or fever. For specific advice, please consult your doctor.");
            return;
        }

        speak("Sorry, I didn't get that. Try: add medicine napa at 8 am.");
    }

    // Robust time parsing: supports 8, 8:30, 20, 8am, 8 pm, noon, midnight, and comma/and separated lists
    private String normalizeTimes(String spoken){
        if (spoken == null) return "";
        String s = spoken.toLowerCase(Locale.getDefault());
        // Split on commas or 'and'
        String[] parts = s.split("(?:,|\\band\\b)+");
        List<String> out = new ArrayList<>();
        for(String raw : parts){
            String t = to24h(raw.trim());
            if(t != null) out.add(t);
        }
        return String.join(",", out);
    }

    private String to24h(String token){
        if (isEmpty(token)) return null;
        token = token.trim().toLowerCase(Locale.getDefault());
        if (token.equals("noon")) return "12:00";
        if (token.equals("midnight")) return "00:00";

        // allow 'at ' prefix
        token = token.replaceFirst("^at\\s+", "");

        Matcher m = Pattern.compile("^(\\d{1,2})(?::(\\d{1,2}))?\\s*(am|pm)?$").matcher(token);
        if(!m.find()) return null;
        int h = parseIntSafe(m.group(1), -1);
        int min = parseIntSafe(m.group(2), 0);
        if (h < 0 || h > 23 || min < 0 || min > 59) return null;

        String ap = m.group(3);
        if (ap != null) {
            if (ap.equals("pm") && h < 12) h += 12;
            if (ap.equals("am") && h == 12) h = 0;
        }
        return String.format(Locale.US, "%02d:%02d", h, min);
    }

    private int parseIntSafe(String s, int def){
        if (s == null) return def;
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return def; }
    }

    private String humanizeTimes(String csv){
        if (isEmpty(csv)) return "";
        String[] t = csv.split(",");
        if (t.length == 1) return t[0];
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<t.length;i++){
            if(i>0) sb.append(i==t.length-1? " and ": ", ");
            sb.append(t[i]);
        }
        return sb.toString();
    }

    private String safeTrim(String s){ return s==null? "" : s.trim(); }
    private boolean isEmpty(String s){ return s==null || s.trim().isEmpty(); }

    private void speak(String s){ Toast.makeText(act,s,Toast.LENGTH_SHORT).show(); tts.speak(s, TextToSpeech.QUEUE_FLUSH, null, "VA"); }

    // Call this from your Activity.onDestroy()
    public void shutdownTts(){
        try { if (tts != null) { tts.stop(); tts.shutdown(); } } catch (Throwable ignored) {}
    }
}
