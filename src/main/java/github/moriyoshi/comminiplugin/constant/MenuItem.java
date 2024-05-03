package github.moriyoshi.comminiplugin.constant;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.button.RedirectItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.button.TeleportButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.system.GameSystem;
import github.moriyoshi.comminiplugin.util.ItemBuilder;

/**
 * MenuItem
 */
public class MenuItem extends CustomItem {

  public MenuItem() {
    this(new ItemBuilder(Material.BOOK).name("<red>Menu").build());
  }

  public MenuItem(@NotNull ItemStack item) {
    super(item);
  }

  @Override
  public void interact(PlayerInteractEvent e) {
    open(e.getPlayer());
  }

  @Override
  public @NotNull String getIdentifier() {
    return "menu";
  }

  public static void open(Player p) {
    new InnerMenu().openInv(p);
  }

  private static class InnerMenu extends MenuHolder<ComMiniPlugin> {
    public InnerMenu() {
      super(ComMiniPlugin.getPlugin(), 27, "<yellow>Menu");
      setButton(11, new TeleportButton<>(new ItemBuilder(Material.ENDER_PEARL).name("<blue>ロビーにテレポート").build(),
          ComMiniWorld.LOBBY));
      if (GameSystem.isStarted()) {
        setButton(14, new ItemButton<>(
            new ItemBuilder(Material.NETHER_STAR).name(GameSystem.nowGame().name + "<reset><gray>を観戦する").build()) {

          @Override
          public void onClick(@NotNull MenuHolder<?> holder, @NotNull InventoryClickEvent event) {
            var p = (Player) event.getWhoClicked();
            if (!GameSystem.nowGame().addSpec(p)) {
              ComMiniPrefix.MAIN.send(p, "<red>このゲームに観戦できません");
            }
          }

        });
      }
      if (GameSystem.inGame()) {
        var game = GameSystem.nowGame();
        setButton(13,
            new RedirectItemButton<>(new ItemBuilder(game.material).name(game.name).lore(game.description).build(),
                (holder, event) -> game.gameMenu((Player) event.getWhoClicked()).getInventory()));
      } else {
        setButton(13, new ItemButton<>(new ItemBuilder(Material.BEDROCK).name("<gray>ゲームは開始されていません").build()));
        setButton(14, new ItemButton<>(new ItemBuilder(Material.BEDROCK).name("<gray>ゲームは開始されていません").build()));
      }
    }
  }

  @Override
  public void dropItem(PlayerDropItemEvent e) {
    e.setCancelled(true);
  }

  @Override
  public boolean canMoveOtherInv(InventoryClickEvent e) {
    return false;
  }
}
