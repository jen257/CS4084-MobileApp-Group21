package com.example.reloop.ui.message.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.reloop.models.Message;
import com.example.reloop.repository.MessageRepository;
import java.util.List;

public class ChatViewModel extends ViewModel {
    private MessageRepository messageRepository;
    private MediatorLiveData<List<Message>> messages = new MediatorLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();
    private LiveData<List<Message>> repositorySource;

    public ChatViewModel(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public void loadMessages(String conversationId) {
        isLoading.setValue(true);

        // Remove old source if reloading a different conversation
        if (repositorySource != null) {
            messages.removeSource(repositorySource);
        }

        // Connect directly to the repository's real-time LiveData
        repositorySource = messageRepository.getMessages(conversationId);
        messages.addSource(repositorySource, newMessages -> {
            messages.setValue(newMessages);
            isLoading.setValue(false);
        });
    }

    public void sendMessage(Message message) {
        messageRepository.sendMessage(message);
    }

    public void markMessagesAsRead(String conversationId, String currentUserId) {
        messageRepository.markMessagesAsRead(conversationId, currentUserId);
    }

    public LiveData<List<Message>> getMessages() {
        return messages;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }
}