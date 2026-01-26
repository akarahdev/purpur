package dev.akarah.purpur.parser.ast.value;

import dev.akarah.purpur.mappings.MappingsRepository;
import dev.akarah.purpur.misc.SpanData;
import dev.akarah.purpur.misc.SpannedException;
import dev.akarah.purpur.parser.CodegenContext;
import dev.dfonline.flint.actiondump.codeblocks.ActionType;
import dev.dfonline.flint.templates.argument.NumberArgument;
import dev.dfonline.flint.templates.argument.TagArgument;
import dev.dfonline.flint.templates.argument.abstracts.Argument;

public record TagLiteral(String tag, String option, SpanData spanData) implements Value {
    @Override
    public void lowerToParsable(StringBuilder builder, int depth) {
        builder.append("tag ");
        builder.append(tag);
        builder.append(".");
        builder.append(option);
    }

    public Argument createArgument(CodegenContext ctx, ActionType actionType, int argIndex) {
        var dfTag = MappingsRepository.get().getDfTag(new MappingsRepository.ScriptBlockTag(this.tag, this.option));
        if (dfTag == null) {
            ctx.errors().add(new SpannedException(
                    this.tag + "." + this.option + " is not a valid block tag",
                    this.spanData()
            ));
            return new NumberArgument(argIndex, 0);
        }
        for (var tagPossibility : actionType.tags()) {
            if (tagPossibility.name().equals(dfTag.tag())) {
                return new TagArgument(tagPossibility.slot(), dfTag.option(), dfTag.tag(), actionType.name(), MappingsRepository.fancyNameToId(actionType.codeblockName()));
            }
        }
        ctx.errors().add(new SpannedException(
                this.tag + "." + this.option + " is not a valid block tag for this codeblock",
                this.spanData()
        ));
        return new NumberArgument(argIndex, 0);
    }
}
