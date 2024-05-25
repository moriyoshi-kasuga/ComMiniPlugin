package github.moriyoshi.comminiplugin.api;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Objects;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import lombok.val;

/**
 * .json ファイルを読み込む {@link FileAPI}
 */
public abstract class JsonAPI extends FileAPI<JsonElement> {

  public JsonAPI(Plugin plugin, String name) {
    super(plugin, name);
  }

  public JsonAPI(Plugin plugin, String path, String name) {
    super(plugin, path, name);
  }

  /**
   * obj を jsonElementに変換
   *
   * @param obj 対象
   * @return 変換後
   */
  public static JsonElement convertElement(Object obj) {
    return obj instanceof JsonElement ? ((JsonElement) obj)
        : ComMiniPlugin.gson.toJsonTree(obj);
  }

  /**
   * jsonからpathの通りにして{@link JsonObject} を取得します
   *
   * @param json 取得する対象
   * @param path ["data",1,] など
   * @return pathの通りにあった場合はその値を返しますがない場合はnullを返します
   * @see #getJsonObject(JsonObject, List, boolean)
   */
  public static @Nullable JsonObject getExistJsonObject(@NotNull JsonObject json,
      List<Object> path) {
    return getJsonObject(json, path, false);
  }

  /**
   * jsonからpathの通りにして{@link JsonObject} を取得します
   *
   * @param json 取得する対象
   * @param path ["data",1,] など
   * @return pathの通りにあった場合はその値を返しますがない場合はpathを作成して空のJsonObjectを返します
   * @see #getJsonObject(JsonObject, List, boolean)
   */
  public static @NotNull JsonObject getCreateJsonObject(@NotNull JsonObject json,
      List<Object> path) {
    return Objects.requireNonNull(getJsonObject(json, path, true));
  }

  /**
   * <p>
   * jsonからpathの通りにして{@link JsonObject} を取得します
   * </p>
   * creatableがtrueの場合に
   * <p>
   * pathがない場合は空のJsonObjectを追加します
   * </p>
   *
   * @param json 取得する対象
   * @param path ["data",1,] など
   * @return pathの通りにある値を返します
   */
  public static JsonObject getJsonObject(@NotNull JsonObject json,
      List<Object> path, boolean creatable) {
    JsonObject value = json;
    for (Object o : path) {
      String s = o.toString();
      if (!value.has(s)) {
        if (!creatable) {
          return null;
        }
        value.add(s, new JsonObject());
      }
      value = value.getAsJsonObject(s);
    }
    return value;
  }

  /**
   * @return ファイルの拡張子
   */
  @Override
  protected String getExtension() {
    return ".json";
  }

  /**
   * ファイルからデータの読み込み
   */
  @Override
  public void loadData() {
    try {
      FileReader fileReader = new FileReader(file);
      if (file.length() == 0) {
        ComMiniPlugin.gson.toJson("{}", new FileWriter(file));
        generateLoadData(new JsonObject());
      } else {
        generateLoadData(ComMiniPlugin.gson.fromJson(new JsonReader(fileReader),
            JsonObject.class));
      }
    } catch (IOException ignored) {
      generateLoadData(new JsonObject());
    }
  }

  /**
   * データをファイルに保存
   */
  @Override
  public void saveFile() {
    try (Writer writer = new FileWriter(file)) {
      val obj = generateSaveData();
      ComMiniPlugin.gson.toJson(obj, writer);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * ファイルの作成
   */
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
