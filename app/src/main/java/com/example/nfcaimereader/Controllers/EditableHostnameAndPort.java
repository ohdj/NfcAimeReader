package com.example.nfcaimereader.Controllers;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.nfcaimereader.Client.SpiceClient;
import com.example.nfcaimereader.R;
import com.google.android.material.button.MaterialButton;

public class EditableHostnameAndPort {
    private final Activity activity;

    private final EditText editTextHostname, editTextPort, editTextPassword;
    private final MaterialButton buttonControlEditText;
    private final Button buttonConnectServer;

    public EditableHostnameAndPort(Activity activity) {
        this.activity = activity;

        // UI
        editTextHostname = activity.findViewById(R.id.edittext_hostname);
        editTextPort = activity.findViewById(R.id.edittext_port);
        editTextPassword = activity.findViewById(R.id.edittext_password);
        buttonControlEditText = activity.findViewById(R.id.button_control_edit_text);

        setListeners();

        loadHostnameAndPort();

        // 连接服务器按钮
        buttonConnectServer = activity.findViewById(R.id.button_connect_server);
        buttonConnectServer.setOnClickListener(v -> handleConnectButtonClick());
    }

    private void setListeners() {
        TextWatcher textWatcher = createTextWatcher();
        editTextHostname.addTextChangedListener(textWatcher);
        editTextPort.addTextChangedListener(textWatcher);

        buttonControlEditText.setOnClickListener(v -> handleButtonClick());
    }

    private TextWatcher createTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hostnameIsEmpty = editTextHostname.getText().toString().isEmpty();
                boolean portIsEmpty = editTextPort.getText().toString().isEmpty();

                if (hostnameIsEmpty || portIsEmpty) {
                    buttonControlEditText.setEnabled(false);
                    return;
                }

            /*
                ^
                (
                    (?!0)[0-9]{1,4}    # 匹配1到9999之间的任意数字且首位不能为0；其中{1,4}表示长度为1到4的数字串
                |
                    [1-5][0-9]{4}      # 匹配10000到59999之间的数字；首位为1-5，其余为0-9的四位数
                |
                    6[0-4][0-9]{3}     # 匹配60000到64999之间的数字；首位为6，第二位为0-4，后跟任意三位数字
                |
                    65[0-4][0-9]{2}    # 匹配65000到65499之间的数字；前两位为65，第三位为0-4，后跟任意两位数字
                |
                    655[0-2][0-9]      # 匹配65500到65529之间的数字；前三位为655，第四位为0-2，后跟任意一位数字
                |
                    6553[0-5]          # 匹配65530到65535之间的数字；前四位为6553，最后一位为0-5之间的数字
                )
                $
            */
                // 匹配从0到65535之间的数字，用于验证端口号
                final String PORT_PATTERN = "^((?!0)[0-9]{1,4}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$";

                String portString = editTextPort.getText().toString();
                buttonControlEditText.setEnabled(portString.matches(PORT_PATTERN));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
    }

    private void handleButtonClick() {
        String buttonText = buttonControlEditText.getText().toString();
        switch (buttonText) {
            case "编辑":
                editTextHostname.setEnabled(true);
                editTextPort.setEnabled(true);
                buttonControlEditText.setText("保存");
                buttonControlEditText.setIcon(ContextCompat.getDrawable(activity, R.drawable.ic_editor_save));
                break;
            case "保存":
                editTextHostname.setEnabled(false);
                editTextPort.setEnabled(false);
                buttonControlEditText.setText("编辑");
                buttonControlEditText.setIcon(ContextCompat.getDrawable(activity, R.drawable.ic_editor_edit));

                // 永久化保存hostname和port
                String hostnameString = editTextHostname.getText().toString();
                String portString = editTextPort.getText().toString();
                saveHostnameAndPort(hostnameString, portString);

                break;
        }
    }

    private void loadHostnameAndPort() {
        SharedPreferences sharedPref = activity.getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        String hostname = sharedPref.getString("hostname", "");
        String port = sharedPref.getString("port", "");

        editTextHostname.setText(hostname);
        editTextPort.setText(port);

        // 判断 SharedPreferences 中是否有保存的值
        boolean hasSavedValues = !hostname.isEmpty() && !port.isEmpty();
        buttonControlEditText.setEnabled(hasSavedValues); // 如果有保存的值则启用按钮

        // 如果有保存的值，设置按钮文本为“编辑”，否则保持为“保存”
        if (hasSavedValues) {
            editTextHostname.setEnabled(false);
            editTextPort.setEnabled(false);
            buttonControlEditText.setText("编辑");
            buttonControlEditText.setIcon(ContextCompat.getDrawable(activity, R.drawable.ic_editor_edit));
            Toast.makeText(activity, "已读取之前保存的服务器", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveHostnameAndPort(String ip, String port) {
        SharedPreferences sharedPref = activity.getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("hostname", ip);
        editor.putString("port", port);
        editor.apply();
        Toast.makeText(activity, "已保存", Toast.LENGTH_SHORT).show();
    }

    private void handleConnectButtonClick() {
        String buttonText = buttonConnectServer.getText().toString();
        switch (buttonText) {
            case "连接服务器":
                String server = editTextHostname.getText().toString();
                String port = editTextPort.getText().toString();
                String password = editTextPassword.getText().toString();
                // 开始WebSocket连接
                SpiceClient.getInstance().connectWebSocket("ws://" + server + ":" + port, password);
                break;
            case "断开连接":
                // 断开WebSocket连接
                SpiceClient.getInstance().closeWebSocket();
                break;
        }
    }
}
