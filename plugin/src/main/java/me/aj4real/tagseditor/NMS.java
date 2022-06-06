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
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface NMS extends Loader {

    void onEnable(Plugin plugin);
    Class getPacketClass();
    Field getField();
    Constructor getConstructor();
    Object getDefaultPacket();
    Object buildPacket(Map tags);
    Object getFluidRegistry();
    Object getEntityTypeRegistry();
    Object getBiomeRegistry();
    Object getGameEventRegistry();
    Object getItemRegistry();
    Object getBlockRegistry();
    int getId(BlockData state);
    int getId(ItemStack item);
    int getId(EntityType type);
    int getId(Biome biome);
    int getId(GameEvent e);
    int getId(Fluid f);
    BlockData blockDataFromId(int id);
    ItemStack itemFromId(int id);
    EntityType entityTypeFromId(int id);
    org.bukkit.block.Biome biomeFromId(int id);
    GameEvent gameEventFromId(int id);
    Fluid fluidFromId(int id);
    Object getResourceLocation(String namespace, String path);
    List<Integer> fromIntList(Object in);
    Object toIntList(List<Integer> in);
    Map getTagsFromPacket(Object packet);
    String pathFromResourceLocation(Object resourceLocation);
    Function<String, DataTag> funcFromRegistry(Object registry);
}
