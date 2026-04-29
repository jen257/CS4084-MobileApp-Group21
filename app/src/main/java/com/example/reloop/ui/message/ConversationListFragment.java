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
import com.example.reloop.repository.MessageRepository;
import com.example.reloop.ui.message.adapters.ConversationAdapter;
import com.example.reloop.ui.message.viewmodel.ConversationViewModel;
import com.example.reloop.ui.message.viewmodel.MessageViewModelFactory;

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
        // Initialize Room DB and Repository
        com.example.reloop.database.AppDataBase db = com.example.reloop.database.AppDataBase.getInstance(requireContext());
        MessageRepository repo = new MessageRepository(db.messageDao());
        MessageViewModelFactory factory = new MessageViewModelFactory(repo);

        conversationViewModel = new ViewModelProvider(this, factory).get(ConversationViewModel.class);

        conversationViewModel.getConversations().observe(getViewLifecycleOwner(), conversations -> {
            if (conversations != null) {
                conversationAdapter.setConversations(conversations);
            }
        });
    }

    private void setupRecyclerView() {
        conversationAdapter = new ConversationAdapter(new ArrayList<>(), this);
        conversationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        conversationsRecyclerView.setAdapter(conversationAdapter);
    }

    private void loadConversations() {
        String currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        conversationViewModel.loadUserConversations(currentUserId);
    }

    @Override
    public void onConversationClick(Conversation conversation) {
        String currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        Bundle args = new Bundle();
        args.putString("conversationId", conversation.getId());
        args.putString("currentUserId", currentUserId);
        args.putString("otherUserId", conversation.getOtherParticipant(currentUserId));
        args.putString("productId", conversation.getProductId());

        androidx.navigation.Navigation.findNavController(requireView())
                .navigate(R.id.action_messageFragment_to_chatFragment, args);
    }
}