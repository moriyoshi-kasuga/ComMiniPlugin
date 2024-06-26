package github.moriyoshi.comminiplugin.lib.block;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import github.moriyoshi.comminiplugin.lib.PluginLib;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;

/**
 * 左クリック、右クリックで処理を受け取るカスタムブロック {@link #clearData()} を自分で拡張して {@link #remove()} でブロックを削除できます
 *
 * <p>登録する場合は {@link #registers(Reflections)} で登録してください
 */
@Getter
public abstract class CustomBlock {

  private static final TreeMap<String, Class<? extends CustomBlock>> customBlocks = new TreeMap<>();
  private static final Map<Location, CustomBlock> blocks = new HashMap<>();
  protected final Block block;

  /**
   * カスタムブロックの初期化コンストラクタ
   *
   * @param block カスタムブロックの位置
   */
  public CustomBlock(Block block) {
    if (blocks.containsKey(block.getLocation())) {
      throw new RuntimeException("このBlockはすでにCustomBlockです (" + block.getLocation() + ")");
    }
    block.setType(getOriginMaterial());
    blocks.put(block.getLocation(), this);
    this.block = block;
  }

  /**
   * ここで鯖起動もしくは再起動時に独自の値を持たせている場合に{@link #getBlockData()} 保存したものを読み込むためのコンストラクタです
   *
   * @param block カスタムブロックのブロック
   * @param dataElement ロードするデータ
   */
  public CustomBlock(Block block, JsonElement dataElement) {
    this(block);
  }

  /**
   * これはブロックをプレイヤーが設置したという風に処理したい場合に使用します
   *
   * @param block カスタムブロックの位置
   * @param player 設置したプレイヤー
   */
  public CustomBlock(Block block, Player player) {
    this(block);
  }

  /**
   * ブロックの向きを変えたい場合に使用します
   *
   * @param block block
   * @param face face
   */
  public CustomBlock(Block block, BlockFace face) {
    this(block);
    setFace(face);
  }

  public static void registers(final Reflections reflections) {
    reflections
        .getSubTypesOf(CustomBlock.class)
        .forEach(
            block -> {
              if (Modifier.isAbstract(block.getModifiers())) {
                return;
              }
              val id = block.getSimpleName();
              if (CustomBlock.customBlocks.containsKey(id)) {
                throw new IllegalArgumentException(
                    id
                        + "のカスタムアイテムがかぶっています、"
                        + block.getName()
                        + " >>==<< "
                        + CustomBlock.customBlocks.get(id).getName());
              }
              PluginLib.getPlugin().getSystemPrefix().logDebug("<aqua>REGISTER BLOCK " + id);
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
    return blocks.containsKey(toBlockLocation(location));
  }

  public static boolean isCustomBlock(Location location, Class<? extends CustomBlock> clazz) {
    val temp = blocks.get(toBlockLocation(location));
    if (temp == null) {
      return false;
    }
    return temp.getClass().isAssignableFrom(clazz);
  }

  /**
   * このBlockにカスタムブロックが存在するかどうか
   *
   * @param block block
   * @return trueの場合存在します
   */
  public static boolean isCustomBlock(Block block) {
    return blocks.containsKey(block.getLocation());
  }

  public static boolean isCustomBlock(Block block, Class<? extends CustomBlock> clazz) {
    val temp = blocks.get(block.getLocation());
    if (temp == null) {
      return false;
    }
    return temp.getClass().isAssignableFrom(clazz);
  }

  /**
   * {@link #getCustomBlock(Location)} を {@link Block} でも取得できるように
   *
   * @param block 対象の場所
   * @return 取得したCustomBlock
   */
  @Nullable
  public static CustomBlock getCustomBlock(Block block) {
    return blocks.get(block.getLocation());
  }

  @NotNull
  public static <T extends CustomBlock> T getCustomBlock(Block block, Class<T> clazz) {
    return clazz.cast(blocks.get(block.getLocation()));
  }

  /**
   * locationから存在する場合はCustomBlockを返します
   *
   * @param location 取得する場所
   * @return 取得したCustomBlock
   */
  @Nullable
  public static CustomBlock getCustomBlock(Location location) {
    return blocks.get(toBlockLocation(location));
  }

  @NotNull
  public static <T extends CustomBlock> T getCustomBlock(Location location, Class<T> clazz) {
    return clazz.cast(blocks.get(toBlockLocation(location)));
  }

  static Map<Location, CustomBlock> getBlocks() {
    return blocks;
  }

  static void loadCustomBlock(String identifier, Location location, JsonElement element) {
    try {
      CustomBlock.customBlocks
          .get(identifier)
          .getDeclaredConstructor(Block.class, JsonElement.class)
          .newInstance(location.getBlock(), element);
    } catch (InstantiationException
        | IllegalAccessException
        | InvocationTargetException
        | NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  public static Location toBlockLocation(Location location) {
    val loc = location.toBlockLocation();
    loc.setYaw(0);
    loc.setPitch(0);
    return loc;
  }

  public final void remove() {
    this.clearData();
    block.setType(Material.AIR);
    blocks.remove(block.getLocation());
  }

  /** block を削除される前の処理 (セーブされるまえも) */
  public abstract void clearData();

  /**
   * 何かブロックにデータを保存させたい場合はここにデータを保存してください (null or JsonNull を返すと保存しなくなります)
   *
   * @return data
   */
  public JsonElement getBlockData() {
    return new JsonObject();
  }

  /**
   * default cancelled please {@code e.setCancelled(false);}
   *
   * @param e event
   */
  public void interactMainHand(PlayerInteractEvent e) {}

  /**
   * default cancelled please {@code e.setCancelled(false);}
   *
   * @param e event
   */
  public void interactOffHand(PlayerInteractEvent e) {}

  /**
   * default cancelled please {@code e.setCancelled(false);}
   *
   * @param e event
   */
  public void blockBreak(BlockBreakEvent e) {}

  public void walk(PlayerMoveEvent e) {}

  /**
   * ブロックの識別子を取得します
   *
   * @return 識別子
   */
  public @NotNull String getIdentifier() {
    return getClass().getSimpleName();
  }

  protected abstract @NotNull Material getOriginMaterial();

  public boolean setFace(BlockFace face) {
    if (!(block.getBlockData() instanceof org.bukkit.block.data.Directional directional)) {
      return false;
    }
    directional.setFacing(face);
    block.setBlockData(directional);
    return true;
  }
}
