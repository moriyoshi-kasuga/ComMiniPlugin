package github.moriyoshi.comminiplugin.api;

import com.google.gson.JsonObject;
import github.moriyoshi.comminiplugin.util.PrefixUtil;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

/**
 * ファイルを読み込むabstract class
 * <p>
 * {@link JsonAPI} {@link YmlAPI} {@link ConfigAPI}
 *
 * @param <T> {@link JsonObject} や {@link FileConfiguration} などのファイルを読み込んだクラス
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public abstract class FileAPI<T> {

  public final File file;
  public final String name, path, paths;
  protected final PrefixUtil message;
  protected final Plugin plugin;

  /**
   * 引数のプラグインのフォルダーの第一階層からnameのファイルを読み込みます
   *
   * @param plugin 読み込みたいフォルダーのプラグイン
   * @param name   読み込むファイルの名前
   */
  public FileAPI(Plugin plugin, String name) {
    this(plugin, "", name);
  }

  /**
   * 引数のプラグインのフォルダーの #path #name のファイルを読み込みます <br>
   * 例 [plugin=TEST] [path=first/second]
   * [name=fileName] <br>
   * -> TEST/first/second/fileName のファイルを読み込みます
   *
   * @param plugin 読み込みたいフォルダーのプラグイン
   * @param name   読み込むファイルの名前
   * @param path   ファイル階層
   */
  public FileAPI(Plugin plugin, String path, String name) {
    Objects.requireNonNull(name);
    this.name = name.endsWith(getExtension()) ? name : name + getExtension();
    this.path = path == null || path.isEmpty() ? "" : File.separator + path;
    this.paths = path == null || path.isEmpty() ? name : path + File.separator + name;
    this.plugin = plugin;
    this.message = new PrefixUtil("<gray>[<red>" + plugin.getName() + "<gray>] ");
    file = new File(plugin.getDataFolder() + this.path, this.name);
    loadFile();
  }

  /**
   * ファイルやフォルダーを削除する
   *
   * @param folder 削除する対象
   */
  public static void folderClear(File folder) {
    if (folder.exists()) {
      if (folder.isFile()) {
        folder.delete();
      } else if (folder.isDirectory()) {
        File[] files = folder.listFiles();
        if (files != null) {
          for (File file : files) {
            folderClear(file);
          }
        }
        folder.delete();
      }
    }
  }

  /**
   * @return ファイルの拡張子
   */
  protected abstract String getExtension();

  /**
   * ファイルからデータの読み込み
   */
  public abstract void loadData();

  /**
   * データをファイルに保存
   */
  public abstract void saveFile();

  /**
   * ファイル削除
   */
  public final void removeFile() {
    if (file.delete()) {
      message.logInfo(paths + "を削除しました");
    }
  }

  /**
   * ファイルの作成
   */
  public void createFile() {
    try {
      file.createNewFile();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * ファイルの読み込み
   */
  public final void loadFile() {
    File directory = new File(plugin.getDataFolder() + path);
    if (!directory.exists()) {
      directory.mkdirs();
    }
    if (!file.exists()) {
      createFile();
    }
    loadData();
  }

  protected abstract T generateSaveData();

  protected abstract void generateLoadData(T data);

}
