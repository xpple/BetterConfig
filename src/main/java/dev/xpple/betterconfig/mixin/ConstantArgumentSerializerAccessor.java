package dev.xpple.betterconfig.mixin;

import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Function;

@Mixin(ConstantArgumentSerializer.class)
public interface ConstantArgumentSerializerAccessor {
    @Accessor
    ConstantArgumentSerializer.Properties getProperties();

    @Mixin(ConstantArgumentSerializer.Properties.class)
    interface PropertiesAccessor {
        @Accessor
        Function getTypeSupplier();
    }
}
