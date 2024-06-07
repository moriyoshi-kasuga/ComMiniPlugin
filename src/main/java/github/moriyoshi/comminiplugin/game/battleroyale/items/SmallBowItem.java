package github.moriyoshi.comminiplugin.game.battleroyale.items;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class SmallBowItem extends CustomItem {

  public SmallBowItem() {
    this(
        new ItemBuilder(Material.BOW)
            .name("<#94550E>軽弓")
            .lore("<gray>弓を弾いているときの移動速度低下を軽減した弓。", "<gray>威力は下がるが使い勝手がいい弓")
            .customModelData(2)
            .build());
  }

  public SmallBowItem(@NotNull ItemStack item) {
    super(item);
  }

  @Override
  public void projectileLaunch(ProjectileLaunchEvent e, Player player) {
    val vec = player.getLocation().getDirection().multiply(0.5);
    val projectile = e.getEntity();
    new BukkitRunnable() {

      @Override
      public void run() {
        if (projectile.isDead()) {
          this.cancel();
          return;
        }
        projectile.setVelocity(vec);
      }
    }.runTaskTimer(ComMiniPlugin.getPlugin(), 0, 1);
  }
}
