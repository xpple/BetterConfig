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
Finally, register your `Configs` class.
- For Fabric users, register the `Configs` class in your mod's `onInitialize(Client)` method. Replace `<mod id>` with your 
mod's id. Sometimes you may omit the generics and just do `<>` instead.
  - On clients:
    ```java
    new ModConfigBuilder<FabricClientCommandSource, CommandBuildContext>(<mod id>, Configs.class).build();
    ```
  - On servers:
    ```java
    new ModConfigBuilder<CommandSourceStack, CommandBuildContext>(<mod id>, Configs.class).build();
    ```
- For Paper users, register the `Configs` class in your plugin's `onEnable` method. Replace `<plugin name>` with your
plugin's name.
  ```java
  new ModConfigBuilder<>(<plugin name>, Configs.class).build();
  ```
That's it! Now you can access `exampleString` through `Configs.exampleString`. You can edit `exampleString` by using the
config command.
- On Fabric there are different commands for the client and server.  For both, replace `<mod id>` with your mod's id.
  - On clients, execute `/cconfig <mod id> exampleString set <string>`.
  - On servers, execute `/config <mod id> exampleString set <string>`.
- On Paper servers, execute `/config <plugin name> exampleString set <string>`. Replace `<plugin name>` with your
plugin's name.

## That's not all!
This mod also natively supports the use of `Collection`s and `Map`s as variable types. These configurations will have
the options `add`, `put` and `remove` available. Moreover, you can define your own (de)serialisers to create
configurations with arbitrary types. To do this, all you have to do is register the (de)serialiser when you build your
config. For instance, to create configurations with type `Block` you can do
```java
new ModConfigBuilder<>(<mod id>, Configs.class)
    .registerTypeHierarchy(Block.class, new BlockAdapter(), BlockArgumentType::block)
    .build();
```
where `BlockAdapter` extends `TypeAdapter<Block>` and `BlockArgumentType` implements `ArgumentType<Block>`. See
[these tests](fabric/src/testmod/java/dev/xpple/betterconfig) for a complete picture. An identical setup for Paper can
be found [here](paper/src/testplugin/java/dev/xpple/betterconfig).

Furthermore, you can completely change the behaviour of updating your config values by creating your own methods. Simply
add one or more of `setter`, `adder`, `putter` or `remover` as attribute to your `@Config` annotation. A great use for
this would be adding key-value entries to a `Map` based on a single value. Consider the following configuration.
```java
@Config(putter = @Config.Putter("none"), adder = @Config.Adder("customMapAdder"))
public static Map<String, String> exampleMapAdder = new HashMap<>(Map.of("a", "A", "b", "B"));
public static void customMapAdder(String string) {
    exampleMapAdder.put(string.toLowerCase(Locale.ROOT), string.toUpperCase(Locale.ROOT));
}
```
The value of `"none"` for the putter indicates that no putter will be available. This way, you can use this `Map` in your
code like usual, and add values to it using `/(c)config <mod id> exampleMapAdder add <string>`. For more details, see
[the JavaDocs for `@Config`](common/src/main/java/dev/xpple/betterconfig/api/Config.java).

The parameters of the update method can also be customised.
```java
@Config(adder = @Config.Adder(value = "customTypeAdder", type = int.class))
public static Collection<String> exampleCustomType = new ArrayList<>(List.of("%", "@"));
public static void customTypeAdder(int codepoint) {
    exampleCustomType.add(Character.toString(codepoint));
}
```
For putters, there are separate key and value type attributes.

And many more things! For some illustrative examples, see the `Configs` class for both
[Fabric](fabric/src/testmod/java/dev/xpple/betterconfig/Configs.java) and
[Paper](paper/src/testplugin/java/dev/xpple/betterconfig/Configs.java).

## Installation
Replace `${betterconfig_version}` with the artifact version.

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
    // Fabric
    include modImplementation('dev.xpple:betterconfig-fabric:${betterconfig_version}')
    // Paper (also include the JAR in the plugins folder)
    compileOnly 'dev.xpple:betterconfig-paper:${betterconfig_version}'
}
```
