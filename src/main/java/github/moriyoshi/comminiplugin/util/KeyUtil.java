package github.moriyoshi.comminiplugin.util;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import java.util.UUID;
import org.bukkit.NamespacedKey;

public class KeyUtil {

  public static NamespacedKey createKey(String name) {
    return NamespacedKey.fromString(name, ComMiniPlugin.getPlugin());
  }

  public static NamespacedKey createUUIDKey() {
    return NamespacedKey.fromString(UUID.randomUUID().toString(), ComMiniPlugin.getPlugin());
  }
}
