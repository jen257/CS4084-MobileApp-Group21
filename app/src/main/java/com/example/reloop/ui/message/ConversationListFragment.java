package com.example.reloop.ui.message;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.reloop.R;
import com.example.reloop.models.Conversation;
import com.example.reloop.ui.message.adapters.ConversationAdapter;
import com.example.reloop.ui.message.viewmodel.ConversationViewModel;
import com.example.reloop.ui.message.viewmodel.MessageViewModelFactory;
import com.example.reloop.repository.MessageRepository;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;

public class ConversationListFragment extends Fragment implements ConversationAdapter.OnConversationClickListener {
    private ConversationViewModel conversationViewModel;
    private ConversationAdapter conversationAdapter;
    private RecyclerView recyclerView;
    private LinearLayout emptyState;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_conversations, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.conversationsRecyclerView);
        emptyState = view.findViewById(R.id.emptyStateLayout);

        setupRecyclerView();
        setupViewModel();
        loadData();
    }

    private void setupRecyclerView() {
        conversationAdapter = new ConversationAdapter(new ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(conversationAdapter);
    }

    private void setupViewModel() {
        com.example.reloop.database.AppDataBase db = com.example.reloop.database.AppDataBase.getInstance(requireContext());
        MessageRepository repo = new MessageRepository(db.messageDao());
        conversationViewModel = new ViewModelProvider(this, new MessageViewModelFactory(repo)).get(ConversationViewModel.class);

        conversationViewModel.getConversations().observe(getViewLifecycleOwner(), conversations -> {
            if (conversations != null) {
                conversationAdapter.setConversations(conversations);
                // Switch visibility based on list size[cite: 2]
                if (conversations.isEmpty()) {
                    emptyState.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyState.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void loadData() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            conversationViewModel.loadUserConversations(FirebaseAuth.getInstance().getCurrentUser().getUid());
        }
    }

    @Override
    public void onConversationClick(Conversation conv) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Bundle args = new Bundle();

        args.putString("conversationId", conv.getId());
        args.putString("currentUserId", uid);
        args.putString("otherUserId", conv.getOtherParticipant(uid));
        args.putString("productId", conv.getProductId());

        // Use getDisplayName(uid) to ensure the Chat screen receives
        // the correct partner's name, not your own name.
        args.putString("otherUserName", conv.getDisplayName(uid));

        Navigation.findNavController(requireView())
                .navigate(R.id.action_messageFragment_to_chatFragment, args);
    }
}