package dev.akarah.purpur.parser;

import com.google.common.collect.Lists;
import dev.akarah.purpur.lexer.TokenTree;
import dev.akarah.purpur.misc.SpannedException;
import dev.akarah.purpur.parser.ast.Block;
import dev.akarah.purpur.parser.ast.Invoke;
import dev.akarah.purpur.parser.ast.Value;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class Parser {
    List<TokenTree> tokens;
    List<SpannedException> errors = Lists.newArrayList();
    int index = 0;

    public Parser(List<TokenTree> tokens) {
        this.tokens = tokens;
    }

    public Parser(List<TokenTree> tokens, List<SpannedException> errors) {
        this.tokens = tokens;
        this.errors = errors;
    }

    public void skipStartStreams() {
        while(this.index < this.tokens.size() && this.tokens.get(this.index) instanceof TokenTree.StartOfStream) {
            this.index++;
        }
    }

    public boolean canRead() {
        return this.index < this.tokens.size() && !(this.tokens.get(this.index) instanceof TokenTree.EndOfStream);
    }

    public TokenTree peek() {
        skipStartStreams();
        return this.tokens.get(this.index);
    }

    public TokenTree read() {
        var t = this.tokens.get(this.index);
        index += 1;
        return t;
    }

    public <T extends TokenTree> T expect(Class<T> clazz) {
        skipStartStreams();
        var tok = this.tokens.get(this.index);
        this.index += 1;
        if(!clazz.isInstance(tok)) {
            this.errors.add(new SpannedException(
                    "Expected " + clazz.getName() + ", found " + tok.getClass().getName(),
                    tok.spanData()
            ));
            throw new SpannedException("Expected " + clazz.getName() + ", found " + tok.getClass().getName(),
                    tok.spanData());
        }
        return clazz.cast(tok);
    }

    public List<SpannedException> errors() {
        return this.errors;
    }

    public @Nullable Invoke parseInvoke() {
        try {
            var ident = expect(TokenTree.Identifier.class);
            var paren = expect(TokenTree.Parenthesis.class);

            var argParser = new Parser(paren.children(), this.errors);
            var args = Lists.<Value>newArrayList();
            while(argParser.canRead()) {
                var arg = argParser.parseValue();
                args.add(arg);
                if(argParser.canRead()) {
                    argParser.expect(TokenTree.Comma.class);
                }
            }

            var block = Optional.<Block>empty();
            if(peek() instanceof TokenTree.Braces braces) {
                var blockParser = new Parser(braces.children(), this.errors);
                var invokes = Lists.<Invoke>newArrayList();
                while(blockParser.canRead()) {
                    var invoke = blockParser.parseInvoke();
                    if(invoke != null) invokes.add(invoke);
                }
                block = Optional.of(new Block(invokes));
            }

            return new Invoke(
                    new Value.Variable(ident.name(), "line", ident.spanData()),
                    args,
                    block
            );
        } catch (Exception e) {
            return null;
        }
    }

    public Value parseValue() {
        return switch (read()) {
            case TokenTree.Identifier identifier -> new Value.Variable(identifier.name(), "line", identifier.spanData());
            case TokenTree.Number number -> new Value.Number(number.value(), number.spanData());
            case TokenTree.StringLiteral stringLiteral -> new Value.StringLiteral(stringLiteral.value(), stringLiteral.spanData());
            case TokenTree.ComponentLiteral componentLiteral -> new Value.ComponentLiteral(componentLiteral.value(), componentLiteral.spanData());
            case TokenTree.LocalKeyword localKeyword -> {
                var name = expect(TokenTree.Identifier.class);
                yield new Value.Variable(name.name(), "local", name.spanData());
            }
            case TokenTree.GameKeyword gameKeyword -> {
                var name = expect(TokenTree.Identifier.class);
                yield new Value.Variable(name.name(), "game", name.spanData());
            }
            case TokenTree.SavedKeyword savedKeyword -> {
                var name = expect(TokenTree.Identifier.class);
                yield new Value.Variable(name.name(), "saved", name.spanData());
            }
            case TokenTree.TagKeyword tagKeyword -> {
                var ident = expect(TokenTree.Identifier.class);
                var split = ident.name().split("\\.");
                if(split.length != 2) {
                    this.errors.add(new SpannedException(
                            "A block tag's ID must have two parts, formatted like `tag.option`",
                            ident.spanData()
                    ));
                    yield null;
                }
                yield new Value.TagLiteral(split[0], split[1], ident.spanData());
            }
            default -> {
                this.index -= 1;
                yield null;
            }
        };
    }
}
