package github.moriyoshi.comminiplugin.block;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;

import com.google.gson.JsonObject;

import javassist.Modifier;
import lombok.Getter;
import lombok.val;

/**
 * 左クリック、右クリックで処理を受け取るカスタムブロック {@link
 * #clearData(Location)} を自分で拡張して {@link #remove()}
 * でブロックを削除できます
 * <p>
 * サーバー起動時に登録する場合は {@link
 * RMPlugin#registerBlock(String, Class)} で登録してください
 * サーバーが起動中に登録する場合は
 * {@link #register(String, Class)} で登録してください
 */
public abstract class CustomBlock {

  private static final TreeMap<String, Class<? extends CustomBlock>> customBlocks = new TreeMap<>();
  private static final Map<Location, CustomBlock> blocks = new HashMap<>();

  /**
   * この位置のブロックを削除します
   *
   * @param location 削除する位置
   */
  public static void remove(Location location) {
    Location block = location.toBlockLocation();
    if (isCustomBlock(block)) {
      getCustomBlock(block).clearData(location);
      blocks.remove(block);
    }
  }

  /**
   * {@link #remove(Location)} をブロックでできるように
   *
   * @param block block
   */
  public static void remove(Block block) {
    remove(block.getLocation());
  }

  public static void registers(final String packageName) {
    val reflections = new Reflections(packageName);
    for (Class<? extends CustomBlock> block : reflections.getSubTypesOf(CustomBlock.class)) {
      if (Modifier.isAbstract(block.getModifiers())) {
        return;
      }
      customBlocks.put(block.getSimpleName(), block);
    }
  }

  /**
   * この識別子でカスタムブロックが登録されているかどうかを確認します
   *
   * @param identifier カスタムブロックの識別子
   * @return trueの場合すでに登録されています
   */
  public static boolean isRegister(String identifier) {
    return customBlocks.containsKey(identifier);
  }

  /**
   * このLocationにカスタムブロックが存在するかどうか
   *
   * @param location カスタムブロックの場所
   * @return trueの場合存在します
   */
  public static boolean isCustomBlock(Location location) {
    return blocks.containsKey(location.toBlockLocation());
  }

  /**
   * このBlockにカスタムブロックが存在するかどうか
   *
   * @param block block
   * @return trueの場合存在します
   */
  public static boolean isCustomBlock(Block block) {
    return isCustomBlock(block.getLocation());
  }

  /**
   * blockにあるカスタムブロックの識別子を取得します
   *
   * @param block 取得するカスタムブロックのブロック
   * @return カスタムブロックの識別子
   */
  @NotNull
  public static String getIdentifier(Block block) {
    return getIdentifier(block.getLocation());
  }

  /**
   * locationにあるカスタムブロックの識別子を取得します
   *
   * @param location 取得するカスタムブロックの場所
   * @return カスタムブロックの識別子
   */
  @NotNull
  public static String getIdentifier(Location location) {
    Location bLoc = location.toBlockLocation();
    if (isCustomBlock(bLoc)) {
      return getCustomBlock(bLoc).getIdentifier();
    }
    throw new IllegalArgumentException(
        "そのBlockはCustomBlockではありません(もしくは登録されていません)");
  }

  /**
   * {@link #getCustomBlock(Location)} を {@link Block} でも取得できるように
   *
   * @param block 対象の場所
   * @return 取得したCustomBlock
   * @throws IllegalArgumentException
   *                                  そのlocationにCustomBlockがない場合はなります
   */
  @NotNull
  public static CustomBlock getCustomBlock(Block block) {
    return getCustomBlock(block.getLocation());
  }

  /**
   * locationから存在する場合はCustomBlockを返します
   *
   * @param location 取得する場所
   * @return 取得したCustomBlock
   * @throws IllegalArgumentException
   *                                  そのlocationにCustomBlockがない場合はなります
   */
  @NotNull
  public static CustomBlock getCustomBlock(Location location) {
    var block = location.toBlockLocation();
    if (isCustomBlock(block)) {
      return blocks.get(block);
    }
    throw new IllegalArgumentException("そのBlockはCustomBlockではありません");
  }

  static Map<Location, CustomBlock> getBlocks() {
    return blocks;
  }

  static void loadCustomBlock(String identifier, Location location,
      JsonObject object) {
    if (!isRegister(identifier)) {
      throw new IllegalArgumentException(identifier +
          "のCustomBlockは登録されていません");
    }
    try {
      CustomBlock.customBlocks.get(identifier)
          .getDeclaredConstructor(Location.class, JsonObject.class)
          .newInstance(location, object);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  @Getter
  private final Location location;

  /**
   * カスタムブロックの初期化コンストラクタ
   *
   * @param location カスタムブロックの位置
   */
  public CustomBlock(Location location) {
    this.location = location.toBlockLocation();
    if (blocks.containsKey(this.location)) {
      throw new RuntimeException("このBlockはすでにCustomBlockです");
    }
    blocks.put(this.location, this);
  }

  /**
   * ここで鯖起動もしくは再起動時に独自の値を持たせている場合に{@link
   * #getBlockData()} 保存したものを読み込むためのコンストラクタです
   *
   * @param location カスタムブロックの位置
   * @param data     ロードするデータ
   */
  public CustomBlock(Location location, JsonObject data) {
    this(location);
  }

  /**
   * これはブロックをプレイヤーが設置したという風に処理したい場合に使用します
   *
   * @param location カスタムブロックの位置
   * @param player   設置したプレイヤー
   */
  public CustomBlock(Location location, Player player) {
    this(location);
  }

  /**
   * {@link #remove(Location)} をinstance methodから呼び出せるように
   */
  public final void remove() {
    remove(getLocation());
  }

  /**
   * {@link CustomModelBlock#clearData(Location)}
   * のようにブロックを削除する場合の処理
   *
   * @param Location このブロックのlocation
   */
  public abstract void clearData(Location Location);

  /**
   * 何かブロックにデータを保存させたい場合はここにデータを保存してください
   *
   * @return data
   */
  public JsonObject getBlockData() {
    return new JsonObject();
  }

  public void interact(PlayerInteractEvent e) {
  }

  public void blockBreak(BlockBreakEvent e) {
  }

  /**
   * ブロックの識別子を取得します
   *
   * @return 識別子
   */
  public @NotNull String getIdentifier() {
    return getClass().getSimpleName();
  }

}
