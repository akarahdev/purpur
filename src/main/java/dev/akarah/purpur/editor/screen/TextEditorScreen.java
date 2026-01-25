package dev.akarah.purpur.editor.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class TextEditorScreen extends Screen {
    private static final TextEditorScreen INSTANCE = new TextEditorScreen(Component.literal("Text Editor"));

    boolean initialized = false;
    EditorBox editBox;

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

        int offX = (int) (((double) this.width) * 0.15);
        int offY = (int) (((double) this.height) * 0.1);

        this.editBox = new EditorBox(offX, offY, this.width - (offX * 2), this.height - (offY * 2));

        int buttonY = offY;
        for(int i = 0; i < 11; i++) {
            var button = Button.builder(Component.literal("Tab"), bt -> {})
                    .pos(offX / 6, buttonY)
                    .width(offX * 2 / 3)
                    .build();
            buttonY += button.getHeight() + 4;
            this.addRenderableWidget(button);
        }

        var text = new MultiLineTextWidget(
                offX,
                offY / 2,
                Component.literal("Current Open File:"),
                Minecraft.getInstance().font
        );
        this.addRenderableWidget(text);

        this.addRenderableWidget(
                Button.builder(Component.literal("Reload"), button -> {
                    this.init();
                })
                        .width(200)
                        .pos((this.width / 2) - 100,offY + editBox.getHeight() + 15).build()
        );

        this.addRenderableWidget(this.editBox);
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if(keyEvent.key() == GLFW.GLFW_KEY_TAB && this.editBox.isFocused()) {
            for(int i = 0; i < 4; i++) {
                this.editBox.charTyped(
                        new CharacterEvent(0x0020, 0)
                );
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
}
