package io.github.nanashithenameless.nbtvoid.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import java.util.List;

public class Util {
    public static boolean hasNbt(ItemStack stack) {
        NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        return customData != null && !customData.isEmpty();
    }

    public static NbtCompound getNbt(ItemStack stack) {
        NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData == null || customData.isEmpty()) return null;
        return customData.copyNbt();
    }

    public static void setNbt(ItemStack stack, NbtCompound nbt) {
        if (nbt == null || nbt.isEmpty()) {
            stack.remove(DataComponentTypes.CUSTOM_DATA);
        } else {
            stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        }
    }

    public static ItemStack removeNbt(ItemStack stack, List<String> paths) {
        stack = stack.copy();
        NbtCompound nbt = getNbt(stack);
        if (nbt == null) return stack;
        NbtPathArgumentType parser = NbtPathArgumentType.nbtPath();
        for (String path : paths) {
            try {
                NbtPathArgumentType.NbtPath nbtPath = parser.parse(new StringReader(path));
                nbtPath.remove(nbt);
            } catch (CommandSyntaxException ignored) { }
        }
        setNbt(stack, nbt);
        return stack;
    }
}
