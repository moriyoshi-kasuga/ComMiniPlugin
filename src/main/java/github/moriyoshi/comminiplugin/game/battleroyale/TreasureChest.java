package github.moriyoshi.comminiplugin.game.battleroyale;

import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonElement;

import github.moriyoshi.comminiplugin.block.CustomModelBlock;
import github.moriyoshi.comminiplugin.constant.ComMiniPrefix;
import github.moriyoshi.comminiplugin.system.GameSystem;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import lombok.val;
import org.jetbrains.annotations.NotNull;

public class TreasureChest extends CustomModelBlock {

  // 鉄Lv.1 エメラルドLv.2 ラピスラズリLv.3 ダイアlv.4 アメジストLv.5
  private final int level;

  public TreasureChest(Block block, Player player) {
    super(block, player);
    this.level = 1;
    spawn();
  }

  public TreasureChest(Block block) {
    super(block);
    this.level = 1;
    spawn();
  }

  public TreasureChest(Block block, int level) {
    super(block);
    this.level = level;
    spawn();
  }

  @Override
  public void interact(PlayerInteractEvent e) {
    e.setCancelled(false);
  }

  @Override
  public void blockBreak(BlockBreakEvent e) {
    ComMiniPrefix.MAIN.send(e.getPlayer(), "<red>don't break me!");
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
  public @NotNull Material getOriginMaterial() {
    return Material.CHEST;
  }

  private void spawn() {
    updateDisplayItem();
    if (!GameSystem.isIn(BRGame.class)) {
      return;
    }
    Optional.ofNullable(GameSystem.getGame(BRGame.class).getField()).ifPresent(field -> {
      val loottable = switch (this.level) {
        case 1 -> field.getLevel1();
        case 2 -> field.getLevel2();
        case 3 -> field.getLevel3();
        case 4 -> field.getLevel4();
        default -> field.getLevel5();
      };
      loottable.setChest(getBlock().getLocation());
    });
  }

}