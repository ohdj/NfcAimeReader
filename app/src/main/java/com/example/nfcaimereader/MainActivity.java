package com.example.nfcaimereader;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.nfcaimereader.Client.SpiceClient;
import com.example.nfcaimereader.Controllers.ServerSettingsDialog;
import com.example.nfcaimereader.Services.NfcManager;
import com.example.nfcaimereader.Services.NfcViewModel;
import com.example.nfcaimereader.Utils.AppSetting;
import com.example.nfcaimereader.databinding.ActivityMainBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Set;
public class MainActivity extends AppCompatActivity implements SpiceClient.ConnectionStatusCallback {
    // ViewBinding和ViewModel
    private ActivityMainBinding binding;
    private NfcViewModel nfcViewModel;

    // NfcStateReceiver
    private NfcViewModel.NfcStateReceiver nfcStateReceiver;

    // NFC初始化
    private NfcManager nfcManager;

    // 永久化存储
    private AppSetting appSetting;

    Set<String> cardNumbers;
    ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 绑定ViewBinding和ViewModel
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        nfcViewModel = new ViewModelProvider(this).get(NfcViewModel.class);

        // 设置UI监听器
        setupUIListeners();

        // 观察LiveData对象
        observeLiveData();

        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
        if (adapter != null && adapter.isEnabled()) {
            binding.progressbarNfcDelay.setVisibility(View.GONE);
            binding.buttonNfcSetting.setVisibility(View.GONE);
            binding.textviewNfcStatus.setText("NFC已开启");
        } else if (adapter != null && !adapter.isEnabled()) {
            binding.progressbarNfcDelay.setVisibility(View.GONE);
            binding.buttonNfcSetting.setVisibility(View.VISIBLE);
            binding.textviewNfcStatus.setText("NFC已关闭");
        }

        nfcStateReceiver = nfcViewModel.new NfcStateReceiver();
        IntentFilter filter = new IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED);
        this.registerReceiver(nfcStateReceiver, filter);

        // NFC初始化
        nfcManager = new NfcManager(this);

        // WebSocket回调
        SpiceClient.getInstance().setConnectionStatusCallback(this);
        // SpiceClient.getInstance().sendCardId(cardNumber, appSetting.getPassword());

        // 永久化存储
        appSetting = new AppSetting(this);
        loadHostnameAndPort();

        // 卡号列表
        cardNumbers = appSetting.getCardNumbers();
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>(cardNumbers));
        binding.listviewCardNumbers.setAdapter(arrayAdapter);
    }

    private void setupUIListeners() {
        // 去开启NFC按钮
        binding.buttonNfcSetting.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_NFC_SETTINGS)));

        // 编辑服务器按钮
        binding.buttonSettingServer.setOnClickListener(v -> ServerSettingsDialog.showServerSettingsDialog(
                this,
                appSetting,
                binding.buttonSettingServer.getText().equals("编辑"),
                (hostname, port, password) -> {
                    // 保存设置到SharedPreferences
                    appSetting.saveServerSettings(hostname, port, password);
                    Toast.makeText(MainActivity.this, "已保存", Toast.LENGTH_SHORT).show();

                    // 更新UI
                    binding.buttonSettingServer.setText("编辑");
                    binding.textviewServerAddress.setText(String.format("%s:%s（是否启用加密传输）: %b", hostname, port, !password.isEmpty()));
                }
        ));

        // 连接服务器按钮
        binding.buttonConnectServer.setOnClickListener(v -> handleConnectButtonClick());

        binding.autocompleteTextviewListItem.setOnItemClickListener((parent, view, position, id) -> {
            switch (position) {
                case 0:
                    binding.cardInput.setVisibility(View.GONE);
                    break;
                case 1:
                    binding.cardInput.setVisibility(View.VISIBLE);
                    break;
            }
        });

        binding.buttonSaveCardNumber.setOnClickListener(v -> {
            String cardNumber = binding.edittextCardNumber.getText().toString();
            appSetting.saveCardNumber(cardNumber); // 保存卡号到列表
            updateCardList();
            Toast.makeText(MainActivity.this, "卡号已保存", Toast.LENGTH_SHORT).show();
        });

        binding.listviewCardNumbers.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCardNumber = arrayAdapter.getItem(position);
            // 发送选择的卡号到WebSocket服务器
            SpiceClient.getInstance().sendCardId(selectedCardNumber, appSetting.getPassword());
        });

        binding.listviewCardNumbers.setOnItemLongClickListener((parent, view, position, id) -> {
            String selectedCardNumber = arrayAdapter.getItem(position);
            showDeleteConfirmationDialog(selectedCardNumber);
            return true;
        });

        binding.edittextCardNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString().toUpperCase();
                binding.textviewInputStatus.setText(input.length() + "/16");

                boolean validLength = input.length() == 16;

                if (validLength) {
                    binding.textviewInputStatus.setTextColor(Color.BLACK);
                } else {
                    binding.textviewInputStatus.setTextColor(Color.RED);
                }

                // 检查输入是否只包含十六进制允许的字符
                boolean validHex = input.matches("[0-9A-F]*");

                if (validHex) {
                    binding.textviewInputHint.setTextColor(Color.BLACK);
                } else {
                    binding.textviewInputHint.setTextColor(Color.RED);
                }

                binding.buttonSaveCardNumber.setEnabled(validLength && validHex);
                binding.buttonPadInput.setEnabled(!validLength && validHex);
            }
        });

        binding.buttonPadInput.setOnClickListener(v -> {
            String currentInput = binding.edittextCardNumber.getText().toString().toUpperCase();

            // 检查输入是否为合法的十六进制
            boolean isHex = currentInput.matches("[0-9A-F]+") || currentInput.isEmpty();
            if (isHex && currentInput.length() < 16) {
                // 补足0到开头直到长度为16
                String paddedInput = String.format("%" + 16 + "s", currentInput).replace(' ', '0');
                binding.edittextCardNumber.setText(paddedInput);
                binding.edittextCardNumber.setSelection(paddedInput.length() - currentInput.length()); // 移动光标到补齐前的位置
            }
        });
    }

    private void observeLiveData() {
        // 观察NFC状态
        nfcViewModel.getNfcState().observe(this, state -> {
            switch (state) {
                case NfcAdapter.STATE_OFF:
                    // NFC已关闭
                    binding.progressbarNfcDelay.setVisibility(View.GONE);
                    binding.buttonNfcSetting.setVisibility(View.VISIBLE);
                    binding.textviewNfcStatus.setText("NFC已关闭");
                    break;
                case NfcAdapter.STATE_TURNING_OFF:
                    // NFC正在关闭
                    binding.progressbarNfcDelay.setVisibility(View.VISIBLE);
                    binding.buttonNfcSetting.setVisibility(View.GONE);
                    binding.textviewNfcStatus.setText("NFC正在关闭");
                    break;
                case NfcAdapter.STATE_ON:
                    // NFC已开启
                    binding.progressbarNfcDelay.setVisibility(View.GONE);
                    binding.buttonNfcSetting.setVisibility(View.GONE);
                    binding.textviewNfcStatus.setText("NFC已开启");
                    break;
                case NfcAdapter.STATE_TURNING_ON:
                    // NFC正在开启
                    binding.progressbarNfcDelay.setVisibility(View.VISIBLE);
                    binding.buttonNfcSetting.setVisibility(View.GONE);
                    binding.textviewNfcStatus.setText("NFC正在开启");
                    break;
            }
        });

        nfcViewModel.getCardType().observe(this, type -> {
            // 更新UI展示标签类型
            binding.textviewCardType.setText("卡片类型: " + type);
        });

        nfcViewModel.getCardNumber().observe(this, number -> {
            // 更新UI展示标签号码
            binding.textviewCardNumber.setText("卡号: " + number);
        });
    }

    private void updateCardList() {
        cardNumbers = appSetting.getCardNumbers();
        arrayAdapter.clear();
        arrayAdapter.addAll(cardNumbers);
        arrayAdapter.notifyDataSetChanged();
    }

    // 删除卡号确认
    private void showDeleteConfirmationDialog(String cardNumber) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("卡号设置")
                .setMessage(cardNumber + "要怎么做呢")
                .setNeutralButton("编辑", (dialog, which) -> showEditCardNumberDialog(cardNumber))
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (dialog, which) -> {
                    appSetting.deleteCardNumber(cardNumber);
                    updateCardList();
                })
                .show();
    }

    // 显示编辑卡号的对话框
    private void showEditCardNumberDialog(String cardNumber) {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(cardNumber);

        new MaterialAlertDialogBuilder(this)
                .setTitle("编辑卡号")
                .setView(input)
                .setPositiveButton("保存", (dialog, which) -> {
                    String newCardNumber = input.getText().toString();
                    if (!newCardNumber.isEmpty()) {
                        appSetting.editCardNumber(cardNumber, newCardNumber);
                        updateCardList();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void loadHostnameAndPort() {
        String hostname = appSetting.getHostname();
        String port = appSetting.getPort();
        String password = appSetting.getPassword();

        if (appSetting.hasServerSettings()) {
            binding.buttonSettingServer.setText("编辑");
            binding.textviewServerAddress.setText(hostname + ":" + port + "（是否启用加密传输）: " + !password.isEmpty());
            Toast.makeText(this, "已读取之前保存的服务器", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleConnectButtonClick() {
        String buttonText = binding.buttonConnectServer.getText().toString();
        switch (buttonText) {
            case "连接服务器":
                String hostname = appSetting.getHostname();
                String port = appSetting.getPort();
                String password = appSetting.getPassword();

                // 开始WebSocket连接
                SpiceClient.getInstance().connectWebSocket("ws://" + hostname + ":" + port, password);
                break;
            case "断开连接":
                // 断开WebSocket连接
                SpiceClient.getInstance().closeWebSocket();
                break;
        }
    }

    @Override
    public void onConnectionStatusChanged(boolean isConnected) {
        runOnUiThread(() -> {
            binding.buttonConnectServer.setText(isConnected ? "断开连接" : "连接服务器");
            binding.buttonConnectServer.setIcon(ContextCompat.getDrawable(this, isConnected ? R.drawable.ic_connect_server_close : R.drawable.ic_connect_server));
        });
        runOnUiThread(() -> {
            String connectionStatus;
            int color;
            if (isConnected) {
                connectionStatus = "已连接 ✓";
                color = Color.GREEN;
            } else {
                connectionStatus = "已断开 ×";
                color = Color.RED;
            }

            SpannableString spannableString = new SpannableString(connectionStatus);
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(color);
            spannableString.setSpan(colorSpan, connectionStatus.length() - 1, connectionStatus.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            binding.textviewServerConnectionStatus.setText(spannableString);
        });
    }

    @Override
    public void onMessageReceived(final String message) {
        // 确保在主线程上运行
        runOnUiThread(() -> binding.textviewServerResponse.setText(message));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            nfcManager.processTag(tag, nfcViewModel);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 注册NfcStateReceiver
        if (nfcManager.isNfcSupported()) {
            // NFC，启动
            nfcManager.enableForegroundDispatch(this);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        // NFC，关闭
        nfcManager.disableForegroundDispatch(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销NfcStateReceiver
        if (nfcStateReceiver != null) {
            unregisterReceiver(nfcStateReceiver);
        }
        // 关闭WebSocket连接
        SpiceClient.getInstance().closeWebSocket();
    }
}
