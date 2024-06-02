package github.moriyoshi.comminiplugin.item;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadableNBT;
import github.moriyoshi.comminiplugin.constant.ComMiniPrefix;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.val;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;

public abstract class CustomItem implements InterfaceItem {

  private static final BiMap<String, Class<? extends CustomItem>> registers = HashBiMap.create();

  public static final BiMap<String, Class<? extends CustomItem>> canShowingRegisters =
      HashBiMap.create();

  public static void registers(final Reflections reflections) {
    github.moriyoshi.comminiplugin.util.ReflectionUtil.forEachAllClass(
        reflections,
        CustomItem.class,
        item -> {
          String id;
          boolean canShowing;
          try {
            val instance = item.getDeclaredConstructor().newInstance();
            id = instance.getIdentifier();
            canShowing = instance.canShowing();
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
          ComMiniPrefix.SYSTEM.logDebug(
              "<gray>REGISTER ITEM " + item.getSimpleName() + " (canShowing: " + canShowing + ")");
          CustomItem.registers.put(id, item);
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
    if (CustomItem.isCustomItem(item)) {
      return Optional.of(getCustomItem(item));
    }
    return Optional.empty();
  }

  public static CustomItem getCustomItem(final ItemStack item) {
    final var ci = getIdentifier(item);
    if (ci.isPresent()) {
      try {
        return registers.get(ci.get()).getDeclaredConstructor(ItemStack.class).newInstance(item);
      } catch (InstantiationException
          | IllegalAccessException
          | IllegalArgumentException
          | InvocationTargetException
          | NoSuchMethodException
          | SecurityException e) {
        throw new RuntimeException(e);
      }
    }
    throw new IllegalArgumentException("このアイテムは CustomItem ではありません。" + item.toString());
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
          final ReadableNBT compound = readableNBT.getCompound(nbtKey);
          assert compound != null;
          return compound.hasTag("identifier");
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
  public static Optional<String> getIdentifier(final ItemStack item) {
    if (item == null || item.getType().isAir()) {
      return Optional.empty();
    }
    return NBT.get(
        item,
        nbt -> {
          if (!nbt.hasTag(nbtKey)) {
            return Optional.empty();
          }
          final ReadableNBT compound = nbt.getCompound(nbtKey);
          assert compound != null;
          return Optional.of(compound.getString("identifier"));
        });
  }

  @NotNull private final ItemStack item;

  private UUID uuid;

  public CustomItem(@NotNull final ItemStack item) {
    NBT.modify(
        item,
        nbt -> {
          final var compound = nbt.getOrCreateCompound(nbtKey);
          if (!compound.hasTag("identifier")) {
            compound.setString("identifier", getIdentifier());
          }
          generateUUID()
              .ifPresent(
                  supplier -> {
                    if (compound.hasTag("uuid")) {
                      this.uuid = compound.getUUID("uuid");
                    } else {
                      val uuid = supplier.get();
                      compound.setUUID("uuid", uuid);
                      this.uuid = uuid;
                    }
                  });
        });
    this.item = item;
  }

  @Override
  public @Nullable UUID getUniqueId() {
    return this.uuid;
  }

  @Override
  public @NotNull ItemStack getItem() {
    return this.item;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof final CustomItem item) {
      return item.getIdentifier().equals(getIdentifier())
          && Objects.equals(item.getUniqueId(), getUniqueId());
    }
    return false;
  }

  @Override
  public @NotNull String getIdentifier() {
    return getClass().getSimpleName();
  }

  @Override
  public void runTick(Player player) {}

  @Override
  public void clickItem(InventoryClickEvent e) {}

  @Override
  public void itemSpawn(ItemSpawnEvent e) {}

  @Override
  public void blockBreak(BlockBreakEvent e) {}

  @Override
  public void heldOfOther(PlayerItemHeldEvent e) {}

  @Override
  public void shiftItem(PlayerToggleSneakEvent e, @Nullable EquipmentSlot equipmentSlot) {}

  @Override
  public void dropItem(PlayerDropItemEvent e) {}

  @Override
  public void swapToMainHand(PlayerSwapHandItemsEvent e) {}

  @Override
  public void swapToOffHand(PlayerSwapHandItemsEvent e) {}

  @Override
  public void interact(PlayerInteractEvent e) {}

  @Override
  public void projectileLaunch(ProjectileLaunchEvent e, Player player) {}
}
