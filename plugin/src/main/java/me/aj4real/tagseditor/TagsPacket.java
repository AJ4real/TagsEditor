/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.tagseditor;

import org.bukkit.Fluid;
import org.bukkit.GameEvent;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class TagsPacket {
    public static final Field field = Main.nms.getField();
    public static final Constructor constructor = Main.nms.getConstructor();

    private final Object original; // ClientboundUpdateTagsPacket
    private final Map tags; // <ResourceKey<Registry>, TagNetworkSerialization.NetworkPayload>
    private final Map<DataTag, Map<Integer, Boolean>> storage = new HashMap<>();
    public TagsPacket() {
        this(Main.nms.getDefaultPacket());
    }

    public TagsPacket(Object packet) {
        original = packet;
        tags = Main.nms.getTagsFromPacket(packet);
    }

    public TagsSerializer dump() throws IllegalAccessException {
        Map<DataTag, Map<BlockData, Boolean>> blocks = new HashMap<>();
        Map<DataTag, Map<ItemStack, Boolean>> items = new HashMap<>();
        Map<DataTag, Map<EntityType, Boolean>> entityTypes = new HashMap<>();
        Map<DataTag, Map<Biome, Boolean>> biomes = new HashMap<>();
        Map<DataTag, Map<Fluid, Boolean>> fluids = new HashMap<>();
        Map<DataTag, Map<GameEvent, Boolean>> gameEvents = new HashMap<>();
        for (Object key : Main.nms.getTagsFromPacket(original).keySet()) {
            Function<String, DataTag> c = Main.nms.funcFromRegistry(key);
            BiConsumer<Object, Object> runner = (k,v) -> {
                DataTag tag = c.apply(Main.nms.pathFromResourceLocation(k));
                Object registry = tag.getRegistry(); // ResourceKey<Registry>
                List<Integer> ints = Main.nms.fromIntList(v);
                try {
                    if (registry == Main.nms.getBlockRegistry()) for (Integer i : ints)
                        blocks.computeIfAbsent(tag, (t) -> new HashMap<>()).put(Main.nms.blockDataFromId(i), true);
                    if (registry == Main.nms.getItemRegistry()) for (Integer i : ints)
                        items.computeIfAbsent(tag, (t) -> new HashMap<>()).put(Main.nms.itemFromId(i), true);
                    if (registry == Main.nms.getBiomeRegistry()) for (Integer i : ints)
                        biomes.computeIfAbsent(tag, (t) -> new HashMap<>()).put(Main.nms.biomeFromId(i), true);
                    if (registry == Main.nms.getEntityTypeRegistry()) for (Integer i : ints)
                        entityTypes.computeIfAbsent(tag, (t) -> new HashMap<>()).put(Main.nms.entityTypeFromId(i), true);
                    if (registry == Main.nms.getFluidRegistry()) for (Integer i : ints)
                        fluids.computeIfAbsent(tag, (t) -> new HashMap<>()).put(Main.nms.fluidFromId(i), true);
                    if (registry == Main.nms.getGameEventRegistry()) for (Integer i : ints)
                        gameEvents.computeIfAbsent(tag, (t) -> new HashMap<>()).put(Main.nms.gameEventFromId(i), true);
                } catch (UnsupportedOperationException err) {}
            };
            ((Map<Object, Object>) field.get(Main.nms.getTagsFromPacket(original).get(key))).forEach(runner);
            storage.forEach(runner);
        }
        TagsSerializer serializer = new TagsSerializer(blocks, items, entityTypes, biomes, fluids, gameEvents);
        return serializer;
    }

    public <T> void setDataKey(DataTag<T> tag, T data, boolean value) {
        storage.computeIfAbsent(tag, (r) -> new HashMap<>()).put(tag.getIdFor(data), value);
    }

    // @ret ClientboundUpdateTagsPacket
    public Object build() {
        try {
            patch();
            return Main.nms.buildPacket(tags);
        } catch (Exception e) {
            e.printStackTrace();
            return original;
        }
    }


    private void patch() throws IllegalAccessException, InvocationTargetException, InstantiationException {
        for (Map.Entry<DataTag, Map<Integer, Boolean>> e : storage.entrySet()) {
            Object r = e.getKey().getRegistry(); // ResourceKey<Registry<?>>
            Object t = tags.get(r);
            if(t == null) continue; // v1_17_R1 fix ( no worldgen/biome )
            Map newIntLists = (Map) field.get(t);
            Object rl = e.getKey().getResourceLocation(); // ResourceLocation
            List<Integer> list = Main.nms.fromIntList(newIntLists.get(rl));
            e.getValue().forEach((k,v) -> {
                if(v) list.add(k);
                else list.remove(k);
            });
            newIntLists.put(rl, Main.nms.toIntList(list));
            tags.put(r, constructor.newInstance(newIntLists));
        }
    }

}
