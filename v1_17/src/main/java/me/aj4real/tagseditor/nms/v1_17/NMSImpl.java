/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.tagseditor.nms.v1_17;

import me.aj4real.tagseditor.DataTag;
import me.aj4real.tagseditor.NMS;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.protocol.game.ClientboundUpdateTagsPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagCollection;
import net.minecraft.world.level.biome.Biome;
import org.bukkit.Bukkit;
import org.bukkit.Fluid;
import org.bukkit.GameEvent;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.IntArrayList;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.IntList;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_17_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftNamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class NMSImpl implements NMS {

    private static final RegistryAccess access = ((CraftServer) Bukkit.getServer()).getHandle().getServer().registryAccess();
    private static final Registry<Biome> biomes = access.registryOrThrow(Registry.BIOME_REGISTRY);
    private static final Map<Integer, EntityType> entityIds = new HashMap<>();

    public void onEnable(Plugin plugin) {
        for (EntityType t : EntityType.values()) {
            if (t == EntityType.UNKNOWN) continue;
            Registry<net.minecraft.world.entity.EntityType<?>> r = access.registryOrThrow(Registry.ENTITY_TYPE_REGISTRY);
            int id = r.getId(r.get(CraftNamespacedKey.toMinecraft(t.getKey())));
            entityIds.put(id, t);
        }
    }

    public Class getPacketClass() {
        return ClientboundUpdateTagsPacket.class;
    }

    public Field getField() {

        for (Field f : TagCollection.NetworkPayload.class.getDeclaredFields()) {
            if (f.getType() == Map.class) {
                f.setAccessible(true);
                return f;
            }
        }
        return null;
    }

    public Constructor getConstructor() {
        Constructor ret = TagCollection.NetworkPayload.class.getDeclaredConstructors()[0];
        ret.setAccessible(true);
        return ret;
    }

    public Object getDefaultPacket() {
        return new ClientboundUpdateTagsPacket(((CraftServer) Bukkit.getServer()).getHandle().getServer().getTags().serializeToNetwork(access));
    }

    public Object buildPacket(Map tags) {
        return new ClientboundUpdateTagsPacket(tags);
    }

    public Object getFluidRegistry() {
        return Registry.FLUID_REGISTRY;
    }

    public Object getEntityTypeRegistry() {
        return Registry.ENTITY_TYPE_REGISTRY;
    }

    public Object getBiomeRegistry() {
        return Registry.BIOME_REGISTRY;
    }

    public Object getGameEventRegistry() {
        return Registry.GAME_EVENT_REGISTRY;
    }

    public Object getItemRegistry() {
        return Registry.ITEM_REGISTRY;
    }

    public Object getBlockRegistry() {
        return Registry.BLOCK_REGISTRY;
    }

    public int getId(BlockData state) {
        return Registry.BLOCK.getId(((CraftBlockData) state).getState().getBlock());
    }

    public int getId(ItemStack item) {
        return Registry.ITEM.getId(CraftItemStack.asNMSCopy(item).getItem());
    }

    public int getId(EntityType type) {
        if (type == EntityType.UNKNOWN) return -1;
        Registry<net.minecraft.world.entity.EntityType<?>> r = access.registryOrThrow(Registry.ENTITY_TYPE_REGISTRY);
        return r.getId(r.get(CraftNamespacedKey.toMinecraft(type.getKey())));
    }

    public int getId(org.bukkit.block.Biome biome) {
        net.minecraft.world.level.biome.Biome holder = CraftBlock.biomeToBiomeBase(biomes, biome);
        if (holder == null) return -1;
        return biomes.getId(holder);
    }

    public int getId(GameEvent e) {
        Registry<net.minecraft.world.level.gameevent.GameEvent> r = access.registryOrThrow(Registry.GAME_EVENT_REGISTRY);
        return r.getId(r.get(CraftNamespacedKey.toMinecraft(e.getKey())));
    }

    public int getId(Fluid f) {
        Registry<net.minecraft.world.level.material.Fluid> r = access.registryOrThrow(Registry.FLUID_REGISTRY);
        return r.getId(r.get(CraftNamespacedKey.toMinecraft(f.getKey())));
    }

    public BlockData blockDataFromId(int id) {
        return Bukkit.createBlockData(CraftMagicNumbers.getMaterial(access.registryOrThrow(Registry.BLOCK_REGISTRY).byId(id)));
    }

    public ItemStack itemFromId(int id) {
        return CraftItemStack.asNewCraftStack(access.registryOrThrow(Registry.ITEM_REGISTRY).byId(id));
    }

    public EntityType entityTypeFromId(int id) {
        if (entityIds.containsKey(id)) return entityIds.get(id);
        else return EntityType.UNKNOWN;
    }

    public org.bukkit.block.Biome biomeFromId(int id) {
        Registry<net.minecraft.world.level.biome.Biome> r = access.registryOrThrow(Registry.BIOME_REGISTRY);
        return CraftBlock.biomeBaseToBiome(r, r.byId(id));
    }

    public GameEvent gameEventFromId(int id) {
        Registry<net.minecraft.world.level.gameevent.GameEvent> r = access.registryOrThrow(Registry.GAME_EVENT_REGISTRY);
        return GameEvent.getByKey(CraftNamespacedKey.fromMinecraft(r.getKey(r.byId(id))));
    }

    public Fluid fluidFromId(int id) {
        Registry<net.minecraft.world.level.material.Fluid> r = access.registryOrThrow(Registry.FLUID_REGISTRY);
        return Fluid.valueOf(CraftNamespacedKey.fromMinecraft(r.getKey(r.byId(id))).getKey().toUpperCase());
    }

    public Object getResourceLocation(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }


    public List<Integer> fromIntList(Object in) {
        if (!(in instanceof IntList)) return null;
        return new ArrayList<>((IntList) in);
    }

    public Object toIntList(List<Integer> in) {
        return new IntArrayList(in);
    }

    public Map getTagsFromPacket(Object packet) {
        return ((ClientboundUpdateTagsPacket) packet).getTags();
    }

    public String pathFromResourceLocation(Object resourceLocation) {
        return ((ResourceLocation) resourceLocation).getPath();
    }

    public Function<String, DataTag> funcFromRegistry(Object registry) {
        Object r = access.registryOrThrow((ResourceKey) registry).key();
        if (r == getBlockRegistry()) return DataTag.Block::fromString;
        if (r == getItemRegistry()) return DataTag.Item::fromString;
        if (r == getEntityTypeRegistry()) return DataTag.EntityType::fromString;
        if (r == getFluidRegistry()) return DataTag.Fluid::fromString;
        if (r == getBiomeRegistry()) return DataTag.Biome::fromString;
        if (r == getGameEventRegistry()) return DataTag.GameEvent::fromString;
        return null;
    }
}