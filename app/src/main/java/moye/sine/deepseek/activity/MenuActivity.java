package moye.sine.deepseek.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import moye.sine.deepseek.R;
import moye.sine.deepseek.adapter.ChatAdapter;
import moye.sine.deepseek.helper.ChatDatabaseHelper;
import moye.sine.deepseek.model.Chat;

public class MenuActivity extends BaseActivity {
    private RecyclerView recyclerView;
    private ChatDatabaseHelper chatDbHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        chatDbHelper = new ChatDatabaseHelper(this);
        setupRecyclerView();
        loadChatHistory();

        findViewById(R.id.about_btn).setOnClickListener(view -> {
            Intent intent = new Intent(MenuActivity.this, AboutActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.setting_btn).setOnClickListener(view -> {
            Intent intent = new Intent(MenuActivity.this, SettingActivity.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadChatHistory() {
        List<Chat> chats = chatDbHelper.getAllChats();
        ChatAdapter adapter = new ChatAdapter(chats, this::openChat);
        recyclerView.setAdapter(adapter);
    }

    private void openChat(long chatId, String title) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("chat_id", chatId);
        intent.putExtra("title", title);
        startActivity(intent);
        finish();
    }
}