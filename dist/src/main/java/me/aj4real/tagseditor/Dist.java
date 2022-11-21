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

import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Dist extends JavaPlugin {
    Dist plugin = null;
    Main main = new Main();
    public void onLoad() {
        this.plugin = this;
        main.onLoad(this);
    }
    public void onEnable() {
        String regex = "\\d+(\\.\\d+)+";
        String strVer = Bukkit.getVersion();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(strVer);
        matcher.find();
        strVer = 'v' + matcher.group();
        try {
            getLogger().log(Level.INFO, "Attempting to load NMS interface for " + strVer);
            Version ver = Version.valueOf(strVer.replace('.', '_'));
            NMS nms = ver.nms.getConstructor().newInstance();
            nms.onEnable(this);
            Network network = ver.network.getConstructor().newInstance();
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
            getLogger().log(Level.SEVERE, "Could not initiate support for " + strVer + ", Is it a supported version?", e);
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }
    public void onDisable() {
        main.onDisable(this);
    }
    public enum Version {
        v1_17(me.aj4real.tagseditor.nms.v1_17.NMSImpl.class, me.aj4real.tagseditor.nms.v1_17.NetworkImpl.class),
        v1_17_1(me.aj4real.tagseditor.nms.v1_17_1.NMSImpl.class, me.aj4real.tagseditor.nms.v1_17_1.NetworkImpl.class),
        v1_18(me.aj4real.tagseditor.nms.v1_18.NMSImpl.class, me.aj4real.tagseditor.nms.v1_18.NetworkImpl.class),
        v1_18_1(me.aj4real.tagseditor.nms.v1_18_1.NMSImpl.class, me.aj4real.tagseditor.nms.v1_18_1.NetworkImpl.class),
        v1_18_2(me.aj4real.tagseditor.nms.v1_18_2.NMSImpl.class, me.aj4real.tagseditor.nms.v1_18_2.NetworkImpl.class),
        v1_19(me.aj4real.tagseditor.nms.v1_19.NMSImpl.class, me.aj4real.tagseditor.nms.v1_19.NetworkImpl.class),
        v1_19_1(me.aj4real.tagseditor.nms.v1_19_1.NMSImpl.class, me.aj4real.tagseditor.nms.v1_19_1.NetworkImpl.class),
        v1_19_2(me.aj4real.tagseditor.nms.v1_19_2.NMSImpl.class, me.aj4real.tagseditor.nms.v1_19_2.NetworkImpl.class);
        private final Class<? extends NMS> nms;
        private final Class<? extends Network> network;
        Version(Class<? extends NMS> nms, Class<? extends Network> network) {
            this.nms = nms;
            this.network = network;
        }
    }
}
