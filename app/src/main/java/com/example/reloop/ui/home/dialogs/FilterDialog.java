package com.example.reloop.ui.home.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.reloop.R;

/**
 * DialogFragment for advanced product filtering capabilities (Price & Distance).
 */
public class FilterDialog extends DialogFragment {

    private EditText etMinPrice;
    private EditText etMaxPrice;
    private SeekBar seekBarDistance;
    private TextView tvDistanceValue;

    private FilterListener listener;
    private int selectedDistance = 10; // default 10km
    public interface FilterListener {
        void onFilterApplied(Double minPrice, Double maxPrice, int maxDistanceKm);
    }

    public void setFilterListener(FilterListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_filter, container, false);

        etMinPrice = view.findViewById(R.id.etMinPrice);
        etMaxPrice = view.findViewById(R.id.etMaxPrice);
        seekBarDistance = view.findViewById(R.id.seekBarDistance);
        tvDistanceValue = view.findViewById(R.id.tvDistanceValue);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnApply = view.findViewById(R.id.btnApply);

        tvDistanceValue.setText(selectedDistance + " km");

        seekBarDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Prevent distance from dropping to 0km; minimum limit is 1km
                selectedDistance = Math.max(progress, 1);
                tvDistanceValue.setText(selectedDistance + " km");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Handle cancel action
        btnCancel.setOnClickListener(v -> dismiss());

        // Handle apply action
        btnApply.setOnClickListener(v -> {
            if (listener != null) {
                Double min = parseDoubleOrNull(etMinPrice.getText().toString());
                Double max = parseDoubleOrNull(etMaxPrice.getText().toString());
                listener.onFilterApplied(min, max, selectedDistance);
            }
            dismiss();
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            // Adjust dialog width to match the screen width with margins
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    private Double parseDoubleOrNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}