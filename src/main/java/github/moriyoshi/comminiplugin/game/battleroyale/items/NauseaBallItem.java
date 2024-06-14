package github.moriyoshi.comminiplugin.game.battleroyale.items;

import github.moriyoshi.comminiplugin.lib.item.CustomItem;
import github.moriyoshi.comminiplugin.system.GameListener;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import lombok.val;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Snowball;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class NauseaBallItem extends CustomItem {

  public NauseaBallItem() {
    this(
        new ItemBuilder(Material.PHANTOM_MEMBRANE)
            .name("<#8BD376>酔玉(よいだま)")
            .lore("<gray>あたった敵は20秒間吐き気状態になる", "<red>自分に当たらないように注意!")
            .customModelData(27)
            .build());
  }

  public NauseaBallItem(@NotNull ItemStack item) {
    super(item);
  }

  @Override
  public void interactMainHand(PlayerInteractEvent e) {
    if (e.getAction().isLeftClick()) {
      return;
    }
    useItemAmount();
    val player = e.getPlayer();
    val projectile =
        player.launchProjectile(
            Snowball.class,
            player.getLocation().getDirection().multiply(3),
            snowball ->
                snowball.setItem(
                    new ItemBuilder(Material.PHANTOM_MEMBRANE).customModelData(27).build()));
    GameListener.addProjectileHitListener(
        projectile.getUniqueId(),
        (entity, event) -> {
          val loc = entity.getLocation();
          loc.getWorld().playSound(loc, Sound.BLOCK_SCULK_VEIN_STEP, SoundCategory.MASTER, 3, 1);
          loc.getWorld()
              .spawnParticle(
                  Particle.DUST,
                  loc,
                  100,
                  1.5,
                  1.5,
                  1.5,
                  1,
                  new Particle.DustOptions(Color.fromRGB(0x8BD376), 4),
                  true);
          val nause =
              new org.bukkit.potion.PotionEffect(
                  org.bukkit.potion.PotionEffectType.NAUSEA, 20 * 20, 0);
          loc.getNearbyPlayers(3, p -> p.getGameMode() != GameMode.SPECTATOR)
              .forEach(p -> p.addPotionEffect(nause));
        });
  }

  @Override
  public boolean canStack() {
    return true;
  }
}
