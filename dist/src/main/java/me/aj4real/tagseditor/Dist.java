/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.tagseditor;

import me.aj4real.tagseditor.denizen.DenizenImpl;
import me.aj4real.tagseditor.network.Network;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Dist extends JavaPlugin {
    Dist plugin = null;
    Main main = new Main();
    public void onLoad() {
        this.plugin = this;
        main.onLoad(this);
    }
    public void onEnable() {
        String s = Arrays.stream(Package.getPackages())
                .map(Package::getName)
                .filter(n -> n.startsWith("org.bukkit.craftbukkit.v1_"))
                .collect(Collectors.toList()).stream().findFirst().get()
                .replace("org.bukkit.craftbukkit.", "").split("\\.")[0];
        try {
            getLogger().log(Level.INFO, "Attempting to load NMS interface for " + s);
            NMS nms = Version.valueOf(s).nms.newInstance();
            nms.onEnable(this);
            Network network = Version.valueOf(s).network.newInstance();
            network.onEnable(this);
            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void e(ServerLoadEvent e) {
                    if(e.getType() == ServerLoadEvent.LoadType.RELOAD) return;
                    if(Bukkit.getPluginManager().isPluginEnabled("Denizen")) {
                        Main.denizenImpl = new DenizenImpl();
                        Main.denizenImpl.onEnable(plugin);
                    }
                }
            }, plugin);
            main.onEnable(this, nms, network);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Could not initiate support for " + s + ", Is it a supported version?", e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
    }
    public void onDisable() {
        main.onDisable(this);
    }
    public enum Version {
        v1_17_R1(me.aj4real.tagseditor.nms.v1_17_R1.NMSImpl.class, me.aj4real.tagseditor.nms.v1_17_R1.NetworkImpl.class),
        v1_18_R1(me.aj4real.tagseditor.nms.v1_18_R1.NMSImpl.class, me.aj4real.tagseditor.nms.v1_18_R1.NetworkImpl.class),
        v1_18_R2(me.aj4real.tagseditor.nms.v1_18_R2.NMSImpl.class, me.aj4real.tagseditor.nms.v1_18_R2.NetworkImpl.class);
        private final Class<? extends NMS> nms;
        private final Class<? extends Network> network;
        Version(Class<? extends NMS> nms, Class<? extends Network> network) {
            this.nms = nms;
            this.network = network;
        }
    }
}
