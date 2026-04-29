package com.example.reloop.ui.message.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.reloop.models.Conversation;
import com.example.reloop.repository.MessageRepository;
import java.util.List;

public class ConversationViewModel extends ViewModel {
    private MessageRepository messageRepository;
    private MutableLiveData<List<Conversation>> conversations = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();

    public ConversationViewModel() {
        this.isLoading = new MutableLiveData<>();
        this.error = new MutableLiveData<>();
    }
    public void loadUserConversations(String userId) {
        isLoading.setValue(true);
        // Implementation would connect to repository
        // For now, using mock data
        isLoading.setValue(false);
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