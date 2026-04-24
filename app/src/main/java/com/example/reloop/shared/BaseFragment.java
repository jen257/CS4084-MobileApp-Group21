package com.example.reloop.shared;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.viewbinding.ViewBinding;

/**
 * Base Fragment class that provides common functionality for all fragments
 * Uses generics for ViewBinding and ViewModel
 */
public abstract class BaseFragment<VB extends ViewBinding, VM extends ViewModel> extends Fragment {

    protected VB binding;
    protected VM viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = createViewBinding(inflater, container);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = createViewModel();
        initializeViews();
        setupObservers();
        setupListeners();
    }

    // Abstract methods that child classes must implement
    protected abstract VB createViewBinding(LayoutInflater inflater, ViewGroup container);
    protected abstract VM createViewModel();
    protected abstract void initializeViews();
    protected abstract void setupObservers();
    protected abstract void setupListeners();

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
