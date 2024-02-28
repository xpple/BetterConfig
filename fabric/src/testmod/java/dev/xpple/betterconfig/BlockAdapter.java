package dev.xpple.betterconfig;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.io.IOException;

class BlockAdapter extends TypeAdapter<Block> {
    @Override
    public void write(JsonWriter writer, Block block) throws IOException {
        writer.value(BuiltInRegistries.BLOCK.getKey(block).toString());
    }

    @Override
    public Block read(JsonReader reader) throws IOException {
        return BuiltInRegistries.BLOCK.get(new ResourceLocation(reader.nextString()));
    }
}
