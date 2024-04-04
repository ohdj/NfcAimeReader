package com.example.nfcaimereader.Controllers;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.nfcaimereader.R;

public class EditableHostnameAndPort extends LinearLayout {

    private EditText editTextHostname;
    private EditText editTextPort;
    private Button buttonControlEditText;

    public EditableHostnameAndPort(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.activity_main, this, true);

        editTextHostname = findViewById(R.id.edittext_hostname);
        editTextPort = findViewById(R.id.edittext_port);
        buttonControlEditText = findViewById(R.id.button_serverEdit);

        setListeners();
    }

    private void setListeners() {
        TextWatcher textWatcher = createTextWatcher();
        editTextHostname.addTextChangedListener(textWatcher);
        editTextPort.addTextChangedListener(textWatcher);

        buttonControlEditText.setOnClickListener(view -> handleButtonClick());
    }

    private TextWatcher createTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hostnameNotEmpty = !editTextHostname.getText().toString().isEmpty();
                boolean portNotEmpty = !editTextPort.getText().toString().isEmpty();

                if (hostnameNotEmpty || portNotEmpty) {
                    buttonControlEditText.setVisibility(View.VISIBLE);
                    buttonControlEditText.setText("保存");
                    buttonControlEditText.setEnabled(true);
                } else {
                    buttonControlEditText.setVisibility(View.GONE);
                }

                if (!editTextPort.getText().toString().equals("")) {
                    int port = Integer.parseInt(editTextPort.getText().toString());
                    if (port >= 0 && port <= 65535) {
                        buttonControlEditText.setEnabled(true);
                    } else {
                        buttonControlEditText.setEnabled(false);
                    }
                }
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
                break;
            case "取消":
                editTextHostname.setText("");
                editTextPort.setText("");
                buttonControlEditText.setVisibility(View.GONE);
                break;
            case "保存":
                try {
                    saveHostnameAndPort();
                    editTextHostname.setEnabled(false);
                    editTextPort.setEnabled(false);
                    buttonControlEditText.setText("编辑");
                    Toast.makeText(getContext(), "已保存", Toast.LENGTH_SHORT).show();
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "端口号无效", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void saveHostnameAndPort() {
    }
}
