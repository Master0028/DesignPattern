package com.example.vopet.pattern;

import com.example.vopet.activity.StudyByFlashcardActivity;

public class FlipCardCommand implements Command {
    private StudyByFlashcardActivity activity;

    public FlipCardCommand(StudyByFlashcardActivity activity) {
        this.activity = activity;
    }

    @Override
    public void execute() {
        activity.flipCard();
    }
}
