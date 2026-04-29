package com.example.reloop.ui.message;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.reloop.R;
import com.example.reloop.models.Conversation;
import com.example.reloop.ui.message.adapters.ConversationAdapter;
import com.example.reloop.ui.message.viewmodel.ConversationViewModel;
import java.util.ArrayList;
import java.util.List;

public class ConversationListFragment extends Fragment implements ConversationAdapter.OnConversationClickListener {
    private ConversationViewModel conversationViewModel;
    private ConversationAdapter conversationAdapter;
    private RecyclerView conversationsRecyclerView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_conversations, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupViewModel();
        setupRecyclerView();
        loadConversations();
    }

    private void initViews(View view) {
        conversationsRecyclerView = view.findViewById(R.id.conversationsRecyclerView);
    }

    private void setupViewModel() {
        conversationViewModel = new ViewModelProvider(this).get(ConversationViewModel.class);

        conversationViewModel.getConversations().observe(getViewLifecycleOwner(), conversations -> {
            conversationAdapter.setConversations(conversations);
        });

        conversationViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // Handle loading state
        });

        conversationViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                // Show error message
            }
        });
    }

    private void setupRecyclerView() {
        conversationAdapter = new ConversationAdapter(new ArrayList<>(), this);
        conversationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        conversationsRecyclerView.setAdapter(conversationAdapter);
    }

    private void loadConversations() {
        String currentUserId = "current_user_id"; // Get from shared preferences or auth
        conversationViewModel.loadUserConversations(currentUserId);
    }

    @Override
    public void onConversationClick(Conversation conversation) {
        // Navigate to chat fragment
        Bundle args = new Bundle();
        args.putString("conversationId", conversation.getId());
        args.putString("currentUserId", "current_user_id"); // Get from auth
        args.putString("otherUserId", conversation.getOtherParticipant("current_user_id"));
        args.putString("productId", conversation.getProductId());

        Navigation.findNavController(requireView())
                .navigate(R.id.action_messageFragment_to_chatFragment, args);
    }
}