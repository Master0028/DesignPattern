package com.example.vopet.pattern.command;

import android.content.Context;
import android.util.AttributeSet;

public class CommandButton extends androidx.appcompat.widget.AppCompatButton {
    private ICommand command;

    public CommandButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CommandButton(Context context) {
        super(context);
        init();
    }

    private void init() {
        setOnClickListener(v -> {
            if (command != null) {
                command.execute();
            }
        });
    }

    public void setCommand(ICommand command) {
        this.command = command;
    }
}
