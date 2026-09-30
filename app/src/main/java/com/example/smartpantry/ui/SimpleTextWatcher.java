package com.example.smartpantry.ui;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * TextWatcher needs three methods but we usually only care about one. This base
 * class supplies empty versions of the other two, so screens only override
 * afterTextChanged.
 */
public abstract class SimpleTextWatcher implements TextWatcher {

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // not needed
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // not needed
    }

    @Override
    public abstract void afterTextChanged(Editable s);
}
