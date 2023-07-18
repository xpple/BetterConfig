package dev.xpple.betterconfig.command.client;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.xpple.betterconfig.command.ConfigCommandHelper;
import dev.xpple.betterconfig.command.suggestion.EnumSuggestionProvider;
import dev.xpple.betterconfig.impl.AbstractConfigImpl;
import dev.xpple.betterconfig.impl.BetterConfigImpl;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

public class ConfigCommandClient extends ConfigCommandHelper<FabricClientCommandSource> {

    @Override
    protected <T extends Enum<T>> SuggestionProvider<FabricClientCommandSource> enumSuggestionProvider(Class<T> type) {
        return new EnumSuggestionProvider<>(type);
    }

    @Override
    protected DynamicCommandExceptionType invalidEnumException() {
        return new DynamicCommandExceptionType(value -> Text.translatable("argument.enum.invalid", value));
    }

    @SuppressWarnings("unchecked")
    public void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(this.create("cconfig", BetterConfigImpl.getModConfigs().values().stream().map(modConfig -> (AbstractConfigImpl<FabricClientCommandSource>) modConfig).toList()));
    }

    @Override
    protected int get(FabricClientCommandSource source, AbstractConfigImpl<FabricClientCommandSource> abstractConfig, String config) {
        source.sendFeedback(Text.translatable("betterconfig.commands.config.get", config, abstractConfig.asString(config)));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int reset(FabricClientCommandSource source, AbstractConfigImpl<FabricClientCommandSource> abstractConfig, String config) {
        abstractConfig.reset(config);
        source.sendFeedback(Text.translatable("betterconfig.commands.config.reset", config, abstractConfig.asString(config)));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int set(FabricClientCommandSource source, AbstractConfigImpl<FabricClientCommandSource> abstractConfig, String config, Object value) throws CommandSyntaxException {
        abstractConfig.set(config, value);
        source.sendFeedback(Text.translatable("betterconfig.commands.config.set", config, abstractConfig.asString(config)));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int add(FabricClientCommandSource source, AbstractConfigImpl<FabricClientCommandSource> abstractConfig, String config, Object value) throws CommandSyntaxException {
        abstractConfig.add(config, value);
        source.sendFeedback(Text.translatable("betterconfig.commands.config.add", abstractConfig.asString(value), config));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int put(FabricClientCommandSource source, AbstractConfigImpl<FabricClientCommandSource> abstractConfig, String config, Object key, Object value) throws CommandSyntaxException {
        abstractConfig.put(config, key, value);
        source.sendFeedback(Text.translatable("betterconfig.commands.config.put", key, abstractConfig.asString(value), config));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int remove(FabricClientCommandSource source, AbstractConfigImpl<FabricClientCommandSource> abstractConfig, String config, Object value) throws CommandSyntaxException {
        abstractConfig.remove(config, value);
        source.sendFeedback(Text.translatable("betterconfig.commands.config.remove", abstractConfig.asString(value), config));
        return Command.SINGLE_SUCCESS;
    }
}
