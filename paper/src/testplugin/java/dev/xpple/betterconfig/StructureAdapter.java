package dev.xpple.betterconfig;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.generator.structure.Structure;

import java.io.IOException;

class StructureAdapter extends TypeAdapter<Structure> {
    @Override
    public void write(JsonWriter writer, Structure value) throws IOException {
        writer.value(value.getKey().toString());
    }

    @Override
    public Structure read(JsonReader reader) throws IOException {
        return Registry.STRUCTURE.get(NamespacedKey.fromString(reader.nextString()));
    }
}
