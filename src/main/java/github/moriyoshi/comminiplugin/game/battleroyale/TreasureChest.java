package github.moriyoshi.comminiplugin.game.battleroyale;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonObject;

import github.moriyoshi.comminiplugin.block.CustomModelBlock;
import github.moriyoshi.comminiplugin.constant.ComMiniPrefix;
import github.moriyoshi.comminiplugin.util.ItemBuilder;

public class TreasureChest extends CustomModelBlock {

  private int level = 1;

  @Override
  public void interact(PlayerInteractEvent e) {
    ComMiniPrefix.MAIN.send(e.getPlayer(), "<red>don't touch me!");
  }

  @Override
  public void blockBreak(BlockBreakEvent e) {
    ComMiniPrefix.MAIN.send(e.getPlayer(), "<red>don't break me!");
  }

  public TreasureChest(Location location) {
    super(location);
  }

  public TreasureChest(Location location, JsonObject data) {
    super(location, data);
    if (data.has("level")) {
      this.level = data.get("level").getAsInt();
    }
    updateDisplayItem();
  }

  public TreasureChest(Location location, Player player) {
    super(location, player);
  }

  @Override
  public ItemStack getItem() {
    return new ItemBuilder(Material.STONE).customModelData(level).build();
  }

  @Override
  public Material getOriginMaterial() {
    return Material.CHEST;
  }

}
