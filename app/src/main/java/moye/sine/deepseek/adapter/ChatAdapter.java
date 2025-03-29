package moye.sine.deepseek.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import moye.sine.deepseek.R;
import moye.sine.deepseek.model.Chat;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private List<Chat> chatList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(long chatId, String title);
    }

    public ChatAdapter(List<Chat> chats, OnItemClickListener listener) {
        this.chatList = chats;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chat chat = chatList.get(position);
        holder.bind(chat, listener);
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView content;

        public ViewHolder(View view) {
            super(view);
            content = view.findViewById(R.id.content);
        }

        public void bind(final Chat chat, final OnItemClickListener listener) {
            content.setText(chat.getTitle());
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(chat.getChat_id(), chat.getTitle());
                }
            });
        }
    }
}