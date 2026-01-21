package dev.akarah.purpur.editor.screen;

import dev.dfonline.flint.Flint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.MultilineTextField;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class TextEditorScreen extends Screen {
    MultiLineEditBox editBox;

    public TextEditorScreen(Component component) {
        super(component);
    }

    @Override
    protected void init() {
        super.init();

        assert Minecraft.getInstance().screen != null;
        var offX = (int) (((double) Minecraft.getInstance().screen.width) * 0.2);
        var offY = (int) (((double) Minecraft.getInstance().screen.height) * 0.2);
        System.out.println(offX);
        System.out.println(offY);
        this.editBox = MultiLineEditBox.builder()
                .setX(offX)
                .setY(offY)
                .build(
                        Minecraft.getInstance().font,
                        Minecraft.getInstance().screen.width - (offX * 2),
                        Minecraft.getInstance().screen.height - (offY * 2),
                        Component.literal("meow")
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
        }
        return super.keyPressed(keyEvent);
    }
}
