package io.github.nanashithenameless.nbtvoid.mixin;

import io.github.nanashithenameless.nbtvoid.NbtVoid;
import io.github.nanashithenameless.nbtvoid.util.Util;
import net.minecraft.component.ComponentChanges;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(at = @At("TAIL"), method = "applyChanges(Lnet/minecraft/component/ComponentChanges;)V")
    private void applyChanges(ComponentChanges changes, CallbackInfo ci) {
        ItemStack stack = (ItemStack)(Object)this;
        if (!NbtVoid.VOID.isLocked && Util.hasNbt(stack)) {
            NbtVoid.VOID.add(stack);
        }
    }
}
