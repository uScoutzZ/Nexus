package de.uscoutz.nexus.networking.packet.packets.coop;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.coop.CoopInvitation;
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
        nexusPlayer.getPlayer().sendMessage(NexusPlugin.getInstance().getLocaleManager().translate(
                "de_DE", "command_coop_request-received", sender));

        if(nexusPlayer.isBedrockUser()) {
            nexusPlayer.getPlayer().sendMessage(NexusPlugin.getInstance().getLocaleManager().translate(
                    "de_DE", "command_coop_deny-or-accept"));
        } else {
            ComponentBuilder message = new ComponentBuilder(NexusPlugin.getInstance().getLocaleManager().translate(
                    "de_DE", "questions_click"));
            message.append(NexusPlugin.getInstance().getLocaleManager().translate("de_DE", "clickevent_accept"));
            message.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/coop accept " + profileId + " " + sender));
            message.append(" " + NexusPlugin.getInstance().getLocaleManager().translate("de_DE", "clickevent_deny") + " ");
            message.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/coop deny " + profileId + " " + sender));
            nexusPlayer.getPlayer().spigot().sendMessage(message.create());
        }

        NexusPlugin.getInstance().getDatabaseAdapter().set("coopInvitations", profileId, sender, nexusPlayer.getPlayer().getUniqueId());
        nexusPlayer.getCoopInvitations().add(new CoopInvitation(sender, nexusPlayer.getPlayer().getUniqueId(), profileId));

        return this;
    }
}
