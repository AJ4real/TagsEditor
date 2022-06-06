/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.tagseditor;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.aj4real.tagseditor.network.Client;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class PlayerHelper implements Loader {
    private static Plugin plugin;
    private static final Map<String, TagsProfile.Provider> providers = new HashMap<>();
    private static final Map<Player, PlayerHolder> players = new HashMap<>();
    // /ex tags profile:TEST_TAGS_PROFILE3 persistent

    public void onEnable(Plugin plugin) {
        PlayerHelper.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void e(PlayerQuitEvent e) throws IOException {
                players.get(e.getPlayer()).save();
                players.remove(e.getPlayer());
            }
            @EventHandler
            public void e(PlayerJoinEvent e) throws IOException {
                PlayerHolder holder = new PlayerHolder(e.getPlayer());
                players.put(e.getPlayer(), holder);
                holder.load();
            }
        }, plugin);
    }
    public void removeActiveProfile(Player player) {
        players.get(player).removeActiveProfile();
    }
    public void setActiveProfile(Player player, TagsProfile profile, boolean persistent) {
        players.get(player).setActiveProfile(profile, persistent);
    }
    public void resendPacket(Player player) {
        players.get(player).resendPacket();
    }
    public void addProvider(TagsProfile.Provider provider) {
        providers.put(provider.getName(), provider);
    }
    public class PlayerHolder {
        private final Player player;
        private final File f;
        private TagsProfile profile;
        private boolean persistent;
        public PlayerHolder(Player player) {
            this.player = player;
            f = new File(plugin.getDataFolder(), "players/" + player.getUniqueId() + ".dat");
        }
        public void setActiveProfile(TagsProfile profile, boolean persistent) {
            this.profile = profile;
            this.persistent = persistent;
            try {
                save();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save player data for " + player.getDisplayName() + " ( " + player.getUniqueId() + " )", e);
            }
            resendPacket();
        }
        public void removeActiveProfile() {
            profile = null;
            persistent = false;
            f.delete();
            resendPacket();
        }
        public void resendPacket() {
            Client c = Client.getFromPlayer(player);
            TagsProfile profile = this.profile;
            if(profile == null) c.sendPacket(new TagsPacket().build());
            else profile.apply(c);
        }
        public void save() throws IOException {
            if(profile == null) {
                f.delete();
                return;
            }
            String provider = profile.getProvider().getClass().getSimpleName();
            ByteBuf buffer = Unpooled.buffer();
            int i = provider.length();
            buffer.writeInt(i);
            char[] chars = provider.toCharArray();
            for (int j = 0; j < i; j++) buffer.writeChar(chars[j]);
            i = profile.getName().length();
            buffer.writeInt(i);
            chars = profile.getName().toCharArray();
            for (int j = 0; j < i; j++) buffer.writeChar(chars[j]);
            buffer.writeBoolean(persistent);
            f.getParentFile().mkdirs();
            if(!f.exists()) f.createNewFile();
            FileOutputStream os = new FileOutputStream(f);
            os.write(buffer.array());
        }
        public void load() throws IOException {
            if(!f.exists()) return;
            ByteBuf buffer = Unpooled.wrappedBuffer(Files.readAllBytes(f.toPath()));
            int i = buffer.readInt();
            String strProvider = "";
            for (int j = 0; j < i; j++) strProvider += buffer.readChar();
            i = buffer.readInt();
            String strProfile = "";
            for (int j = 0; j < i; j++) strProfile += buffer.readChar();
            TagsProfile.Provider provider = providers.get(strProvider);
            if(provider == null) {
                plugin.getLogger().log(Level.SEVERE, "Profile Provider " + strProvider + " was not found.");
//                f.delete();
                return;
            }
            TagsProfile profile = provider.getProfile(strProfile);
            if(profile == null) {
                plugin.getLogger().log(Level.SEVERE, "Attempted to assign player '" + player.getDisplayName() + "' profile " + strProfile + " from " + strProvider + ", but no such profile exists.");
//                f.delete();
                return;
            }
            boolean persistent = buffer.readBoolean();
            if(persistent) setActiveProfile(profile, persistent);
        }
    }
}
