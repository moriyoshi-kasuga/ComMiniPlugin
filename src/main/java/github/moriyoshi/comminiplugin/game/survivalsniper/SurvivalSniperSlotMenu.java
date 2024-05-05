package github.moriyoshi.comminiplugin.game.survivalsniper;

import java.util.Collections;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.system.buttons.GameMenuButton;
import github.moriyoshi.comminiplugin.util.ItemBuilder;

public class SurvivalSniperSlotMenu extends MenuHolder<ComMiniPlugin> {
  private static final ItemStack pane = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("").build();

  private final SurvivalSniperSlot slots;

  public SurvivalSniperSlotMenu(SurvivalSniperSlot slots) {
    super(ComMiniPlugin.getPlugin(), 27, "<blue>インベントリーのカスタマイズ");
    this.slots = slots;
    set();
    for (int i = 9; i < 27; i++) {
      setButton(i, new ItemButton<>(pane));
    }
    setButton(18, GameMenuButton.of());
  }

  private int swapSlot = -1;

  private void update(Player p, int slot) {
    if (swapSlot == -1) {
      swapSlot = slot;
      p.playSound(p.getLocation(), Sound.BLOCK_COPPER_TRAPDOOR_OPEN, 1, 1);
      return;
    }
    p.playSound(p.getLocation(), Sound.BLOCK_COPPER_TRAPDOOR_OPEN, 1, 1);
    Collections.swap(slots.slots, swapSlot, slot);
    swapSlot = -1;
    set();
  }

  private void set() {
    var i = 0;
    for (var item : slots.toItemStacks()) {
      setButton(i, new ItemButton<>(item) {

        @Override
        public void onClick(@NotNull MenuHolder<?> holder, @NotNull InventoryClickEvent event) {
          if (holder.getInventory().equals(event.getClickedInventory())) {
            update(((Player) event.getWhoClicked()), event.getSlot());
          }
        }

      });
      i++;
    }
  }

}
