/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.tagseditor;

import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public abstract class DataTag<T> {
    private static final String defaultNamespace = "minecraft";
    private final Object resourceLocation;
    private final String namespace;
    private final String path;

    public static void onEnable(Plugin plugin) {
        try {
            new TagsPacket().dump();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    DataTag(String path) {
        this(defaultNamespace, path);
    }

    DataTag(String namespace, String path) {
        this.resourceLocation = Main.nms.getResourceLocation(namespace, path);
        this.namespace = namespace;
        this.path = path;
    }

    Object getResourceLocation() {
        return this.resourceLocation;
    }
    String getNamespace() {
        return this.namespace;
    }
    String getPath() {
        return this.path;
    }
    abstract String getKeyLocation();
    abstract Object getRegistry();
    abstract String toString(T o);
    abstract int getIdFor(T object);
    abstract T getFromId(int id);
    public static class EntityType extends DataTag<org.bukkit.entity.EntityType> {
        private static final String key = "minecraft:entity_type";
        private static final Object registry = Main.nms.getEntityTypeRegistry();
        private static final Map<String, DataTag.EntityType> fromString = new HashMap<>();

        private EntityType(String path) {
            super(path);
        }

        public String getKeyLocation() {
            return key;
        }
        public int getIdFor(org.bukkit.entity.EntityType object) {
            return Main.nms.getId(object);
        }
        public org.bukkit.entity.EntityType getFromId(int id) {
            return Main.nms.entityTypeFromId(id);
        }
        public Object getRegistry() {
            return registry;
        }
        public static EntityType fromString(String name) {
            return fromString.computeIfAbsent(name, EntityType::new);
        }
        public String toString(org.bukkit.entity.EntityType o) {
            return o.name();
        }
    }
    public static class Fluid extends DataTag<org.bukkit.Fluid> {

        private static final String key = "minecraft:fluid";
        private static final Object registry = Main.nms.getFluidRegistry();
        private static final Map<String, DataTag.Fluid> fromString = new HashMap<>();

        private Fluid(String path) {
            super(path);
        }

        public String getKeyLocation() {
            return key;
        }
        public int getIdFor(org.bukkit.Fluid object) {
            return Main.nms.getId(object);
        }
        public org.bukkit.Fluid getFromId(int id) {
            return Main.nms.fluidFromId(id);
        }
        public Object getRegistry() {
            return registry;
        }
        public static Fluid fromString(String name) {
            return fromString.computeIfAbsent(name, Fluid::new);
        }
        public String toString(org.bukkit.Fluid o) {
            return o.name();
        }
    }
    public static class Block extends DataTag<BlockData> {

        private static final String key = "minecraft:block";
        private static final Object registry = Main.nms.getBlockRegistry();
        private static final Map<String, DataTag.Block> fromString = new HashMap<>();

        private Block(String path) {
            super(path);
        }

        public String getKeyLocation() {
            return key;
        }
        public int getIdFor(BlockData object) {
            return Main.nms.getId(object);
        }
        public BlockData getFromId(int id) {
            return Main.nms.blockDataFromId(id);
        }
        public Object getRegistry() {
            return registry;
        }
        public static Block fromString(String name) {
            return fromString.computeIfAbsent(name, Block::new);
        }
        public String toString(BlockData o) {
            return o.getMaterial().name();
        }
    }

    public static class Item extends DataTag<ItemStack> {

        private static final String key = "minecraft:item";
        private static final Object registry = Main.nms.getItemRegistry();
        private static final Map<String, DataTag.Item> fromString = new HashMap<>();

        private Item(String path) {
            super(path);
        }

        public String getKeyLocation() {
            return key;
        }
        public int getIdFor(ItemStack object) {
            return Main.nms.getId(object);
        }
        public ItemStack getFromId(int id) {
            return Main.nms.itemFromId(id);
        }
        public Object getRegistry() {
            return registry;
        }
        public static Item fromString(String name) {
            return fromString.computeIfAbsent(name, Item::new);
        }
        public String toString(ItemStack o) {
            return o.getType().name();
        }
    }
    public static class Biome extends DataTag<org.bukkit.block.Biome> {

        private static final String key = "minecraft:worldgen/biome";
        private static final Object registry = Main.nms.getBiomeRegistry();
        private static final Map<String, DataTag.Biome> fromString = new HashMap<>();

        private Biome(String path) {
            super(path);
        }

        public String getKeyLocation() {
            return key;
        }
        public int getIdFor(org.bukkit.block.Biome object) {
            return Main.nms.getId(object);
        }
        public org.bukkit.block.Biome getFromId(int id) {
            return Main.nms.biomeFromId(id);
        }
        public Object getRegistry() {
            return registry;
        }
        public static Biome fromString(String name) {
            return fromString.computeIfAbsent(name, Biome::new);
        }
        public String toString(org.bukkit.block.Biome o) {
            return o.name();
        }
    }
    public static class GameEvent extends DataTag<org.bukkit.GameEvent> {

        private static final String key = "minecraft:game_event";
        private static final Object registry = Main.nms.getGameEventRegistry();
        private static final Map<String, DataTag.GameEvent> fromString = new HashMap<>();

        private GameEvent(String path) {
            super(path);
        }

        public String getKeyLocation() {
            return key;
        }
        public int getIdFor(org.bukkit.GameEvent object) {
            return Main.nms.getId(object);
        }
        public org.bukkit.GameEvent getFromId(int id) {
            return Main.nms.gameEventFromId(id);
        }
        public Object getRegistry() {
            return registry;
        }
        public static GameEvent fromString(String name) {
            return fromString.computeIfAbsent(name, GameEvent::new);
        }
        public String toString(org.bukkit.GameEvent o) {
            return o.getKey().getKey();
        }
    }
}
