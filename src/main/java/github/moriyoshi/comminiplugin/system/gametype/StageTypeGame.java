package github.moriyoshi.comminiplugin.system.gametype;

import org.bukkit.Location;
import org.bukkit.World;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class StageTypeGame {
  private final World world;
  private final Location center;
  private final double maxBorderSize;
  private final double minBorderSize;
  private final long secondsToMinSize;

  private double speedRate = 1.25;

  private long previousTime;

  private double previousBorderSize;

  public StageTypeGame setSpeedRate(final double speedRate) {
    this.speedRate = speedRate;
    return this;
  }

  public void stageInitialize() {
    this.previousTime = secondsToMinSize;
    this.previousBorderSize = maxBorderSize;

    world.getWorldBorder().setCenter(center);
    world.getWorldBorder().setSize(maxBorderSize);
  }

  public void stageStart() {
    world.getWorldBorder().setSize(minBorderSize, secondsToMinSize);
  }

  public void stageEnd() {
    world.getWorldBorder().reset();
  }

  public void stageSpeedUP() {
    val size = world.getWorldBorder().getSize();
    val speed = previousBorderSize / previousTime;
    val afterTime = (previousTime - ((previousBorderSize - size) / speed)) * (1.0 / speedRate);
    previousTime = (int) afterTime;
    previousBorderSize = size;
    world.getWorldBorder().setSize(minBorderSize, (long) afterTime);
  }
}
