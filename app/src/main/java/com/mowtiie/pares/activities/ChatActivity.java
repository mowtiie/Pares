package com.mowtiie.pares.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mowtiie.pares.adapters.GenericAdapter;
import com.mowtiie.pares.data.Data;
import com.mowtiie.pares.databinding.ActivityChatBinding;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private GenericAdapter<String[]> adapter;
    private String otherId;
    private String otherName;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        otherId = getIntent().getStringExtra("user_id");
        otherName = getIntent().getStringExtra("name");
        binding.toolbar.setTitle(otherName);
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.viewEmpty.tvEmptyTitle.setText("No messages yet");
        binding.viewEmpty.tvEmptyMessage.setText("Say hi to " + otherName + ".");

        adapter = new GenericAdapter<>(new ArrayList<>(), (b, item, position) -> {
            b.tvTitle.setText(item[1].equals(Data.me()) ? "You" : otherName);
            b.tvSubtitle.setText(item[2]);
            b.tvMeta.setText(item[3]);
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.rvMessages.setLayoutManager(layoutManager);
        binding.rvMessages.setAdapter(adapter);

        binding.btnSend.setOnClickListener(v -> send());
        binding.toolbar.setOnMenuItemClickListener(item -> {
            Intent intent = new Intent(this, ProfileViewActivity.class);
            intent.putExtra("user_id", otherId);
            startActivity(intent);
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!Data.canChat(this, otherId)) {
            finish();
            return;
        }
        load();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    private void load() {
        ArrayList<String[]> messages = Data.messages(this, otherId);
        adapter.setItems(messages);
        binding.viewEmpty.getRoot().setVisibility(messages.isEmpty() ? View.VISIBLE : View.GONE);
        if (!messages.isEmpty()) {
            binding.rvMessages.scrollToPosition(messages.size() - 1);
        }
    }

    private void send() {
        binding.tilMessage.setError(null);
        String body = binding.etMessage.getText().toString().trim();
        if (body.isEmpty()) {
            binding.tilMessage.setError("Type a message");
            return;
        }
        Data.addMessage(this, Data.me(), otherId, body);
        binding.etMessage.setText("");
        load();

        if (Data.isSeededMember(otherId)) {
            String replyTo = Data.me();
            handler.postDelayed(() -> {
                Data.addMessage(getApplicationContext(), otherId, replyTo, Data.cannedReply());
                Toast.makeText(getApplicationContext(), "New message from " + otherName, Toast.LENGTH_SHORT).show();
                load();
            }, 1500);
        }
    }
}
