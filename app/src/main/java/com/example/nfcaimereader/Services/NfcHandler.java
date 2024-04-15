package com.example.nfcaimereader.Services;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcF;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nfcaimereader.MainActivity;
import com.example.nfcaimereader.R;

public class NfcHandler {
    private final Activity activity;

    // UI
    private final TextView textviewNfcStatus, textViewCardType, textViewCardNumber;
    private final ProgressBar progressBarNfcDelay;
    private final Button buttonNfcSetting;

    // NFC
    private final NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;

    public NfcHandler(Activity activity) {
        this.activity = activity;

        // UI
        textviewNfcStatus = activity.findViewById(R.id.textview_nfc_status);
        textViewCardType = activity.findViewById(R.id.textview_card_type);
        textViewCardNumber = activity.findViewById(R.id.textview_card_number);
        progressBarNfcDelay = activity.findViewById(R.id.progressBar_nfc_delay);
        buttonNfcSetting = activity.findViewById(R.id.button_nfc_setting);

        // 检查设备是否支持NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(activity);

        // 设置NFC设置按钮的点击事件
        buttonNfcSetting.setOnClickListener(v -> activity.startActivity(new Intent(Settings.ACTION_NFC_SETTINGS)));

        setupForegroundDispatchSystem();
    }

    private void setupForegroundDispatchSystem() {
        pendingIntent = PendingIntent.getActivity(
                activity, 0, new Intent(activity, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_MUTABLE
        );

        IntentFilter tech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);

        intentFiltersArray = new IntentFilter[]{tech};
        techListsArray = new String[][]{
                new String[]{"android.nfc.tech.NfcF"},
                new String[]{"android.nfc.tech.MifareClassic"}
        };
    }

    public void handleIntent(Intent intent) {
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag == null) {
                Toast.makeText(activity, "NFC标签无法识别", Toast.LENGTH_LONG).show();
                return;
            }
            processTag(tag);
        } else {
            Toast.makeText(activity, "不是NfcF或Mifare Classic类型的卡片", Toast.LENGTH_LONG).show();
        }
    }

    private void processTag(Tag tag) {
        StringBuilder cardType = new StringBuilder();
        StringBuilder cardNumber = new StringBuilder();

        // 检索支持的技术列表
        String[] techList = tag.getTechList();
        for (String tech : techList) {
            if (tech.equals(NfcF.class.getName())) {
                // 处理NfcF类型的卡片
                cardType.append("卡片类型: Felica");

                byte[] tagId = tag.getId();
                cardNumber.append("卡号: ").append(bytesToHex(tagId));

                break;
            } else if (tech.equals(MifareClassic.class.getName())) {
                // 处理Mifare Classic类型的卡片
                cardType.append("卡片类型: Mifare Classic");

                byte[] tagId = tag.getId();
                cardNumber.append("卡号: ").append(bytesToHex(tagId));

                break;
            }
        }

        textViewCardType.setText(cardType);
        textViewCardNumber.setText(cardNumber);
    }

    // 将字节数组转换为十六进制字符串
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    public void enableForegroundDispatch() {
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(activity, pendingIntent, intentFiltersArray, techListsArray);
        }
    }

    public void disableForegroundDispatch() {
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(activity);
        }
    }

    private CountDownTimer countDownTimer;

    public void checkNfcStatus() {
        // NFC不可用
        if (nfcAdapter == null) return;

        buttonNfcSetting.setVisibility(View.GONE);

        // NFC已启用
        if (nfcAdapter.isEnabled()) {
            textviewNfcStatus.setText("NFC已启用");
            return;
        }

        progressBarNfcDelay.setVisibility(View.VISIBLE);

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(3000, 1000) {
            public void onTick(long millisUntilFinished) {
                // 更新文本视图的倒计时
                int secondsLeft = (int) (millisUntilFinished / 1000) + 1;
                textviewNfcStatus.setText("NFC状态检测中..." + secondsLeft);
            }

            public void onFinish() {
                // 隐藏进度条
                progressBarNfcDelay.setVisibility(View.GONE);

                if (nfcAdapter.isEnabled()) {
                    // NFC已启用
                    buttonNfcSetting.setVisibility(View.GONE);
                    textviewNfcStatus.setText("NFC已启用");
                } else {
                    // 提示用户在设置中启用NFC
                    buttonNfcSetting.setVisibility(View.VISIBLE);
                    textviewNfcStatus.setText("NFC功能未启用，请在设置中开启");
                }
            }
        }.start();

        // Thread statusCheckThread = new Thread(() -> {
        //     try {
        //         boolean initialStatus = nfcAdapter.isEnabled();
        //         boolean currentStatus;
        //         int retries = 3; // 最多检查次数
        //         do {
        //             Thread.sleep(1000); // 1秒暂停时间，减少轮询频率
        //             currentStatus = nfcAdapter.isEnabled();
        //             retries--;
        //         } while (initialStatus == currentStatus && retries > 0);
        //         activity.runOnUiThread(this::updateNfcStatus);
        //     } catch (InterruptedException e) {
        //         Thread.currentThread().interrupt();
        //     }
        // });
        // statusCheckThread.start();
    }
}
