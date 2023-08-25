package dev.xpple.betterconfig;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.serialization.JsonOps;
import net.minecraft.block.BlockState;
import net.minecraft.command.argument.BlockStateArgument;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

class BlockStateAdapter extends TypeAdapter<BlockStateArgument> {
    @Override
    public void write(JsonWriter writer, BlockStateArgument blockState) throws IOException {
        Optional<JsonElement> jsonElement = BlockState.CODEC.encodeStart(JsonOps.INSTANCE, blockState.getBlockState()).result();
        if (jsonElement.isEmpty()) {
            throw new IOException();
        }
        Streams.write(jsonElement.get(), writer);
    }

    @Override
    public BlockStateArgument read(JsonReader reader) throws IOException {
        Optional<BlockState> blockState = BlockState.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader)).result();
        if (blockState.isEmpty()) {
            throw new IOException();
        }
        return new BlockStateArgument(blockState.get(), Collections.emptySet(), null);
    }
}
