package dev.xpple.betterconfig;

import dev.xpple.betterconfig.command.ConfigCommand;
import io.papermc.paper.event.server.ServerResourcesLoadEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

public final class BetterConfig extends JavaPlugin {

    public static final Path PLUGIN_PATH = Bukkit.getPluginsFolder().toPath();

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void load(ServerResourcesLoadEvent event) {
                event.getCommands().register(BetterConfig.this, ConfigCommand.build());
            }
        }, this);
    }
}
