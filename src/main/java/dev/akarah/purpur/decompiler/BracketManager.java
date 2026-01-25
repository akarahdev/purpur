package dev.akarah.purpur.decompiler;

import com.google.common.collect.Lists;
import dev.dfonline.flint.templates.CodeBlock;
import dev.dfonline.flint.templates.Template;
import dev.dfonline.flint.templates.codeblock.Bracket;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class BracketManager {
    public static BracketDraft makeDraft(Template template) {
        var head = template.getBlocks().getBlocks().getFirst();
        var remainder = template.getBlocks().getBlocks().subList(1, template.getBlocks().getBlocks().size());

        return new BracketDraft(
                head,
                makeDrafts(remainder)
        );
    }

    public static List<BracketDraft> makeDrafts(List<CodeBlock> blocks) {
        List<BracketDraft> drafts = Lists.newArrayList();
        int idx = 0;
        BracketDraft lastDraft = null;
        while (idx < blocks.size()) {
            var block = blocks.get(idx);
            if (Objects.requireNonNull(block) instanceof Bracket bracket && bracket.getDirection() == Bracket.Direction.OPEN) {
                int depth = 0;
                List<CodeBlock> children = Lists.newArrayList();
                while (idx < blocks.size()) {
                    var bracket2 = blocks.get(idx);
                    if (bracket2 instanceof Bracket b && b.getDirection() == Bracket.Direction.OPEN) {
                        depth += 1;
                    } else if (bracket2 instanceof Bracket b && b.getDirection() == Bracket.Direction.CLOSE) {
                        depth -= 1;
                    }

                    if (depth > 0) {
                        if (depth == 1 && bracket2 instanceof Bracket b && b.getDirection() == Bracket.Direction.OPEN) {
                            // Don't add the first open bracket to children
                        } else {
                            children.add(bracket2);
                        }
                    }

                    idx++;
                    if (depth == 0) {
                        break;
                    }
                }
                if (lastDraft != null) {
                    lastDraft.children = makeDrafts(children);
                }
                continue; // idx is already incremented
            } else {
                var draft = new BracketDraft(block, null);
                drafts.add(draft);
                lastDraft = draft;
            }
            idx++;
        }
        return drafts;
    }

    public static class BracketDraft {
        CodeBlock codeBlock;
        @Nullable List<BracketDraft> children;

        public BracketDraft(CodeBlock block, @Nullable List<BracketDraft> children) {
            this.codeBlock = block;
            this.children = children;
        }
    }
}
