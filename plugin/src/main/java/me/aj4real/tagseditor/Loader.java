/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.tagseditor;

import org.bukkit.plugin.Plugin;

public interface Loader {
    default void onLoad(Plugin plugin) {}
    default void onEnable(Plugin plugin) {}
    default void onDisable(Plugin plugin) {}
}
