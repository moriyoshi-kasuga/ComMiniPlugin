package github.moriyoshi.comminiplugin.biggame.battleroyale.items;

import github.moriyoshi.comminiplugin.lib.item.CustomItem;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import github.moriyoshi.comminiplugin.system.GameListener;
import lombok.val;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CowBallItem extends CustomItem implements CustomItem.InteractMainHand {

  public CowBallItem() {
    this(
        new ItemBuilder(Material.PHANTOM_MEMBRANE)
            .name("<white>牛玉")
            .lore("<gray>当たったプレイヤーのエフェクトをすべて消します", "<gray>(ビリビリ玉も無効加できるよ)")
            .customModelData(29)
            .build());
  }

  public CowBallItem(@NotNull ItemStack item) {
    super(item);
  }

  @Override
  public void interactMainHand(PlayerInteractEvent e, final Player player) {
    if (e.getAction().isLeftClick()) {
      return;
    }
    useItemAmount();
    val projectile =
        player.launchProjectile(
            Snowball.class,
            player.getLocation().getDirection().multiply(3),
            snowball ->
                snowball.setItem(
                    new ItemBuilder(Material.PHANTOM_MEMBRANE).customModelData(29).build()));
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
