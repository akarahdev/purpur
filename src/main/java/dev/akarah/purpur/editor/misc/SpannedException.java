package dev.akarah.purpur.editor.misc;

public class SpannedException extends RuntimeException {
    SpanData spanData;
    String message;

    public SpanData spanData() {
        return spanData;
    }

    public SpannedException(String message, SpanData spanData) {
        super(message);
        this.message = message;
        this.spanData = spanData;
    }
}
