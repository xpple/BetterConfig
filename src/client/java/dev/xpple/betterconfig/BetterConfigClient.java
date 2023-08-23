package dev.xpple.betterconfig;

import com.mojang.brigadier.CommandDispatcher;
import dev.xpple.betterconfig.command.client.ConfigCommandClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;

public class BetterConfigClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register(BetterConfigClient::registerCommands);
    }

    private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        ConfigCommandClient.register(dispatcher, registryAccess);
    }
}
