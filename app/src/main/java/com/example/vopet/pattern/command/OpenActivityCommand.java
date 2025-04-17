package com.example.vopet.pattern.command;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class OpenActivityCommand implements ICommand {
    private Context context;
    private Class<?> activityClass;
    private IBundleProvider bundleProvider;

    public OpenActivityCommand(Context context, Class<?> activityClass, IBundleProvider bundleProvider) {
        this.context = context;
        this.activityClass = activityClass;
        this.bundleProvider = bundleProvider;
    }

    @Override
    public void execute() {
        Intent intent = new Intent(context, activityClass);
        if (bundleProvider != null) {
            intent.putExtras(bundleProvider.getBundle());
        }
        context.startActivity(intent);
    }

}
