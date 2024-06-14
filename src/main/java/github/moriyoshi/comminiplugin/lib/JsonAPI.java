package github.moriyoshi.comminiplugin.lib;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

/** .json ファイルを読み込む {@link FileAPI} */
@Setter
@Getter
public abstract class JsonAPI extends FileAPI<JsonElement> {

  public JsonAPI(InterfaceAPIPlugin plugin, String name) {
    super(plugin, name);
  }

  public JsonAPI(InterfaceAPIPlugin plugin, String path, String name) {
    super(plugin, path, name);
  }

  private boolean correct;

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
    setCorrect(false);
    try {
      FileReader fileReader = new FileReader(file);
      if (file.length() == 0) {
        PluginLib.gson.toJson("{}", new FileWriter(file));
        generateLoadData(new JsonObject());
      } else {
        generateLoadData(PluginLib.gson.fromJson(new JsonReader(fileReader), JsonObject.class));
      }
    } catch (IOException ignored) {
      generateLoadData(new JsonObject());
    }
    setCorrect(true);
  }

  /** データをファイルに保存 */
  @Override
  public void saveFile() {
    if (isCorrect()) {
      try (Writer writer = new FileWriter(file)) {
        val obj = generateSaveData();
        PluginLib.gson.toJson(obj, writer);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  /** ファイルの作成 */
  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Override
  public void createFile() {
    try {
      file.createNewFile();
      PluginLib.gson.toJson("{}", new FileWriter(file));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected abstract void generateLoadData(JsonElement dataElement);
}
