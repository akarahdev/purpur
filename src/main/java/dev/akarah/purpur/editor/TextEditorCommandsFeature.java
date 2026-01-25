package dev.akarah.purpur.editor;

import com.google.gson.JsonParser;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.serialization.JsonOps;
import dev.akarah.purpur.decompiler.Decompiler;
import dev.akarah.purpur.editor.screen.TextEditorScreen;
import dev.dfonline.flint.feature.trait.CommandFeature;
import dev.dfonline.flint.templates.Template;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;

public class TextEditorCommandsFeature implements CommandFeature {
    @Override
    public String commandName() {
        return "editor";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> createCommand(LiteralArgumentBuilder<FabricClientCommandSource> literalArgumentBuilder, CommandBuildContext commandBuildContext) {
        return literalArgumentBuilder.then(
                ClientCommandManager.literal("open").executes(ctx -> {
                    Minecraft.getInstance().schedule(() -> {
                        Minecraft.getInstance().setScreenAndShow(TextEditorScreen.getInstance());
                    });
                    return 0;
                })
        ).then(
                ClientCommandManager.literal("template").executes(ctx -> {
                    Minecraft.getInstance().schedule(() -> {
                        var is = Minecraft.getInstance().player.getMainHandItem();
                        var encoded = Template.fromItem(is);
                        var decompiled = Decompiler.decompile(encoded);

                        var sb = new StringBuilder();
                        decompiled.lowerToParsable(sb, 0);

                        ctx.getSource().sendFeedback(Component.literal(sb.toString()));
                        Minecraft.getInstance().setScreenAndShow(TextEditorScreen.getInstance());
                        TextEditorScreen.getInstance().setContents(sb.toString());
                    });
                    return 0;
                })
        );
    }
}
