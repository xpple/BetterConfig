package dev.xpple.betterconfig;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockState;

import java.io.IOException;

class BlockStateAdapter extends TypeAdapter<BlockState> {
    @Override
    public void write(JsonWriter writer, BlockState blockState) throws IOException {
        writer.value(blockState.getBlockData().getAsString());
    }

    @Override
    public BlockState read(JsonReader reader) throws IOException {
        return Bukkit.getServer().createBlockData(reader.nextString()).createBlockState();
    }
}
