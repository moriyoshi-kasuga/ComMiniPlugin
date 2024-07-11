package github.moriyoshi.comminiplugin.biggame.battleroyale.items;

import github.moriyoshi.comminiplugin.lib.item.CustomItem;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import lombok.val;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class AllOrNothingItem extends CustomItem implements CustomItem.InteractMainHand {

  public AllOrNothingItem() {
    super(
        new ItemBuilder(Material.PHANTOM_MEMBRANE)
            .name("<#FFD700>オールオアナッシング ")
            .lore("<gray>アイテム使用時に半々の確率でバフかデバフがかかる")
            .customModelData(13)
            .build());
  }

  public AllOrNothingItem(@NotNull ItemStack item) {
    super(item);
  }

  @Override
  public void interactMainHand(PlayerInteractEvent e, final Player player) {
    if (e.getAction().isLeftClick()) {
      return;
    }
    useItemAmount();
    val random = new Random();
    List<PotionEffectType> types = new ArrayList<>();
    Color color;
    Sound sound;
    if (random.nextBoolean()) {
      color = Color.fromRGB(0x898883);
      types.add(PotionEffectType.SLOWNESS);
      types.add(PotionEffectType.WEAKNESS);
      types.add(PotionEffectType.DARKNESS);
      types.add(PotionEffectType.GLOWING);
      sound = Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE;
    } else {
      color = Color.fromRGB(0xE3CA13);
      types.add(PotionEffectType.STRENGTH);
      types.add(PotionEffectType.SPEED);
      types.add(PotionEffectType.REGENERATION);
      types.add(PotionEffectType.NIGHT_VISION);
      sound = Sound.BLOCK_BEACON_POWER_SELECT;
    }
    Collections.shuffle(types, random);
    val duration = random.nextInt(30 * 20, 60 * 20);
    for (int i = 0; i < random.nextInt(1, 4); i++) {
      player.addPotionEffect(new PotionEffect(types.removeFirst(), duration, 1, true, true));
    }
    val loc = player.getLocation();
    loc.getWorld().playSound(loc, sound, 3, 1);
    loc.getWorld()
        .spawnParticle(
            Particle.DUST, loc, 50, 1, 1, 1, 1, new Particle.DustOptions(color, 4), true);
  }
}
