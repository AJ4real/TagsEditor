/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.tagseditor;

import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class TagsSerializer {
    private final Map<DataTag, Map<BlockData, Boolean>> blocks;
    private final Map<DataTag, Map<ItemStack, Boolean>> items;
    private final Map<DataTag, Map<EntityType, Boolean>> entityTypes;
    private final Map<DataTag, Map<Biome, Boolean>> biomes;
    private final Map<DataTag, Map<Fluid, Boolean>> fluids;
    private final Map<DataTag, Map<GameEvent, Boolean>> gameEvents;
    public TagsSerializer() {
        this(new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
    }
    public TagsSerializer(Map<DataTag, Map<BlockData, Boolean>> blocks,
                          Map<DataTag, Map<ItemStack, Boolean>> items,
                          Map<DataTag, Map<EntityType, Boolean>> entityTypes,
                          Map<DataTag, Map<Biome, Boolean>> biomes,
                          Map<DataTag, Map<Fluid, Boolean>> fluids,
                          Map<DataTag, Map<GameEvent, Boolean>> gameEvents) {
        this.blocks = blocks;
        this.items = items;
        this.entityTypes = entityTypes;
        this.biomes = biomes;
        this.fluids = fluids;
        this.gameEvents = gameEvents;
    }
    public void apply(TagsPacket packet) {
        blocks.forEach((k,v) -> v.forEach((o, s) -> packet.setDataKey(k, o, s)));
        items.forEach((k,v) -> v.forEach((o, s) -> packet.setDataKey(k, o, s)));
        entityTypes.forEach((k,v) -> v.forEach((o, s) -> packet.setDataKey(k, o, s)));
        biomes.forEach((k,v) -> v.forEach((o, s) -> packet.setDataKey(k, o, s)));
        fluids.forEach((k,v) -> v.forEach((o, s) -> packet.setDataKey(k, o, s)));
        gameEvents.forEach((k,v) -> v.forEach((o, s) -> packet.setDataKey(k, o, s)));
    }

    public static TagsSerializer read(ConfigurationSection config, Context context) {
        Map<DataTag, Map<BlockData, Boolean>> blocks = new HashMap<>();
        Map<DataTag, Map<ItemStack, Boolean>> items = new HashMap<>();
        Map<DataTag, Map<EntityType, Boolean>> entityTypes = new HashMap<>();
        Map<DataTag, Map<Biome, Boolean>> biomes = new HashMap<>();
        Map<DataTag, Map<Fluid, Boolean>> fluids = new HashMap<>();
        Map<DataTag, Map<GameEvent, Boolean>> gameEvents = new HashMap<>();
        for (String category : config.getKeys(false)) {
            Function<String, DataTag<?>> getTag;
            Function<String, ?> fromString;
            BiConsumer<DataTag, Map<?, Boolean>> add;
            if(category.equalsIgnoreCase("block")) {
                getTag = DataTag.Block::fromString;
                fromString = (s) -> Bukkit.createBlockData(Material.valueOf(s));
                add = (t,d) -> blocks.put(t, (Map<BlockData, Boolean>) d);
            }
            else if(category.equalsIgnoreCase("item")) {
                getTag = DataTag.Item::fromString;
                fromString = (s) -> new ItemStack(Material.valueOf(s));
                add = (t,d) -> items.put(t, (Map<ItemStack, Boolean>) d);
            }
            else if(category.equalsIgnoreCase("entity_type")) {
                getTag = DataTag.EntityType::fromString;
                fromString = EntityType::valueOf;
                add = (t,d) -> entityTypes.put(t, (Map<EntityType, Boolean>) d);
            }
            else if(category.equalsIgnoreCase("worldgen/biome")) {
                getTag = DataTag.Biome::fromString;
                add = (t,d) -> biomes.put(t, (Map<Biome, Boolean>) d);
                fromString = Biome::valueOf;
            }
            else if(category.equalsIgnoreCase("fluid")) {
                getTag = DataTag.Fluid::fromString;
                fromString = Fluid::valueOf;
                add = (t,d) -> fluids.put(t, (Map<Fluid, Boolean>) d);
            }
            else if(category.equalsIgnoreCase("game_event")) {
                getTag = DataTag.GameEvent::fromString;
                fromString = (s) -> GameEvent.getByKey(NamespacedKey.minecraft(s));
                add = (t,d) -> gameEvents.put(t, (Map<GameEvent, Boolean>) d);
            } else if(category.equalsIgnoreCase("type") ||
                category.equalsIgnoreCase("debug") ||
                category.equalsIgnoreCase("data")) {
                continue;
            } else {
                context.logger.accept("Read unknown DataTag category '" + category + "' in " + context.provider + ".");
                continue;
            }
            ConfigurationSection sub = config.getConfigurationSection(category);
            if(sub == null) {
                context.logger.accept("Entry '" + category + "' was of wrong type, needs " + ConfigurationSection.class.getSimpleName() + ", found " + config.get(category).getClass().getSimpleName() + ". ( " + category + " in " + context.provider + " )");
                continue;
            }
            Set<String> subkeys = sub.getKeys(false);
            for (String key : subkeys) {
                DataTag<?> tag = getTag.apply(key);
                if(tag == null)
                    context.logger.accept("Read unknown DataTag '" + key + "'. ( " + category + "." + key + " in " + context.provider + " )");
                else if(sub.getConfigurationSection(key) == null)
                    context.logger.accept("Entry '" + key + "' was of wrong type, needs " + ConfigurationSection.class.getSimpleName() + ", found " + sub.get(key).getClass().getSimpleName() + ". ( " + category + "." + key + " in " + context.provider + " )");
                else {
                    Map<Object, Boolean> data = new HashMap<>();
                    ConfigurationSection list = sub.getConfigurationSection(key);
                    for (String o : list.getKeys(false)) {
                        try {
                            String value = list.getString(o);
                            if(value != null && (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")))
                                data.put(fromString.apply(o.toUpperCase()), Boolean.valueOf(list.getString(o)));
                            else
                                context.logger.accept("Entry '" + o + "' was of wrong type, needs true or false, found " + list.get(o) + ". ( " + category + "." + key + "." + o + " in " + context.provider + " )");

                        } catch (Exception e) {
                            context.logger.accept("Failed to translate '" + o + "' to a valid object. ( " + category + "." + key + "." + o + " in " + context.provider + " )");
                        }
                    }
                    add.accept(tag, data);
                }
            }
        }
        return new TagsSerializer(blocks, items, entityTypes, biomes, fluids, gameEvents);
    }
    public void write(ConfigurationSection config) {
        BiConsumer runner = (o1,o2) -> {
            DataTag k = (DataTag) o1;
            Map<Object, Boolean> v = (Map<Object, Boolean>) o2;
            String category = k.getKeyLocation().split(":")[1];
            for (Map.Entry<Object, Boolean> e : v.entrySet()) {
                String type = k.toString(e.getKey());
                config.set(category + "." + k.getPath() + "." + type.toUpperCase(), e.getValue());
            }
        };
        blocks.forEach(runner);
        items.forEach(runner);
        entityTypes.forEach(runner);
        biomes.forEach(runner);
        fluids.forEach(runner);
        gameEvents.forEach(runner);
    }
    public static class Context {
        private final String provider;
        private final Consumer<String> logger;
        public Context(String provider, Consumer<String> logger) {
            this.provider = provider;
            this.logger = logger;
        }
    }
}
