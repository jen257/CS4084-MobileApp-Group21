package com.example.reloop.ui.message;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
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
import com.example.reloop.repository.MessageRepository;
import com.example.reloop.ui.message.viewmodel.MessageViewModelFactory;
import java.util.ArrayList;
import java.util.Date;

public class ChatFragment extends Fragment {
    private ChatViewModel chatViewModel;
    private MessageAdapter messageAdapter;
    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private TextView tvChatPartnerName;
    private String conversationId, currentUserId, otherUserId, productId, otherUserName;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Initialize the header view first
        tvChatPartnerName = view.findViewById(R.id.tvChatPartnerName);

        // 2. Get data from arguments
        if (getArguments() != null) {
            conversationId = getArguments().getString("conversationId");
            currentUserId = getArguments().getString("currentUserId");
            otherUserId = getArguments().getString("otherUserId");
            productId = getArguments().getString("productId");
            otherUserName = getArguments().getString("otherUserName");
        }

        // 3. Set the username to the header
        if (tvChatPartnerName != null) {
            tvChatPartnerName.setText(otherUserName != null ? otherUserName : "Chat");
        }

        initViews(view);
        setupViewModel();
        setupRecyclerView();

        if (conversationId != null) chatViewModel.loadMessages(conversationId);
    }

    private void initViews(View view) {
        messagesRecyclerView = view.findViewById(R.id.messagesRecyclerView);
        messageInput = view.findViewById(R.id.messageInput);
        ImageButton sendButton = view.findViewById(R.id.sendButton);
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void setupViewModel() {
        com.example.reloop.database.AppDataBase db = com.example.reloop.database.AppDataBase.getInstance(requireContext());
        MessageRepository repo = new MessageRepository(db.messageDao());
        chatViewModel = new ViewModelProvider(this, new MessageViewModelFactory(repo)).get(ChatViewModel.class);
        chatViewModel.getMessages().observe(getViewLifecycleOwner(), messages -> {
            if (messages != null) {
                messageAdapter.setMessages(messages);
                if (!messages.isEmpty()) messagesRecyclerView.scrollToPosition(messages.size() - 1);
            }
        });
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(new ArrayList<>(), currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        messagesRecyclerView.setLayoutManager(layoutManager);
        messagesRecyclerView.setAdapter(messageAdapter);
    }

    private void sendMessage() {
        String content = messageInput.getText().toString().trim();
        if (content.isEmpty() || conversationId == null) return;

        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderId(currentUserId);
        message.setReceiverId(otherUserId);
        message.setProductId(productId);
        message.setContent(content);
        message.setMessageType("TEXT");
        message.setTimestamp(new Date());

        chatViewModel.sendMessage(message);
        messageInput.setText("");
    }
}