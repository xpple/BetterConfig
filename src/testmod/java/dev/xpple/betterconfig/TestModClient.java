package dev.xpple.betterconfig;

import dev.xpple.betterconfig.api.ModConfigBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.block.Block;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.BlockStateArgumentType;

public class TestModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        new ModConfigBuilder("testmodclient", Configs.class)
            .registerTypeHierarchy(Block.class, new BlockAdapter(), BlockArgumentType::block)
            .registerTypeHierarchy(BlockStateArgument.class, new BlockStateAdapter(), BlockStateArgumentType::blockState)
            .build();
    }
}
