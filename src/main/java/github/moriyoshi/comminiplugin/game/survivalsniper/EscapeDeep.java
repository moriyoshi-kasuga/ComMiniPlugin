package github.moriyoshi.comminiplugin.game.survivalsniper;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import github.moriyoshi.comminiplugin.constant.ComMiniPrefix;
import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.util.BukkitUtil;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import lombok.val;

public class EscapeDeep extends CustomItem {

  public EscapeDeep() {
    this(new ItemBuilder(Material.CARROT_ON_A_STICK).name("<red>緊急脱出矢印!")
        .lore("<gray>これを使えば地上まで一っ飛びだ!")
        .customModelData(1).build());
  }

  public EscapeDeep(final ItemStack item) {
    super(item);
  }

  @Override
  public void interact(final PlayerInteractEvent e) {
    if (e.getAction().isLeftClick()) {
      e.setCancelled(false);
      return;
    }
    val p = e.getPlayer();
    if (!BukkitUtil.randomTeleport(p, p.getLocation(), 10)) {
      ComMiniPrefix.MAIN.send(p, "<red>ここの地上に脱出できません(海の可能性があります)");
      return;
    }
    p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_HURT, 8, 1);
    itemUse();
    BukkitUtil.disableMove(p, 100);
  }
}
