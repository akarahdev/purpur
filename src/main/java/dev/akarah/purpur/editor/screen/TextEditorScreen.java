package dev.akarah.purpur.editor.screen;

import net.minecraft.client.Minecraft;
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
    MultiLineTextWidget exceptions;

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

        this.exceptions = new MultiLineTextWidget(
                offX + this.editBox.getWidth() + 10,
                offY,
                Component.empty(),
                Minecraft.getInstance().font
        );
        this.addRenderableWidget(this.exceptions);
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
