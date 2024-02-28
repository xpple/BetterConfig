package dev.xpple.betterconfig;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.io.IOException;

class StructureAdapter extends TypeAdapter<StructureType<?>> {
    @Override
    public void write(JsonWriter writer, StructureType structure) throws IOException {
        writer.value(BuiltInRegistries.STRUCTURE_TYPE.getKey(structure).toString());
    }

    @Override
    public StructureType<?> read(JsonReader reader) throws IOException {
        return BuiltInRegistries.STRUCTURE_TYPE.get(new ResourceLocation(reader.nextString()));
    }
}
