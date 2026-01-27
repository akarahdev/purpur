package dev.akarah.purpur.editor.screen;

import com.google.common.collect.Lists;
import dev.akarah.purpur.mappings.MappingsRepository;
import dev.akarah.purpur.misc.SpannedException;
import dev.akarah.purpur.parser.ast.AST;
import dev.akarah.purpur.parser.ast.value.Variable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.Whence;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.ARGB;
import org.apache.commons.text.similarity.FuzzyScore;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.include.com.google.common.collect.Sets;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class TextEditorScreen extends Screen {
    private static final TextEditorScreen INSTANCE = new TextEditorScreen(Component.literal("Text Editor"));

    boolean initialized = false;
    EditorBox editBox;
    Exception activeException;
    List<SpannedException> spannedExceptions = Lists.newArrayList();
    MultiLineTextWidget sideText;
    List<String> suggestions = Lists.newArrayList();

    private TextEditorScreen(Component component) {
        super(component);
    }

    public static TextEditorScreen getInstance() {
        if(!INSTANCE.initialized) INSTANCE.init();
        return INSTANCE;
    }

    @Override
    protected void init() {
        this.clearWidgets();

        int offX = (int) (((double) this.width) * 0.05);
        int offY = (int) (((double) this.height) * 0.05);

        this.editBox = new EditorBox(offX, offY, this.width - (offX * 8), this.height - (offY * 2));
        this.editBox.screen = this;

        this.editBox.textField.setValueListener(value -> {
            this.editBox.tryCompile();
        });

        this.addRenderableWidget(this.editBox);

        this.sideText = new MultiLineTextWidget(
                offX + this.editBox.getWidth() + 10,
                offY,
                Component.empty(),
                Minecraft.getInstance().font
        );
        this.addRenderableWidget(this.sideText);
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if(keyEvent.key() == GLFW.GLFW_KEY_TAB && this.editBox.isFocused()) {

            var currentLine = this.editBox.textField.getLineView(this.editBox.textField.getLineAtCursor());
            var currentLineText = this.editBox.textField.value().substring(currentLine.beginIndex(), this.editBox.textField.cursor());
            if(currentLineText.isBlank()) {
                for(int i = 0; i < 4; i++) {
                    this.editBox.charTyped(
                            new CharacterEvent(GLFW.GLFW_KEY_SPACE, 0)
                    );
                }
            } else if(!this.suggestions.isEmpty()) {
                var topSuggestion = this.suggestions.getFirst();
                int endIdentifierIdx = this.editBox.textField.cursor();
                while(endIdentifierIdx > currentLine.beginIndex() && Variable.charIsAllowedInIdentifier(this.editBox.textField.value().charAt(endIdentifierIdx - 1))) {
                    endIdentifierIdx -= 1;
                }
                var identifierText = this.editBox.textField.value().substring(endIdentifierIdx, this.editBox.textField.cursor()).trim();
                for(int i = 0; i < identifierText.length(); i++) {
                    this.editBox.textField.keyPressed(new KeyEvent(
                            GLFW.GLFW_KEY_BACKSPACE, 0, 0
                    ));
                }
                this.editBox.textField.insertText(topSuggestion);
            }
            return true;
        }
        return super.keyPressed(keyEvent);
    }

    public void setContents(String contents) {
        this.editBox.setValue(contents);
    }

    public static void resetInstance() {
        INSTANCE.initialized = false;
    }

    public void reloadSideText() {
        var txt = Component.empty().withStyle(Style::withoutShadow);

        boolean hasErrors = false;
        txt.append(Component.literal("Errors:").withColor(0xFF0000));
        if(this.activeException != null) {
            var sw = new StringWriter();
            var pw = new PrintWriter(sw);
            this.activeException.printStackTrace(pw);

            txt.append(Component.literal("\n" + sw).withColor(0xFF0000));
            hasErrors = true;
        }

        for(var exception : this.spannedExceptions) {
            txt.append(
                    Component.empty()
                            .append("\n\n")
                            .append(exception.getMessage())
                            .append("\n")
                            .append("Line " + exception.spanData().line())
            );
            hasErrors = true;
        }

        if(!hasErrors) txt.append(Component.literal("No errors!").withColor(0x0000FF));

        var currentLine = this.editBox.textField.getLineView(this.editBox.textField.getLineAtCursor());
        int endIdentifierIdx = this.editBox.textField.cursor();
        while(endIdentifierIdx > currentLine.beginIndex() && Variable.charIsAllowedInIdentifier(this.editBox.textField.value().charAt(endIdentifierIdx - 1))) {
            endIdentifierIdx -= 1;
        }
        var textBeforeIdentifier = this.editBox.textField.value().substring(currentLine.beginIndex(), endIdentifierIdx).trim();
        var identifierText = this.editBox.textField.value().substring(endIdentifierIdx, this.editBox.textField.cursor()).trim();

        var suggestions = Sets.<String>newHashSet();

        if(textBeforeIdentifier.endsWith("gamevalue")) {
            suggestions.addAll(MappingsRepository.get().allScriptGameValues());
        } else if (textBeforeIdentifier.isEmpty()) {
            suggestions.addAll(MappingsRepository.get().allScriptFunctions());
        }

        var scoring = new FuzzyScore(Locale.getDefault());
        var list = suggestions.stream()
                .sorted(
                        (s1, s2) -> Integer.compare(
                            scoring.fuzzyScore(identifierText, s2),
                            scoring.fuzzyScore(identifierText, s1)
                        ))
                .limit(10)
                .toList();

        this.sideText.setMessage(txt);
        this.suggestions = list;
    }
}
