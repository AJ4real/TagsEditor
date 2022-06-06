/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.tagseditor.denizen;

import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import me.aj4real.tagseditor.Loader;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;

public class DenizenImpl implements Loader {
    public void onEnable(Plugin plugin) {
        DenizenCore.commandRegistry.registerCommand(TagsCommand.class);
        TagsProfileScriptContainer.init(plugin);
        ScriptRegistry._registerType("tags_profile", TagsProfileScriptContainer.class);
        plugin.getLogger().log(Level.INFO, "Denizen extension loaded.");
    }
    public void onDisable(Plugin plugin) {
        DenizenCore.commandRegistry.instances.remove("tags");
        ScriptRegistry.typeConstructors.remove("tags_profile");
    }
}
