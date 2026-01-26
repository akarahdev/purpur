package dev.akarah.purpur.misc;

public record SpanData(
        String buffer,
        int start,
        int end
) {
    public String toString() {
        return "@" + start + ".." + end;
    }
}
