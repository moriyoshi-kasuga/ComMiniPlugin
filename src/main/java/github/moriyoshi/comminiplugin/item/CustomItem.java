package github.moriyoshi.comminiplugin.item;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadableNBT;
import github.moriyoshi.comminiplugin.ComMiniPlugin;
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

public abstract class CustomItem implements InterfaceItem {

  @NotNull
  private final ItemStack item;
  private UUID uuid;

  public static final BiMap<String, Class<? extends CustomItem>> registers = HashBiMap.create();

  public static CustomItem getNewCustomItem(String identifier) {
    if (registers.containsKey(identifier)) {
      try {
        return registers.get(identifier).getDeclaredConstructor().newInstance();
      } catch (InstantiationException | IllegalAccessException | IllegalArgumentException |
               InvocationTargetException
               | NoSuchMethodException | SecurityException e) {
        throw new RuntimeException(e);
      }
    }
    throw new IllegalArgumentException(identifier + " のIDはカスタムアイテムに登録されていません");
  }

  public static CustomItem getCustomItem(ItemStack item) {
    var ci = getIdentifier(item);
    if (ci.isPresent()) {
      try {
        return registers.get(ci.get()).getDeclaredConstructor(ItemStack.class).newInstance(item);
      } catch (InstantiationException | IllegalAccessException | IllegalArgumentException |
               InvocationTargetException
               | NoSuchMethodException | SecurityException e) {
        throw new RuntimeException(e);
      }
    }
    throw new IllegalArgumentException(
        "このアイテムは CustomItem ではありません。" + item.toString());
  }

  public CustomItem(@NotNull ItemStack item) {
    NBT.modify(item, nbt -> {
      var compound = nbt.getOrCreateCompound(nbtKey);
      if (!compound.hasTag("identifier")) {
        compound.setString("identifier", getIdentifier());
      }
      generatUUID().ifPresent(uuid -> {
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

  public Optional<UUID> generatUUID() {
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

  public static boolean equalsItem(ItemStack itemStack, Class<?> clazz) {
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
  public static boolean equalsIdentifier(String identifier, ItemStack item) {
    var id = getIdentifier(item);
    return id.map(s -> s.equals(identifier)).orElse(false);
  }

  /**
   * どちらのアイテムも同じ識別子かどうか判別します
   *
   * @param itemStack item1
   * @param item      item2
   * @return 同じだった場合trueを返します
   */
  public static boolean equalsIdentifier(ItemStack itemStack, ItemStack item) {
    var id = getIdentifier(itemStack);
    return id.filter(s -> equalsIdentifier(s, item)).isPresent();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof CustomItem item) {
      return item.getIdentifier().equals(getIdentifier()) && item.getUniqueId().equals(getUniqueId());
    }
    return false;
  }

  /**
   * そのアイテムがもつ識別子が登録されているかを判別します
   *
   * @param item 対象のアイテム
   * @return 登録されいたらtrueを返します
   */
  public static boolean isCustomItem(ItemStack item) {
    if (item == null || item.getType().isAir()) {
      return false;
    }
    return NBT.get(item, readableNBT -> {
      if (!readableNBT.hasTag(nbtKey)) {
        return false;
      }
      ReadableNBT compound = readableNBT.getCompound(nbtKey);
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
  public static Optional<String> getIdentifier(ItemStack item) {
    if (item == null || item.getType().isAir()) {
      return Optional.empty();
    }
    return NBT.get(item, nbt -> {
      if (!nbt.hasTag(nbtKey)) {
        return Optional.empty();
      }
      ReadableNBT compound = nbt.getCompound(nbtKey);
      assert compound != null;
      return Optional.of(compound.getString("identifier"));
    });
  }

  /**
   * デフォルトではeventはキャンセルされますが {@code e.setCancelled(false)} をすることでキャンセルするのを防げます
   *
   * @param e event
   */
  public void interact(PlayerInteractEvent e) {
  }

  /**
   * ほかのアイテムからこのカスタムアイテムにswapした時の処理
   *
   * @param e event
   */
  public void heldOfThis(PlayerItemHeldEvent e) {
    Player player = e.getPlayer();
    CustomItem v = this;
    if (heldItem(getItem()).isPresent()) {
      new BukkitRunnable() {

        @Override
        public void run() {
          ItemStack item = player.getInventory().getItemInMainHand();
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
  public void heldOfOther(PlayerItemHeldEvent e) {
  }

  /**
   * このアイテムをもちsneakをするときの処理
   *
   * @param e event
   */
  public void shiftItem(PlayerToggleSneakEvent e) {
  }

  /**
   * このアイテムを捨てたさいの処理
   *
   * @param e event
   */
  public void dropItem(PlayerDropItemEvent e) {

  }

  /**
   * マインハンドにアイテムを切り替えたら発動 先にオフハンドが呼び出されます、キャンセルされたらこれは呼び出されません
   *
   * @param e event
   */
  public void swapToMainHand(PlayerSwapHandItemsEvent e) {

  }

  /**
   * オフハンドに切り替えたら発動 先にこれが呼び出されキャンセルたらメインハンドは呼び出されません
   *
   * @param e event
   */
  public void swapToOffHand(PlayerSwapHandItemsEvent e) {

  }

  public boolean canMoveOtherInv(InventoryClickEvent e) {
    return true;
  }

  /**
   * インベントリー上でアイテムをクリックしました
   *
   * @param e event
   */
  public void clickItem(InventoryClickEvent e) {

  }

  /**
   * このアイテムがスポーンした際に呼ばれます
   *
   * @param e event
   */
  public void itemSpawn(ItemSpawnEvent e) {
  }
}
