/*
 * Copyright (c) 2024 Owen1212055
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.xpple.betterconfig.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public abstract class WrappedArgumentType<T, N> implements ArgumentType<T> {
    private final ArgumentType<N> nativeType;

    protected WrappedArgumentType(ArgumentType<N> nativeType) {
        this.nativeType = nativeType;
    }

    public abstract T parse(StringReader reader) throws CommandSyntaxException;

    public final ArgumentType<N> getNativeType() {
        return this.nativeType;
    }

    public Collection<String> getExamples() {
        return this.nativeType.getExamples();
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return this.nativeType.listSuggestions(context, builder);
    }

    public abstract static class Converted<T, N> extends WrappedArgumentType<T, N> {
        protected Converted(ArgumentType<N> nativeType) {
            super(nativeType);
        }

        public final T parse(StringReader reader) throws CommandSyntaxException {
            return this.convert(this.getNativeType().parse(reader));
        }

        public abstract T convert(N nativeType) throws CommandSyntaxException;
    }
}
