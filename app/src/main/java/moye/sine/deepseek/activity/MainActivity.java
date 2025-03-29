package moye.sine.deepseek.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import moye.sine.deepseek.R;
import moye.sine.deepseek.adapter.MessageAdapter;
import moye.sine.deepseek.helper.ChatDatabaseHelper;
import moye.sine.deepseek.helper.DeepSeekClient;
import moye.sine.deepseek.helper.MessageDatabaseHelper;
import moye.sine.deepseek.model.Message;
import moye.sine.deepseek.util.SharedPreferencesUtil;
import moye.sine.deepseek.view.SpacingDecoration;

public class MainActivity extends BaseActivity {
    private RecyclerView recyclerView;
    private EditText editText;
    private MaterialCardView sendBtn;

    private MessageAdapter adapter;
    private MessageDatabaseHelper dbHelper;
    private ChatDatabaseHelper chatDbHelper;
    private List<Message> messages = new ArrayList<>();
    private DeepSeekClient client;
    private long currentChatId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        editText = findViewById(R.id.editText);
        sendBtn = findViewById(R.id.send_btn);

        // 初始化组件
        dbHelper = new MessageDatabaseHelper(this);
        chatDbHelper = new ChatDatabaseHelper(this);
        client = new DeepSeekClient(SharedPreferencesUtil.getString(SharedPreferencesUtil.API_KEY, ""));
//        client = new DeepSeekClient("sk-3e52772140ee48ceab954edfd3798cda");

        // 设置RecyclerView
        adapter = new MessageAdapter(messages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        if(getIntent().getLongExtra("chat_id", -1) != -1) {
            currentChatId = getIntent().getLongExtra("chat_id", -1);
            ((TextView) findViewById(R.id.title)).setText(getIntent().getStringExtra("title"));
            loadHistoryMessages();
        }

        findViewById(R.id.add_btn).setOnClickListener(view -> createChat());
        findViewById(R.id.menu_btn).setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, MenuActivity.class);
            startActivity(intent);
            finish();
        });

        int edgeSpace = getResources().getDimensionPixelSize(R.dimen.edge_space);
        recyclerView.addItemDecoration(new SpacingDecoration(edgeSpace));

        // 发送按钮点击事件
        sendBtn.setOnClickListener(v -> sendMessage());

        // 输入框回车发送
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                sendBtn.setEnabled(s.length() > 0);
            }
        });
    }

    public void createChat(){
        currentChatId = chatDbHelper.createChat("新对话");
        loadHistoryMessages();
        ((TextView) findViewById(R.id.title)).setText("新对话");
    }

    private void loadHistoryMessages() {
        List<Message> history = dbHelper.getMessagesByChatId(currentChatId);
        messages.clear();
        messages.addAll(history);
        adapter.notifyDataSetChanged();
        scrollToBottom();
    }

    private void sendMessage() {
        String input = editText.getText().toString().trim();
        if (input.isEmpty()) return;

        if(currentChatId == -1) {
            createChat();
        }
        editText.setText("");

        // 创建用户消息
        Message userMessage = new Message();
        userMessage.setChat_id(currentChatId);
        userMessage.setRole("user");
        userMessage.setContent(input);
        userMessage.setTimestamp(System.currentTimeMillis());
        int userPosition = messages.size();
        saveAndDisplayMessage(userMessage);
        adapter.updateMessage(userPosition, userMessage, MessageAdapter.PAYLOAD_CONTENT);

        if(messages.size() == 1) {
            chatDbHelper.updateChatTitle(currentChatId, input);
            ((TextView) findViewById(R.id.title)).setText(input);
        }

        // 创建AI消息
        Message aiMessage = new Message();
        aiMessage.setChat_id(currentChatId);
        aiMessage.setRole("assistant");
        aiMessage.setThinking(true);
        aiMessage.setTimestamp(System.currentTimeMillis());
        int aiPosition = messages.size();
        saveAndDisplayMessage(aiMessage);
        adapter.updateMessage(aiPosition, aiMessage, MessageAdapter.PAYLOAD_REASON);

        // 准备API请求
        List<Map<String, String>> messageList = new ArrayList<>();
        messageList.add(getSystemPrompt());
        for (Message msg : messages) {
            if(msg.isThinking()) continue;

            Map<String, String> m = new HashMap<>();
            m.put("role", msg.getRole());
            m.put("content", msg.getContent());
            messageList.add(m);
        }

        // 修改发送消息部分的代码
        client.streamCompletion(SharedPreferencesUtil.getString("selected_model", "deepseek-reasoner"), messageList, new DeepSeekClient.StreamListener() {
            private final StringBuilder contentBuilder = new StringBuilder();
            private final StringBuilder reasonBuilder = new StringBuilder();
            private long lastUpdateTime = 0;
            private static final long UPDATE_INTERVAL = 100; // 100毫秒更新间隔

            @Override
            public void onContentChunk(String chunk) {
                contentBuilder.append(chunk);
                aiMessage.setThinking(false);
                scheduleUpdate(aiPosition, MessageAdapter.PAYLOAD_CONTENT);
            }

            @Override
            public void onReasoningChunk(String chunk) {
                reasonBuilder.append(chunk);
                aiMessage.setThinking(true);
                scheduleUpdate(aiPosition, MessageAdapter.PAYLOAD_REASON);
            }

            @Override
            public void onComplete() {
                scrollToBottom();
                dbHelper.updateMessage(aiMessage);
            }

            @Override
            public void onError(String error) {

            }

            private void scheduleUpdate(int position, int payload) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastUpdateTime > UPDATE_INTERVAL) {
                    immediateUpdate(position, payload);
                    lastUpdateTime = currentTime;
                } else {
                    recyclerView.postDelayed(() -> immediateUpdate(position, payload), UPDATE_INTERVAL);
                }
            }

            private void immediateUpdate(int position, int payload) {
                runOnUiThread(() -> {
                    aiMessage.setContent(contentBuilder.toString());
                    aiMessage.setReason(reasonBuilder.toString());
                    adapter.updateMessage(position, aiMessage, payload);
                    dbHelper.updateMessage(aiMessage);
                });
            }
        });
    }

    private Map<String, String> getSystemPrompt(){
        Map<String, String> m = new HashMap<>();
        m.put("role", "system");
        m.put("content", "You are a helpful assistant");
        return m;
    }

    private void saveAndDisplayMessage(Message message) {
        long id = dbHelper.insertMessage(message);
        message.setMessage_id(id);
        adapter.addMessage(message);
        scrollToBottom();
    }

    private void scrollToBottom() {
        if(adapter.getItemCount() > 0) recyclerView.post(() -> recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1));
    }

    @Override
    protected void onDestroy() {
        if(messages.isEmpty()) chatDbHelper.deleteChat(currentChatId);
        dbHelper.close();
        chatDbHelper.close();
        super.onDestroy();
    }
}