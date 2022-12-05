package de.yonedash.solity;

public class TextPrompt {

    public static final int UNSKIPPABLE = 0, MANUAL_SKIP = -1;

    private final String author, text;
    private final Runnable runnable;
    private final int waitTime;

    public TextPrompt(String author, String text, Runnable runnable, int waitTime) {
        this.author = author;
        this.text = text;
        this.runnable = runnable;
        this.waitTime = waitTime;
    }

    public TextPrompt(String author, String text, Runnable runnable) {
        this(author, text, runnable, 5);
    }

    public String author() {
        return this.author;
    }

    public String text() {
        return this.text;
    }

    public Runnable runnable() {
        return this.runnable;
    }

    public int waitTime() {
        return this.waitTime;
    }

}
