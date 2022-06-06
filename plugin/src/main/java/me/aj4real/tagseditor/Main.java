/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.tagseditor;

import me.aj4real.tagseditor.network.Network;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

public class Main implements Loader {
    public static NMS nms;
    public static Network network;
    public static Loader denizenImpl;
    public static PlayerHelper playerHelper;
    public static ConfigLoader config;
    public void onEnable(Plugin plugin, NMS nms, Network network) {
        Main.nms = nms;
        Main.network = network;
        DataTag.onEnable(plugin);
        playerHelper = new PlayerHelper();
        playerHelper.onEnable(plugin);
        config = new ConfigLoader();
        config.onEnable(plugin);
        PluginCommand pcmd = Bukkit.getPluginCommand("tags");
        TagsCommand cmd = new TagsCommand();
        pcmd.setExecutor(cmd);
        pcmd.setTabCompleter(cmd);
    }
    public void onDisable(Plugin plugin) {
        if(nms != null) nms.onDisable(plugin);
        if(network != null) network.onDisable(plugin);
        if(denizenImpl != null) denizenImpl.onDisable(plugin);
        if(playerHelper != null) playerHelper.onDisable(plugin);
        if(config != null) config.onDisable(plugin);
    }
}
