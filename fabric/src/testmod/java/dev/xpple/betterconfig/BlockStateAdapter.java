package dev.xpple.betterconfig;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.serialization.JsonOps;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.world.level.block.state.BlockState;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

class BlockStateAdapter extends TypeAdapter<BlockInput> {
    @Override
    public void write(JsonWriter writer, BlockInput blockState) throws IOException {
        Optional<JsonElement> jsonElement = BlockState.CODEC.encodeStart(JsonOps.INSTANCE, blockState.getState()).result();
        if (jsonElement.isEmpty()) {
            throw new IOException();
        }
        Streams.write(jsonElement.get(), writer);
    }

    @Override
    public BlockInput read(JsonReader reader) throws IOException {
        Optional<BlockState> blockState = BlockState.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader)).result();
        if (blockState.isEmpty()) {
            throw new IOException();
        }
        return new BlockInput(blockState.get(), Collections.emptySet(), null);
    }
}
