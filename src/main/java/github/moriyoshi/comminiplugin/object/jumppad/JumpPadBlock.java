package github.moriyoshi.comminiplugin.object.jumppad;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.block.CustomBlock;
import github.moriyoshi.comminiplugin.util.BukkitUtil;
import github.moriyoshi.comminiplugin.util.JumpState;
import github.moriyoshi.comminiplugin.util.tuple.Pair;
import java.util.Objects;
import java.util.Random;
import java.util.stream.IntStream;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class JumpPadBlock extends CustomBlock {

  private BukkitRunnable task;
  @Getter @Setter private float angel = 30;
  @Getter @Setter private float direction = -1;
  @Getter @Setter private float power = 10;
  @Getter private Material material = Material.BEDROCK;
  @Getter @Setter private Sound sound = Sound.ENTITY_ENDER_DRAGON_FLAP;
  @Getter @Setter private Particle particle = Particle.HAPPY_VILLAGER;
  @Getter @Setter private JumpState state = JumpState.DOWN;

  public JumpPadBlock(Block block, Player player) {
    super(block, player);
    spawn();
    val loc = getBlock().getLocation();
    for (val pair :
        IntStream.range(0, 4)
            .boxed()
            .map(
                i ->
                    Pair.of(
                        Math.round(Math.cos(i * Math.PI / 2)),
                        Math.round(Math.sin(i * Math.PI / 2))))
            .toList()) {
      val b = loc.clone().add(pair.getFirst(), 0, pair.getSecond());
      if (!CustomBlock.isCustomBlock(b, JumpPadBlock.class)) {
        continue;
      }
      val jp = (JumpPadBlock) CustomBlock.getCustomBlock(b, JumpPadBlock.class);
      setAngel(jp.getAngel());
      setDirection(jp.getDirection());
      setPower(jp.getPower());
      setMaterial(jp.getMaterial());
      setSound(jp.getSound());
      setParticle(jp.getParticle());
      setState(jp.getState());
      break;
    }
  }

  public JumpPadBlock(Block block, JsonElement dataElement) {
    super(block, dataElement);
    val data = dataElement.getAsJsonObject();
    this.angel = data.get("angel").getAsFloat();
    this.power = data.get("power").getAsFloat();
    if (data.has("direction")) {
      this.direction = data.get("direction").getAsFloat();
    }
    setMaterial(Material.valueOf(data.get("material").getAsString()));
    this.sound = Sound.valueOf(data.get("sound").getAsString());
    if (data.has("particle")) {
      this.particle = Particle.valueOf(data.get("particle").getAsString());
    }
    this.state =
        JumpState.valueOf(
            Objects.requireNonNullElse(data.get("state"), new JsonPrimitive("DOWN")).getAsString());
    spawn();
  }

  public JumpPadBlock(Block block) {
    super(block);
    spawn();
  }

  public void setMaterial(Material material) {
    this.material = material;
    getBlock().setType(material);
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
    data.addProperty("state", state.name());
    return data;
  }

  @Override
  public @NotNull Material getOriginMaterial() {
    return material == null ? Material.BEDROCK : material;
  }

  @Override
  public void walk(PlayerMoveEvent e) {
    val player = e.getPlayer();
    if (BukkitUtil.isFalling(player.getUniqueId())) {
      return;
    }
    val launchLocation = e.getTo().clone();
    launchLocation.getWorld().playSound(launchLocation, sound, SoundCategory.MASTER, 1, 1);
    launchLocation.setPitch(-angel);
    if (direction != -1.0) {
      launchLocation.setYaw(direction);
    }
    BukkitUtil.setVelocity(
        player, launchLocation.getDirection().normalize().multiply(power / 16f), state);
  }

  private void spawn() {
    val random = new Random();
    this.task =
        new BukkitRunnable() {
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
