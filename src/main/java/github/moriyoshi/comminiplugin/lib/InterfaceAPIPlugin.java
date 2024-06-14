package github.moriyoshi.comminiplugin.lib;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public interface InterfaceAPIPlugin extends Plugin {

  PrefixUtil getMainPrefix();

  PrefixUtil getSystemPrefix();

  /**
   * {@link Listener} を登録するメゾット
   *
   * @param listener instance
   */
  default void registerEvent(final Listener listener) {
    Bukkit.getServer().getPluginManager().registerEvents(listener, this);
  }
}
