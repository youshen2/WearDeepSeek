package moye.sine.deepseek.adapter;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import io.noties.markwon.Markwon;
import moye.sine.deepseek.R;
import moye.sine.deepseek.model.Message;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_AI = 2;

    public static final int PAYLOAD_CONTENT = 1;
    public static final int PAYLOAD_REASON = 2;

    private final List<Message> messages;

    public MessageAdapter(List<Message> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_USER) {
            return new UserViewHolder(inflater.inflate(R.layout.item_message_user, parent, false));
        }
        return new AIViewHolder(inflater.inflate(R.layout.item_message_ai, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).content.setText(message.getContent());
        } else if (holder instanceof AIViewHolder) {
            AIViewHolder aiHolder = (AIViewHolder) holder;

            if(message.getContent() != null){
                Markwon markwon = Markwon.create(holder.itemView.getContext());
                markwon.setMarkdown(aiHolder.content, message.getContent());
            } else aiHolder.content.setText("");

            aiHolder.reasonContent.setText(message.getReason());
            aiHolder.reasonContent.setVisibility(
                    (aiHolder.showReason && message.getReason() != null && !message.getReason().isEmpty()) ?
                            View.VISIBLE : View.GONE);

            setup_reason_title(aiHolder);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty()) {
            Message message = messages.get(position);
            for (Object payload : payloads) {
                if (payload instanceof Integer) {
                    int type = (Integer) payload;
                    if (holder instanceof AIViewHolder) {
                        AIViewHolder aiHolder = (AIViewHolder) holder;
                        if (type == PAYLOAD_CONTENT) {
                            Markwon markwon = Markwon.create(holder.itemView.getContext());
                            markwon.setMarkdown(aiHolder.content, message.getContent());
                        } else if (type == PAYLOAD_REASON) {
                            aiHolder.reasonContent.setText(message.getReason());
                            aiHolder.reasonContent.setVisibility(
                                    (aiHolder.showReason && !TextUtils.isEmpty(message.getReason())) ? View.VISIBLE : View.GONE);
                        }
                        if(!message.isThinking()) aiHolder.reasonTitle.setText("思考结束");
                        else aiHolder.reasonTitle.setText("思考中...");
                    } else if (holder instanceof UserViewHolder) {
                        ((UserViewHolder) holder).content.setText(message.getContent());
                    }
                }
            }
        } else {
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getRole().equals("user") ? VIEW_TYPE_USER : VIEW_TYPE_AI;
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    public void updateMessage(int position, Message message, int payload) {
        messages.set(position, message);
        notifyItemChanged(position, payload);
    }

    @Override
    public long getItemId(int position) {
        return messages.get(position).getMessage_id(); // 确保每个消息有唯一ID
    }

    private void setup_reason_title(AIViewHolder holder){
        holder.reasonTitle.setOnClickListener(view -> {
            if(holder.showReason){
                Drawable drawable = holder.itemView.getContext().getResources().getDrawable(R.drawable.icon_arrow_top);
                holder.reasonTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, null, null, null);
                holder.reasonContent.setVisibility(View.GONE);
            } else {
                Drawable drawable = holder.itemView.getContext().getResources().getDrawable(R.drawable.icon_arrow_bottom);
                holder.reasonTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, null, null, null);
                holder.reasonContent.setVisibility(View.VISIBLE);
            }
            holder.showReason = !holder.showReason;
        });
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView content;

        UserViewHolder(View itemView) {
            super(itemView);
            content = itemView.findViewById(R.id.content);
        }
    }

    static class AIViewHolder extends RecyclerView.ViewHolder {
        TextView content;
        TextView reasonTitle;
        TextView reasonContent;
        boolean showReason = true;

        AIViewHolder(View itemView) {
            super(itemView);
            content = itemView.findViewById(R.id.content);
            reasonTitle = itemView.findViewById(R.id.reason_title);
            reasonContent = itemView.findViewById(R.id.reason_content);
        }
    }
}