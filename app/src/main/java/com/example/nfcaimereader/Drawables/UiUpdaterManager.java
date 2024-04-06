package com.example.nfcaimereader.Drawables;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.nfcaimereader.R;

public class UiUpdaterManager implements UiUpdater {
    private final Activity activity;
    private final TextView textviewCardType;
    private final TextView textviewCardNumber;

    public UiUpdaterManager(Activity activity) {
        this.activity = activity;

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

    @Override
    public void setOnClickListener(int viewId, View.OnClickListener onClickListener) {
        View view = activity.findViewById(viewId);
        if (view instanceof Button) {
            view.setOnClickListener(onClickListener);
        }
    }
}
