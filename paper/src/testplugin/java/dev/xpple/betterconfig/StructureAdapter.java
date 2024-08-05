package dev.xpple.betterconfig;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.generator.structure.Structure;

import java.io.IOException;

class StructureAdapter extends TypeAdapter<Structure> {
    @Override
    public void write(JsonWriter writer, Structure value) throws IOException {
        writer.value(RegistryAccess.registryAccess().getRegistry(RegistryKey.STRUCTURE).getKey(value).toString());
    }

    @Override
    public Structure read(JsonReader reader) throws IOException {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.STRUCTURE).get(NamespacedKey.fromString(reader.nextString()));
    }
}
