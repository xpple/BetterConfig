package dev.xpple.betterconfig.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.xpple.betterconfig.impl.BetterConfigImpl;
import dev.xpple.betterconfig.impl.ModConfigImpl;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.Server;

public class ConfigCommand extends AbstractConfigCommand<CommandSourceStack, Void, Component> {

    private ConfigCommand() {
        super("config");
    }

    @SuppressWarnings("unchecked")
    public static LiteralCommandNode<CommandSourceStack> build() {
        return new ConfigCommand().create(BetterConfigImpl.getModConfigs().values().stream().map(modConfig -> (ModConfigImpl<CommandSourceStack, Void, Component>) modConfig).toList(), null).requires(source -> source.getSender().hasPermission("betterconfig.config")).build();
    }

    @Override
    protected int comment(CommandSourceStack source, String config, String comment) {
        source.getSender().sendMessage(Component.translatable("betterconfig.commands.config.comment", "Comment for %s:", Component.text(config)));
        source.getSender().sendMessage(Component.text(comment));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int get(CommandSourceStack source, ModConfigImpl<CommandSourceStack, Void, Component> modConfig, String config) {
        Component component = Component.translatable("betterconfig.commands.config.get", "%s is currently set to %s.", Component.text(config), modConfig.asComponent(config));
        source.getSender().sendMessage(component);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int reset(CommandSourceStack source, ModConfigImpl<CommandSourceStack, Void, Component> modConfig, String config) {
        modConfig.reset(config);
        Component component = Component.translatable("betterconfig.commands.config.reset", "%s has been reset to %s.", Component.text(config), modConfig.asComponent(config));
        source.getSender().getServer().broadcast(component, Server.BROADCAST_CHANNEL_ADMINISTRATIVE);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int set(CommandSourceStack source, ModConfigImpl<CommandSourceStack, Void, Component> modConfig, String config, Object value) throws CommandSyntaxException {
        modConfig.set(config, value);
        Component component = Component.translatable("betterconfig.commands.config.set", "%s has been set to %s.", Component.text(config), modConfig.asComponent(config));
        source.getSender().getServer().broadcast(component, Server.BROADCAST_CHANNEL_ADMINISTRATIVE);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int add(CommandSourceStack source, ModConfigImpl<CommandSourceStack, Void, Component> modConfig, String config, Object value) throws CommandSyntaxException {
        modConfig.add(config, value);
        Component component = Component.translatable("betterconfig.commands.config.add", "%s has been added to %s.", Component.text(modConfig.asString(value)), Component.text(config));
        source.getSender().getServer().broadcast(component, Server.BROADCAST_CHANNEL_ADMINISTRATIVE);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int put(CommandSourceStack source, ModConfigImpl<CommandSourceStack, Void, Component> modConfig, String config, Object key, Object value) throws CommandSyntaxException {
        modConfig.put(config, key, value);
        Component component = Component.translatable("betterconfig.commands.config.put", "The mapping %s=%s has been added to %s.", Component.text(modConfig.asString(key)), Component.text(modConfig.asString(value)), Component.text(config));
        source.getSender().getServer().broadcast(component, Server.BROADCAST_CHANNEL_ADMINISTRATIVE);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int remove(CommandSourceStack source, ModConfigImpl<CommandSourceStack, Void, Component> modConfig, String config, Object value) throws CommandSyntaxException {
        modConfig.remove(config, value);
        Component component = Component.translatable("betterconfig.commands.config.remove", "%s has been removed from %s.", Component.text(modConfig.asString(value)), Component.text(config));
        source.getSender().getServer().broadcast(component, Server.BROADCAST_CHANNEL_ADMINISTRATIVE);
        return Command.SINGLE_SUCCESS;
    }
}
