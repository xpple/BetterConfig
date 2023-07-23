package dev.xpple.betterconfig;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;

import java.io.IOException;

class MaterialAdapter extends TypeAdapter<Material> {
    @Override
    public void write(JsonWriter writer, Material value) throws IOException {
        writer.value(value.getKey().toString());
    }

    @Override
    public Material read(JsonReader reader) throws IOException {
        return Registry.MATERIAL.get(NamespacedKey.fromString(reader.nextString()));
    }
}
