package dev.akarah.purpur;

import dev.akarah.purpur.editor.TextEditorCommandsFeature;
import dev.dfonline.flint.FlintAPI;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;

public class Purpur implements ModInitializer, ClientModInitializer {
    @Override
    public void onInitialize() {

    }

    @Override
    public void onInitializeClient() {
        FlintAPI.registerFeature(new TextEditorCommandsFeature());
    }
}
