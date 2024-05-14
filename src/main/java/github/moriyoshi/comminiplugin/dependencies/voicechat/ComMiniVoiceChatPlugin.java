package github.moriyoshi.comminiplugin.dependencies.voicechat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.Group.Type;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import lombok.Getter;

public class ComMiniVoiceChatPlugin implements VoicechatPlugin {

  @Getter
  private static VoicechatServerApi api;

  public static Group getOrCreateGroup(String groupName) {
    return getOrCreateGroup(groupName, Type.NORMAL);
  }

  public static Group getOrCreateGroup(String groupName, Type type) {
    var group = api.getGroups().stream().filter(g -> g.getName().equals(groupName));
    return group.findFirst()
        .orElseGet(
            () -> api.groupBuilder().setName(groupName).setHidden(true).setPersistent(false).setType(type).build());
  }

  @Override
  public String getPluginId() {
    return "ComMiniVoiceChatPlugin";
  }

  @Override
  public void registerEvents(EventRegistration registration) {
    registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophone);
    registration.registerEvent(VoicechatServerStartedEvent.class, this::onServerStarted);
  }

  private void onServerStarted(VoicechatServerStartedEvent event) {
    api = event.getVoicechat();
  }

  private void onMicrophone(MicrophonePacketEvent event) {
    // The connection might be null if the event is caused by other means
    if (event.getSenderConnection() == null) {
      return;
    }
    // Cast the generic player object of the voice chat API to an actual bukkit
    // player
    // This object should always be a bukkit player object on bukkit based servers
    if (!(event.getSenderConnection().getPlayer().getPlayer() instanceof Player player)) {
      return;
    }

    // Check if the player has the broadcast permission
    if (!player.isOp()) {
      return;
    }

    Group group = event.getSenderConnection().getGroup();

    // Check if the player sending the audio is actually in a group
    if (group == null) {
      return;
    }

    // Only broadcast the voice when the group name is "broadcast"
    if (!group.getName().strip().equalsIgnoreCase("broadcast")) {
      return;
    }

    // Cancel the actual microphone packet event that people in that group or close
    // by don't hear the broadcaster twice
    event.cancel();

    VoicechatServerApi api = event.getVoicechat();

    // Iterating over every player on the server
    for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
      // Don't send the audio to the player that is broadcasting
      if (onlinePlayer.getUniqueId().equals(player.getUniqueId())) {
        continue;
      }
      VoicechatConnection connection = api.getConnectionOf(onlinePlayer.getUniqueId());
      // Check if the player is actually connected to the voice chat
      if (connection == null) {
        continue;
      }
      // Send a static audio packet of the microphone data to the connection of each
      // player
      api.sendStaticSoundPacketTo(connection, event.getPacket().staticSoundPacketBuilder().build());
    }
  }
}
