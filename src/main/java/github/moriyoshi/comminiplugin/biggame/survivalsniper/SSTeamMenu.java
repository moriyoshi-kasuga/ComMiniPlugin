package github.moriyoshi.comminiplugin.biggame.survivalsniper;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.biggame.survivalsniper.SSBigGame.Mode;
import github.moriyoshi.comminiplugin.lib.BukkitUtil;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import github.moriyoshi.comminiplugin.system.BigGameSystem;
import github.moriyoshi.comminiplugin.system.buttons.InventorySlotButton;
import github.moriyoshi.comminiplugin.system.menu.OnlyBeforeStartGameMenu;
import java.util.ArrayList;
import java.util.stream.Collectors;
import lombok.val;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public class SSTeamMenu extends MenuHolder<ComMiniPlugin>
    implements  OnlyBeforeStartGameMenu {

  private final ItemStack SPEC = new ItemBuilder(Material.GRAY_CONCRETE).name("<gray>観戦する").build();
  private final ItemStack LEAVE = new ItemBuilder(Material.RED_CONCRETE).name("<red>抜ける").build();

  private final BukkitRunnable task = createAutoCloseTask();

  public SSTeamMenu() {
    super(ComMiniPlugin.getPlugin(), 27, "<blue>サバイバルスナイパー");
    setButton(
        2,
        new ItemButton<>(SPEC) {
          @Override
          public void onClick(
              @NotNull final MenuHolder<?> holder, @NotNull final InventoryClickEvent event) {
            BigGameSystem.getGame(SSBigGame.class).joinPlayer(((Player) event.getWhoClicked()), false, null);
          }
        });
    setButton(
        6,
        new ItemButton<>(LEAVE) {
          @Override
          public void onClick(
              @NotNull final MenuHolder<?> holder, @NotNull final InventoryClickEvent event) {
            BigGameSystem.getGame(SSBigGame.class).leavePlayer((Player) event.getWhoClicked());
          }
        });
    setButton(4, new InventorySlotButton(SSPlayer.class));
  }

  private ItemButton<?> createTeamButton(ChatColor color) {
    val list =
        BigGameSystem.getGame(SSBigGame.class).players.entrySet().stream()
            .filter(entry -> entry.getValue().getSecond() == color)
            .map(entry -> "<gray> " + Bukkit.getOfflinePlayer(entry.getKey()).getName())
            .collect(Collectors.toCollection(ArrayList::new));
    if (list.isEmpty()) {
      list.add("<gray>empty");
    }
    return new ItemButton<>(
        new ItemBuilder(Material.valueOf(color.name() + "_GLAZED_TERRACOTTA"))
            .name("<#" + BukkitUtil.chatColorToHex(color) + ">" + color.name() + " <white>チームに参加する")
            .lore("<gray>参加者リスト：")
            .addLore(list)
            .build()) {
      @Override
      public void onClick(@NotNull MenuHolder<?> holder, @NotNull InventoryClickEvent event) {
        BigGameSystem.getGame(SSBigGame.class).joinPlayer((Player) event.getWhoClicked(), true, color);
      }
    };
  }

  @Override
  public void onClose(InventoryCloseEvent event) {
    this.task.cancel();
  }

  @Override
  public void onClick(InventoryClickEvent event) {
    if (isClosed()) {
      return;
    }
    super.onClick(event);
  }

  @Override
  public void onOpen(InventoryOpenEvent event) {
    this.task.runTaskTimer(getPlugin(), 1, 1);
  }

  @Override
  public Component additional() {
    if (BigGameSystem.getGame(SSBigGame.class).getMode() == Mode.TEAM) {
      setButton(20, createTeamButton(ChatColor.RED));
      setButton(21, createTeamButton(ChatColor.GREEN));
      setButton(23, createTeamButton(ChatColor.YELLOW));
      setButton(24, createTeamButton(ChatColor.BLUE));
      return null;
    }
    return BukkitUtil.mm("<red>モードが変わりました、もう一度メニューを開いてください");
  }
}
