package github.moriyoshi.comminiplugin.lib;

import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * ファイルを読み込むabstract class
 *
 * <p>{@link JsonAPI} {@link YmlAPI} {@link ConfigAPI}
 *
 * @param <T> {@link JsonObject} や {@link FileConfiguration} などのファイルを読み込んだクラス
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public abstract class FileAPI<T> {

  public final File file;
  public final String name, path, paths;
  protected final PrefixUtil message;
  protected final InterfaceAPIPlugin plugin;

  /**
   * 引数のプラグインのフォルダーの第一階層からnameのファイルを読み込みます
   *
   * @param plugin 読み込みたいフォルダーのプラグイン
   * @param name 読み込むファイルの名前
   */
  public FileAPI(InterfaceAPIPlugin plugin, String name) {
    this(plugin, "", name);
  }

  /**
   * 引数のプラグインのフォルダーの #path #name のファイルを読み込みます <br>
   * 例 [plugin=TEST] [path=first/second] [name=fileName] <br>
   * -> TEST/first/second/fileName のファイルを読み込みます
   *
   * @param plugin 読み込みたいフォルダーのプラグイン
   * @param name 読み込むファイルの名前
   * @param path ファイル階層
   */
  public FileAPI(InterfaceAPIPlugin plugin, String path, String name) {
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

  /** ファイルからデータの読み込み */
  public abstract void loadData();

  /** データをファイルに保存 */
  public abstract void saveFile();

  /** ファイル削除 */
  public final void removeFile() {
    if (file.delete()) {
      message.logInfo(paths + "を削除しました");
    }
  }

  /** ファイルの作成 */
  public void createFile() {
    try {
      file.createNewFile();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /** ファイルの読み込み */
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

  /**
   * Generates UUIDv3 (MD5) and UUIDv5 (SHA1).
   *
   * <p>It is fully compliant with RFC-4122.
   */
  public static class HashUUID {

    public static final UUID NAMESPACE_DNS = new UUID(0x6ba7b8109dad11d1L, 0x80b400c04fd430c8L);
    public static final UUID NAMESPACE_URL = new UUID(0x6ba7b8119dad11d1L, 0x80b400c04fd430c8L);
    public static final UUID NAMESPACE_OID = new UUID(0x6ba7b8129dad11d1L, 0x80b400c04fd430c8L);
    public static final UUID NAMESPACE_X500 = new UUID(0x6ba7b8149dad11d1L, 0x80b400c04fd430c8L);
    private static final int V3 = 3; // MD5
    private static final int V5 = 5; // SHA-1
    private static final String HASH_V3 = "MD5";
    private static final String HASH_V5 = "SHA-1";

    public static UUID v3(String name) {
      return generate(V3, HASH_V3, null, name);
    }

    public static UUID v5(String name) {
      return generate(V5, HASH_V5, null, name);
    }

    public static UUID v3(UUID namespace, String name) {
      return generate(V3, HASH_V3, namespace, name);
    }

    public static UUID v5(UUID namespace, String name) {
      return generate(V5, HASH_V5, namespace, name);
    }

    private static UUID generate(int version, String algorithm, UUID namespace, String name) {

      MessageDigest hasher = hasher(algorithm);

      if (namespace != null) {
        ByteBuffer ns = ByteBuffer.allocate(16);
        ns.putLong(namespace.getMostSignificantBits());
        ns.putLong(namespace.getLeastSignificantBits());
        hasher.update(ns.array());
      }

      hasher.update(name.getBytes(StandardCharsets.UTF_8));
      ByteBuffer hash = ByteBuffer.wrap(hasher.digest());

      final long msb = (hash.getLong() & 0xffffffffffff0fffL) | (version & 0x0f) << 12;
      final long lsb = (hash.getLong() & 0x3fffffffffffffffL) | 0x8000000000000000L;

      return new UUID(msb, lsb);
    }

    private static MessageDigest hasher(String algorithm) {
      try {
        return MessageDigest.getInstance(algorithm);
      } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException(String.format("%s not supported.", algorithm));
      }
    }
  }
}
