package com.example.reloop.ui.message.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.reloop.models.Conversation;
import com.example.reloop.repository.MessageRepository;
import java.util.List;

public class ConversationViewModel extends ViewModel {
    private MessageRepository messageRepository;
    private MediatorLiveData<List<Conversation>> conversations = new MediatorLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();
    private LiveData<List<Conversation>> repositorySource;

    public ConversationViewModel(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public void loadUserConversations(String userId) {
        isLoading.setValue(true);

        if (repositorySource != null) {
            conversations.removeSource(repositorySource);
        }

        repositorySource = messageRepository.getUserConversations(userId);
        conversations.addSource(repositorySource, newConversations -> {
            conversations.setValue(newConversations);
            isLoading.setValue(false);
        });
    }

    public LiveData<List<Conversation>> getConversations() {
        return conversations;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }
}