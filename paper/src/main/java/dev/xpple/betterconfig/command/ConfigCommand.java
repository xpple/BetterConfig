package dev.xpple.betterconfig.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.xpple.betterconfig.command.suggestion.EnumSuggestionProvider;
import dev.xpple.betterconfig.impl.AbstractConfigImpl;
import dev.xpple.betterconfig.impl.BetterConfigImpl;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.kyori.adventure.text.Component;
import org.bukkit.Server;

public class ConfigCommand extends AbstractConfigCommand<CommandSourceStack, Void> {

    @Override
    protected <T extends Enum<T>> SuggestionProvider<CommandSourceStack> enumSuggestionProvider(Class<T> type) {
        return new EnumSuggestionProvider<>(type);
    }

    @Override
    protected DynamicCommandExceptionType invalidEnumException() {
        return new DynamicCommandExceptionType(value -> MessageComponentSerializer.message().serialize(Component.translatable("argument.enum.invalid", Component.text(String.valueOf(value)))));
    }

    private ConfigCommand() {
        super("config");
    }

    public static LiteralCommandNode<CommandSourceStack> build() {
        return new ConfigCommand().create(BetterConfigImpl.getPluginConfigs().values(), null).requires(source -> source.getSender().hasPermission("betterconfig.config")).build();
    }

    @Override
    protected int comment(CommandSourceStack source, String config, String comment) {
        source.getSender().sendMessage(Component.translatable("betterconfig.commands.config.comment", "Comment for %s:", Component.text(config)));
        source.getSender().sendMessage(Component.text(comment));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int get(CommandSourceStack source, AbstractConfigImpl<CommandSourceStack, Void> abstractConfig, String config) {
        Component component = Component.translatable("betterconfig.commands.config.get", "%s is currently set to %s.", Component.text(config), Component.text(abstractConfig.asString(config)));
        source.getSender().sendMessage(component);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int reset(CommandSourceStack source, AbstractConfigImpl<CommandSourceStack, Void> abstractConfig, String config) {
        abstractConfig.reset(config);
        Component component = Component.translatable("betterconfig.commands.config.reset", "%s has been reset to %s.", Component.text(config), Component.text(abstractConfig.asString(config)));
        source.getSender().getServer().broadcast(component, Server.BROADCAST_CHANNEL_ADMINISTRATIVE);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int set(CommandSourceStack source, AbstractConfigImpl<CommandSourceStack, Void> abstractConfig, String config, Object value) throws CommandSyntaxException {
        abstractConfig.set(config, value);
        Component component = Component.translatable("betterconfig.commands.config.set", "%s has been set to %s.", Component.text(config), Component.text(abstractConfig.asString(config)));
        source.getSender().getServer().broadcast(component, Server.BROADCAST_CHANNEL_ADMINISTRATIVE);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int add(CommandSourceStack source, AbstractConfigImpl<CommandSourceStack, Void> abstractConfig, String config, Object value) throws CommandSyntaxException {
        abstractConfig.add(config, value);
        Component component = Component.translatable("betterconfig.commands.config.add", "%s has been added to %s.", Component.text(abstractConfig.asString(value)), Component.text(config));
        source.getSender().getServer().broadcast(component, Server.BROADCAST_CHANNEL_ADMINISTRATIVE);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int put(CommandSourceStack source, AbstractConfigImpl<CommandSourceStack, Void> abstractConfig, String config, Object key, Object value) throws CommandSyntaxException {
        abstractConfig.put(config, key, value);
        Component component = Component.translatable("betterconfig.commands.config.put", "The mapping %s=%s has been added to %s.", Component.text(abstractConfig.asString(key)), Component.text(abstractConfig.asString(value)), Component.text(config));
        source.getSender().getServer().broadcast(component, Server.BROADCAST_CHANNEL_ADMINISTRATIVE);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int remove(CommandSourceStack source, AbstractConfigImpl<CommandSourceStack, Void> abstractConfig, String config, Object value) throws CommandSyntaxException {
        abstractConfig.remove(config, value);
        Component component = Component.translatable("betterconfig.commands.config.remove", "%s has been removed from %s.", Component.text(abstractConfig.asString(value)), Component.text(config));
        source.getSender().getServer().broadcast(component, Server.BROADCAST_CHANNEL_ADMINISTRATIVE);
        return Command.SINGLE_SUCCESS;
    }
}
