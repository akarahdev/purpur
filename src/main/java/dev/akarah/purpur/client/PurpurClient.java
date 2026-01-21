package dev.akarah.purpur.client;

import dev.akarah.purpur.editor.TextEditorCommandsFeature;
import dev.dfonline.flint.FlintAPI;
import net.fabricmc.api.ClientModInitializer;

public class PurpurClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FlintAPI.registerFeature(new TextEditorCommandsFeature());
    }
}
