package github.moriyoshi.comminiplugin.object.jumppad;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.block.CustomBlock;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

public class JumpPadBlock extends CustomBlock {

  private static Set<FallingBlock> fallingBlocks = new HashSet<>();

  public static void clear() {
    fallingBlocks.forEach(falling -> {
      if (!falling.isDead()) {
        falling.remove();
      }
    });
    fallingBlocks.clear();
  }

  private BukkitRunnable task;

  @Getter
  @Setter
  private float angel = 30;
  @Getter
  @Setter
  private float direction = -1;
  @Getter
  @Setter
  private float power = 10;
  @Getter
  @Setter
  private Material material = Material.BEDROCK;
  @Getter
  @Setter
  private Sound sound = Sound.ENTITY_ENDER_DRAGON_FLAP;
  @Getter
  @Setter
  private Particle particle = Particle.HAPPY_VILLAGER;

  public JumpPadBlock(Block block, Player player) {
    super(block, player);
    spawn();
  }

  public JumpPadBlock(Block block, JsonElement dataElement) {
    super(block, dataElement);
    val data = dataElement.getAsJsonObject();
    this.angel = data.get("angel").getAsFloat();
    this.power = data.get("power").getAsFloat();
    if (data.has("direction")) {
      this.direction = data.get("direction").getAsFloat();
    }
    this.material = Material.valueOf(data.get("material").getAsString());
    getBlock().setType(material);
    this.sound = Sound.valueOf(data.get("sound").getAsString());
    if (data.has("particle")) {
      this.particle = Particle.valueOf(data.get("particle").getAsString());
    }
    spawn();
  }

  public JumpPadBlock(Block block) {
    super(block);
    spawn();
  }

  public void settings(Player player) {
    new JumpPadSettingsMenu(this).openInv(player);
  }

  @Override
  public JsonElement getBlockData() {
    val data = new JsonObject();
    data.addProperty("angel", angel);
    data.addProperty("power", power);
    if (direction != -1.0) {
      data.addProperty("direction", direction);
    }
    data.addProperty("material", material.name());
    data.addProperty("sound", sound.name());
    if (particle != null) {
      data.addProperty("particle", particle.name());
    }
    return data;
  }

  @Override
  public @NotNull Material getOriginMaterial() {
    return material == null ? Material.BEDROCK : material;
  }

  public void jump(Player player, Location launchLocation) {
    launchLocation.setPitch(-angel);
    if (direction != -1.0) {
      launchLocation.setYaw(direction);
    }
    val falling = launchLocation.getWorld().spawn(launchLocation,
        FallingBlock.class, entity -> {
          entity.setBlockData(Material.MOVING_PISTON.createBlockData());
          entity.setInvisible(true);
          entity.setSilent(true);
          entity.setGravity(true);
          entity.setDropItem(false);
          entity.setCancelDrop(true);
          entity.setVelocity(launchLocation.getDirection().normalize().multiply(power / 15f));
        });

    fallingBlocks.add(falling);

    new BukkitRunnable() {

      @Override
      public void run() {
        if (falling.isDead()) {
          this.cancel();
          return;
        }
        if (0 >= falling.getVelocity().getY() || 0 == falling.getVelocity().getX() || 0 == falling.getVelocity().getZ()
            || falling.isOnGround()) {
          falling.remove();
          this.cancel();
          return;
        }
        player.setVelocity(falling.getVelocity());
      }

    }.runTaskTimer(ComMiniPlugin.getPlugin(), 0, 1);
    val loc = getBlock().getLocation();
    loc.getWorld().playSound(loc, sound, SoundCategory.MASTER, 1, 1);
  }

  private void spawn() {
    val random = new Random();
    this.task = new BukkitRunnable() {
      private final Location shift = getBlock().getLocation().add(0.5, 0.9, 0.5);

      @Override
      public void run() {
        if (particle != null && random.nextInt(5) >= 3) {
          shift.getWorld().spawnParticle(particle, shift, 1, 0.4, 0.2, 0.4);
        }
      }

    };
    task.runTaskTimer(ComMiniPlugin.getPlugin(), 1, 1);
  }

  @Override
  public void clearData() {
    this.task.cancel();
  }

}
