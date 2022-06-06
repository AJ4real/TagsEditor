/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.tagseditor;

import me.aj4real.tagseditor.network.Client;

import java.util.List;

public class TagsProfile {
    private final Provider provider;
    private final String name;
    private TagsSerializer serializer;
    public TagsProfile(Provider provider, String name, TagsSerializer serializer) {
        this.provider = provider;
        this.name = name;
        this.serializer = serializer;
    }
    public String getName() {
        return this.name;
    }
    public Provider getProvider() {
        return this.provider;
    }
    public void patch(TagsSerializer serializer) {
        this.serializer = serializer;
    }
    public void apply(Client client) {
        TagsPacket packet = new TagsPacket();
        serializer.apply(packet);
        client.sendPacket(packet.build());
    }
    @Override
    public boolean equals(Object o) {
        if(!(o instanceof TagsProfile)) return false;
        if(!((TagsProfile) o).getName().equalsIgnoreCase(this.getName())) return false;
        //TODO
        return true;
    }
    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }
    public interface Provider {
        String getName();
        List<TagsProfile> getAllProfiles();
        TagsProfile getProfile(String name);
    }
}
