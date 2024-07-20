package github.moriyoshi.comminiplugin.system;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import github.moriyoshi.comminiplugin.ComMiniPlugin;
import org.bukkit.entity.Player;

public class ResourcePackUtil {

  public static void send(Player player) {
    ByteArrayDataOutput out = ByteStreams.newDataOutput();
    out.writeUTF("load");
    player.sendPluginMessage(
        ComMiniPlugin.getPlugin(), "velocityresourcesync:main", out.toByteArray());
  }

  public static void remove(Player player) {

    ByteArrayDataOutput out = ByteStreams.newDataOutput();
    out.writeUTF("unload");
    player.sendPluginMessage(
        ComMiniPlugin.getPlugin(), "velocityresourcesync:main", out.toByteArray());
  }
}
