/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.tagseditor.denizen;

import com.denizenscript.denizen.events.bukkit.ScriptReloadEvent;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.YamlConfiguration;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import me.aj4real.tagseditor.Main;
import me.aj4real.tagseditor.TagsProfile;
import me.aj4real.tagseditor.TagsSerializer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.Plugin;

import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TagsProfileScriptContainer extends ScriptContainer {
    private static final DenizenProfileProvider provider = new DenizenProfileProvider();
    static final Map<String, TagsProfileScriptContainer> containers = new HashMap<>();
    private static boolean init = false;
    private static boolean reload = false;
    public static void init(Plugin plugin) {
        if(init) return;
        init = true;
        Main.playerHelper.addProvider(provider);
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void e(ScriptReloadEvent e) {
                load();
            }
            @EventHandler
            public void e(ServerLoadEvent e) {
                if (e.getType() == ServerLoadEvent.LoadType.STARTUP) load();
            }
            public void load() {
                reload = true;
            }
        }, plugin);
    }
    public static Map<String, TagsProfileScriptContainer> getContainers() {
        return Collections.unmodifiableMap(containers);
    }
    private final TagsProfile profile;
    public TagsProfileScriptContainer(YamlConfiguration configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
        if(reload) {
            reload = false;
            containers.clear();
        }
        TagsSerializer serializer = TagsSerializer.read(
                org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(
                        new StringReader(configurationSection.saveToString(true))), new TagsSerializer.Context(scriptContainerName, Debug::echoError));
        containers.put(scriptContainerName, this);
        this.profile = new TagsProfile(provider, scriptContainerName, serializer);
    }
    public TagsProfile getProfile() {
        return this.profile;
    }

}
