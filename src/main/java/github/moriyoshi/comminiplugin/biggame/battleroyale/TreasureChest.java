package github.moriyoshi.comminiplugin.biggame.battleroyale;

import com.google.gson.JsonElement;
import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.lib.block.CustomModelBlock;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import github.moriyoshi.comminiplugin.system.BigGameSystem;

import java.util.Optional;
import lombok.Getter;
import lombok.val;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class TreasureChest extends CustomModelBlock {

  @Getter private final int level;
  private BukkitRunnable task;

  public TreasureChest(Block block, BlockFace face, int level) {
    super(block, face);
    this.level = level;
    display.setGlowing(true);
    display.setGlowColorOverride(Color.fromRGB(0xCF2424));
    display.setViewRange((float) 0.15);
    spawn();
  }

  @Override
  public void blockBreak(BlockBreakEvent e) {
    ComMiniPlugin.MAIN.send(e.getPlayer(), "<red>don't break me!");
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

  @Override
  public void clearData() {
    super.clearData();
    if (this.task != null) {
      this.task.cancel();
    }
  }

  private void spawn() {
    updateDisplayItem();
    if (!BigGameSystem.isIn(BRBigBigGame.class)) {
      return;
    }
    Optional.ofNullable(BigGameSystem.getGame(BRBigBigGame.class).getField())
        .ifPresent(
            field -> {
              val loottable =
                  switch (this.level) {
                    case 1 -> field.getLevel1();
                    case 2 -> field.getLevel2();
                    case 3 -> field.getLevel3();
                    case 4 -> field.getLevel4();
                    default -> field.getLevel5();
                  };
              loottable.setChest(getBlock().getLocation());
              this.task =
                  new BukkitRunnable() {

                    @Override
                    public void run() {
                      if (getBlock().getState() instanceof Chest chest) {
                        val inv = chest.getInventory();
                        if (inv.isEmpty() && inv.getViewers().size() == 0) {
                          remove();
                        }
                      }
                    }
                  };
              this.task.runTaskTimer(ComMiniPlugin.getPlugin(), 20L, 20L);
            });
  }
}
