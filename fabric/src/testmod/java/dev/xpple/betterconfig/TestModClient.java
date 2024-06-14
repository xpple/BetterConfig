package dev.xpple.betterconfig;

import dev.xpple.betterconfig.api.ModConfigBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.StructureType;

public class TestModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        new ModConfigBuilder("testmodclient", Configs.class)
            .registerTypeHierarchy(Block.class, new BlockAdapter(), BlockArgumentType::block)
            .registerTypeHierarchy(BlockInput.class, new BlockStateAdapter(), BlockStateArgument::block)
            .registerTypeHierarchy((Class<StructureType<?>>) (Class) StructureType.class, new StructureAdapter(), StructureArgumentType::structure)
            .build();
    }
}
