package com.example.nfcaimereader.Controllers;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import com.example.nfcaimereader.R;
import com.example.nfcaimereader.Utils.AppSetting;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ServerSettingsDialog {

    public static void showServerSettingsDialog(Context context, AppSetting appSetting, boolean isEdit, final ServerDialogListener listener) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.server_settings_view, null);
        EditText editTextHostname = view.findViewById(R.id.edittext_hostname);
        EditText editTextPort = view.findViewById(R.id.edittext_port);
        EditText editTextPassword = view.findViewById(R.id.edittext_password);

        // 如果是“编辑”状态，就加载存储的值
        if (isEdit && appSetting != null) {
            editTextHostname.setText(appSetting.getHostname());
            editTextPort.setText(appSetting.getPort());
            editTextPassword.setText(appSetting.getPassword());
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
                .setTitle(isEdit ? "编辑服务器" : "设定服务器")
                .setView(view)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", null)
                .setCancelable(false);

        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            Button buttonSave = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            // 如果处于“设定服务器”状态，禁用保存按钮
            if (!isEdit) {
                buttonSave.setEnabled(false);
            }

            TextWatcher textWatcher = getTextWatcher(buttonSave, editTextHostname, editTextPort);

            // 给EditText添加监听
            editTextHostname.addTextChangedListener(textWatcher);
            editTextPort.addTextChangedListener(textWatcher);

            buttonSave.setOnClickListener(v -> {
                String hostname = editTextHostname.getText().toString();
                String port = editTextPort.getText().toString();
                String password = editTextPassword.getText().toString();

                if (listener != null) {
                    listener.onSave(hostname, port, password);
                }
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private static TextWatcher getTextWatcher(Button saveButton, EditText editTextHostname, EditText editTextPort) {
        // 创建TextWatcher来检查用户输入值是否符合保存服务器的条件
        return new TextWatcher() {
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
    }

    public interface ServerDialogListener {
        void onSave(String hostname, String port, String password);
    }
}