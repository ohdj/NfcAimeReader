package com.example.nfcaimereader.Drawables;

import android.app.Activity;
import android.widget.TextView;

import com.example.nfcaimereader.R;

public class UiUpdaterManager implements UiUpdater {
    private final TextView textviewCardType;
    private final TextView textviewCardNumber;

    public UiUpdaterManager(Activity activity) {
        // 卡片类型、卡号
        textviewCardType = activity.findViewById(R.id.textview_cardType);
        textviewCardNumber = activity.findViewById(R.id.textview_cardNumber);
    }

    @Override
    public void setCardType(String cardType) {
        textviewCardType.setText(cardType);
    }

    @Override
    public void setCardNumber(String cardNumber) {
        textviewCardNumber.setText(cardNumber);
    }
}
