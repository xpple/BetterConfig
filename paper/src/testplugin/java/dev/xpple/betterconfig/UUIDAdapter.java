package dev.xpple.betterconfig;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.UUID;

class UUIDAdapter extends TypeAdapter<UUID> {
    @Override
    public void write(final JsonWriter writer, final UUID value) throws IOException {
        writer.value(value.toString());
    }

    @Override
    public UUID read(final JsonReader reader) throws IOException {
        return UUID.fromString(reader.nextString());
    }
}
