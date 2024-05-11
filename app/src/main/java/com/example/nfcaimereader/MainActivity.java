package com.example.nfcaimereader;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.nfcaimereader.Client.SpiceClient;
import com.example.nfcaimereader.Services.NfcEventListener;
import com.example.nfcaimereader.Services.NfcHandler;
import com.example.nfcaimereader.Services.NfcHelper;
import com.example.nfcaimereader.Services.NfcStateReceiver;
import com.example.nfcaimereader.Utils.AppSetting;
import com.example.nfcaimereader.databinding.ActivityMainBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements NfcStateReceiver.NfcStateChangeListener, SpiceClient.ConnectionStatusCallback, NfcEventListener {
    // NFC状态变化
    private NfcStateReceiver nfcStateReceiver;

    // NFC初始化
    private NfcHelper nfcHelper;
    private NfcHandler nfcHandler;

    // viewBinding
    private ActivityMainBinding binding;

    private AppSetting appSetting;

    ArrayAdapter<String> arrayAdapter;
    Set<String> cardNumbers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

        // 初始化BroadcastReceiver
        nfcStateReceiver = new NfcStateReceiver(this);
        // 注册BroadcastReceiver
        IntentFilter filter = new IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED);
        this.registerReceiver(nfcStateReceiver, filter);

        // NFC初始化
        nfcHelper = new NfcHelper(this);
        nfcHandler = new NfcHandler(this);

        // WebSocket回调
        SpiceClient.getInstance().setConnectionStatusCallback(this);

        // 去开启NFC按钮
        binding.buttonNfcSetting.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_NFC_SETTINGS)));

        // 编辑服务器按钮
        binding.buttonSettingServer.setOnClickListener(v -> handleSettingButtonClick());

        // 连接服务器按钮
        binding.buttonConnectServer.setOnClickListener(v -> handleConnectButtonClick());

        appSetting = new AppSetting(this);
        loadHostnameAndPort();

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

        cardNumbers = appSetting.getCardNumbers(); // 获取卡号列表

        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>(cardNumbers));
        binding.listviewCardNumbers.setAdapter(arrayAdapter);

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

    private void updateCardList() {
        cardNumbers = appSetting.getCardNumbers();
        arrayAdapter.clear();
        arrayAdapter.addAll(cardNumbers);
        arrayAdapter.notifyDataSetChanged();
    }

    // 删除卡号确认
    private void showDeleteConfirmationDialog(String cardNumber) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("删除卡号")
                .setMessage("您确定要删除\"" + cardNumber + "\"这个卡号吗?")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (dialog, which) -> {
                    appSetting.deleteCardNumber(cardNumber);
                    updateCardList();
                })
                .show();
    }

    private void handleSettingButtonClick() {
        String buttonText = binding.buttonSettingServer.getText().toString();
        switch (buttonText) {
            case "设定服务器":
                showEditServerDialog(false);
                break;
            case "编辑":
                showEditServerDialog(true);
                break;
        }
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

    private void showEditServerDialog(boolean isEdit) {
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(isEdit ? "编辑服务器" : "设定服务器")
                .setView(R.layout.dialog_server_edit)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", null)
                .setCancelable(false)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            EditText editTextHostname = dialog.findViewById(R.id.edittext_hostname);
            EditText editTextPort = dialog.findViewById(R.id.edittext_port);
            EditText editTextPassword = dialog.findViewById(R.id.edittext_password);

            // 如果是“编辑”状态，就加载存储的值
            if (isEdit) {
                String savedHostname = appSetting.getHostname();
                String savedPort = appSetting.getPort();
                String savePassword = appSetting.getPassword();
                editTextHostname.setText(savedHostname);
                editTextPort.setText(savedPort);
                editTextPassword.setText(savePassword);
            } else {
                // 如果是“设定服务器”状态，则禁用保存按钮
                saveButton.setEnabled(false);
            }

            // 创建TextWatcher来检查用户输入值是否符合保存服务器的条件
            TextWatcher watcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                /*
                    ^
                    (
                        0                   # 匹配0
                    |
                        (?!0)[0-9]{1,4}     # 匹配1到9999之间的任意数字且首位不能为0；其中{1,4}表示长度为1到4的数字串
                    |
                        [1-5][0-9]{4}       # 匹配10000到59999之间的数字；首位为1-5，其余为0-9的四位数
                    |
                        6[0-4][0-9]{3}      # 匹配60000到64999之间的数字；首位为6，第二位为0-4，后跟任意三位数字
                    |
                        65[0-4][0-9]{2}     # 匹配65000到65499之间的数字；前两位为65，第三位为0-4，后跟任意两位数字
                    |
                        655[0-2][0-9]       # 匹配65500到65529之间的数字；前三位为655，第四位为0-2，后跟任意一位数字
                    |
                        6553[0-5]           # 匹配65530到65535之间的数字；前四位为6553，最后一位为0-5之间的数字
                    )
                    $
                */
                    // 匹配从0到65535之间的数字，用于验证端口号
                    final String PORT_PATTERN = "^(0|(?!0)[0-9]{1,4}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$";

                    String hostname = editTextHostname.getText().toString();
                    String portString = editTextPort.getText().toString();
                    boolean hostnameIsValid = !hostname.isEmpty();          // 确保hostname不为空
                    boolean portIsValid = portString.matches(PORT_PATTERN); // 检查端口号是否有效
                    saveButton.setEnabled(hostnameIsValid && portIsValid);  // 仅当hostname和端口号都有效时，才使保存按钮可点击
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            };

            // 给EditText添加监听
            editTextHostname.addTextChangedListener(watcher);
            editTextPort.addTextChangedListener(watcher);

            saveButton.setOnClickListener(view -> {
                String hostname = editTextHostname.getText().toString();
                String port = editTextPort.getText().toString();
                String password = editTextPassword.getText().toString();

                // 保存到SharedPreferences
                appSetting.saveServerSettings(hostname, port, password);

                Toast.makeText(MainActivity.this, "已保存", Toast.LENGTH_SHORT).show();

                binding.buttonSettingServer.setText("编辑");
                binding.textviewServerAddress.setText(hostname + ":" + port + "（是否启用加密传输）: " + !password.isEmpty());

                // 关闭对话框
                dialog.dismiss();
            });
        });

        dialog.show();
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
    public void onNfcStateChanged(int state) {
        // 处理NFC状态变化
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
    public void onTagDiscovered(String cardType, String cardNumber) {
        // 显示卡片类型和卡号
        binding.textviewCardType.setText("卡片类型: " + cardType);
        binding.textviewCardNumber.setText("卡号: " + cardNumber);
        SpiceClient.getInstance().sendCardId(cardNumber, appSetting.getPassword());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                nfcHandler.processTag(tag);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        nfcHelper.enableNfcScan(this);
    }


    @Override
    protected void onPause() {
        super.onPause();
        nfcHelper.disableNfcScan(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销BroadcastReceiver
        this.unregisterReceiver(nfcStateReceiver);
        // 关闭WebSocket连接
        SpiceClient.getInstance().closeWebSocket();
    }
}
