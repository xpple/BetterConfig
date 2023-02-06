package dev.xpple.betterconfig;

import com.mojang.brigadier.CommandDispatcher;
import dev.xpple.betterconfig.command.ConfigCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

public class BetterConfig implements ModInitializer {

    public static final String MOD_ID = "betterconfig";
    public static final Path MOD_PATH = FabricLoader.getInstance().getConfigDir();

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(BetterConfig::registerCommands);
    }

    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        if (environment.dedicated) {
            ConfigCommand.register(dispatcher);
        }
    }
}
