package dev.xpple.betterconfig;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.betterconfig.api.PluginConfigBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.plugin.java.JavaPlugin;

public class TestPlugin extends JavaPlugin {

    private static final String PLUGIN_NAME = "TestPlugin";

    @Override
    public void onEnable() {
        new PluginConfigBuilder(PLUGIN_NAME, Configs.class)
            .registerTypeHierarchyWithSuggestor(Material.class, new MaterialAdapter(), Pair.of(MaterialSuggestionProvider::new, (ctx, name) -> {
                String materialString = ctx.getArgument(name, String.class);
                NamespacedKey blockId = NamespacedKey.fromString(materialString);
                if (blockId == null) {
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create();
                }
                Material material = Registry.MATERIAL.get(blockId);
                if (material == null) {
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create();
                }
                return material;
            }))
            .build();
    }
}
