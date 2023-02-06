package dev.xpple.betterconfig;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.betterconfig.api.Config;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

import java.util.*;

@SuppressWarnings({"FieldMayBeFinal", "unused"})
public class Configs {
    @Config
    public static Block exampleBlock = Blocks.STONE;

    @Config
    public static Collection<Block> exampleBlockList = new ArrayList<>(List.of(Blocks.PISTON, Blocks.STICKY_PISTON));

    @Config
    public static Map<String, Block> exampleStringBlockMap = new HashMap<>(Map.of("sand", Blocks.SAND, "red_sand", Blocks.RED_SAND));

    @Config
    public static Map<Block, String> exampleBlockStringMap = new HashMap<>(Map.of(Blocks.NETHERRACK, "nether", Blocks.END_STONE, "end"));

    @Config(adder = "customAdder")
    public static Collection<String> exampleCustomAdder = new ArrayList<>(List.of("1", "2"));
    public static void customAdder(Object string) throws CommandSyntaxException {
        try {
            Integer.parseInt((String) string);
        } catch (NumberFormatException e) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().create(string);
        }
        exampleCustomAdder.add((String) string);
    }

    @Config(putter = "none", adder = "customMapAdder")
    public static Map<String, String> exampleMapAdder = new HashMap<>(Map.of("a", "A", "b", "B"));
    public static void customMapAdder(Object string) {
        exampleMapAdder.put(((String) string).toLowerCase(Locale.ROOT), ((String) string).toUpperCase(Locale.ROOT));
    }
}
