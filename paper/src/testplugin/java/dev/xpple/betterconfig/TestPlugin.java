package dev.xpple.betterconfig;

import dev.xpple.betterconfig.api.PluginConfigBuilder;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.generator.structure.Structure;
import org.bukkit.plugin.java.JavaPlugin;

public class TestPlugin extends JavaPlugin {

    private static final String PLUGIN_NAME = "TestPlugin";

    @Override
    public void onEnable() {
        new PluginConfigBuilder(PLUGIN_NAME, Configs.class)
            .registerType(Material.class, new MaterialAdapter(), BlockMaterialArgumentType::block)
            .registerTypeHierarchy(BlockState.class, new BlockStateAdapter(), ArgumentTypes::blockState)
            .registerTypeHierarchy(Structure.class, new StructureAdapter(), StructureArgumentType::structure)
            .build();
    }
}
