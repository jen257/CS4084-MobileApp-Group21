package com.example.reloop.ui.message;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.reloop.R;
import com.example.reloop.models.Message;
import com.example.reloop.ui.message.adapters.MessageAdapter;
import com.example.reloop.ui.message.viewmodel.ChatViewModel;
import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {
    private ChatViewModel chatViewModel;
    private MessageAdapter messageAdapter;
    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private ImageButton sendButton;

    private String conversationId;
    private String currentUserId;
    private String otherUserId;
    private String productId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupViewModel();
        getArgumentsData();
        setupRecyclerView();
        setupClickListeners();
        loadMessages();
    }

    private void initViews(View view) {
        messagesRecyclerView = view.findViewById(R.id.messagesRecyclerView);
        messageInput = view.findViewById(R.id.messageInput);
        sendButton = view.findViewById(R.id.sendButton);
    }

    private void setupViewModel() {
        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        // Observe messages
        chatViewModel.getMessages().observe(getViewLifecycleOwner(), messages -> {
            messageAdapter.setMessages(messages);
            scrollToBottom();
        });

        // Observe loading state
        chatViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // Handle loading state
        });

        // Observe errors
        chatViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                // Show error message
            }
        });
    }

    private void getArgumentsData() {
        if (getArguments() != null) {
            conversationId = getArguments().getString("conversationId");
            currentUserId = getArguments().getString("currentUserId");
            otherUserId = getArguments().getString("otherUserId");
            productId = getArguments().getString("productId");
        }
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(new ArrayList<>(), currentUserId);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        messagesRecyclerView.setAdapter(messageAdapter);
    }

    private void setupClickListeners() {
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String content = messageInput.getText().toString().trim();
        if (!content.isEmpty() && conversationId != null) {
            Message message = new Message();
            message.setConversationId(conversationId);
            message.setSenderId(currentUserId);
            message.setReceiverId(otherUserId);
            message.setProductId(productId);
            message.setContent(content);
            message.setMessageType("TEXT");

            chatViewModel.sendMessage(message);
            messageInput.setText("");
        }
    }

    private void loadMessages() {
        if (conversationId != null) {
            chatViewModel.loadMessages(conversationId);
        }
    }

    private void scrollToBottom() {
        if (messageAdapter.getItemCount() > 0) {
            messagesRecyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (conversationId != null && currentUserId != null) {
            chatViewModel.markMessagesAsRead(conversationId, currentUserId);
        }
    }
}