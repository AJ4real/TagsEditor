/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.tagseditor.denizen;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import me.aj4real.tagseditor.Main;
import me.aj4real.tagseditor.TagsProfile;

public class TagsCommand extends AbstractCommand {

    public TagsCommand() {
        setName("tags");
        setSyntax("tags profile:<ElementTag> (targets:<PlayerTag>/<ListTag[PlayerTag]>) (persistent) (cancel)");
        setRequiredArguments(1,3);
    }

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        tab.addWithPrefix("profile:", TagsProfileScriptContainer.getContainers().keySet());
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if(arg.matchesPrefix("target", "targets", "t")){
                if(arg.matchesArgumentType(PlayerTag.class))
                    scriptEntry.addObject("targets", new ListTag(arg.asType(PlayerTag.class)));
                else if(arg.matchesArgumentList(PlayerTag.class))
                    scriptEntry.addObject("targets", arg.asType(ListTag.class));
            } else if(arg.matchesPrefix("profile"))
                scriptEntry.addObject("profile", arg.asElement());
            else if(arg.matches("cancel"))
                scriptEntry.addObject("cancel", true);
            else if(arg.matches("persistent"))
                scriptEntry.addObject("persistent", true);
        }
        if(Utilities.entryHasPlayer(scriptEntry)) scriptEntry.defaultObject("targets", new ListTag(Utilities.getEntryPlayer(scriptEntry)));
        if(!scriptEntry.hasObject("targets")) throw new InvalidArgumentsException("Missing target argument.");
        if(scriptEntry.hasObject("persistent") && !scriptEntry.hasObject("profile")) throw new InvalidArgumentsException("persistent must be used with a valid profile.");
        if(!scriptEntry.hasObject("cancel")) {
            if(!scriptEntry.hasObject("profile")) throw new InvalidArgumentsException("Missing profile argument.");
            else if(!TagsProfileScriptContainer.getContainers().containsKey(scriptEntry.getElement("profile").asString().toUpperCase()))
                throw new InvalidArgumentsException("Profile '" + scriptEntry.getElement("profile").asString().toUpperCase() + "' not found.");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ListTag targets = scriptEntry.getObjectTag("targets");
        if(scriptEntry.hasObject("cancel")) {
            for (ObjectTag o : targets.objectForms) {
                PlayerTag t = (PlayerTag) o;
                Main.playerHelper.removeActiveProfile(t.getPlayerEntity());
            }
        } else {
            ElementTag p = scriptEntry.getObjectTag("profile");
            TagsProfile profile = TagsProfileScriptContainer.getContainers().get(p.asString().toUpperCase()).getProfile();
            for (ObjectTag o : targets.objectForms) {
                PlayerTag t = (PlayerTag) o;
                Main.playerHelper.setActiveProfile(t.getPlayerEntity(), profile, scriptEntry.hasObject("persistent"));
            }
        }
        scriptEntry.setFinished(true);
    }

}
