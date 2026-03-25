package io.github.nanashithenameless.nbtvoid;

import io.github.nanashithenameless.nbtvoid.util.Util;
import io.github.nanashithenameless.nbtvoid.util.VoidCollection;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class NbtVoid implements ClientModInitializer {
    private static final Path VOID_SAVE_PATH = FabricLoader.getInstance().getGameDir().resolve("void.nbt");
    public static final String MOD_ID = "nbtvoid";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final VoidCollection VOID = new VoidCollection();
        public static final ItemGroup VOID_GROUP = Registry.register(Registries.ITEM_GROUP, Identifier.of(MOD_ID, "void"), FabricItemGroup.builder()
            .displayName(Text.translatable("itemGroup.nbtvoid.void"))
            .icon(Items.ENDER_CHEST::getDefaultStack)
            .texture(Identifier.of(MOD_ID, "item_search"))
            .type(ItemGroup.Type.SEARCH)
            .build()
    );

    public static void load() {
        VOID.setMaxSize(Config.getInstance().getMaxStoredItemRows() * 9);
        NbtCompound nbt;
        try {
            nbt = NbtIo.read(VOID_SAVE_PATH);
        } catch (IOException e) {
            LOGGER.error("Could not load NBT void: " + e);
            return;
        }
        if (nbt == null) return;
        NbtList entries = nbt.getList("entries", NbtElement.COMPOUND_TYPE);
        for (NbtElement entry : entries) {
            NbtCompound stackNbt = (NbtCompound) entry;
            // fix the old format
            if (stackNbt.contains("item", NbtElement.COMPOUND_TYPE)) {
                stackNbt = stackNbt.getCompound("item");
            }
            if (!stackNbt.contains("id", NbtElement.STRING_TYPE)) continue;
            Identifier id = Identifier.tryParse(stackNbt.getString("id"));
            if (id == null) continue;

            Item item = Registries.ITEM.get(id);
            if (item == Items.AIR) continue;

            int count = stackNbt.contains("Count", NbtElement.NUMBER_TYPE) ? Byte.toUnsignedInt(stackNbt.getByte("Count")) : 1;
            ItemStack stack = new ItemStack(item, Math.max(1, count));
            if (stackNbt.contains("tag", NbtElement.COMPOUND_TYPE)) {
                Util.setNbt(stack, stackNbt.getCompound("tag").copy());
            }
            if (stack != ItemStack.EMPTY) {
                VOID.add(stack);
            }
        }
        LOGGER.info("Loaded NBT void (" + VOID.getItems().size() + " items)");
    }

    public static void save() {
        NbtList entries = new NbtList();
        for (ItemStack stack : VOID.getItems()) {
            NbtCompound stackNbt = new NbtCompound();
            stackNbt.putString("id", Registries.ITEM.getId(stack.getItem()).toString());
            stackNbt.putByte("Count", (byte) stack.getCount());
            // check is kinda redundant
            NbtCompound customData = Util.getNbt(stack);
            if (customData != null) {
                stackNbt.put("tag", customData);
            }
            entries.add(stackNbt);
        }
        NbtCompound nbt = new NbtCompound();
        nbt.put("entries", entries);
        try {
            NbtIo.write(nbt, VOID_SAVE_PATH);
            LOGGER.info("Saved NBT void (" + VOID.getItems().size() + " items)");
        } catch (IOException e) {
            LOGGER.error("Could not save NBT void: " + e);
        }
    }

    public static void update() {
        int oldSize = VOID.getItems().size();
        List<ItemStack> items = new ArrayList<>(VOID.getItems());
        VOID.clear();
        items.forEach(VOID::add);
        int difference = oldSize - VOID.getItems().size();
        LOGGER.info("Updated NBT void (removed " + difference + " items)");
    }

    @Override
    public void onInitializeClient() {
        if (Config.getInstance().getDoSave()) {
            load();
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Config.save();
            if (Config.getInstance().getDoSave()) {
                save();
            }
        }));
    }
}
