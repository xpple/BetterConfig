package dev.xpple.betterconfig;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.betterconfig.api.ModConfigBuilder;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.block.Block;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class TestMod implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        //ArgumentTypeRegistry.registerArgumentType(new Identifier("testmod", "block"), BlockArgumentType.class, ConstantArgumentSerializer.of(BlockArgumentType::block));

        new ModConfigBuilder("testmod", Configs.class)
            .registerTypeHierarchy(Block.class, new BlockAdapter(), new BlockSuggestionProvider(), (ctx, name) -> {
                String blockString = ctx.getArgument(name, String.class);
                Identifier blockId = Identifier.tryParse(blockString);
                if (blockId == null) {
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create();
                }
                if (Registries.BLOCK.containsId(blockId)) {
                    return Registries.BLOCK.get(blockId);
                }
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create();
            })
            .registerTypeHierarchy(BlockStateArgument.class, new BlockStateAdapter(), BlockStateArgumentType::blockState)
            .build();
    }
}
