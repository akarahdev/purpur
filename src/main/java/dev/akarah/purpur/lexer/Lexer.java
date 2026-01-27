package dev.akarah.purpur.lexer;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.akarah.purpur.misc.ParseResult;
import dev.akarah.purpur.misc.SpanData;
import dev.akarah.purpur.misc.SpannedException;
import dev.akarah.purpur.parser.ast.value.Variable;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class Lexer {
    StringReader stringReader;
    List<SpannedException> errors = Lists.newArrayList();

    public Lexer(String string) {
        this.stringReader = new StringReader(string);
    }

    public List<SpannedException> errors() {
        return errors;
    }

    public static ParseResult<List<TokenTree>> parse(String string) {
        return new Lexer(string).parse();
    }

    public ParseResult<List<TokenTree>> parse() {
        List<TokenTree> trees = Lists.newArrayList();
        trees.add(new TokenTree.StartOfStream(new SpanData(this.stringReader.getString(), 0, 0)));
        while(stringReader.canRead()) {
            var tree = parseSingleToken();
            if(tree != null) trees.add(tree);
        }
        for(int i = 0; i < 100; i++) {
            trees.add(new TokenTree.EndOfStream(new SpanData(this.stringReader.getString(), this.stringReader.getString().length(), this.stringReader.getString().length())));
        }
        return new ParseResult<>(trees, errors);
    }

    public @Nullable TokenTree parseSingleToken() {
        this.stringReader.skipWhitespace();
        if(!this.stringReader.canRead()) return null;
        Predicate<Character> numPredicate = ch -> Character.isDigit(ch) || ch == '-' || ch == '.';
        if(numPredicate.test(stringReader.peek())) {
            var sb = new StringBuilder();
            var start = this.stringReader.getCursor();
            while(numPredicate.test(stringReader.peek())) {
                sb.append(stringReader.read());
            }
            return new TokenTree.Number(sb.toString(), this.endSpan(start));
        }

        try {
            if(stringReader.peek() == 'n' && stringReader.peek(1) == '\"') {
                var start = this.stringReader.getCursor();
                expect('n');
                return new TokenTree.Number(parseStringLiteral(), this.endSpan(start));
            }
        } catch (Exception ignored) {

        }
        if(stringReader.peek() == '`') {
            var start = this.stringReader.getCursor();
            var sb = new StringBuilder();
            stringReader.skip();
            while(stringReader.canRead() && stringReader.peek() != '`') {
                sb.append(stringReader.read());
            }
            stringReader.skip();
            return new TokenTree.Identifier(sb.toString(), this.endSpan(start));
        }
        if(Variable.charIsAllowedInIdentifier(stringReader.peek())) {
            var start = this.stringReader.getCursor();
            var sb = new StringBuilder();
            while(this.stringReader.canRead() && Variable.charIsAllowedInIdentifier(stringReader.peek())) {
                sb.append(stringReader.read());
            }
            return switch (sb.toString()) {
                case "loc" -> new TokenTree.LocKeyword(this.endSpan(start));
                case "vec" -> new TokenTree.VecKeyword(this.endSpan(start));
                case "item" -> new TokenTree.ItemKeyword(this.endSpan(start));
                case "potion" -> new TokenTree.PotionKeyword(this.endSpan(start));
                case "tag" -> new TokenTree.TagKeyword(this.endSpan(start));
                case "hint" -> new TokenTree.HintKeyword(this.endSpan(start));
                case "game" -> new TokenTree.GameKeyword(this.endSpan(start));
                case "saved" -> new TokenTree.SavedKeyword(this.endSpan(start));
                case "local" -> new TokenTree.LocalKeyword(this.endSpan(start));
                case "gamevalue" -> new TokenTree.GameValueKeyword(this.endSpan(start));
                case "particle" -> new TokenTree.ParticleKeyword(this.endSpan(start));
                case "param" -> new TokenTree.ParamKeyword(this.endSpan(start));
                case "plural" -> new TokenTree.PluralKeyword(this.endSpan(start));
                case "optional" -> new TokenTree.OptionalKeyword(this.endSpan(start));
                case "sound" -> new TokenTree.SoundKeyword(this.endSpan(start));
                case "nbt" -> new TokenTree.NbtKeyword(this.endSpan(start));
                default -> new TokenTree.Identifier(sb.toString(), this.endSpan(start));
            };
        }
        if(stringReader.peek() == '"') {
            var start = this.stringReader.getCursor();
            return new TokenTree.StringLiteral(parseStringLiteral(), this.endSpan(start));
        }
        if(stringReader.peek() == '$') {
            var start = this.stringReader.getCursor();
            expect('$');
            return new TokenTree.ComponentLiteral(parseStringLiteral(), this.endSpan(start));
        }
        if(stringReader.peek() == '=') {
            var start = this.stringReader.getCursor();
            expect('=');
            return new TokenTree.Equals(this.endSpan(start));
        }

        var braces = lexGroup('{', '}', TokenTree.Braces::new);
        if(braces != null) return braces;
        var parens = lexGroup('(', ')', TokenTree.Parenthesis::new);
        if(parens != null) return parens;
        var brackets = lexGroup('[', ']', TokenTree.Brackets::new);
        if(brackets != null) return brackets;
        var angleBrackets = lexGroup('<', '>', TokenTree.AngleBrackets::new);
        if(angleBrackets != null) return angleBrackets;

        if(stringReader.peek() == ';') {
            var start = this.stringReader.getCursor();
            expect(';');
            return new TokenTree.Semicolon(this.endSpan(start));
        }
        if(stringReader.peek() == '?') {
            var start = this.stringReader.getCursor();
            expect('?');
            return new TokenTree.Question(this.endSpan(start));
        }
        if(stringReader.peek() == ',') {
            var start = this.stringReader.getCursor();
            expect(',');
            return new TokenTree.Comma(this.endSpan(start));
        }
        this.errors.add(new SpannedException("Unexpected character: " + stringReader.peek(), this.endSpan(this.stringReader.getCursor())));
        this.stringReader.skip();
        return null;
    }

    public void expect(char ch) {
        if(!stringReader.canRead()) {
            this.errors.add(new SpannedException("Expected " + ch + " but reached end of string", this.endSpan(this.stringReader.getCursor())));
            return;
        }
        if(stringReader.peek() != ch) {
            this.errors.add(new SpannedException("Expected " + ch + " but got " + stringReader.peek(), this.endSpan(this.stringReader.getCursor())));
            return;
        }
        stringReader.skip();
    }

    public String parseStringLiteral() {
        try {
            return this.stringReader.readQuotedString();
        } catch (Exception e) {
            this.errors.add(new SpannedException("Failed to read string literal", this.endSpan(this.stringReader.getCursor())));
        }
        return "";
    }

    public double parseNumber() {
        double d = 0;
        try {
            d = this.stringReader.readDouble();
        } catch (CommandSyntaxException e) {
            this.errors.add(new SpannedException("Failed to read number", this.endSpan(this.stringReader.getCursor())));
        }
        return d;
    }

    public SpanData endSpan(int start) {
        return new SpanData(this.stringReader.getString(), start, this.stringReader.getCursor());
    }

    public @Nullable TokenTree lexGroup(char startCh, char endCh, BiFunction<List<TokenTree>, SpanData, TokenTree> constructor) {
        if(stringReader.peek() == startCh) {
            var start = this.stringReader.getCursor();
            expect(startCh);
            var trees = Lists.<TokenTree>newArrayList();
            while(stringReader.canRead()) {
                stringReader.skipWhitespace();
                if(!stringReader.canRead() || stringReader.peek() == endCh) break;
                var token = parseSingleToken();
                if(token != null) trees.add(token);
            }
            for(int i = 0; i < 10; i++) {
                trees.add(new TokenTree.EndOfStream(new SpanData(this.stringReader.getString(), this.stringReader.getString().length(), this.stringReader.getString().length())));
            }
            if(stringReader.canRead() && stringReader.peek() == endCh) {
                expect(endCh);
            }
            return constructor.apply(trees, this.endSpan(start));
        }
        return null;
    }
}
