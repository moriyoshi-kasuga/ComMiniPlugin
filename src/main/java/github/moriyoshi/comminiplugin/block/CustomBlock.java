package github.moriyoshi.comminiplugin.block;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;

import com.google.gson.JsonObject;

import github.moriyoshi.comminiplugin.constant.ComMiniPrefix;
import github.moriyoshi.comminiplugin.util.ReflectionUtil;
import lombok.Getter;

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

  public static void registers(final Reflections reflections) {
    ReflectionUtil.forEachAllClass(reflections, CustomBlock.class, block -> {
      ComMiniPrefix.SYSTEM.logDebug("<aqua>REGISTER BLOCK " + block.getSimpleName());
      customBlocks.put(block.getSimpleName(), block);
    });
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
   * {@link #getCustomBlock(Location)} を {@link Block} でも取得できるように
   *
   * @param block 対象の場所
   * @return 取得したCustomBlock
   */
  @Nullable
  public static CustomBlock getCustomBlock(Block block) {
    return getCustomBlock(block.getLocation());
  }

  /**
   * locationから存在する場合はCustomBlockを返します
   *
   * @param location 取得する場所
   * @return 取得したCustomBlock
   */
  @Nullable
  public static CustomBlock getCustomBlock(Location location) {
    return blocks.get(location.toBlockLocation());
  }

  static Map<Location, CustomBlock> getBlocks() {
    return blocks;
  }

  static void loadCustomBlock(String identifier, Location location, JsonObject object) {
    if (!isRegister(identifier)) {
      throw new IllegalArgumentException(identifier +
          "のCustomBlockは登録されていません");
    }
    try {
      CustomBlock.customBlocks.get(identifier)
          .getDeclaredConstructor(Block.class, JsonObject.class)
          .newInstance(location.getBlock(), object);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  @Getter
  protected final Block block;

  /**
   * カスタムブロックの初期化コンストラクタ
   *
   * @param location カスタムブロックの位置
   */
  public CustomBlock(Block block) {
    this.block = block;
    if (blocks.containsKey(this.block.getLocation())) {
      throw new RuntimeException("このBlockはすでにCustomBlockです");
    }
    blocks.put(this.block.getLocation(), this);
  }

  /**
   * ここで鯖起動もしくは再起動時に独自の値を持たせている場合に{@link
   * #getBlockData()} 保存したものを読み込むためのコンストラクタです
   *
   * @param location カスタムブロックの位置
   * @param data     ロードするデータ
   */
  public CustomBlock(Block block, JsonObject data) {
    this(block);
  }

  /**
   * これはブロックをプレイヤーが設置したという風に処理したい場合に使用します
   *
   * @param location カスタムブロックの位置
   * @param player   設置したプレイヤー
   */
  public CustomBlock(Block block, Player player) {
    this(block);
  }

  /**
   * {@link #remove(Location)} をinstance methodから呼び出せるように
   */
  public final void remove() {
    Location loc = block.getLocation();
    if (isCustomBlock(loc)) {
      getCustomBlock(loc).clearData();
      blocks.remove(loc);
    }
  }

  /**
   * block を削除される前の処理 (セーブされるまえも)
   */
  public abstract void clearData();

  /**
   * 何かブロックにデータを保存させたい場合はここにデータを保存してください
   *
   * @return data
   */
  public JsonObject getBlockData() {
    return new JsonObject();
  }

  /**
   * default cancelled
   * please {@code e.setCancelled(false);}
   *
   * @param e event
   */
  public void interact(PlayerInteractEvent e) {
  }

  /**
   * default cancelled
   * please {@code e.setCancelled(false);}
   *
   * @param e event
   */
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
