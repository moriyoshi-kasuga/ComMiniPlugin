package github.moriyoshi.comminiplugin.game.battleroyale;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import github.moriyoshi.comminiplugin.block.CustomModelBlock;
import github.moriyoshi.comminiplugin.constant.ComMiniPrefix;
import github.moriyoshi.comminiplugin.util.ItemBuilder;

public class TreasureChest extends CustomModelBlock {

  // 鉄Lv.1 エメラルドLv.2 ラピスラズリLv.3 ダイアlv.4 アメジストLv.5
  private int level = 1;

  @Override
  public void interact(PlayerInteractEvent e) {
    e.setCancelled(false);
  }

  @Override
  public void blockBreak(BlockBreakEvent e) {
    ComMiniPrefix.MAIN.send(e.getPlayer(), "<red>don't break me!");
  }

  public TreasureChest(Block block) {
    super(block);
    updateDisplayItem();
  }

  public TreasureChest(Block block, int level) {
    super(block);
    this.level = level;
    updateDisplayItem();
  }

  public TreasureChest(Block block, JsonObject data) {
    super(block, data);
    this.level = data.get("level").getAsInt();
    updateDisplayItem();
  }

  public TreasureChest(Block block, Player player) {
    super(block, player);
    updateDisplayItem();
  }

  public TreasureChest(Block block, Player player, int level) {
    super(block, player);
    this.level = level;
    updateDisplayItem();
  }

  @Override
  public JsonElement getBlockData() {
    return null;
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
