package dev.xpple.betterconfig;

import com.mojang.util.UUIDTypeAdapter;
import dev.xpple.betterconfig.api.ModConfigBuilder;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.UUID;

public class TestMod implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        //ArgumentTypeRegistry.registerArgumentType(new ResourceLocation("testmod", "block"), BlockArgumentType.class, SingletonArgumentInfo.contextFree(BlockArgumentType::block));

        new ModConfigBuilder<CommandSourceStack, CommandBuildContext>("testmod", Configs.class)
            .registerTypeHierarchy(Block.class, new BlockAdapter(), BlockWrappedArgumentType::block)
            .registerTypeHierarchy(BlockInput.class, new BlockStateAdapter(), BlockStateArgument::block)
            .registerTypeHierarchy((Class<StructureType<?>>) (Class) StructureType.class, new StructureAdapter(), StructureArgumentType::structure)
            .registerType(UUID.class, new UUIDTypeAdapter(), UuidArgument::uuid)
            .registerGlobalChangeHook(event -> BetterConfigCommon.LOGGER.info("{} was updated | old: {}, new: {}", event.config(), event.oldValue(), event.newValue()))
            .build();
    }
}
