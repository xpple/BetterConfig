package dev.xpple.betterconfig;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.betterconfig.api.ModConfigBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.block.Block;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

public class TestMod implements ModInitializer {
    @Override
    public void onInitialize() {
        //ArgumentTypeRegistry.registerArgumentType(new Identifier("testmod", "block"), BlockArgumentType.class, ConstantArgumentSerializer.of(BlockArgumentType::block));

        new ModConfigBuilder("testmod", Configs.class)
            .registerTypeHierarchyWithSuggestor(Block.class, new BlockAdapter(), new Pair<>(BlockSuggestionProvider::new, (ctx, name) -> {
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
