package dev.xpple.betterconfig;

import com.mojang.util.UUIDTypeAdapter;
import dev.xpple.betterconfig.api.ModConfigBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.UUID;

public class TestModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        new ModConfigBuilder<FabricClientCommandSource, CommandBuildContext>("testmodclient", Configs.class)
            .registerTypeHierarchy(Block.class, new BlockAdapter(), BlockArgumentType::block)
            .registerTypeHierarchy(BlockInput.class, new BlockStateAdapter(), BlockStateArgument::block)
            .registerTypeHierarchy((Class<StructureType<?>>) (Class) StructureType.class, new StructureAdapter(), StructureArgumentType::structure)
            .registerType(UUID.class, new UUIDTypeAdapter(), UuidArgument::uuid)
            .registerGlobalChangeHook(event -> BetterConfigCommon.LOGGER.info("{} was updated | old: {}, new: {}", event.config(), event.oldValue(), event.newValue()))
            .build();
    }
}
