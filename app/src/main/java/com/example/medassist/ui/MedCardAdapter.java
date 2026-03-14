package com.example.medassist.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import com.example.medassist.R;

public class MedCardAdapter extends ArrayAdapter<String> {
    private final List<String> subtitles;
    private final List<String> footers;
    private final List<Integer> imageRes; // nullable

    public MedCardAdapter(Context ctx, List<String> names, List<String> subtitles, List<String> footers, List<Integer> imageRes) {
        super(ctx, R.layout.item_med_card, R.id.med_name, names);
        this.subtitles = subtitles;
        this.footers = footers;
        this.imageRes = imageRes;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        TextView name = v.findViewById(R.id.med_name);
        TextView sub = v.findViewById(R.id.med_short_use);
        TextView foot = v.findViewById(R.id.med_price);
        ImageView img = v.findViewById(R.id.med_image);

        String s = (subtitles != null && position < subtitles.size()) ? subtitles.get(position) : "";
        String f = (footers != null && position < footers.size()) ? footers.get(position) : "";
        sub.setText(s == null ? "" : s);
        foot.setText(f == null ? "" : f);

        if (imageRes != null && position < imageRes.size() && imageRes.get(position) != null) {
            img.setImageResource(imageRes.get(position));
        } else {
            img.setImageResource(R.mipmap.ic_launcher);
        }
        return v;
    }
}
