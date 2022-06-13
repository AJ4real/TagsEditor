/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.tagseditor.nms.v1_19_R1;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import me.aj4real.tagseditor.DataTag;
import me.aj4real.tagseditor.NMS;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.protocol.game.ClientboundUpdateTagsPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.bukkit.Bukkit;
import org.bukkit.Fluid;
import org.bukkit.GameEvent;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftNamespacedKey;
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
    private static final Registry<net.minecraft.world.level.biome.Biome> biomes = access.registryOrThrow(Registry.BIOME_REGISTRY);
    private static final Map<Integer, EntityType> entityIds = new HashMap<>();

    public void onEnable(Plugin plugin) {
        for (EntityType t : EntityType.values()) {
            if (t == EntityType.UNKNOWN) continue;
            Registry<net.minecraft.world.entity.EntityType<?>> r = access.registryOrThrow(Registry.ENTITY_TYPE_REGISTRY);
            int id = r.getId(r.get(CraftNamespacedKey.toMinecraft(t.getKey())));
            entityIds.put(id, t);
        }
    }

    public Class<ClientboundUpdateTagsPacket> getPacketClass() {
        return ClientboundUpdateTagsPacket.class;
    }

    public Field getField() {
        for (Field f : TagNetworkSerialization.NetworkPayload.class.getDeclaredFields()) {
            if (f.getType() == Map.class) {
                f.setAccessible(true);
                return f;
            }
        }
        return null;
    }

    public Constructor<TagNetworkSerialization.NetworkPayload> getConstructor() {
        Constructor ret = TagNetworkSerialization.NetworkPayload.class.getDeclaredConstructors()[0];
        ret.setAccessible(true);
        return ret;
    }

    public ClientboundUpdateTagsPacket getDefaultPacket() {
        ClientboundUpdateTagsPacket packet = new ClientboundUpdateTagsPacket(TagNetworkSerialization.serializeTagsToNetwork(((CraftServer) Bukkit.getServer()).getHandle().getServer().registryAccess()));
        return packet;
    }

    public ClientboundUpdateTagsPacket buildPacket(Map tags) {
        return new ClientboundUpdateTagsPacket(tags);
    }

    public ResourceKey<Registry<net.minecraft.world.level.material.Fluid>> getFluidRegistry() {
        return Registry.FLUID_REGISTRY;
    }

    public ResourceKey<Registry<net.minecraft.world.entity.EntityType<?>>> getEntityTypeRegistry() {
        return Registry.ENTITY_TYPE_REGISTRY;
    }

    public ResourceKey<Registry<net.minecraft.world.level.biome.Biome>> getBiomeRegistry() {
        return Registry.BIOME_REGISTRY;
    }

    public ResourceKey<Registry<net.minecraft.world.level.gameevent.GameEvent>> getGameEventRegistry() {
        return Registry.GAME_EVENT_REGISTRY;
    }

    public ResourceKey<Registry<Item>> getItemRegistry() {
        return Registry.ITEM_REGISTRY;
    }

    public ResourceKey<Registry<Block>> getBlockRegistry() {
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

    public int getId(Biome biome) {
        Holder<net.minecraft.world.level.biome.Biome> holder = CraftBlock.biomeToBiomeBase(biomes, biome);
        if (holder == null || holder.value() == null) return -1;
        return biomes.getId(holder.value());
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

    public Biome biomeFromId(int id) {
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

    public ResourceLocation getResourceLocation(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }


    public List<Integer> fromIntList(Object in) {
        if (!(in instanceof IntList)) return null;
        return new ArrayList<>((IntList) in);
    }

    public IntArrayList toIntList(List<Integer> in) {
        return new IntArrayList(in);
    }

    public Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> getTagsFromPacket(Object packet) {
        return ((ClientboundUpdateTagsPacket) packet).getTags();
    }

    public String pathFromResourceLocation(Object resourceLocation) {
        return ((ResourceLocation) resourceLocation).getPath();
    }

    public Function<String, DataTag> funcFromRegistry(Object registry) {
        Object r = access.registryOrThrow((ResourceKey) registry).key();
        if (r == getBlockRegistry()) return DataTag.Block::fromString;              // 1.17
        if (r == getItemRegistry()) return DataTag.Item::fromString;                // 1.17
        if (r == getEntityTypeRegistry()) return DataTag.EntityType::fromString;    // 1.17
        if (r == getFluidRegistry()) return DataTag.Fluid::fromString;              // 1.17
        if (r == getBiomeRegistry()) return DataTag.Biome::fromString;              // 1.18
        if (r == getGameEventRegistry()) return DataTag.GameEvent::fromString;      // 1.17
        // ResourceKey[minecraft:root / minecraft:instrument]                       // 1.19
        return null;
    }
}
