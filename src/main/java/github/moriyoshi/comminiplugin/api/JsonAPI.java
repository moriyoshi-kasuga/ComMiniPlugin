package github.moriyoshi.comminiplugin.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import github.moriyoshi.comminiplugin.ComMiniPlugin;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Objects;
import org.bukkit.plugin.Plugin;
import org.codehaus.plexus.util.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * .json ファイルを読み込む {@link FileAPI}
 */
public class JsonAPI extends FileAPI<JsonObject> {

  /**
   * 引数のプラグインのフォルダーの第一階層からnameのファイルを読み込みます
   *
   * @param plugin 読み込みたいフォルダーのプラグイン
   * @param name   読み込むファイルの名前
   */
  public JsonAPI(Plugin plugin, String name) {
    super(plugin, name);
  }

  /**
   * 引数のプラグインのフォルダーの path name
   * </br>
   * のファイルを読み込みます 例 [plugin=TEST] [path=first/second]
   * </br>
   * [path=fileName] -> TEST/first/second/fileName.json のファイルを読み込みます
   *
   * @param plugin 読み込みたいフォルダーのプラグイン
   * @param name   読み込むファイルの名前
   * @param path   ファイル階層
   */
  public JsonAPI(Plugin plugin, String name, String path) {
    super(plugin, name, path);
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
      if (FileUtils.fileRead(file).trim().isEmpty()) {
        ComMiniPlugin.gson.toJson("{}", new FileWriter(file));
      } else {
        data = ComMiniPlugin.gson.fromJson(new JsonReader(fileReader),
            JsonObject.class
        );
      }
    } catch (IOException ignored) {
    } finally {
      if (data == null) {
        data = new JsonObject();
      }
    }
  }

  /**
   * データをファイルに保存
   */
  @Override
  public void saveFile() {
    try (Writer writer = new FileWriter(file)) {
      ComMiniPlugin.gson.toJson(getSavaData(), writer);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * ファイルにセーブするJsonElementを変更できるように
   *
   * @return セーブするJsonElement
   */
  public JsonElement getSavaData() {
    return data;
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
}
