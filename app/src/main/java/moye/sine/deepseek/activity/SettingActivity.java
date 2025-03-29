package moye.sine.deepseek.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;

import com.google.android.material.radiobutton.MaterialRadioButton;

import moye.sine.deepseek.R;
import moye.sine.deepseek.util.SharedPreferencesUtil;

public class SettingActivity extends BaseActivity {

    private EditText apiKeyInput;
    private RadioGroup modelGroup;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // 初始化视图
        apiKeyInput = findViewById(R.id.api_key_input);
        modelGroup = findViewById(R.id.model_group);

        // 加载保存的设置
        loadSavedPreferences();

        // 设置监听器
        setupListeners();
    }

    private void loadSavedPreferences() {
        // 加载API KEY
        String savedApiKey = SharedPreferencesUtil.getString(
                SharedPreferencesUtil.API_KEY,
                ""
        );
        apiKeyInput.setText(savedApiKey);

        // 加载模型选择
        String savedModel = SharedPreferencesUtil.getString(
                SharedPreferencesUtil.SELECTED_MODEL,
                "deepseek-reasoner"
        );
        if (savedModel.equals("deepseek-chat")) {
            modelGroup.check(R.id.model_chat);
        } else {
            modelGroup.check(R.id.model_reasoner);
        }
    }

    private void setupListeners() {
        // API KEY输入监听
        apiKeyInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                SharedPreferencesUtil.putString(
                        SharedPreferencesUtil.API_KEY,
                        s.toString()
                );
            }
        });

        // 模型选择监听
        modelGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String selectedModel = "deepseek-chat";
            if (checkedId == R.id.model_reasoner) {
                selectedModel = "deepseek-reasoner";
            }
            SharedPreferencesUtil.putString(SharedPreferencesUtil.SELECTED_MODEL, selectedModel);
        });
    }
}