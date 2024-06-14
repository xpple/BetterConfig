package dev.xpple.betterconfig;

import dev.xpple.betterconfig.command.ConfigCommand;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

public final class BetterConfig extends JavaPlugin {

    public static final Path PLUGIN_PATH = Bukkit.getPluginsFolder().toPath();

    @Override
    public void onEnable() {
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> event.registrar().register(ConfigCommand.build()));
    }
}
