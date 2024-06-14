package github.moriyoshi.comminiplugin.lib;

public abstract class ConfigAPI extends YmlAPI {

  public ConfigAPI(InterfaceAPIPlugin plugin, String name) {
    super(plugin, name);
  }

  public ConfigAPI(InterfaceAPIPlugin plugin, String path, String name) {
    super(plugin, path, name);
  }

  /** ファイルの作成 */
  @Override
  public void createFile() {
    plugin.saveResource(paths, false);
  }

}
