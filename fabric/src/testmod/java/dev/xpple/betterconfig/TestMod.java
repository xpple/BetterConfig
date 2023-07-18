package dev.xpple.betterconfig;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.betterconfig.api.ModConfigBuilder;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.tuple.Pair;

public class TestMod implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        //ArgumentTypeRegistry.registerArgumentType(new Identifier("testmod", "block"), BlockArgumentType.class, ConstantArgumentSerializer.of(BlockArgumentType::block));

        new ModConfigBuilder("testmod", Configs.class)
            .registerTypeHierarchyWithSuggestor(Block.class, new BlockAdapter(), Pair.of(BlockSuggestionProvider::new, (ctx, name) -> {
                String blockString = ctx.getArgument(name, String.class);
                Identifier blockId = Identifier.tryParse(blockString);
                if (blockId == null) {
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create();
                }
                if (Registries.BLOCK.containsId(blockId)) {
                    return Registries.BLOCK.get(blockId);
                }
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create();
            }))
            .build();
    }
}
