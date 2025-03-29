package moye.sine.deepseek.model;

public class Chat {
    private long chat_id;
    private String title;
    private long create_time; // 可选的时间戳

    public Chat() {}

    public Chat(long chat_id, String title) {
        this.chat_id = chat_id;
        this.title = title;
        this.create_time = System.currentTimeMillis();
    }

    // Getter & Setter
    public long getChat_id() { return chat_id; }
    public void setChat_id(long chat_id) { this.chat_id = chat_id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public long getCreate_time() { return create_time; }
    public void setCreate_time(long create_time) { this.create_time = create_time; }
}