/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.tagseditor;

import me.aj4real.tagseditor.network.Client;

import java.util.List;

public class TagsProfile {
    private final Provider provider;
    private final String name;
    private final TagsSerializer serializer;
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
    public void apply(Client client) {
        TagsPacket packet = new TagsPacket();
        serializer.apply(packet);
        client.sendPacket(packet.build());
    }
    public interface Provider {
        String getName();
        List<TagsProfile> getAllProfiles();
        TagsProfile getProfile(String name);
    }
}
