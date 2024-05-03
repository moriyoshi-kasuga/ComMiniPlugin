package github.moriyoshi.comminiplugin.api;

import org.bukkit.plugin.Plugin;

public class ConfigAPI extends YmlAPI {

  /**
   * 引数のプラグインのフォルダーの第一階層からnameのファイルを読み込みます
   *
   * @param plugin 読み込みたいフォルダーのプラグイン
   * @param name   読み込むファイルの名前
   */
  public ConfigAPI(Plugin plugin, String name) {
    super(plugin, name);
  }

  /**
   * 引数のプラグインのフォルダーの {@link #path} {@link #name} のファイルを読み込みます 例 [plugin=TEST] [path=first/second]
   * [path=fileName] -> TEST/first/second/fileName.yml のファイルを読み込みます
   *
   * @param plugin 読み込みたいフォルダーのプラグイン
   * @param name   読み込むファイルの名前
   * @param path   ファイル階層
   */
  public ConfigAPI(Plugin plugin, String name, String path) {
    super(plugin, name, path);
  }

  /**
   * ファイルの作成
   */
  @Override
  public void createFile() {
    plugin.saveResource(paths, false);
  }

}
