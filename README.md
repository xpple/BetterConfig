# BetterConfig
A very powerful and easy to use command based configuration library for servers and clients.

## Creating a simple configuration
To start, create a new class. This will be the class where all your configurations are stored. For this example, we'll
call this class `Configs`. Make sure that this class is `public`! Next, create a field for your configuration entry.
Mark this field with the annotation `@Config`. Like the `Configs` class, this field also needs to be `public` and 
additionally may **not** be declared final. The initial value of the field will be used as default (fallback) value.
```java
public class Configs {
    @Config
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
    .registerTypeHierarchyWithArgument(Block.class, new BlockAdapter(), new Pair<>(BlockArgumentType::block, BlockArgumentType::getBlock))
    .build();
```
where `BlockAdapter` extends `TypeAdapter<Block>` and `BlockArgumentType` implements `ArgumentType<Block>`. See 
[these tests](src/testmod/java/dev/xpple/betterconfig) for a complete picture.

Furthermore, you can completely change the behaviour of updating your config values by creating your own methods. Simply
add one or more of `setter`, `adder`, `putter` or `remover` as attribute to your `@Config` annotation. A great use for
this would be adding key-pair entries to a `Map` based on a single value. Consider the following configuration.
```java
@Config(putter = "none", adder = "customMapAdder")
public static Map<String, String> exampleMapAdder = new HashMap<>(Map.of("a", "A", "b", "B"));
public static void customMapAdder(Object string) {
    exampleMapAdder.put(((String) string).toLowerCase(Locale.ROOT), ((String) string).toUpperCase(Locale.ROOT));
}
```
The value of `"none"` for `putter` indicates that no putter will be available. This way, you can use this `Map` in your
code like usual, and add values to it using `/(c)onfig <mod id> exampleMapAdder add <string>`. For more details, see
[the JavaDocs for `@Config`](src/main/java/dev/xpple/betterconfig/api/Config.java).

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
```
dependencies {
    modImplementation 'dev.xpple:betterconfig:${version}'
}
```
