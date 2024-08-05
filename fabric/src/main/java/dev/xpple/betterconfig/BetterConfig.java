package dev.xpple.betterconfig;

import com.mojang.brigadier.CommandDispatcher;
import dev.xpple.betterconfig.command.ConfigCommand;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.nio.file.Path;

public class BetterConfig implements DedicatedServerModInitializer {

    public static final Path MOD_PATH = FabricLoader.getInstance().getConfigDir();

    @Override
    public void onInitializeServer() {
        CommandRegistrationCallback.EVENT.register(BetterConfig::registerCommands);
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext, Commands.CommandSelection selection) {
        ConfigCommand.register(dispatcher, buildContext);
    }
}
