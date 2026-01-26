package dev.akarah.purpur.parser;

import com.google.common.collect.Lists;
import dev.akarah.purpur.lexer.TokenTree;
import dev.akarah.purpur.mappings.MappingsRepository;
import dev.akarah.purpur.misc.ParseResult;
import dev.akarah.purpur.misc.SpanData;
import dev.akarah.purpur.misc.SpannedException;
import dev.akarah.purpur.parser.ast.Block;
import dev.akarah.purpur.parser.ast.Invoke;
import dev.akarah.purpur.parser.ast.Program;
import dev.akarah.purpur.parser.ast.Value;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class Parser {
    List<TokenTree> tokens;
    List<SpannedException> errors = Lists.newArrayList();
    int index = 0;

    public static ParseResult<Program> parse(List<TokenTree> tokens) {
        var res = new Parser(tokens).parse();
        return new ParseResult<>(new Program(res.partialResult()), res.errors());
    }

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
            throw new SpannedException("Expected " + clazz.getName() + ", found " + tok.getClass().getName(),
                    tok.spanData());
        }
        return clazz.cast(tok);
    }

    public List<SpannedException> errors() {
        return this.errors;
    }

    public ParseResult<List<Invoke>> parse() {
        var invokes = Lists.<Invoke>newArrayList();
        while(canRead()) {
            var invoke = parseInvoke();
            if(invoke != null) invokes.add(invoke);
        }
        return new ParseResult<>(invokes, errors);
    }

    public @Nullable Invoke parseInvoke() {
        try {
            var ident = expect(TokenTree.Identifier.class);

            var subAction = Optional.<Value.Variable>empty();
            if(peek() instanceof TokenTree.Brackets) {
                var subActionTokens = expect(TokenTree.Brackets.class);

                if(subActionTokens.children().size() < 5) {
                    this.errors.add(new SpannedException(
                            "Sub-action blocks must have a valid action",
                            subActionTokens.spanData()
                    ));
                }
                if(subActionTokens.children().getFirst() instanceof TokenTree.Identifier(String name, SpanData spanData)) {
                    subAction = Optional.of(new Value.Variable(name, "line", spanData));
                } else {
                    this.errors.add(new SpannedException(
                            "Sub-action blocks must have a valid action in it's place",
                            subActionTokens.spanData()
                    ));
                }
            }


            var args = parseValues();

            var block = Optional.<Block>empty();
            if(peek() instanceof TokenTree.Braces) {
                block = Optional.of(parseBlock());
            }

            return new Invoke(
                    new Value.Variable(ident.name(), "line", ident.spanData()),
                    subAction,
                    args,
                    block
            );
        } catch (SpannedException e) {
            this.errors.add(e);
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public List<Value> parseValues() {
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

        return args;
    }

    public Block parseBlock() {
        var braces = expect(TokenTree.Braces.class);
        var blockParser = new Parser(braces.children(), this.errors);
        var invokes = Lists.<Invoke>newArrayList();
        while(blockParser.canRead()) {
            var invoke = blockParser.parseInvoke();
            if(invoke != null) invokes.add(invoke);
        }
        return new Block(invokes);
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
            case TokenTree.GameValueKeyword gameValueKeyword -> {
                var ident = expect(TokenTree.Identifier.class);
                var split = ident.name().split("\\.");
                if(split.length != 2) {
                    this.errors.add(new SpannedException(
                            "A game value's ID must have two parts, formatted like `target.value`",
                            ident.spanData()
                    ));
                    yield null;
                }
                yield new Value.GameValue(split[0], split[1], ident.spanData());
            }
            case TokenTree.LocKeyword locKeyword -> {
                var args = parseValues();
                if(args.size() != 3 && args.size() != 5) {
                    this.errors.add(new SpannedException(
                            "A location constructor must have either 3 or 5 arguments",
                            locKeyword.spanData()
                    ));
                    yield null;
                }
                for(var arg : args) {
                    if(!(arg instanceof Value.Number)) {
                        this.errors.add(new SpannedException(
                                "Location constructor arguments must be numbers",
                                arg.spanData()
                        ));
                    }
                }
                yield new Value.LocationLiteral(
                        Double.parseDouble(((Value.Number) args.get(0)).literal()),
                        Double.parseDouble(((Value.Number) args.get(1)).literal()),
                        Double.parseDouble(((Value.Number) args.get(2)).literal()),
                        Double.parseDouble(((Value.Number) args.get(3)).literal()),
                        Double.parseDouble(((Value.Number) args.get(4)).literal()),
                        locKeyword.spanData()
                );
            }
            case TokenTree.VecKeyword vecKeyword -> {
                var args = parseValues();
                if(args.size() != 3) {
                    this.errors.add(new SpannedException(
                            "A vector constructor must have either 3 or 5 arguments",
                            vecKeyword.spanData()
                    ));
                    yield null;
                }
                for(var arg : args) {
                    if(!(arg instanceof Value.Number)) {
                        this.errors.add(new SpannedException(
                                "Location constructor arguments must be numbers",
                                arg.spanData()
                        ));
                    }
                }
                yield new Value.VecLiteral(
                        Double.parseDouble(((Value.Number) args.get(0)).literal()),
                        Double.parseDouble(((Value.Number) args.get(1)).literal()),
                        Double.parseDouble(((Value.Number) args.get(2)).literal()),
                        vecKeyword.spanData()
                );
            }
            case TokenTree.ItemKeyword itemKeyword -> {
                var args = parseValues();
                if(args.isEmpty()) {
                    this.errors.add(new SpannedException(
                            "An item constructor must have an ID",
                            itemKeyword.spanData()
                    ));
                    yield null;
                }
                if(args.size() > 3) {
                    this.errors.add(new SpannedException(
                            "An item constructor only needs an ID, count, and components",
                            itemKeyword.spanData()
                    ));
                }

                var id = Identifier.fromNamespaceAndPath("minecraft", "air");
                if(!args.isEmpty()) {
                    var val = args.getFirst();
                    if(val instanceof Value.Variable variable) {
                        id = Identifier.parse(variable.name());
                        if(BuiltInRegistries.ITEM.getOptional(id).isEmpty()) {
                            this.errors.add(new SpannedException(
                                    "Invalid item ID",
                                    val.spanData()
                            ));
                        }
                    } else {
                        this.errors.add(new SpannedException(
                                "An item constructor's first argument must be an item ID",
                                val.spanData()
                        ));
                    }
                }

                var count = 1;
                if(args.size() >= 2) {
                    var val = args.get(1);
                    if(val instanceof Value.Number number) {
                        count = (int) Double.parseDouble(number.literal());
                        if(count > 99) {
                            this.errors.add(new SpannedException(
                                    "Item stacks can't have more than 99",
                                    val.spanData()
                            ));
                        }
                        if(count <= 0) {
                            this.errors.add(new SpannedException(
                                    "Item stacks can't have less than 1",
                                    val.spanData()
                            ));
                        }
                    } else {
                        this.errors.add(new SpannedException(
                                "An item constructor's second argument must be a count",
                                val.spanData()
                        ));
                    }
                }
                var is = new ItemStack(BuiltInRegistries.ITEM.getValue(id));
                is.setCount(count);
                yield new Value.ItemStackVarItem(
                        is,
                        itemKeyword.spanData()
                );
            }
            case TokenTree.ParamKeyword paramKeyword -> {
                var paramName = expect(TokenTree.Identifier.class);
                var paramTypeTokens = expect(TokenTree.Brackets.class);

                boolean plural = false;
                boolean optional = false;
                String type = "any";
                Value defaultValue = null;

                for(var arg : paramTypeTokens.children()) {
                    switch (arg) {
                        case TokenTree.PluralKeyword pluralKeyword -> plural = true;
                        case TokenTree.OptionalKeyword optionalKeyword -> optional = true;
                        case TokenTree.Identifier identifier -> type = identifier.name();
                        case TokenTree.EndOfStream end -> {}
                        default -> this.errors.add(new SpannedException(
                                "Invalid parameter modifier " + arg.getClass().getName() + " in parameter type",
                                arg.spanData()
                        ));
                    }
                }

                if(peek() instanceof TokenTree.Equals) {
                    expect(TokenTree.Equals.class);
                    defaultValue = parseValue();
                }

                var newType = MappingsRepository.scriptTypeToDfType(type);
                if(newType.isEmpty()) {
                    this.errors.add(new SpannedException(
                            type + " is not a valid parameter type",
                            paramTypeTokens.spanData()
                    ));
                    yield new Value.Number("0", paramTypeTokens.spanData());
                }

                yield new Value.ParameterLiteral(
                        paramName.name(),
                        newType.orElseThrow(),
                        plural,
                        optional,
                        defaultValue,
                        paramTypeTokens.spanData()
                );
            }
            default -> {
                this.index -= 1;
                this.errors.add(new SpannedException("Unexpected token " + peek().getClass().getName(), peek().spanData()));
                yield null;
            }
        };
    }
}
