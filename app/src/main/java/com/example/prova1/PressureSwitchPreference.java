package com.example.prova1;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import androidx.preference.PreferenceViewHolder;
import androidx.preference.SwitchPreferenceCompat;

public class PressureSwitchPreference extends SwitchPreferenceCompat {

    public PressureSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public PressureSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PressureSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PressureSwitchPreference(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        // Trova il TextView del testo informativo e attiva il marquee
        View marqueeText = holder.findViewById(R.id.pressure_info_text);
        if (marqueeText instanceof TextView) {
            marqueeText.setSelected(true);
        }
    }
}
