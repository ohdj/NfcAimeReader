package com.example.nfcaimereader.Drawables;

import android.view.View;

public interface UiUpdater {
    void setCardType(String cardType);

    void setCardNumber(String cardNumber);

    void setOnClickListener(int viewId, View.OnClickListener onClickListener);
}
