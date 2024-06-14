package github.moriyoshi.comminiplugin.game.survivalsniper;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.lib.item.CustomItem;
import github.moriyoshi.comminiplugin.lib.BukkitUtil;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class SSEscapeDeep extends CustomItem {

  public SSEscapeDeep() {
    this(
        new ItemBuilder(Material.CARROT_ON_A_STICK)
            .name("<red>緊急脱出矢印!")
            .lore("<gray>これを使えば地上まで一っ飛びだ!")
            .customModelData(1)
            .build());
  }

  public SSEscapeDeep(final ItemStack item) {
    super(item);
  }

  @Override
  public void interactMainHand(final PlayerInteractEvent e) {
    if (e.getAction().isLeftClick()) {
      e.setCancelled(false);
      return;
    }
    val p = e.getPlayer();
    if (!BukkitUtil.randomTeleport(p, p.getLocation(), 10)) {
      ComMiniPlugin.MAIN.send(p, "<red>ここの地上に脱出できません(海の可能性があります)");
      return;
    }
    p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_HURT, 8, 1);
    useItemAmount();
    BukkitUtil.disableMove(p, 100);
  }
}
