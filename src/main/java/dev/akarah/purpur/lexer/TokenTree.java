package dev.akarah.purpur.lexer;

import dev.akarah.purpur.misc.SpanData;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.stream.Collectors;

public interface TokenTree {
    record StartOfStream(SpanData spanData) implements TokenTree {
        @Override
        public @NonNull String toString() {
            return "";
        }
    }
    record EndOfStream(SpanData spanData) implements TokenTree {
        @Override
        public @NonNull String toString() {
            return "";
        }
    }
    record Identifier(String name, SpanData spanData) implements TokenTree {
        @Override
        public @NonNull String toString() {
            return name;
        }
    }
    record Number(String value, SpanData spanData) implements TokenTree {
        @Override
        public @NonNull String toString() {
            return value;
        }
    }
    record StringLiteral(String value, SpanData spanData) implements TokenTree {
        @Override
        public @NonNull String toString() {
            return '"' + value.replace("\"", "\\\"") + '"';
        }
    }
    record ComponentLiteral(String value, SpanData spanData) implements TokenTree {
        @Override
        public @NonNull String toString() {
            return '$' + '"' + value.replace("\"", "\\\"") + '"';
        }
    }
    record LocKeyword(SpanData spanData) implements TokenTree {
        @Override
        public @NonNull String toString() {
            return "loc";
        }
    }
    record VecKeyword(SpanData spanData) implements TokenTree {
        @Override
        public @NonNull String toString() {
            return "vec";
        }
    }
    record LocalKeyword(SpanData spanData) implements TokenTree {
        @Override
        public @NonNull String toString() {
            return "local";
        }
    }
    record GameKeyword(SpanData spanData) implements TokenTree {
        @Override
        public @NonNull String toString() {
            return "game";
        }
    }
    record SavedKeyword(SpanData spanData) implements TokenTree {
        @Override
        public @NonNull String toString() {
            return "saved";
        }
    }

    record GameValueKeyword(SpanData spanData) implements TokenTree {
        @Override
        public @NonNull String toString() {
            return "gamevalue";
        }
    }
    record ParticleKeyword(SpanData spanData) implements TokenTree {
        @Override
        public @NonNull String toString() {
            return "particle";
        }
    }
    record SoundKeyword(SpanData spanData) implements TokenTree {
        @Override
        public @NonNull String toString() {
            return "sound";
        }
    }
    record NbtKeyword(SpanData spanData) implements TokenTree {
        @Override
        public @NonNull String toString() {
            return "nbt";
        }
    }
    record ParamKeyword(SpanData spanData) implements TokenTree {
        @Override
        public @NonNull String toString() {
            return "param";
        }
    }
    record PluralKeyword(SpanData spanData) implements TokenTree {
        @Override
        public @NonNull String toString() {
            return "plural";
        }
    }
    record OptionalKeyword(SpanData spanData) implements TokenTree {
        @Override
        public @NonNull String toString() {
            return "optional";
        }
    }
    record ItemKeyword(SpanData spanData) implements TokenTree {
        @Override
        public @NonNull String toString() {
            return "item";
        }
    }
    record PotionKeyword(SpanData spanData) implements TokenTree {
        @Override
        public @NonNull String toString() {
            return "potion";
        }
    }
    record TagKeyword(SpanData spanData) implements TokenTree {
        @Override
        public @NonNull String toString() {
            return "tag";
        }
    }
    record HintKeyword(SpanData spanData) implements TokenTree {
        @Override
        public @NonNull String toString() {
            return "tag";
        }
    }
    record Parenthesis(List<TokenTree> children, SpanData spanData) implements TokenTree {
        @Override
        public @NonNull String toString() {
            return "(" + children.stream().map(Object::toString).collect(Collectors.joining()) + ")";
        }
    }
    record Brackets(List<TokenTree> children, SpanData spanData) implements TokenTree {
        @Override
        public @NonNull String toString() {
            return "[" + children.stream().map(Object::toString).collect(Collectors.joining()) + "]";
        }
    }
    record AngleBrackets(List<TokenTree> children, SpanData spanData) implements TokenTree {
        @Override
        public @NonNull String toString() {
            return "<" + children.stream().map(Object::toString).collect(Collectors.joining()) + ">";
        }
    }
    record Braces(List<TokenTree> children, SpanData spanData) implements TokenTree {
        @Override
        public @NonNull String toString() {
            return "{" + children.stream().map(Object::toString).collect(Collectors.joining()) + "}";
        }
    }
    record Equals(SpanData spanData) implements TokenTree {
        @Override
        public @NonNull String toString() {
            return "=";
        }
    }
    record Comma(SpanData spanData) implements TokenTree {
        @Override
        public @NonNull String toString() {
            return ",";
        }
    }
    record Semicolon(SpanData spanData) implements TokenTree {
        @Override
        public @NonNull String toString() {
            return ";";
        }
    }
    record Question(SpanData spanData) implements TokenTree {
        @Override
        public @NonNull String toString() {
            return "?";
        }
    }

    SpanData spanData();
}
