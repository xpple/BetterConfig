# BetterConfig
A very powerful and easy to use command based configuration library for servers and clients.

## Creating a simple configuration
To start, create a new class. This will be the class where all your configurations are stored. For this example, we'll
call this class `Configs`. Make sure that this class is `public`! Next, create a field for your configuration entry. The
field should not be final. Mark this field with the annotation `@Config`. The initial value of the field will be used as
default (fallback) value. You can add a comment by setting the `comment` attribute.
```java
public class Configs {
    @Config(comment = "This is an example!")
    public static String exampleString = "default";
}
```
Finally, in your mod's `onInitialize(Client)` method, register the `Configs` class. Replace `<mod id>` with your mod's
id.
```java
new ModConfigBuilder(<mod id>, Configs.class).build();
```
That's it! Now you can access `exampleString` through `Configs.exampleString`. You can edit `exampleString` by executing
the following command.
```
/(c)config <mod id> exampleString set <string>
```

## That's not all!
This mod also supports the use of `Collection`s and `Map`s as variable types. These configurations will have the options
`add`, `put` and `remove` available. Moreover, you can define your own (de)serialisers to create configurations with 
arbitrary types. To do this, all you have to do is register the (de)serialiser when you build your config. For instance,
to create a variable with type `Block` you can do
```java
new ModConfigBuilder(<mod id>, Configs.class)
    .registerTypeHierarchy(Block.class, new BlockAdapter(), BlockArgumentType::block)
    .build();
```
where `BlockAdapter` extends `TypeAdapter<Block>` and `BlockArgumentType` implements `ArgumentType<Block>`. See 
[these tests](src/testmod/java/dev/xpple/betterconfig) for a complete picture.

Furthermore, you can completely change the behaviour of updating your config values by creating your own methods. Simply
add one or more of `setter`, `adder`, `putter` or `remover` as attribute to your `@Config` annotation. A great use for
this would be adding key-pair entries to a `Map` based on a single value. Consider the following configuration.
```java
@Config(putter = @Config.Putter("none"), adder = @Config.Adder("customMapAdder"))
public static Map<String, String> exampleMapAdder = new HashMap<>(Map.of("a", "A", "b", "B"));
public static void customMapAdder(String string) {
    exampleMapAdder.put(string.toLowerCase(Locale.ROOT), string.toUpperCase(Locale.ROOT));
}
```
The value of `"none"` for the putter indicates that no putter will be available. This way, you can use this `Map` in your
code like usual, and add values to it using `/(c)config <mod id> exampleMapAdder add <string>`. For more details, see
[the JavaDocs for `@Config`](src/main/java/dev/xpple/betterconfig/api/Config.java).

The parameters of the update method can also be customised.
```java
@Config(adder = @Config.Adder(value = "customTypeAdder", type = int.class))
public static Collection<String> exampleCustomType = new ArrayList<>(List.of("%", "@"));
public static void customTypeAdder(int codepoint) {
    exampleCustomType.add(Character.toString(codepoint));
}
```
For putters, there are separate key and value type attributes.

## Installation
Replace `${version}` with the artifact version.

You may choose between my own maven repository and GitHub's package repository.
### My own
```gradle
repositories {
    maven {
        url 'https://maven.xpple.dev/maven2'
    }
}
```
### GitHub packages
```gradle
repositories {
    maven {
        url 'https://maven.pkg.github.com/xpple/BetterConfig'
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") ?: System.getenv("TOKEN")
        }
    }
}
```
Import it:
```gradle
dependencies {
    include modImplementation('dev.xpple:betterconfig:${betterconfig_version}')
}
```
