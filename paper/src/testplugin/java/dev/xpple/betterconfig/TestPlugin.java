package dev.xpple.betterconfig;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.betterconfig.api.PluginConfigBuilder;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.BlockState;
import org.bukkit.generator.structure.Structure;
import org.bukkit.plugin.java.JavaPlugin;

public class TestPlugin extends JavaPlugin {

    private static final String PLUGIN_NAME = "TestPlugin";

    @Override
    public void onEnable() {
        new PluginConfigBuilder(PLUGIN_NAME, Configs.class)
            .registerType(Material.class, new MaterialAdapter(), new MaterialSuggestionProvider(), (ctx, name) -> {
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
            })
            .registerTypeHierarchy(BlockState.class, new BlockStateAdapter(), ArgumentTypes::blockState)
            .registerTypeHierarchy(Structure.class, new StructureAdapter(), StructureArgumentType::structure)
            .build();
    }
}
