package github.moriyoshi.comminiplugin.game.battleroyale.items;

import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.system.GameListener;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
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

public class CowBallItem extends CustomItem {

  public CowBallItem() {
    this(
        new ItemBuilder(Material.PHANTOM_MEMBRANE)
            .name("<white>牛玉")
            .lore("<gray>当たったプレイヤーのエフェクトをすべて消します(ビリビリ玉も無効加できるよ)")
            .customModelData(29)
            .build());
  }

  public CowBallItem(@NotNull ItemStack item) {
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
          loc.getWorld().playSound(loc, Sound.ENTITY_COW_MILK, SoundCategory.MASTER, 3, 1);
          loc.getWorld()
              .spawnParticle(
                  Particle.DUST,
                  loc,
                  100,
                  1.5,
                  1.5,
                  1.5,
                  1,
                  new Particle.DustOptions(Color.fromRGB(0xFFFFFF), 4),
                  true);
          loc.getNearbyPlayers(3, p -> p.getGameMode() != GameMode.SPECTATOR)
              .forEach(p -> p.clearActivePotionEffects());
        });
  }

  @Override
  public boolean canStack() {
    return true;
  }
}
