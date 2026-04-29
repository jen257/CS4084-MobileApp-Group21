package com.example.reloop.shared.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class ThemeSwitchButton extends SwitchMaterial {

    public ThemeSwitchButton(@NonNull Context context) {
        super(context);
        init();
    }

    public ThemeSwitchButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ThemeSwitchButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setText("Dark Mode");
        setTextSize(18f);
        setTextColor(0xFF555555); // Hex equivalent for #555555
        setPadding(0, 32, 0, 32);
    }

    public void toggleThemeState(boolean isDarkTheme) {
        setChecked(isDarkTheme);
    }
}