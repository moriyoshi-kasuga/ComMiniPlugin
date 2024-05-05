package github.moriyoshi.comminiplugin.api;

import org.bukkit.plugin.Plugin;

public abstract class ConfigAPI extends YmlAPI {

  public ConfigAPI(Plugin plugin, String name) {
    super(plugin, name);
  }

  public ConfigAPI(Plugin plugin, String path, String name) {
    super(plugin, path, name);
  }

  /**
   * ファイルの作成
   */
  @Override
  public void createFile() {
    plugin.saveResource(paths, false);
  }

}
