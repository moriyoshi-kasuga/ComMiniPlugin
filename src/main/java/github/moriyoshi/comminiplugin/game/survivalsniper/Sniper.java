package github.moriyoshi.comminiplugin.game.survivalsniper;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import github.moriyoshi.comminiplugin.item.CooldownItem;
import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import github.moriyoshi.comminiplugin.util.ItemUtil;
import github.moriyoshi.comminiplugin.util.Util;
import net.kyori.adventure.text.Component;

public class Sniper extends CustomItem implements CooldownItem {

  private static final Component DEFAULT_NAME = Util.mm("<blue>スナイパー");
  private static final Vector EYE_SIZE = new Vector(0.3, 0.3, 0.3);
  private static final int MAX_LENGTH = 1000;
  private static final int DEFAULT_COOLDOWN_TICK = 15;

  public Sniper() {
    this(new ItemBuilder(Material.SPYGLASS).name(DEFAULT_NAME).customModelData(1).build());
  }

  public Sniper(ItemStack item) {
    super(item);
  }

  @Override
  public @NotNull String getIdentifier() {
    return "sniper";
  }

  @Override
  public boolean isInteractCancel() {
    return false;
  }

  @Override
  public void swapToOffHand(PlayerSwapHandItemsEvent e) {
    e.setCancelled(true);
    var p = e.getPlayer();
    var eyeLoc = p.getEyeLocation();
    if (inCooldown()) {
      p.playSound(eyeLoc, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
      return;
    }
    var optBullet = BULLET.firstBullet(p);
    if (optBullet.isEmpty()) {
      p.playSound(eyeLoc, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
      return;
    }
    var bullet = optBullet.get();
    ItemUtil.removeItemStack(p, bullet.item, 1);
    p.playSound(eyeLoc, bullet.sound, 1, 1);
    setCooldown(DEFAULT_COOLDOWN_TICK);
    var world = eyeLoc.getWorld();
    var vec = eyeLoc.getDirection().normalize().multiply(0.1);
    var loc = eyeLoc.clone();
    var already = new HashSet<Entity>();
    for (int i = 1; i < MAX_LENGTH; i += 1) {
      loc.add(vec);
      if (i % 5 == 0) {
        world.spawnParticle(Particle.WAX_OFF, loc, 1, 0, 0, 0, 1, null, true);
      }
      loc.getNearbyLivingEntities(2).forEach(entity -> {
        if (entity != p && !already.contains(entity)
            && entity.getBoundingBox().contains(loc.x(), loc.y(), loc.z())) {
          already.add(entity);
          var eye = entity.getEyeLocation().toVector();
          var min = eye.clone().subtract(EYE_SIZE);
          var max = eye.clone().add(EYE_SIZE);
          var isHeadShot = BoundingBox.of(min, max).contains(loc.toVector());
          entity.damage(isHeadShot ? bullet.headshot : bullet.damage);
          p.playSound(eyeLoc, isHeadShot ? Sound.ENTITY_EXPERIENCE_ORB_PICKUP : Sound.BLOCK_ANVIL_BREAK,
              isHeadShot ? 1 : 2, 1);
        }
      });
    }
    p.getInventory().setItemInMainHand(new ItemBuilder(getItem()).type(Material.CLOCK).build());
  }

  @Override
  public void heldOfThis(PlayerItemHeldEvent e) {
    Player player = e.getPlayer();
    player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1, 1);
  }

  @Override
  public void runTick(Player player) {
    if (!countDown()) {
      new ItemBuilder(getItem()).type(Material.SPYGLASS);
    }
  }

  @Override
  public void dropItem(PlayerDropItemEvent e) {
    e.setCancelled(true);
  }

  @Override
  public boolean canMoveOtherInv(InventoryClickEvent e) {
    return false;
  }

  private static final Set<BULLET> BULLETS = Set.of(BULLET.values());

  private static enum BULLET {
    IRON(Sound.ENTITY_FIREWORK_ROCKET_BLAST, new ItemStack(Material.IRON_NUGGET), 5, 10),
    GOLD(Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, new ItemStack(Material.GOLD_NUGGET), 7, 14),
    DIAMOND(Sound.ENTITY_GENERIC_EXPLODE, new ItemStack(Material.DIAMOND), 20, 40);

    public final Sound sound;
    public final ItemStack item;
    public final int damage;
    public final int headshot;

    private BULLET(Sound sound, ItemStack item, int damage, int headshot) {
      this.sound = sound;
      this.item = item;
      this.damage = damage;
      this.headshot = headshot;
    }

    private static final Optional<BULLET> firstBullet(Player p) {
      ItemStack[] inventory = p.getInventory().getContents();
      for (ItemStack item : inventory) {
        for (var bullet : BULLETS) {
          if (bullet.item.isSimilar(item)) {
            return Optional.of(bullet);
          }
        }
      }
      return Optional.empty();
    }
  }

}
