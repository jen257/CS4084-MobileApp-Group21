package com.example.reloop.shared.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.reloop.databinding.ViewLoadingButtonBinding;

/**
 * Custom button with loading state functionality
 * Shows a progress indicator when loading
 */
@SuppressWarnings("unused")
public class LoadingButton extends FrameLayout {

    private ViewLoadingButtonBinding binding;
    private String originalText;
    private boolean isLoading = false;

    public LoadingButton(@NonNull Context context) {
        super(context);
        init(context);
    }

    public LoadingButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LoadingButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        binding = ViewLoadingButtonBinding.inflate(LayoutInflater.from(context), this, true);
        setClickable(true);
        setFocusable(true);
    }

    /**
     * Set button text
     */
    public void setText(String text) {
        originalText = text;
        if (!isLoading) {
            binding.buttonText.setText(text);
        }
    }

    /**
     * Set loading state
     * When loading, shows progress bar and disables button
     */
    public void setLoading(boolean loading) {
        this.isLoading = loading;

        if (loading) {
            binding.buttonText.setText("");
            binding.progressBar.setVisibility(VISIBLE);
            setEnabled(false);
        } else {
            binding.buttonText.setText(originalText);
            binding.progressBar.setVisibility(GONE);
            setEnabled(true);
        }
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setOnButtonClickListener(OnClickListener listener) {
        binding.getRoot().setOnClickListener(listener);
    }
}
