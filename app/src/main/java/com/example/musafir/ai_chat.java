package com.example.musafir;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ai_chat extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private List<Map<String, Object>> messages = new ArrayList<>();
    String BASE_URL = UserUtils.BASE_URL;
    //    private final String API_URL = BASE_URL + "ai-chat/";
    ImageButton sendmsg;
    EditText textmsg;
    private DBHelper dbHelper;
    private int userId;

    private Map<String, Object> typingMessage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_ai_chat);

        recyclerView = findViewById(R.id.chatRecyclerView);
        textmsg = findViewById(R.id.textmsg);
        sendmsg = findViewById(R.id.sendmsg);
        SharedPreferences prefs = SharedPrefsHelper.get(this);
        userId = prefs.getInt("user_id", -1);
        adapter = new ChatAdapter(messages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        dbHelper = new DBHelper(this);
        messages.addAll(dbHelper.getAllMessages(userId));
//        if (activity.getSupportActionBar() != null) {
//            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(showBackArrow);
//        }
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }

            TextView toolbarTitle = findViewById(R.id.toolbar_title);
            ImageView toolbarIcon = findViewById(R.id.toolbar_icon);

            if (toolbarTitle != null) toolbarTitle.setText("مساعد مسافر");
            if (toolbarIcon != null)
                toolbarIcon.setImageResource(R.drawable.robot2);

            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
// ---------------------------------
        sendmsg.setOnClickListener(v -> {
            String query = textmsg.getText().toString().trim();

            if (!query.isEmpty()) {
                sendMessageToBot(query);

                textmsg.setText("");
            }
        });
        recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    if (messages.size() > 0) {
                        recyclerView.postDelayed(() -> {
                            recyclerView.smoothScrollToPosition(messages.size() - 1);
                        }, 100);
                    }
                }
            }
        });
        if (messages.size() > 0) {
            recyclerView.scrollToPosition(messages.size() - 1);
        }
        if (messages.isEmpty()) {
            addLocalWelcomeMessage("أهلاً بك، كيف يمكنني مساعدتك اليوم؟");
        }
    }

    private void sendMessageToBot(String userText) {
        addNewMessage(userText, true);
        showTypingIndicator(true);

        try {
            SharedPreferences prefs = SharedPrefsHelper.get(this);
            String token = prefs.getString("auth_token", null);
            int user_id = prefs.getInt("user_id", -1);

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("user_id", user_id);
            jsonBody.put("message", userText);

            String deviceId = UserUtils.getDeviceID(this);
            String deviceInfo = UserUtils.getDeviceInfo();


            String url = BASE_URL + "ai-chat/?device_id=" + deviceId + "&device_info=" + deviceInfo;

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    response -> {
                        showTypingIndicator(false);
                        try {
                            JSONObject aiResponse = response.getJSONObject("msg");
                            String botReply = aiResponse.getJSONObject("reply").getString("content");
                            addNewMessage(botReply, false);
                        } catch (Exception e) {
                            UserUtils.sendLog(this, "sendMessageToBot", String.valueOf(e.getMessage()), "user_id = " + userId, "ai_chat");
                            addNewMessage("أعتذر منك، حدث خطأ غير متوقع. يرجى محاولة إرسال استفسارك مرة أخرى، وسأكون سعيداً بمساعدتك.", false);
                        }
                    },
                    error -> {
                        showTypingIndicator(false);
                        UserUtils.sendLog(this, "sendMessageToBot", String.valueOf(error.getMessage()), "user_id = " + userId, "ai_chat");
                        addNewMessage("عذراً، يبدو أنني واجهت صعوبة بسيطة في فهم طلبك. هل يمكنك إعادة كتابة سؤالك بطريقة أخرى لأتمكن من مساعدتك بشكل أفضل؟", false);
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Accept", "application/json");
                    headers.put("Authorization", "Bearer " + token);
                    return headers;
                }
            };

            Volley.newRequestQueue(this).add(request);

        } catch (Exception e) {
            e.printStackTrace();
            showTypingIndicator(false);
        }
    }

    private void showTypingIndicator(boolean show) {
        if (show) {
            typingMessage = new HashMap<>();
            typingMessage.put("isTyping", true);
            typingMessage.put("isMe", false);
            messages.add(typingMessage);
            adapter.notifyItemInserted(messages.size() - 1);
            recyclerView.scrollToPosition(messages.size() - 1);
        } else {
            if (typingMessage != null) {
                int index = messages.indexOf(typingMessage);
                if (index != -1) {
                    messages.remove(index);
                    adapter.notifyItemRemoved(index);
                    typingMessage = null;
                }
            }
        }
    }

    private void addNewMessage(String text, boolean isMe) {
        String currentTime = new java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(new java.util.Date());

        // 1. الحفظ في قاعدة البيانات المحلية (SQLite)
        dbHelper.insertMessage(userId, text, isMe, currentTime);

        // 2. إرسال البيانات للسيرفر (الدالة التي طلبتها)
        // نمرر الـ userId والنص وهل هي من المستخدم (isMe) والتوقيت
        UserUtils.sendMessage(this, userId, text, isMe);

        Map<String, Object> msg = new HashMap<>();
        msg.put("text", text);
        msg.put("isMe", isMe);
        msg.put("time", currentTime);

        messages.add(msg);
        adapter.notifyItemInserted(messages.size() - 1);
        recyclerView.scrollToPosition(messages.size() - 1);
    }

    private void addLocalWelcomeMessage(String text) {
        addNewMessage(text, false);
    }
//    private void addNewMessage(String text, boolean isMe) {
//        Map<String, Object> msg = new HashMap<>();
//        msg.put("text", text);
//        msg.put("isMe", isMe);
//        String currentTime = new java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(new java.util.Date());
//        msg.put("time", currentTime);
//        dbHelper.insertMessage(userId, text, isMe, currentTime);
//        messages.add(msg);
//        adapter.notifyItemInserted(messages.size() - 1);
//        recyclerView.scrollToPosition(messages.size() - 1);
//    }
}