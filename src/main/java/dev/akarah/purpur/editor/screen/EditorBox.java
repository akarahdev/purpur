package dev.akarah.purpur.editor.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import dev.akarah.purpur.lexer.Lexer;
import dev.akarah.purpur.misc.SpannedException;
import dev.akarah.purpur.parser.Parser;
import dev.akarah.purpur.parser.ast.AST;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class EditorBox extends MultiLineEditBox {
    List<SpannedException> spannedExceptions = Lists.newArrayList();
    boolean dirty;

    private static String generateRecursiveRegex(String s) {
        for(int i = 0; i < 7; i++) {
            s = s.replace("?R", s);
        }
        s = s.replace("?R", "");
        return s;
    }

    public static List<HighlightGroup> groups() {
        return List.of(
                // numbers
                new HighlightGroup(
                        Pattern.compile("[0-9]*?"),
                        ARGB.color(255, 170, 255, 170)
                ),
                // identifiers
                new HighlightGroup(
                        Pattern.compile("(([a-zA-Z_0-9?./:]+)|(`(.*?)`))"),
                        ARGB.color(255, 255, 200, 255)
                ),
                // namespaces
                new HighlightGroup(
                        Pattern.compile("(repeat|func|player|playerEvent|ifPlayer|entity|entityEvent|ifEntity|game|gameEvent|ifGame|control|vars|ifVars|proc|select)(?=\\.)"),
                        ARGB.color(255, 255, 100, 100)
                ),
                // pseudo functions
                new HighlightGroup(
                        Pattern.compile("(?<=[ (,])(item|loc|vec|sound|particle|nbt)(?=\\()"),
                        ARGB.color(255, 170, 170, 255)
                ),
                // special syntaxes
                new HighlightGroup(
                        Pattern.compile("(?<=[ (,])(tag|gamevalue|param) "),
                        ARGB.color(255, 170, 170, 255)
                ),
                // functions after namespaces
                new HighlightGroup(
                        Pattern.compile("(?<=\\.)(.*?)(?=([(<\\[\\]]))"),
                        ARGB.color(255, 255, 170, 170)
                ),
                // scopes
                new HighlightGroup(
                        Pattern.compile("(game|local|line|saved|tag|plural|optional) (?=[a-zA-Z_0-9?/]*)"),
                        ARGB.color(255, 170, 170, 255)
                ),
                // strings
                new HighlightGroup(
                        Pattern.compile("\"(.*?)\""),
                        ARGB.color(255, 140, 255, 140)
                ),
                // % codes
                new HighlightGroup(
                        Pattern.compile(generateRecursiveRegex("%([a-zA-Z]+)\\((?:[^()]+|(?R))*\\)")),
                        ARGB.color(255, 190, 255, 190)
                ),
                // comments
                new HighlightGroup(
                        Pattern.compile("//(.*?)$"),
                        ARGB.color(255, 100, 100, 100)
                )
        );
    }

    public EditorBox(int offX, int offY, int sizeX, int sizeY) {
        super(
                Minecraft.getInstance().font,
                offX,
                offY,
                sizeX,
                sizeY,
                Component.empty(),
                Component.empty(),
                ARGB.color(255, 255, 255, 255),
                false,
                ARGB.color(255, 255, 255, 255),
                true,
                true
        );
    }

    record HighlightGroup(Pattern pattern, int color) {}

    // eventually will be `purpur:code` when i feel like fixing it
    public static FontDescription OUR_FONT = new FontDescription.Resource(Identifier.fromNamespaceAndPath("minecraft", "default"));

    protected void renderHighlightedLine(String currentLineText, int lineStartX, int currentLineY, @NonNull GuiGraphics guiGraphics, int lineStartIndex, int lineEndIndex) {
        var currentLineComp = Component.literal(currentLineText).withStyle(style -> style.withFont(OUR_FONT));
        guiGraphics.drawString(this.font, currentLineComp, lineStartX, currentLineY, this.textColor, this.textShadow);

        for(var group : groups()) {
            var matcher = group.pattern.matcher(currentLineText);
            while(matcher.find()) {
                var beforeHighlightedText = currentLineText.substring(0, matcher.start());
                var beforeHighlightedComp = Component.literal(beforeHighlightedText).withStyle(style -> style.withFont(OUR_FONT));
                var highlightedTextFound = currentLineText.substring(matcher.start(), matcher.end());
                var highlightedCompFound = Component.literal(highlightedTextFound).withStyle(style -> style.withFont(OUR_FONT));
                var lineOffset = lineStartX + this.font.width(beforeHighlightedComp);
                guiGraphics.drawString(this.font, highlightedCompFound, lineOffset, currentLineY, group.color, this.textShadow);
            }
        }

        try {
            for(var exception : this.spannedExceptions) {
                var start = exception.spanData().start();
                var end = exception.spanData().end();
                if(start >= lineStartIndex && end <= lineEndIndex) {
                    var beforeExceptionComp = Component.literal(currentLineText.substring(0, start - lineStartIndex)).withStyle(style -> style.withFont(OUR_FONT));
                    guiGraphics.drawString(
                            this.font,
                            Component.literal("_".repeat(end - start + 1)).withStyle(s -> s.withFont(OUR_FONT)),
                            lineStartX + this.font.width(beforeExceptionComp),
                            currentLineY,
                            ARGB.color(133, 255, 0, 0),
                            this.textShadow
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void renderContents(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        String textBuffer = this.textField.value();
        if (textBuffer.isEmpty() && !this.isFocused()) {
            return;
        }
        int cursorIndex = this.textField.cursor();
        boolean shouldRenderBlinkingCursor = this.isFocused();
        boolean cursorWithinText = cursorIndex < textBuffer.length();
        int cursorPixelX = 0;
        int lastLineY = 0;
        int currentLineY = this.getInnerTop();
        boolean cursorRendered = false;

        for(var currentLineView : this.textField.iterateLines()) {
            boolean textLineIsWithinArea = this.withinContentAreaTopBottom(currentLineY, currentLineY + 9);
            int lineStartX = this.getInnerLeft();

            var currentLineText = textBuffer.substring(currentLineView.beginIndex(), currentLineView.endIndex());
            if(textLineIsWithinArea) {
                if (shouldRenderBlinkingCursor && cursorWithinText && cursorIndex >= currentLineView.beginIndex() && cursorIndex <= currentLineView.endIndex()) {
                    var textBeforeCursor = textBuffer.substring(currentLineView.beginIndex(), cursorIndex);
                    this.renderHighlightedLine(currentLineText, lineStartX, currentLineY, guiGraphics, currentLineView.beginIndex(), currentLineView.endIndex());
                    cursorPixelX = lineStartX + this.font.width(Component.literal(textBeforeCursor).withStyle(s -> s.withFont(OUR_FONT)));
                    if (!cursorRendered) {
                        int caretTopY = currentLineY - 1;
                        int caretRightX = cursorPixelX + 1;
                        int caretBottomY = currentLineY + 1;
                        guiGraphics.fill(cursorPixelX, caretTopY, caretRightX, caretBottomY + 9, this.cursorColor);
                        cursorRendered = true;
                    }
                } else {
                    var lineToRender = currentLineText;
                    this.renderHighlightedLine(lineToRender, lineStartX, currentLineY, guiGraphics, currentLineView.beginIndex(), currentLineView.endIndex());
                    cursorPixelX = lineStartX + this.font.width(Component.literal(lineToRender).withStyle(s -> s.withFont(OUR_FONT))) - 1;
                }
            }
            lastLineY = currentLineY;


            Objects.requireNonNull(this.font);
            currentLineY += 9;
        }

        if (shouldRenderBlinkingCursor && !cursorWithinText) {
            if (this.withinContentAreaTopBottom(lastLineY, lastLineY + 9)) {
                int caretTopY = currentLineY - 10;
                int caretRightX = cursorPixelX + 1;
                int caretBottomY = currentLineY + 1;
                guiGraphics.fill(cursorPixelX, caretTopY, caretRightX, caretBottomY, this.cursorColor);
            }
        }

        if (this.textField.hasSelection()) {
            var selectedTextView = this.textField.getSelected();
            int selectionStartX = this.getInnerLeft();
            currentLineY = this.getInnerTop();

            for(var textLine : this.textField.iterateLines()) {
                if (selectedTextView.beginIndex() > textLine.endIndex()) {
                    currentLineY += 9;
                } else {
                    if (textLine.beginIndex() > selectedTextView.endIndex()) {
                        break;
                    }

                    if (this.withinContentAreaTopBottom(currentLineY, currentLineY + 9)) {
                        var substr = textBuffer.substring(textLine.beginIndex(), Math.max(selectedTextView.beginIndex(), textLine.beginIndex()));
                        int selectionStartOffsetPx = this.font.width(Component.literal(substr).withStyle(s -> s.withFont(OUR_FONT)));
                        int selectionOffsetEndPx;
                        if (selectedTextView.endIndex() > textLine.endIndex()) {
                            selectionOffsetEndPx = this.width - this.innerPadding();
                        } else {
                            var substr2 = textBuffer.substring(textLine.beginIndex(), selectedTextView.endIndex());
                            selectionOffsetEndPx = this.font.width(Component.literal(substr2).withStyle(s -> s.withFont(OUR_FONT)));
                        }

                        int selectionHighlightLeftX = selectionStartX + selectionStartOffsetPx;
                        int selectionHighlightRightX = selectionStartX + selectionOffsetEndPx;
                        guiGraphics.textHighlight(selectionHighlightLeftX, currentLineY, selectionHighlightRightX, currentLineY + 9, true);
                    }

                    Objects.requireNonNull(this.font);
                    currentLineY += 9;
                }
            }
        }

        if (this.isHovered()) {
            guiGraphics.requestCursor(CursorTypes.IBEAM);
        }
    }

    public void tryCompile() {
        try {
            this.spannedExceptions.clear();

            var templates = Lexer.parse(this.textField.value())
                    .flatMap(Parser::parse)
                    .flatMap(AST::buildTemplates);

            if(templates.isSuccess()) {
                for(var template : templates.partialResult()) {
                    var packet = new ServerboundSetCreativeModeSlotPacket(
                            Minecraft.getInstance().player.getInventory().getSelectedSlot(),
                            template
                    );
                    Minecraft.getInstance().getConnection().send(packet);
                    Minecraft.getInstance().player.getInventory().setSelectedItem(template);
                }
            } else {
                this.spannedExceptions.addAll(templates.errors());
                System.out.println(templates.errors());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected double scrollRate() {
        return super.scrollRate() * 2;
    }
}
