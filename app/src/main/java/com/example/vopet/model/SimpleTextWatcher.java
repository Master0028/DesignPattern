package com.example.vopet.model;

import android.text.Editable;
import android.text.TextWatcher;

public abstract class SimpleTextWatcher implements TextWatcher {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // Không cần thực hiện gì
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // Không cần thực hiện gì
    }

    @Override
    public void afterTextChanged(Editable s) {
        // Override phương thức này trong adapter
    }
}

