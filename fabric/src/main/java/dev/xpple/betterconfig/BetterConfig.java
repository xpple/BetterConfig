package dev.xpple.betterconfig;

import com.mojang.brigadier.CommandDispatcher;
import dev.xpple.betterconfig.command.ConfigCommand;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.nio.file.Path;

public class BetterConfig implements DedicatedServerModInitializer {

    public static final Path MOD_PATH = FabricLoader.getInstance().getConfigDir();

    @Override
    public void onInitializeServer() {
        CommandRegistrationCallback.EVENT.register(BetterConfig::registerCommands);
    }

    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        ConfigCommand.register(dispatcher, registryAccess);
    }
}
