package github.moriyoshi.comminiplugin.game.battleroyale;

import org.bukkit.Location;

import lombok.Getter;

public class BRField {

  @Getter
  private final String name;
  @Getter
  private final Location lobby;
  private final int max_radius_range;
  private final int min_border_range;
  private final int max_second;

  private final TreasureLocation treasure;

  public BRField(String name, Location lobby, int max_radius_range, int min_border_range, int max_second) {
    this.name = name;
    this.lobby = lobby;
    this.max_radius_range = max_radius_range;
    this.min_border_range = min_border_range;
    this.max_second = max_second;
    this.treasure = new TreasureLocation(name);
  }

  public void start() {

  }

  public void stop() {

  }

}
