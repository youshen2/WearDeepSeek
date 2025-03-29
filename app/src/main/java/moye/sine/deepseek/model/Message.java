package moye.sine.deepseek.model;

public class Message {
    private long message_id;
    private long chat_id;
    private String role;
    private String content;
    private String reason;
    private long timestamp;
    private int token_count;
    private boolean isThinking;

    public Message() {}

    public Message(long message_id, long chat_id, String content, String reason, String role, long timestamp, int token_count, boolean isThinking) {
        this.message_id = message_id;
        this.chat_id = chat_id;
        this.content = content;
        this.reason = reason;
        this.role = role;
        this.timestamp = timestamp;
        this.token_count = token_count;
        this.isThinking = isThinking;
    }

    public long getMessage_id() { return message_id; }
    public void setMessage_id(long message_id) { this.message_id = message_id; }

    public long getChat_id() { return chat_id; }
    public void setChat_id(long chat_id) { this.chat_id = chat_id; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public int getToken_count() { return token_count; }
    public void setToken_count(int token_count) { this.token_count = token_count; }

    public boolean isThinking() { return isThinking; }
    public void setThinking(boolean thinking) { isThinking = thinking; }
}