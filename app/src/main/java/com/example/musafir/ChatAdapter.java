package com.example.musafir;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;
import java.util.Map;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Map<String, Object>> chatData;
    private static final int TYPE_USER = 1;
    private static final int TYPE_BOT = 2;
    private static final int TYPE_TYPING = 3;

    public ChatAdapter(List<Map<String, Object>> chatData) {
        this.chatData = chatData;
    }

    @Override
    public int getItemViewType(int position) {
        Map<String, Object> msg = chatData.get(position);
        if (msg.containsKey("isTyping")) return TYPE_TYPING;
        return (boolean) msg.get("isMe") ? TYPE_USER : TYPE_BOT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_TYPING) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_typing, parent, false);
            return new TypingViewHolder(v);
        }
        int layout = (viewType == 1) ? R.layout.item_chat_sent : R.layout.item_chat_received;
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ChatViewHolder(v);
    }

    static class TypingViewHolder extends RecyclerView.ViewHolder {
        TypingViewHolder(View v) {
            super(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Map<String, Object> msg = chatData.get(position);

        if (holder instanceof ChatViewHolder) {
            ChatViewHolder vh = (ChatViewHolder) holder;

            Object text = msg.get("text");
            Object time = msg.get("time");

            vh.messageText.setText(text != null ? text.toString() : "");
            vh.timeText.setText(time != null ? time.toString() : "");
        } else if (holder instanceof TypingViewHolder) {

        }
    }

    @Override
    public int getItemCount() {
        return chatData.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;

        ChatViewHolder(View v) {
            super(v);
            messageText = v.findViewById(R.id.txt_message);
            timeText = v.findViewById(R.id.txt_time);
        }
    }
}