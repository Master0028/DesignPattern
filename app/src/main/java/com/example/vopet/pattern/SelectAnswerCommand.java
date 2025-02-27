package com.example.vopet.pattern;

import android.widget.Button;

import com.example.vopet.activity.StudyByMulChoiceActivity;

public class SelectAnswerCommand implements Command {
    private StudyByMulChoiceActivity activity;
    private Button selectedButton;

    public SelectAnswerCommand(StudyByMulChoiceActivity activity, Button selectedButton) {
        this.activity = activity;
        this.selectedButton = selectedButton;
    }

    @Override
    public void execute() {
        activity.handleAnswer(selectedButton);
    }

}
