package github.moriyoshi.comminiplugin.game.survivalsniper;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SSTradeMenu extends MenuHolder<ComMiniPlugin> {

  @RequiredArgsConstructor
  private enum TradeItem {
    EscapeDeep(
        () -> new EscapeDeep().getItem(),
        "4レベル",
        (p) -> p.getLevel() >= 4,
        (p) -> p.setLevel(p.getLevel() - 4)),
    BEEF(
        () -> new ItemStack(Material.COOKED_BEEF),
        "1レベル",
        (p) -> p.getLevel() >= 1,
        (p) -> p.setLevel(p.getLevel() - 1));

    public final Supplier<ItemStack> item;
    public final String tradeDescription;
    public final Predicate<Player> predicate;
    public final Consumer<Player> buyCallback;
  }

  public SSTradeMenu() {
    super(ComMiniPlugin.getPlugin(), 27, "<green>Trade");
    for (int i = 0; i < 27; i++) {
      setButton(i, empty);
    }
    var i = 10;
    for (val item : TradeItem.values()) {
      setButton(
          i,
          new ItemButton<>(
              new ItemBuilder(item.item.get())
                  .addLore("")
                  .addLore("<green>" + item.tradeDescription + "でトレード")
                  .build()) {

            @Override
            public void onClick(
                @NotNull final MenuHolder<?> holder, @NotNull final InventoryClickEvent event) {
              val p = (Player) event.getWhoClicked();
              if (!item.predicate.test(p)) {
                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 1, 1);
                return;
              }
              p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
              p.getInventory().addItem(item.item.get());
              item.buyCallback.accept(p);
            }
          });
      i++;
    }
  }
}
