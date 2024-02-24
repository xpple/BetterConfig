package dev.xpple.betterconfig;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.betterconfig.api.ModConfigBuilder;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.StructureType;

public class TestMod implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        //ArgumentTypeRegistry.registerArgumentType(new ResourceLocation("testmod", "block"), BlockArgumentType.class, SingletonArgumentInfo.contextFree(BlockArgumentType::block));

        new ModConfigBuilder("testmod", Configs.class)
            .registerTypeHierarchy(Block.class, new BlockAdapter(), new BlockSuggestionProvider(), (ctx, name) -> {
                String blockString = ctx.getArgument(name, String.class);
                ResourceLocation blockKey = ResourceLocation.tryParse(blockString);
                if (blockKey == null) {
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create();
                }
                if (BuiltInRegistries.BLOCK.containsKey(blockKey)) {
                    return BuiltInRegistries.BLOCK.get(blockKey);
                }
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create();
            })
            .registerTypeHierarchy(BlockInput.class, new BlockStateAdapter(), BlockStateArgument::block)
            .registerTypeHierarchy((Class<StructureType<?>>) (Class) StructureType.class, new StructureAdapter(), StructureArgumentType::structure)
            .build();
    }
}
