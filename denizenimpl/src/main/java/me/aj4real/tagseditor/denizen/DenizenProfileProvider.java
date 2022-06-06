/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.tagseditor.denizen;

import me.aj4real.tagseditor.TagsProfile;

import java.util.List;
import java.util.stream.Collectors;

public class DenizenProfileProvider implements TagsProfile.Provider {

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }
    @Override
    public List<TagsProfile> getAllProfiles() {
        return TagsProfileScriptContainer.containers.values().stream().map((v) -> v.getProfile()).collect(Collectors.toList());
    }

    @Override
    public TagsProfile getProfile(String name) {
        return TagsProfileScriptContainer.containers.get(name).getProfile();
    }
}
