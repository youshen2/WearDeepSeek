package moye.sine.deepseek.helper;
import android.util.Log;

import okhttp3.*;
import okio.BufferedSource;
import okio.Okio;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DeepSeekClient {
    private final OkHttpClient client;
    private final String apiKey;
    private static final String BASE_URL = "https://api.deepseek.com/v1/chat/completions";

    public interface StreamListener {
        void onContentChunk(String chunk);
        void onReasoningChunk(String chunk);
        void onComplete();
        void onError(String error);
    }

    public DeepSeekClient(String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public void streamCompletion(String model, List<Map<String, String>> messages, StreamListener listener) {
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("model", model);
            requestBody.put("messages", new JSONArray(messages));
            requestBody.put("stream", true);
        } catch (Exception e) {
            listener.onError("Request body creation failed: " + e.getMessage());
            return;
        }

        Request request = new Request.Builder()
                .url(BASE_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(MediaType.parse("application/json"), requestBody.toString()))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.onError("Request failed: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("test", response.body().string());
                    listener.onError("Unexpected code: " + response.code());
                    return;
                }

                try (BufferedSource source = Okio.buffer(response.body().source())) {
                    while (!source.exhausted()) {
                        String line = source.readUtf8Line();
                        if (line != null && line.startsWith("data: ")) {
                            String jsonData = line.substring(6).trim();
                            if (jsonData.equals("[DONE]")) {
                                listener.onComplete();
                                return;
                            }

                            try {
                                JSONObject data = new JSONObject(jsonData);
                                JSONArray choices = data.getJSONArray("choices");
                                if (choices.length() > 0) {
                                    JSONObject choice = choices.getJSONObject(0);
                                    JSONObject delta = choice.getJSONObject("delta");

                                    // 根据模型类型处理不同字段
                                    if (delta.has("reasoning_content") && !delta.isNull("reasoning_content")) {
                                        String reasoning = delta.getString("reasoning_content");
                                        listener.onReasoningChunk(reasoning);
                                    }
                                    if (delta.has("content") && !delta.isNull("content")) {
                                        String content = delta.getString("content");
                                        listener.onContentChunk(content);
                                    }
                                }
                            } catch (Exception e) {
                                listener.onError("JSON parsing error: " + e.getMessage());
                            }
                        }
                    }
                    listener.onComplete();
                } catch (Exception e) {
                    listener.onError("Stream reading error: " + e.getMessage());
                }
            }
        });
    }
}