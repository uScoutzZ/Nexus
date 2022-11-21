package de.uscoutz.nexus.networking.packet.packets.coop;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.networking.packet.Packet;
import de.uscoutz.nexus.player.NexusPlayer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import java.util.*;

public class PacketCoopInvite extends Packet {

    private UUID player, profileId;
    private String sender;

    public PacketCoopInvite(String password, UUID player, UUID profileId, String sender) {
        super(password);
        this.player = player;
        this.profileId = profileId;
        this.sender = sender;
    }

    @Override
    public Object execute() {
        NexusPlayer nexusPlayer = NexusPlugin.getInstance().getPlayerManager().getPlayersMap().get(player);
        ComponentBuilder message = new ComponentBuilder(NexusPlugin.getInstance().getLocaleManager().translate(
                "de_DE", "command_coop_request-received", sender));
        message.append(" " + NexusPlugin.getInstance().getLocaleManager().translate("de_DE", "clickevent_accept"));
        message.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/coop accept " + profileId + " " + sender));
        message.append(" " + NexusPlugin.getInstance().getLocaleManager().translate("de_DE", "clickevent_deny") + " ");
        message.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/coop deny " + profileId + " " + sender));

        nexusPlayer.getPlayer().spigot().sendMessage(message.create());
        if(NexusPlugin.getInstance().getProfileManager().getCoopInvitations().containsKey(player)) {
            NexusPlugin.getInstance().getProfileManager().getCoopInvitations().get(player).add(profileId);
        } else {
            List<UUID> list = new ArrayList<>();
            list.add(profileId);
            NexusPlugin.getInstance().getProfileManager().getCoopInvitations().put(player, list);
        }

        return this;
    }
}
