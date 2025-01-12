package dev.xpple.betterconfig;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.betterconfig.api.Config;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings({"FieldMayBeFinal", "unused", "MismatchedQueryAndUpdateOfCollection"})
public class Configs {
    @Config
    public static Block exampleBlock = Blocks.STONE;

    @Config
    public static Collection<Block> exampleBlockList = new ArrayList<>(List.of(Blocks.PISTON, Blocks.STICKY_PISTON));

    @Config
    public static Map<String, Block> exampleStringBlockMap = new HashMap<>(Map.of("sand", Blocks.SAND, "red_sand", Blocks.RED_SAND));

    @Config
    public static Map<Block, String> exampleBlockStringMap = new HashMap<>(Map.of(Blocks.NETHERRACK, "nether", Blocks.END_STONE, "end"));

    @Config(adder = @Config.Adder("customAdder"))
    public static Collection<String> exampleCustomAdder = new ArrayList<>(List.of("1", "2"));
    public static void customAdder(String string) throws CommandSyntaxException {
        try {
            Integer.parseInt(string);
        } catch (NumberFormatException e) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().create(string);
        }
        exampleCustomAdder.add(string);
    }

    @Config(putter = @Config.Putter("none"), adder = @Config.Adder("customMapAdder"))
    public static Map<String, String> exampleMapAdder = new HashMap<>(Map.of("a", "A", "b", "B"));
    public static void customMapAdder(String string) {
        exampleMapAdder.put(string.toLowerCase(Locale.ROOT), string.toUpperCase(Locale.ROOT));
    }

    @Config(adder = @Config.Adder(value = "customTypeAdder", type = int.class))
    public static Collection<String> exampleCustomType = new ArrayList<>(List.of("%", "@"));
    public static void customTypeAdder(int codepoint) {
        exampleCustomType.add(Character.toString(codepoint));
    }

    @Config(representation = @Config.Representation(value = "customRepresentation"))
    public static String exampleCustomRepresentation = "";
    public static String customRepresentation() {
        if (exampleCustomRepresentation.isEmpty()) {
            return "Some Text";
        }
        return exampleCustomRepresentation;
    }

    @Config
    public static TestEnum exampleEnum = TestEnum.ONE;

    @Config(readOnly = true)
    public static double exampleReadOnly = Math.PI;

    @Config(temporary = true)
    public static double exampleTemporary = Math.random();

    @Config
    private static boolean examplePrivate = false;

    @Config(condition = "isServer")
    public static boolean exampleServerOnlyConfig = true;
    public static boolean isServer(SharedSuggestionProvider source) {
        return source instanceof CommandSourceStack;
    }

    @Config(setter = @Config.Setter("privateSetter"))
    public static String examplePrivateSetter = "nice";
    private static void privateSetter(String string) {
        examplePrivateSetter = string + '!';
    }

    @Config(comment = "This is a mysterious object")
    public static Object exampleComment = null;

    @Config
    public static BlockInput exampleRegistryAccess = new BlockInput(Blocks.COMPOSTER.defaultBlockState(), Collections.emptySet(), null);

    @Config
    public static StructureType<?> exampleConvertedArgumentType = StructureType.WOODLAND_MANSION;

    @Config(onChange = "onChange")
    public static List<String> exampleOnChange = new ArrayList<>(List.of("xpple, earthcomputer"));
    private static void onChange(List<String> oldValue, List<String> newValue) {
        BetterConfigCommon.LOGGER.info("exampleOnChange was updated | old: {}, new: {}", oldValue, newValue);
    }
}
