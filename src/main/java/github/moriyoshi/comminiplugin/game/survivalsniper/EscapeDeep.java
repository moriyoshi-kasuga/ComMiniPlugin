package github.moriyoshi.comminiplugin.game.survivalsniper;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import github.moriyoshi.comminiplugin.constant.ComMiniPrefix;
import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.util.BukkitUtil;
import github.moriyoshi.comminiplugin.util.ItemBuilder;

public class EscapeDeep extends CustomItem {

  public EscapeDeep() {
    this(new ItemBuilder(Material.CARROT_ON_A_STICK).name("<red>緊急脱出矢印!")
        .lore("<gray>これを使えば地上まで一っ飛びだ!")
        .customModelData(1).build());
  }

  public EscapeDeep(ItemStack item) {
    super(item);
  }

  @Override
  public void interact(PlayerInteractEvent e) {
    if (e.getAction().isLeftClick()) {
      e.setCancelled(false);
      return;
    }
    var p = e.getPlayer();
    if (!BukkitUtil.randomTeleport(p, p.getLocation(), 10)) {
      ComMiniPrefix.MAIN.send(p, "<red>ここの地上に脱出できません(海の可能性があります)");
      return;
    }
    p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_HURT, 8, 1);
    new ItemBuilder(getItem()).amount(0);
    BukkitUtil.disableMove(p, 100);
  }

  @Override
  public @NotNull String getIdentifier() {
    return "escape_deep";
  }
}
