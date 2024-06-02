package github.moriyoshi.comminiplugin.api;

import java.io.IOException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

/** .yml ファイルを読み込む {@link FileAPI} */
public abstract class YmlAPI extends FileAPI<FileConfiguration> {

  public YmlAPI(Plugin plugin, String name) {
    super(plugin, name);
  }

  public YmlAPI(Plugin plugin, String path, String name) {
    super(plugin, path, name);
  }

  /**
   * @return ファイルの拡張子
   */
  @Override
  protected String getExtension() {
    return ".yml";
  }

  /** ファイルからデータの読み込み */
  @Override
  public void loadData() {
    generateLoadData(YamlConfiguration.loadConfiguration(file));
  }

  /** データをファイルに保存 */
  @Override
  public void saveFile() {
    try {
      generateSaveData().save(file);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
