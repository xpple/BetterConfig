package dev.xpple.betterconfig;

import dev.xpple.betterconfig.api.ModConfigBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.block.Block;
import org.apache.commons.lang3.tuple.Pair;

public class TestModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        new ModConfigBuilder("testmodclient", Configs.class)
            .registerTypeHierarchyWithArgument(Block.class, new BlockAdapter(), Pair.of(BlockArgumentType::block, BlockArgumentType::getBlock))
            .build();
    }
}
