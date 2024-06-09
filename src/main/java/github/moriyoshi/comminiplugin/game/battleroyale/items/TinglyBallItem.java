package github.moriyoshi.comminiplugin.game.battleroyale.items;

import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.system.GameListener;
import github.moriyoshi.comminiplugin.util.BukkitUtil;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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
    useItemAmount();
    val player = e.getPlayer();
    val projectile =
        player.launchProjectile(
            Snowball.class,
            player.getLocation().getDirection().multiply(3),
            snowball ->
                snowball.setItem(
                    new ItemBuilder(Material.PHANTOM_MEMBRANE).customModelData(12).build()));
    GameListener.addProjectileHitListener(
        projectile.getUniqueId(),
        (entity, event) -> {
          val loc = entity.getLocation();
          loc.getWorld()
              .playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_BLAST, SoundCategory.MASTER, 3, 1);
          loc.getWorld()
              .spawnParticle(
                  Particle.DUST,
                  loc,
                  100,
                  1.5,
                  1.5,
                  1.5,
                  1,
                  new Particle.DustOptions(Color.fromRGB(0xF9F451), 4),
                  true);
          loc.getNearbyPlayers(3, p -> p.getGameMode() != GameMode.SPECTATOR)
              .forEach(
                  p -> {
                    BukkitUtil.disableMove(p, 40);
                    p.addPotionEffect(
                        new PotionEffect(PotionEffectType.BLINDNESS, 3 * 20, 0, true, false));
                  });
        });
  }

  @Override
  public boolean canStack() {
    return true;
  }
}
