package github.moriyoshi.comminiplugin.api;

import java.io.IOException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

/**
 * .yml ファイルを読み込む {@link FileAPI}
 */
public class YmlAPI extends FileAPI<FileConfiguration> {

  /**
   * 引数のプラグインのフォルダーの第一階層からnameのファイルを読み込みます
   *
   * @param plugin 読み込みたいフォルダーのプラグイン
   * @param name   読み込むファイルの名前
   */
  public YmlAPI(Plugin plugin, String name) {
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
  public YmlAPI(Plugin plugin, String name, String path) {
    super(plugin, name, path);
  }

  /**
   * @return ファイルの拡張子
   */
  @Override
  protected String getExtension() {
    return ".yml";
  }

  /**
   * ファイルからデータの読み込み
   */
  @Override
  public void loadData() {
    data = YamlConfiguration.loadConfiguration(file);
  }

  /**
   * データをファイルに保存
   */
  @Override
  public void saveFile() {
    try {
      data.save(file);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
