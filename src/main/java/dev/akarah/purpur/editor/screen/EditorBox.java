package dev.akarah.purpur.editor.screen;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.util.ARGB;
import net.minecraft.util.Util;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class EditorBox extends MultiLineEditBox {
    public static List<HighlightGroup> groups() {
        return List.of(
                // numbers
                new HighlightGroup(
                        Pattern.compile("[0-9]*?"),
                        ARGB.color(255, 170, 255, 170)
                ),
                // identifiers
                new HighlightGroup(
                        Pattern.compile("([a-zA-Z_0-9?]+)"),
                        ARGB.color(255, 255, 200, 255)
                ),
                // namespaces
                new HighlightGroup(
                        Pattern.compile("(repeat|func|player|entity|game|Event|control|vars|ifVars|proc|selectObject)(?=\\.)"),
                        ARGB.color(255, 255, 100, 100)
                ),
                // functions after namespaces
                new HighlightGroup(
                        Pattern.compile("(?<=\\.)(.*?)(?=\\()"),
                        ARGB.color(255, 255, 170, 170)
                ),
                // scopes
                new HighlightGroup(
                        Pattern.compile("(game|local|line|saved) (?=[a-zA-Z_0-9?/]*)"),
                        ARGB.color(255, 170, 170, 255)
                ),
                // strings
                new HighlightGroup(
                        Pattern.compile("\"(.*?)\""),
                        ARGB.color(255, 170, 255, 170)
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

    protected void renderHighlightedLine(String currentLineText, int lineStartX, int currentLineY, @NonNull GuiGraphics guiGraphics) {
        guiGraphics.drawString(this.font, currentLineText, lineStartX, currentLineY, this.textColor, this.textShadow);

        for(var group : groups()) {
            var matcher = group.pattern.matcher(currentLineText);
            while(matcher.find()) {
                var beforeHighlightedText = currentLineText.substring(0, matcher.start());
                var highlightedTextFound = currentLineText.substring(matcher.start(), matcher.end());
                var lineOffset = lineStartX + this.font.width(beforeHighlightedText);
                guiGraphics.drawString(this.font, highlightedTextFound, lineOffset, currentLineY, group.color, this.textShadow);
            }
        }
    }

    @Override
    protected void renderContents(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        String textBuffer = this.textField.value();
        if (textBuffer.isEmpty() && !this.isFocused()) {
            return;
        }
        int cursorIndex = this.textField.cursor();
        boolean shouldRenderBlinkingCursor = this.isFocused() && (Util.getMillis() - this.focusedTime) / 300L % 2L == 0L;
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
                    this.renderHighlightedLine(currentLineText, lineStartX, currentLineY, guiGraphics);
                    cursorPixelX = lineStartX + this.font.width(textBeforeCursor);
                    if (!cursorRendered) {
                        int caretTopY = currentLineY - 1;
                        int caretRightX = cursorPixelX + 1;
                        int caretBottomY = currentLineY + 1;
                        guiGraphics.fill(cursorPixelX, caretTopY, caretRightX, caretBottomY + 9, this.cursorColor);
                        cursorRendered = true;
                    }
                } else {
                    var lineToRender = currentLineText;
                    this.renderHighlightedLine(lineToRender, lineStartX, currentLineY, guiGraphics);
                    cursorPixelX = lineStartX + this.font.width(lineToRender) - 1;
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
                        int selectionStartOffsetPx = this.font.width(textBuffer.substring(textLine.beginIndex(), Math.max(selectedTextView.beginIndex(), textLine.beginIndex())));
                        int selectionOffsetEndPx;
                        if (selectedTextView.endIndex() > textLine.endIndex()) {
                            selectionOffsetEndPx = this.width - this.innerPadding();
                        } else {
                            selectionOffsetEndPx = this.font.width(textBuffer.substring(textLine.beginIndex(), selectedTextView.endIndex()));
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
}
