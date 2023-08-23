package dev.xpple.betterconfig.mixin;

import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ArgumentTypes.class)
public interface ArgumentTypesAccessor {
    @Accessor
    static Map<Class<?>, ArgumentSerializer<?, ?>> getCLASS_MAP() {
        throw new AssertionError();
    }
}
