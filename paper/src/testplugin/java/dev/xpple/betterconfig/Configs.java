package dev.xpple.betterconfig;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.betterconfig.api.Config;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.generator.structure.Structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings({"FieldMayBeFinal", "unused", "MismatchedQueryAndUpdateOfCollection"})
public class Configs {
    @Config
    public static Material exampleMaterial = Material.STONE;

    @Config
    public static Collection<Material> exampleMaterialList = new ArrayList<>(List.of(Material.PISTON, Material.STICKY_PISTON));

    @Config
    public static Map<String, Material> exampleStringMaterialMap = new HashMap<>(Map.of("sand", Material.SAND, "red_sand", Material.RED_SAND));

    @Config
    public static Map<Material, String> exampleMaterialStringMap = new HashMap<>(Map.of(Material.NETHERRACK, "nether", Material.END_STONE, "end"));

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

    @Config
    public static TestEnum exampleEnum = TestEnum.ONE;

    @Config(readOnly = true)
    public static double exampleReadOnly = Math.PI;

    @Config(temporary = true)
    public static double exampleTemporary = Math.random();

    @Config
    private static boolean examplePrivate = false;

    @Config(condition = "isXpple")
    public static boolean exampleCondition = true;
    public static boolean isXpple(CommandSourceStack source) {
        return source.getSender().getName().equals("xpple");
    }

    @Config(setter = @Config.Setter("privateSetter"))
    public static String examplePrivateSetter = "nice";
    private static void privateSetter(String string) {
        examplePrivateSetter = string + '!';
    }

    @Config(comment = "This is a mysterious object")
    public static Object exampleComment = null;

    @Config
    public static BlockState exampleNativeArgumentType = Material.COMPOSTER.createBlockData().createBlockState();

    @Config
    public static Structure exampleCustomArgumentType = Structure.MANSION;

    @Config(onChange = "onChange")
    public static List<String> exampleOnChange = new ArrayList<>(List.of("xpple, earthcomputer"));
    private static void onChange(List<String> oldValue, List<String> newValue) {
        BetterConfigCommon.LOGGER.info("exampleOnChange was updated | old: {}, new: {}", oldValue, newValue);
    }
}
