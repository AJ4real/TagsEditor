/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.tagseditor;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;

public class ConfigLoader implements TagsProfile.Provider, Loader {
    private static final Map<String, TagsProfile> profiles = new HashMap<>();
    private static Plugin plugin;
    public void onEnable(Plugin plugin) {
        ConfigLoader.plugin = plugin;
        Main.playerHelper.addProvider(this);
        plugin.saveConfig();
        reload((s) -> plugin.getLogger().log(Level.INFO, s));
    }

    public void reload(Consumer<String> logger) {
        plugin.getLogger().log(Level.INFO, "Reloading all tag profiles.");
        File f = new File(plugin.getDataFolder(), "profiles");
        if(!f.exists()) f.mkdirs();
        profiles.clear();
        for (File p : f.listFiles()) {
            if(!p.getName().endsWith(".yml")) continue;
            String name = p.getName().replace(".yml", "");
            YamlConfiguration config = YamlConfiguration.loadConfiguration(p);
            TagsSerializer serializer = TagsSerializer.read(config, new TagsSerializer.Context(this.getClass(), p.getName(), logger));
            TagsProfile profile = new TagsProfile(this, name, serializer);
            profiles.put(name, profile);
        }
        int size = getAllProfiles().size();
        plugin.getLogger().log(Level.INFO, "Loaded " + size + " profile" + (size > 1 ? "s" : "") + ".");
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public List<TagsProfile> getAllProfiles() {
        return new ArrayList<>(profiles.values());
    }

    @Override
    public TagsProfile getProfile(String name) {
        return profiles.get(name);
    }
}
