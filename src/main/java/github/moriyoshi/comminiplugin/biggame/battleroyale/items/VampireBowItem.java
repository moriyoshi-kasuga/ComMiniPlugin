package github.moriyoshi.comminiplugin.biggame.battleroyale.items;

import de.tr7zw.changeme.nbtapi.NBT;
import github.moriyoshi.comminiplugin.lib.item.CooldownItem;
import github.moriyoshi.comminiplugin.lib.item.CustomItem;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import github.moriyoshi.comminiplugin.system.GameListener;
import java.util.List;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class VampireBowItem extends CustomItem
    implements CooldownItem, CustomItem.ProjectileLaunch, CustomItem.HeldOfOther {

  private int level;

  public VampireBowItem() {
    this(
        new ItemBuilder(Material.BOW)
            .name("<red>弓血鬼")
            .lore(
                "<gray>使う者の攻撃力を飛躍的に向上させるが、",
                "<gray>その<u>代償</u>として体力を徐々に削り取る。",
                "<gray>削り取ったHPの量ダメージを増やす")
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
        (projectile, event) -> event.setDamage(event.getFinalDamage() + level));
    List<Sound> sounds;

    if (level >= 14) {
      sounds = List.of(Sound.ENTITY_ARROW_HIT, Sound.BLOCK_NOTE_BLOCK_BELL);
    } else if (level >= 9) {
      sounds = List.of(Sound.ENTITY_ARROW_HIT_PLAYER, Sound.BLOCK_NOTE_BLOCK_PLING);
    } else {
      sounds = List.of(Sound.ENTITY_ARROW_SHOOT, Sound.BLOCK_WOODEN_BUTTON_CLICK_OFF);
    }
    val loc = player.getLocation();
    sounds.forEach(sound -> loc.getWorld().playSound(loc, sound, SoundCategory.MASTER, 3, 1));
  }

  @Override
  public void heldOfOther(PlayerItemHeldEvent e, final Player player) {
    NBT.modify(
        getItem(),
        nbt -> {
          nbt.getCompound(nbtKey).setInteger("level", 0);
        });
    new ItemBuilder(getItem()).name("<red>弓血鬼");
  }

  @Override
  public void heldItem(final Player player) {
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
        getItem(),
        nbt -> {
          nbt.getCompound(nbtKey).setInteger("level", ++level);
        });
    new ItemBuilder(getItem()).name("<red>弓血鬼 +" + level);
  }

  @Override
  public boolean shouldAutoReduceCountDown() {
    return false;
  }
}
