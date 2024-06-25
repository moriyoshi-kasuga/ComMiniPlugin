package github.moriyoshi.comminiplugin.command;

import dev.jorel.commandapi.CommandAPICommand;
import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import github.moriyoshi.comminiplugin.system.ComMiniPlayer;
import github.moriyoshi.comminiplugin.system.ResourcePackSystem;
import github.moriyoshi.comminiplugin.system.buttons.AddSpecButton;
import github.moriyoshi.comminiplugin.system.buttons.GameHelpMenuButton;
import github.moriyoshi.comminiplugin.system.buttons.GameMenuButton;
import github.moriyoshi.comminiplugin.system.buttons.TeleportLobbyButton;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class MenuCommand extends CommandAPICommand {

  public MenuCommand() {
    super("menu");
    executesPlayer(
        (p, args) -> {
          open(p);
        });
  }

  public static boolean open(final Player p) {
    if (ComMiniPlayer.getPlayer(p.getUniqueId()).getJoinGameIdentifier() != null) {
      ComMiniPlugin.MAIN.send(p, "<red>あなたはmenuを開けません");
      return false;
    }
    new InnerMenu().openInv(p);
    return true;
  }

  private static class InnerMenu extends MenuHolder<ComMiniPlugin> {

    private final BukkitRunnable task;

    public InnerMenu() {
      super(ComMiniPlugin.getPlugin(), 27, "<yellow>Menu");
      setButton(11, TeleportLobbyButton.of());
      setButton(13, GameMenuButton.of());
      setButton(22, GameHelpMenuButton.of());
      setButton(14, AddSpecButton.of());
      this.task =
          new BukkitRunnable() {

            @Override
            public void run() {
              setButton(11, TeleportLobbyButton.of());
              setButton(13, GameMenuButton.of());
              setButton(22, GameHelpMenuButton.of());
              setButton(14, AddSpecButton.of());
            }
          };
      setButton(
          0,
          new ItemButton<>(
              new ItemBuilder(Material.NETHER_STAR).name("<yellow>リソパをロードする (リロード)").build()) {
            @Override
            public void onClick(@NotNull MenuHolder<?> holder, @NotNull InventoryClickEvent event) {
              val who = event.getWhoClicked();
              ComMiniPlayer.getPlayer(who.getUniqueId()).setShouldLoadResourcePack(true);
              ResourcePackSystem.updateComMiniResourcePack(who);
            }
          });
      setButton(
          18,
          new ItemButton<>(new ItemBuilder(Material.NAUTILUS_SHELL).name("<red>リソパを外す").build()) {
            @Override
            public void onClick(@NotNull MenuHolder<?> holder, @NotNull InventoryClickEvent event) {
              val who = event.getWhoClicked();
              ComMiniPlayer.getPlayer(who.getUniqueId()).setShouldLoadResourcePack(false);
              ResourcePackSystem.removeResourcePack(who);
            }
          });
    }

    @Override
    public void onClick(InventoryClickEvent event) {
      val p = event.getWhoClicked();
      if (ComMiniPlayer.getPlayer(p.getUniqueId()).getJoinGameIdentifier() != null) {
        ComMiniPlugin.MAIN.send(p, "<red>あなたはmenuを開けません");
        return;
      }
      super.onClick(event);
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
      task.cancel();
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
      task.runTaskTimer(getPlugin(), 1, 1);
    }
  }
}
