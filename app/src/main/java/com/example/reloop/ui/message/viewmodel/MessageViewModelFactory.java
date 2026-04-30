package com.example.reloop.ui.message.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.reloop.repository.MessageRepository;

public class MessageViewModelFactory implements ViewModelProvider.Factory {
    private final MessageRepository repository;

    public MessageViewModelFactory(MessageRepository repository) {
        this.repository = repository;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ChatViewModel.class)) {
            return (T) new ChatViewModel(repository);
        } else if (modelClass.isAssignableFrom(ConversationViewModel.class)) {
            return (T) new ConversationViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}