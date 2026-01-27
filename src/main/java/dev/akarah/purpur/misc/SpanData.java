package dev.akarah.purpur.misc;

public record SpanData(
        String buffer,
        int start,
        int end
) {
    public int line() {
        return buffer.substring(0, start).split("\n").length;
    }

    public String toString() {
        return "@" + start + ".." + end;
    }
}
