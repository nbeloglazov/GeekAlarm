package com.geekalarm.android.tasks;

import android.graphics.Bitmap;

public class Task {

    private Bitmap question;
    private Bitmap[] choices;
    private int correct;
    private String id;
    // Error message id is needed 
    // when task downloading is failed.
    private int errorMessageId;

    public Task() {
        choices = new Bitmap[4];
    }

    public Bitmap getQuestion() {
        return question;
    }

    public void setQuestion(Bitmap question) {
        this.question = question;
    }

    public Bitmap getChoice(int num) {
        return choices[num];
    }

    public void setChoices(Bitmap[] choices) {
        this.choices = choices;
    }

    public void setChoice(int pos, Bitmap choice) {
        choices[pos] = choice;
    }

    public int getCorrect() {
        return correct;
    }

    public void setCorrect(int correct) {
        this.correct = correct;
    }

    public int getErrorMessageId() {
		return errorMessageId;
	}

	public void setErrorMessageId(int errorMessageId) {
		this.errorMessageId = errorMessageId;
	}

	public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
