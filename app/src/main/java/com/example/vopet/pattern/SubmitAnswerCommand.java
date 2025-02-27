package com.example.vopet.pattern;

import com.example.vopet.activity.StudyByFindAndFillActivity;

public class SubmitAnswerCommand implements Command {
    private StudyByFindAndFillActivity activity;

    public SubmitAnswerCommand(StudyByFindAndFillActivity activity) {
        this.activity = activity;
    }

    @Override
    public void execute() {
        activity.checkAnswer();
    }
}
