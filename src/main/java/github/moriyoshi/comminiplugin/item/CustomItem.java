package github.moriyoshi.comminiplugin.item;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadableNBT;
import github.moriyoshi.comminiplugin.ComMiniPlugin;
import lombok.val;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;

public abstract class CustomItem implements InterfaceItem {

  public static final BiMap<String, Class<? extends CustomItem>> registers = HashBiMap.create();
  @NotNull
  private final ItemStack item;
  private UUID uuid;

  public CustomItem(@NotNull final ItemStack item) {
    NBT.modify(item, nbt -> {
      final var compound = nbt.getOrCreateCompound(nbtKey);
      if (!compound.hasTag("identifier")) {
        compound.setString("identifier", getIdentifier());
      }
      generateUUID().ifPresent(uuid -> {
        if (compound.hasTag("uuid")) {
          this.uuid = compound.getUUID("uuid");
        } else {
          compound.setUUID("uuid", uuid);
          this.uuid = uuid;
        }
      });
    });
    this.item = item;
  }

  public static void registers(final String packageName) {
    val reflections = new Reflections(packageName);
    for (Class<? extends CustomItem> item : reflections.getSubTypesOf(CustomItem.class)) {
      String id;
      try {
        id = item.getDeclaredConstructor().newInstance().getIdentifier();
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
        throw new RuntimeException(e);
      }
      if (CustomItem.registers.containsKey(id)) {
        throw new IllegalArgumentException(
            id + "のカスタムアイテムがかぶっています、" + item.getName() + " >>==<< "
                + CustomItem.registers.get(id).getName());
      }
      CustomItem.registers.put(id, item);
    }
  }

  public static CustomItem getNewCustomItem(final String identifier) {
    if (registers.containsKey(identifier)) {
      try {
        return registers.get(identifier).getDeclaredConstructor().newInstance();
      } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
          | NoSuchMethodException | SecurityException e) {
        throw new RuntimeException(e);
      }
    }
    throw new IllegalArgumentException(identifier + " のIDはカスタムアイテムに登録されていません");
  }

  public static CustomItem getCustomItem(final ItemStack item) {
    final var ci = getIdentifier(item);
    if (ci.isPresent()) {
      try {
        return registers.get(ci.get()).getDeclaredConstructor(ItemStack.class).newInstance(item);
      } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
          | NoSuchMethodException | SecurityException e) {
        throw new RuntimeException(e);
      }
    }
    throw new IllegalArgumentException(
        "このアイテムは CustomItem ではありません。" + item.toString());
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
   * @param item       判定するアイテム
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
   * @param item      item2
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
    return NBT.get(item, readableNBT -> {
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
    return NBT.get(item, nbt -> {
      if (!nbt.hasTag(nbtKey)) {
        return Optional.empty();
      }
      final ReadableNBT compound = nbt.getCompound(nbtKey);
      assert compound != null;
      return Optional.of(compound.getString("identifier"));
    });
  }

  public Optional<UUID> generateUUID() {
    return Optional.of(UUID.randomUUID());
  }

  @Override
  public @NotNull UUID getUniqueId() {
    return Objects.requireNonNull(this.uuid);
  }

  @Override
  public @NotNull ItemStack getItem() {
    return this.item;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof final CustomItem item) {
      return item.getIdentifier().equals(getIdentifier()) && item.getUniqueId()
          .equals(getUniqueId());
    }
    return false;
  }

  /**
   * デフォルトではeventはキャンセルされますが {@code e.setCancelled(false)} をすることでキャンセルするのを防げます
   *
   * @param e event
   */
  public void interact(final PlayerInteractEvent e) {
  }

  /**
   * ほかのアイテムからこのカスタムアイテムにswapした時の処理
   *
   * @param e event
   */
  public void heldOfThis(final PlayerItemHeldEvent e) {
    final Player player = e.getPlayer();
    final CustomItem v = this;
    if (heldItem(getItem()).isPresent()) {
      new BukkitRunnable() {

        @Override
        public void run() {
          final ItemStack item = player.getInventory().getItemInMainHand();
          if (CustomItem.isCustomItem(item) && v.equals(CustomItem.getCustomItem(item))) {
            heldItem(item).ifPresent(consumer -> consumer.accept(player));
            return;
          }
          this.cancel();
        }
      }.runTaskTimer(ComMiniPlugin.getPlugin(), 0L, 1L);
    }
  }

  /**
   * このアイテムからほかのにswapした時の処理
   *
   * @param e event
   */
  public void heldOfOther(final PlayerItemHeldEvent e) {
  }

  /**
   * このアイテムをもちsneakをするときの処理
   *
   * @param e event
   */
  public void shiftItem(final PlayerToggleSneakEvent e) {
  }

  /**
   * このアイテムを捨てたさいの処理
   *
   * @param e event
   */
  public void dropItem(final PlayerDropItemEvent e) {
  }

  /**
   * マインハンドにアイテムを切り替えたら発動 先にオフハンドが呼び出されます、キャンセルされたらこれは呼び出されません
   *
   * @param e event
   */
  public void swapToMainHand(final PlayerSwapHandItemsEvent e) {
  }

  /**
   * オフハンドに切り替えたら発動 先にこれが呼び出されキャンセルたらメインハンドは呼び出されません
   *
   * @param e event
   */
  public void swapToOffHand(final PlayerSwapHandItemsEvent e) {
  }

  public boolean canMoveOtherInv(final InventoryClickEvent e) {
    return true;
  }

  /**
   * インベントリー上でアイテムをクリックしました
   *
   * @param e event
   */
  public void clickItem(final InventoryClickEvent e) {
  }

  /**
   * このアイテムがスポーンした際に呼ばれます
   *
   * @param e event
   */
  public void itemSpawn(final ItemSpawnEvent e) {
  }

  @Override
  public @NotNull String getIdentifier() {
    return getClass().getSimpleName();
  }

}
