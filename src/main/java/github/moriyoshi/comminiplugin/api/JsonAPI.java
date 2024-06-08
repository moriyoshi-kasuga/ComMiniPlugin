package github.moriyoshi.comminiplugin.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import github.moriyoshi.comminiplugin.ComMiniPlugin;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import lombok.val;
import org.bukkit.plugin.Plugin;

/** .json ファイルを読み込む {@link FileAPI} */
public abstract class JsonAPI extends FileAPI<JsonElement> {

  public JsonAPI(Plugin plugin, String name) {
    super(plugin, name);
  }

  public JsonAPI(Plugin plugin, String path, String name) {
    super(plugin, path, name);
  }

  private boolean correct = false;

  /**
   * @return ファイルの拡張子
   */
  @Override
  protected String getExtension() {
    return ".json";
  }

  /** ファイルからデータの読み込み */
  @Override
  public void loadData() {
    try {
      FileReader fileReader = new FileReader(file);
      if (file.length() == 0) {
        ComMiniPlugin.gson.toJson("{}", new FileWriter(file));
        generateLoadData(new JsonObject());
      } else {
        generateLoadData(ComMiniPlugin.gson.fromJson(new JsonReader(fileReader), JsonObject.class));
      }
    } catch (IOException ignored) {
      generateLoadData(new JsonObject());
    }
    correct = true;
  }

  /** データをファイルに保存 */
  @Override
  public void saveFile() {
    if (!correct) {
      return;
    }
    try (Writer writer = new FileWriter(file)) {
      val obj = generateSaveData();
      ComMiniPlugin.gson.toJson(obj, writer);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /** ファイルの作成 */
  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Override
  public void createFile() {
    try {
      file.createNewFile();
      ComMiniPlugin.gson.toJson("{}", new FileWriter(file));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected abstract void generateLoadData(JsonElement dataElement);
}
