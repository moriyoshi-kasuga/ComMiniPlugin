package github.moriyoshi.comminiplugin.game.survivalsniper;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.button.RedirectItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.tuple.Triple;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

/**
 * SurvivalSniperCustomMenu
 */
public class SurvivalSniperCustomMenu extends MenuHolder<ComMiniPlugin> {

  private static final int SIZE = 27;

  private static final RedirectItemButton<MenuHolder<ComMiniPlugin>> back = new RedirectItemButton<>(
      new ItemBuilder(Material.MAGENTA_GLAZED_TERRACOTTA).name("<red>カスタムメニューに戻る")
          .build(), (holder, even) -> new SurvivalSniperCustomMenu().getInventory());

  private static final MenuHolder<ComMiniPlugin> WARHEADS = new MenuHolder<>(
      ComMiniPlugin.getPlugin(), 27,
      "<yellow>弾頭"
  ) {
    {
      int slot = 1;
      for (var head : Bullet.WARHEAD.values()) {
        setButton(slot,
            new ItemButton<>(new ItemBuilder(head.material)
                .name(head.name).lore("<yellow>通常<gray>:<white>" + head.damage,
                    "<red>ヘッドショット<gray>:<white>" + head.headShot,
                    "<green>作成できる個数<gray>:" + head.successAmount
                )
                .build())
        );
        slot++;
      }
      setButton(0, back);
    }
  };

  private static final MenuHolder<ComMiniPlugin> WARTAILS = new MenuHolder<>(
      ComMiniPlugin.getPlugin(), 27,
      "<green>背面"
  ) {
    {
      int slot = 1;
      for (var tail : Bullet.WARTAIL.values()) {
        setButton(slot, new ItemButton<>(new ItemBuilder(tail.icon).name(tail.name)
            .lore("<green>" + tail.description, "<red>ダメージ+" + tail.plusDamage).build()));
        slot++;
      }
      setButton(0, back);
    }
  };

  private static final MenuHolder<ComMiniPlugin> OPTIONS = new MenuHolder<>(
      ComMiniPlugin.getPlugin(), 27,
      "<gray>オプション"
  ) {
    {
      int slot = 1;
      for (var option : Bullet.OptionalItem.values()) {
        setButton(slot, new ItemButton<>(option.material));
        slot++;
      }
      setButton(0, back);
    }
  };

  private static final RedirectItemButton<MenuHolder<ComMiniPlugin>> heads = new RedirectItemButton<>(
      new ItemBuilder(Material.BAMBOO_SIGN).name("<yellow>弾頭").build(), WARHEADS::getInventory);

  private static final RedirectItemButton<MenuHolder<ComMiniPlugin>> tails = new RedirectItemButton<>(
      new ItemBuilder(Material.BAMBOO_HANGING_SIGN).name("<green>背面").build(),
      WARTAILS::getInventory
  );

  private static final RedirectItemButton<MenuHolder<ComMiniPlugin>> options = new RedirectItemButton<>(
      new ItemBuilder(Material.END_CRYSTAL).name("<gray>オプション").build(),
      OPTIONS::getInventory
  );

  private static final Set<Integer> items = new HashSet<>() {
    {
      add(12);
      add(13);
      add(14);
    }
  };

  private final ItemButton<MenuHolder<ComMiniPlugin>> none = new ItemButton<>(
      new ItemBuilder(Material.BEDROCK).name("<red>何も作れません").build());

  public SurvivalSniperCustomMenu() {
    super(ComMiniPlugin.getPlugin(), SIZE, "<red>カスタムメニュー");
    for (int i = 0; i < SIZE; i++) {
      if (items.contains(i)) {
        continue;
      }
      setButton(i, empty);
    }
    setButton(15, new ItemButton<>(
        new ItemBuilder(Material.CARROT_ON_A_STICK).customModelData(4)
            .name("<yellow>作るときはクリック").build()));
    setButton(16, none);
    setButton(18, heads);
    setButton(19, tails);
    setButton(20, options);
  }

  @Override
  public void onClick(InventoryClickEvent event) {
    super.onClick(event);
    update(event);
    Inventory inv;
    if (((inv = event.getClickedInventory()) != null && inv.getType().equals(InventoryType.PLAYER))
        || items.contains(event.getSlot())) {
      event.setCancelled(false);
    }
  }

  @Override
  public void onClose(InventoryCloseEvent event) {
    var inv = event.getPlayer().getInventory();
    for (var i : items) {
      var item = getInventory().getItem(i);
      if (item != null) {
        inv.addItem(item);
      }
    }
  }

  public void update(InventoryClickEvent event) {
    setButton(16, none);
    new BukkitRunnable() {

      @Override
      public void run() {
        Bullet.WARHEAD head = null;
        Bullet.WARTAIL tail = null;
        Bullet.OptionalItem optionalItem = null;
        int headSlot = -1;
        int tailSlot = -1;
        int optionSlot = -1;
        var inv = getInventory();
        var flag = false;
        root:
        for (int i : items) {
          ItemStack item = inv.getItem(i);
          if (item == null) {
            continue;
          }
          var material = item.getType();
          for (var h : Bullet.WARHEAD.values()) {
            if (h.material == material) {
              if (head != null) {
                setButton(16, none);
                return;
              }
              head = h;
              headSlot = i;
              continue root;
            }
          }
          for (var t : Bullet.WARTAIL.values()) {
            if (t.predicate.test(material)) {
              if (tail != null) {
                setButton(16, none);
                return;
              }
              tail = t;
              tailSlot = i;
              continue root;
            }
          }
          for (var o : Bullet.OptionalItem.values()) {
            if (o.predicate.test(item)) {
              if (optionalItem != null) {
                setButton(16, none);
                return;
              }
              optionalItem = o;
              optionSlot = i;
              continue root;
            }
          }
          flag = true;
        }
        if (head == null || tail == null) {
          setButton(16, none);
          return;
        }
        if (optionalItem == null && flag) {
          setButton(16, none);
          return;
        }
        var tmp1 = headSlot;
        var tmp2 = tailSlot;
        var tmp3 = optionSlot;
        var sound = head.sound;
        var _name = head.name + "<reset><gray>(" + tail.name + ")";
        var _damage = head.damage + tail.plusDamage;
        var _headShot = head.headShot + tail.plusDamage;
        String name;
        int damage;
        int headShot;
        if (optionalItem != null) {
          var triple = optionalItem.wrapper.apply(Triple.of(_name, _damage, _headShot));
          name = triple.getLeft();
          damage = triple.getMiddle();
          headShot = triple.getRight();
        } else {
          name = _name;
          damage = _damage;
          headShot = _headShot;
        }
        var optional = optionalItem;
        var result = new ItemBuilder(Material.PHANTOM_MEMBRANE).customModelData(head.model)
            .name(name)
            .lore("<yellow>通常<gray>:" + damage,
                "<red>ヘッドショット<gray>:<white>" + headShot
            )
            .amount(head.successAmount)
            .build();
        setButton(16, new ItemButton<>(result) {

          @Override
          public void onClick(@NotNull MenuHolder<?> holder, @NotNull InventoryClickEvent event) {
            event.getWhoClicked().getInventory().addItem(new Bullet(result, name, damage,
                headShot, sound
            ).getItem());
            var temp1 = getInventory().getItem(tmp1);
            var temp2 = getInventory().getItem(tmp2);
            temp1.setAmount(temp1.getAmount() - 1);
            temp2.setAmount(temp2.getAmount() - 1);
            if (tmp3 != -1) {
              optional.finalize.accept(getInventory().getItem(tmp3));
            }
          }
        });
      }
    }.runTask(getPlugin());

  }

}
