package github.moriyoshi.comminiplugin.game.battleroyale.items;

import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.system.GameListener;
import github.moriyoshi.comminiplugin.util.BukkitUtil;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
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

public class TinglyBallItem extends CustomItem {

  public TinglyBallItem() {
    this(
        new ItemBuilder(Material.PHANTOM_MEMBRANE)
            .name("<yellow>ビリビリ玉")
            .lore("<gray>あたった敵が2秒間動けなくなる", "<red>自分に当たらないように注意!")
            .customModelData(12)
            .build());
  }

  public TinglyBallItem(@NotNull ItemStack item) {
    super(item);
  }

  @Override
  public void interact(PlayerInteractEvent e) {
    if (e.getAction().isLeftClick()) {
      return;
    }
    itemUse();
    val player = e.getPlayer();
    val projectile =
        player.launchProjectile(
            Snowball.class,
            player.getLocation().getDirection().multiply(3),
            snowball -> {
              snowball.setItem(
                  new ItemBuilder(Material.PHANTOM_MEMBRANE).customModelData(12).build());
            });
    GameListener.addProjectileHitListener(
        projectile.getUniqueId(),
        (entity, event) -> {
          val loc = entity.getLocation();
          loc.getWorld()
              .playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_BLAST, SoundCategory.MASTER, 1, 1);
          loc.getWorld()
              .spawnParticle(
                  Particle.DUST,
                  loc,
                  100,
                  1.5,
                  1.5,
                  1.5,
                  1,
                  new Particle.DustOptions(Color.fromRGB(249, 244, 81), 4),
                  true);
          loc.getNearbyPlayers(3, p -> p.getGameMode() != GameMode.SPECTATOR)
              .forEach(
                  p -> {
                    BukkitUtil.disableMove(p, 40);
                  });
        });
  }

  @Override
  public Optional<Supplier<UUID>> generateUUID() {
    return Optional.empty();
  }
}
