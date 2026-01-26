package dev.akarah.purpur.lexer;

import dev.akarah.purpur.misc.SpanData;

import java.util.List;

public interface TokenTree {
    record StartOfStream(SpanData spanData) implements TokenTree { }
    record EndOfStream(SpanData spanData) implements TokenTree { }
    record Identifier(String name, SpanData spanData) implements TokenTree { }
    record Number(String value, SpanData spanData) implements TokenTree {}
    record StringLiteral(String value, SpanData spanData) implements TokenTree {}
    record ComponentLiteral(String value, SpanData spanData) implements TokenTree {}
    record LocKeyword(SpanData spanData) implements TokenTree {}
    record VecKeyword(SpanData spanData) implements TokenTree {}
    record LocalKeyword(SpanData spanData) implements TokenTree {}
    record GameKeyword(SpanData spanData) implements TokenTree {}
    record SavedKeyword(SpanData spanData) implements TokenTree {}
    record GameValueKeyword(SpanData spanData) implements TokenTree {}
    record ParamKeyword(SpanData spanData) implements TokenTree {}
    record PluralKeyword(SpanData spanData) implements TokenTree {}
    record OptionalKeyword(SpanData spanData) implements TokenTree {}
    record ItemKeyword(SpanData spanData) implements TokenTree {}
    record TagKeyword(SpanData spanData) implements TokenTree {}
    record Parenthesis(List<TokenTree> children, SpanData spanData) implements TokenTree {}
    record Brackets(List<TokenTree> children, SpanData spanData) implements TokenTree {}
    record Braces(List<TokenTree> children, SpanData spanData) implements TokenTree {}
    record Equals(SpanData spanData) implements TokenTree {}
    record Comma(SpanData spanData) implements TokenTree {}
    record Semicolon(SpanData spanData) implements TokenTree {}
    record Question(SpanData spanData) implements TokenTree {}

    SpanData spanData();
}
