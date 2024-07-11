package github.moriyoshi.comminiplugin.lib.item;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTItem;
import github.moriyoshi.comminiplugin.lib.HashUUID;
import github.moriyoshi.comminiplugin.lib.PluginLib;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.val;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;

public abstract class CustomItem implements InterfaceItem {

  public static final BiMap<String, Class<? extends CustomItem>> canShowingRegisters =
      HashBiMap.create();
  private static final BiMap<String, Class<? extends CustomItem>> registers = HashBiMap.create();
  private static final Map<String, Constructor<? extends CustomItem>> newConstructors =
      HashBiMap.create();
  @Getter @NotNull private final ItemStack item;
  private UUID uuid;

  public CustomItem(@NotNull final ItemStack item) {
    NBT.modify(
        item,
        nbt -> {
          val temp = nbt.getCompound(nbtKey);
          if (temp != null) {
            this.uuid = temp.getUUID("uuid");
            return;
          }
          val compound = nbt.getOrCreateCompound(nbtKey);
          compound.setString("identifier", getIdentifier());
          this.uuid = canStack() ? HashUUID.v5(getIdentifier()) : UUID.randomUUID();
          compound.setUUID("uuid", uuid);
          compound
              .getStringList("impl")
              .addAll(
                  Stream.of(getClass().getInterfaces())
                      .filter(clazz -> clazz.isAssignableFrom(AddHandler.class))
                      .map(Class::getSimpleName)
                      .toList());
        });
    this.item = item;
  }

  public static void registers(final Reflections reflections) {
    reflections
        .getSubTypesOf(CustomItem.class)
        .forEach(
            item -> {
              if (Modifier.isAbstract(item.getModifiers())) {
                return;
              }
              String id;
              boolean canShowing;
              Constructor<? extends CustomItem> newConstructor;
              try {
                val instance = item.getDeclaredConstructor().newInstance();
                id = instance.getIdentifier();
                canShowing = instance.canShowing();
                newConstructor = item.getDeclaredConstructor(ItemStack.class);
              } catch (InstantiationException
                  | IllegalAccessException
                  | InvocationTargetException
                  | NoSuchMethodException e) {
                throw new RuntimeException(e);
              }
              if (CustomItem.registers.containsKey(id)) {
                throw new IllegalArgumentException(
                    id
                        + "のカスタムアイテムがかぶっています、"
                        + item.getName()
                        + " >>==<< "
                        + CustomItem.registers.get(id).getName());
              }
              PluginLib.getPlugin()
                  .getSystemPrefix()
                  .logDebug(
                      "<gray>REGISTER ITEM "
                          + item.getSimpleName()
                          + " (canShowing: "
                          + canShowing
                          + ")");
              CustomItem.registers.put(id, item);
              CustomItem.newConstructors.put(id, newConstructor);
              if (canShowing) {
                CustomItem.canShowingRegisters.putIfAbsent(id, item);
              }
            });
  }

  public static CustomItem getNewCustomItem(final String identifier) {
    if (registers.containsKey(identifier)) {
      try {
        return registers.get(identifier).getDeclaredConstructor().newInstance();
      } catch (InstantiationException
          | IllegalAccessException
          | IllegalArgumentException
          | InvocationTargetException
          | NoSuchMethodException
          | SecurityException e) {
        throw new RuntimeException(e);
      }
    }
    throw new IllegalArgumentException(identifier + " のIDはカスタムアイテムに登録されていません");
  }

  public static Optional<CustomItem> getCustomItemOptional(final ItemStack item) {
    return Optional.ofNullable(getCustomItem(item));
  }

  @Nullable
  public static CustomItem getCustomItem(final ItemStack item) {
    final var ci = getIdentifier(item);
    if (ci.isPresent()) {
      try {
        return newConstructors.get(ci.get()).newInstance(item);
      } catch (InstantiationException
          | IllegalAccessException
          | IllegalArgumentException
          | InvocationTargetException
          | SecurityException e) {
        e.printStackTrace();
        return null;
      }
    }
    return null;
  }

  @Nullable
  public static <T extends AddHandler> T getCustomItem(final ItemStack item, final Class<T> clazz) {
    if (item == null || item.getType().isAir()) {
      return null;
    }
    val nbt = new NBTItem(item);
    if (!nbt.hasTag(nbtKey)) {
      return null;
    }
    val compound = nbt.getCompound(nbtKey);
    val identifier = compound.getString("identifier");
    if (!compound.getStringList("impl").contains(clazz.getSimpleName())) {
      return null;
    }
    try {
      return clazz.cast(newConstructors.get(identifier).newInstance(item));
    } catch (InstantiationException
        | IllegalAccessException
        | IllegalArgumentException
        | InvocationTargetException
        | SecurityException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static <T extends AddHandler> Optional<T> getCustomItemOptional(
      final ItemStack item, final Class<T> clazz) {
    return Optional.ofNullable(getCustomItem(item, clazz));
  }

  public static boolean hasCustomItem(Inventory inv, Class<?> clazz) {
    for (val item : inv) {
      if (equalsItem(item, clazz)) {
        return true;
      }
    }
    return false;
  }

  public static CustomItem getInventoryCustomItem(Inventory inv, UUID uuid) {
    for (val item : inv) {
      val custom = getCustomItem(item);
      if (custom != null && uuid.equals(custom.getUniqueId())) {
        return custom;
      }
    }
    return null;
  }

  public static boolean equalsItem(final ItemStack itemStack, final Class<?> clazz) {
    if (registers.containsValue(clazz)) {
      return equalsIdentifier(registers.inverse().get(clazz), itemStack);
    }
    return false;
  }

  /**
   * アイテムがその識別子なのかを判別します
   *
   * @param identifier 識別子
   * @param item 判定するアイテム
   * @return 同じ識別子の場合trueを返します
   */
  public static boolean equalsIdentifier(final String identifier, final ItemStack item) {
    final var id = getIdentifier(item);
    return id.map(s -> s.equals(identifier)).orElse(false);
  }

  /**
   * どちらのアイテムも同じ識別子かどうか判別します
   *
   * @param itemStack item1
   * @param item item2
   * @return 同じだった場合trueを返します
   */
  public static boolean equalsIdentifier(final ItemStack itemStack, final ItemStack item) {
    final var id = getIdentifier(itemStack);
    return id.filter(s -> equalsIdentifier(s, item)).isPresent();
  }

  /**
   * そのアイテムがもつ識別子が登録されているかを判別します
   *
   * @param item 対象のアイテム
   * @return 登録されいたらtrueを返します
   */
  public static boolean isCustomItem(final ItemStack item) {
    if (item == null || item.isEmpty()) {
      return false;
    }
    return NBT.get(
        item,
        readableNBT -> {
          if (!readableNBT.hasTag(nbtKey)) {
            return false;
          }
          return readableNBT.getCompound(nbtKey).hasTag("identifier");
        });
  }

  /**
   * そのアイテムから識別子を取得します
   *
   * @param item 対象のアイテム
   * @return アイテムの識別子を返します
   * @throws IllegalArgumentException 渡されたアイテムがCustomItemではない場合にthrowされます
   */
  @NotNull
  public static Optional<String> getIdentifier(final @Nullable ItemStack item) {
    if (item == null || item.getType().isAir()) {
      return Optional.empty();
    }
    return NBT.get(
        item,
        nbt -> {
          if (!nbt.hasTag(nbtKey)) {
            return Optional.empty();
          }
          return Optional.of(nbt.getCompound(nbtKey).getString("identifier"));
        });
  }

  public @NotNull UUID getUniqueId() {
    return uuid;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof final CustomItem objItem) {
      return objItem.getIdentifier().equals(getIdentifier())
          && Objects.equals(objItem.getUniqueId(), getUniqueId());
    }
    return false;
  }

  @Override
  public @NotNull String getIdentifier() {
    return getClass().getSimpleName();
  }

  public static interface AddHandler {}

  public static interface ProjectileLaunch extends AddHandler {

    /**
     * このアイテムで projectile を launch したときに呼ばれます
     *
     * @param e event
     * @param player player
     */
    default void projectileLaunch(final ProjectileLaunchEvent e, final Player player) {}
  }

  public static interface RunTick extends AddHandler {
    /**
     * プレイヤーのインベントリーにあるさいに常に更新されるアイテムです
     *
     * @param e event
     */
    default void runTick(final Player player) {}
  }

  public static interface Click extends AddHandler {
    /**
     * インベントリー上でアイテムをクリックしました
     *
     * @param e event
     */
    default void click(final InventoryClickEvent e) {}
  }

  public static interface BlockBreak extends AddHandler {
    /**
     * このアイテムをメインハンドに持ってブロックを破壊したさいに呼ばれます
     *
     * @param e event
     */
    default void blockBreak(final BlockBreakEvent e) {}
  }

  public static interface Spawn extends AddHandler {
    /**
     * このアイテムがスポーンした際に呼ばれます
     *
     * @param e event
     */
    default void itemSpawn(final ItemSpawnEvent e) {}
  }

  public static interface HeldOfOther extends AddHandler {
    /**
     * ほかのアイテムからこのカスタムアイテムにswapした時の処理
     *
     * @param e event
     * @param player player
     */
    default void heldOfOther(final PlayerItemHeldEvent e, final Player player) {}
  }

  public static interface HeldOfThis extends AddHandler {
    /**
     * ほかのアイテムからこのカスタムアイテムにswapした時の処理
     *
     * @param e event
     * @param player player
     */
    default void heldOfThis(final PlayerItemHeldEvent e, final Player player) {}
  }

  public static interface Held extends AddHandler {
    /** アイテムを持っている間だけ1tickごとにする処理です */
    default void heldItem(final Player player) {}
  }

  public static interface Shift extends AddHandler {
    /**
     * このアイテムをもちsneakをするときの処理
     *
     * @param e event
     * @param player player
     * @param equipmentSlot 要求される装備スロット (null なら inventory です)
     */
    default void shift(
        final PlayerToggleSneakEvent e,
        final Player player,
        final @Nullable EquipmentSlot equipmentSlot) {}
  }

  public static interface Drop extends AddHandler {
    /**
     * アイテムをドロップしたら呼ばれます
     *
     * @param e event
     */
    default void drop(final PlayerDropItemEvent e) {}
  }

  public static interface SwapToMainHand extends AddHandler {
    /**
     * mainhand にアイテムを切り替えたら発動(先にオフハンドが呼び出されます)
     *
     * @param e event
     * @param player player
     */
    default void swapToMainHand(final PlayerSwapHandItemsEvent e, final Player player) {}
  }

  public static interface SwapToOffHand extends AddHandler {
    /**
     * offhand にアイテムを切り替えたら発動(mainhand より前に呼び出されます)
     *
     * @param e event
     * @param player player
     */
    default void swapToOffHand(final PlayerSwapHandItemsEvent e, final Player player) {}
  }

  public static interface InteractMainHand extends AddHandler {
    /**
     * このアイテムを手に持っているプレイヤーがインタラクトしたら呼ばれます (mainhand)
     *
     * @param e event
     */
    default void interactMainHand(final PlayerInteractEvent e, final Player player) {}
  }

  public static interface InteractOffHand extends AddHandler {
    /**
     * このアイテムを手に持っているプレイヤーがインタラクトしたら呼ばれます (offhand)
     *
     * @param e event
     */
    default void interactOffHand(final PlayerInteractEvent e, final Player player) {}
  }

  public static interface DamageToEntity extends AddHandler {
    /**
     * このアイテムを持っているプレイヤーが攻撃をしたら呼ばれます
     *
     * @param e event
     * @param player player
     */
    default void damageToEntity(final EntityDamageByEntityEvent e, final Player player) {}
  }

  public static interface DamageByEntityWithMainHand extends AddHandler {
    /**
     * このアイテムを手に持っているプレイヤーが攻撃されたら呼ばれます (mainhand)
     *
     * @param e event
     * @param player player
     */
    default void damageByEntityMainHand(final EntityDamageByEntityEvent e, final Player player) {}
  }

  public static interface DamageByEntityWithOffHand extends AddHandler {
    /**
     * このアイテムを手に持っているプレイヤーが攻撃されたら呼ばれます (offhand)
     *
     * @param e event
     * @param player player
     */
    default void damageByEntityOffHand(final EntityDamageByEntityEvent e, final Player player) {}
  }
}
