/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.tagseditor;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class TagsCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 0) sender.sendMessage(Strings.ERROR_NOT_ENOUGH_ARGUMENTS);
        else switch(args[0].toLowerCase()) {
            case "reload": {
                try {
                    Main.config.reload((s) -> sender.sendMessage(Strings.COLOR_WARNING + s));
                    sender.sendMessage(String.format(Strings.SUCCESS_RELOAD, Main.config.getAllProfiles().size()));
                } catch (Exception e) {
                    sender.sendMessage(Strings.ERROR_RELOAD_FAILED);
                }
                return true;
            }
            case "info": {
                if(args.length == 1) {
                    List<TagsProfile> profiles = Main.config.getAllProfiles();
                    sender.sendMessage(" " + ChatColor.DARK_AQUA + profiles.size() + " " + ChatColor.AQUA + "Profiles...");
                    for (TagsProfile p : profiles) {
                        sender.sendMessage(ChatColor.DARK_AQUA + " - " + ChatColor.AQUA + p.getName());
                    }
                }
                return true;
            }
            case "set": {
                if(args.length == 1) {
                    sender.sendMessage(Strings.ERROR_NOT_ENOUGH_ARGUMENTS);
                    return true;
                }
                else {
                    Player target;
                    try{
                        target = Bukkit.getPlayer(UUID.fromString(args[1]));
                    } catch (IllegalArgumentException exception){
                        target = Bukkit.getPlayer(args[1]);
                    }
                    if(target == null) {
                        sender.sendMessage(Strings.ERROR_UNKNOWN_TARGET);
                        return true;
                    }
                    if(args.length == 2) {
                        sender.sendMessage(Strings.ERROR_NOT_ENOUGH_ARGUMENTS);
                        return true;
                    }
                    else {
                        if(args[2].equalsIgnoreCase("clear")) {
                            Main.playerHelper.removeActiveProfile(target);
                            sender.sendMessage(String.format(Strings.SUCCESS_PROFILE_REMOVED, target.getDisplayName()));
                            break;
                        }
                        TagsProfile profile = Main.config.getProfile(args[2]);
                        if(profile == null) {
                            sender.sendMessage(Strings.ERROR_UNKNOWN_PROFILE);
                            return true;
                        }
                        boolean persistent = false;
                        if(args.length > 3){
                            if(args[3].equalsIgnoreCase("persistent")) persistent = true;
                            else {
                                sender.sendMessage(Strings.ERROR_UNKNOWN_ARGUMENT);
                                return true;
                            }
                        }
                        Main.playerHelper.setActiveProfile(target, profile, persistent);
                        sender.sendMessage(String.format(Strings.SUCCESS_PROFILE_SET, target.getDisplayName(), profile.getName()));
                        if(persistent) sender.sendMessage(Strings.WARNING_PERSISTENT);
                        return true;
                    }
                }
            }
            default: {
                sender.sendMessage(Strings.ERROR_UNKNOWN_ARGUMENT);
                return true;
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 1) return Arrays.stream(new String[] { "reload", "info", "set" }).filter((s) -> s.startsWith(args[0])).collect(Collectors.toList());
        else {
            switch (args[0].toLowerCase()){
                case "set": {
                    if(args.length == 2)
                        return Bukkit.getOnlinePlayers().stream()
                                .filter((p) -> p.getDisplayName().startsWith(args[1]))
                                .map((p) -> p.getDisplayName())
                                .collect(Collectors.toList());
                    else {
                        if(args.length == 3){
                            List<String> ret = Main.config.getAllProfiles().stream()
                                    .filter((p) -> p.getName().startsWith(args[2]))
                                    .map((p) -> p.getName())
                                    .collect(Collectors.toList());
                            ret.add("clear");
                            return ret;
                        } else if(args.length == 4) return Collections.singletonList("persistent");
                        else return null;
                    }
                }
                default: {
                    return null;
                }
            }
        }
    }
    private static class Strings {
        private static final ChatColor COLOR_ERROR = ChatColor.RED;
        private static final ChatColor COLOR_SUCCESS = ChatColor.AQUA;
        private static final ChatColor COLOR_WARNING = ChatColor.YELLOW;

        private static final String ERROR_UNKNOWN_TARGET = COLOR_ERROR + "Unknown Target Player.";
        private static final String ERROR_UNKNOWN_PROFILE = COLOR_ERROR + "Unknown Target Player.";
        private static final String ERROR_RELOAD_FAILED = COLOR_ERROR + "An error occurred while reloading the plugin.";
        private static final String ERROR_NOT_ENOUGH_ARGUMENTS = COLOR_ERROR + "Not enough arguments.";
        private static final String ERROR_UNKNOWN_ARGUMENT = COLOR_ERROR + "Unknown Argument.";
        private static final String WARNING_PERSISTENT = COLOR_WARNING + "This setting will persist through logouts and restarts.";
        private static final String SUCCESS_PROFILE_SET = COLOR_SUCCESS + "Target %s now has active profile %s.";
        private static final String SUCCESS_RELOAD = COLOR_SUCCESS + "Successfully loaded %s profiles.";
        private static final String SUCCESS_PROFILE_REMOVED = COLOR_SUCCESS + "Target %s had their current profile removed.";
    }
}
