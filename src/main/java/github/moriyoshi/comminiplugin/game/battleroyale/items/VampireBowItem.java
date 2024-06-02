package github.moriyoshi.comminiplugin.game.battleroyale.items;

import de.tr7zw.changeme.nbtapi.NBT;
import github.moriyoshi.comminiplugin.item.CooldownItem;
import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.system.GameListener;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class VampireBowItem extends CustomItem implements CooldownItem {

  private int level;

  public VampireBowItem() {
    this(
        new ItemBuilder(Material.BOW)
            .name("<red>弓血鬼")
            .lore("<gray>使う者の攻撃力を飛躍的に向上させるが、", "<gray>その<u>代償</u>として体力を徐々に削り取る。")
            .customModelData(1)
            .build());
  }

  public VampireBowItem(@NotNull ItemStack item) {
    super(item);
    NBT.modify(
        item,
        nbt -> {
          level = nbt.getCompound(nbtKey).getOrDefault("level", 0);
        });
  }

  @Override
  public void projectileLaunch(ProjectileLaunchEvent e, Player player) {
    GameListener.addProjectileDamageListener(
        e.getEntity().getUniqueId(),
        (projectile, event) -> {
          event.setDamage(event.getDamage() + level);
        });
    List<Sound> sounds;

    if (level >= 14) {
      sounds = List.of(Sound.ENTITY_ARROW_HIT, Sound.BLOCK_NOTE_BLOCK_BELL);
    } else if (level >= 9) {
      sounds = List.of(Sound.ENTITY_ARROW_HIT_PLAYER, Sound.BLOCK_NOTE_BLOCK_PLING);
    } else {
      sounds = List.of(Sound.ENTITY_ARROW_SHOOT, Sound.BLOCK_WOODEN_BUTTON_CLICK_OFF);
    }
    val loc = player.getLocation();
    sounds.forEach(
        sound -> {
          loc.getWorld().playSound(loc, sound, SoundCategory.MASTER, 1, 1);
        });
  }

  @Override
  public void heldOfOther(PlayerItemHeldEvent e) {
    NBT.modify(
        getItem(),
        nbt -> {
          nbt.getCompound(nbtKey).setInteger("level", 0);
        });
    new ItemBuilder(getItem()).name("<red>弓血鬼");
  }

  @Override
  public Optional<BiConsumer<Player, ItemStack>> heldItem() {
    return Optional.of(
        (player, item) -> {
          if (7 > player.getHealth()) {
            return;
          }
          if (!inCooldown()) {
            setCooldown(40);
            return;
          }
          if (countDown()) {
            return;
          }
          player.damage(1);
          NBT.modify(
              item,
              nbt -> {
                nbt.getCompound(nbtKey).setInteger("level", ++level);
              });
          new ItemBuilder(getItem()).name("<red>弓血鬼 +" + level);
        });
  }
}
