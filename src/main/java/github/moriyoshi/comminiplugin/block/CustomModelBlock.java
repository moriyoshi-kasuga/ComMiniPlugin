package github.moriyoshi.comminiplugin.block;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.Display.Brightness;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import com.google.gson.JsonObject;

import lombok.Getter;

public abstract class CustomModelBlock extends CustomBlock {

  @Getter
  protected final ItemDisplay display;

  public CustomModelBlock(Block block) {
    super(block);
    block.setType(getOriginMaterial());

    display = block.getWorld().spawn(block.getLocation().add(0.5f, 0.5f, 0.5f), ItemDisplay.class, display -> {
      display.setItemStack(getItem());
      display.setTransformation(new Transformation(new Vector3f(), new AxisAngle4f(0, 0, 0, 1),
          new Vector3f(1.001f), new AxisAngle4f(0, 0, 0, 1)));
      display.setBillboard(Billboard.FIXED);
      display.setBrightness(new Brightness(0, 15));
      display.setItemDisplayTransform(ItemDisplayTransform.NONE);
      display.setRotation(0, 0);
      display.setPersistent(true);
    });
  }

  public CustomModelBlock(Block block, JsonObject data) {
    this(block);
  }

  public CustomModelBlock(Block block, Player player) {
    this(block);
    setFace(player.getFacing().getOppositeFace());
  }

  public void setFace(BlockFace face) {
    if (block.getBlockData() instanceof org.bukkit.block.data.Directional directional) {
      directional.setFacing(face);
      block.setBlockData(directional);
      display.setRotation(switch (face) {
        case NORTH -> 180;
        case WEST -> 90;
        case EAST -> -90;
        default -> 0;
      }, 0);
    }
  }

  public void updateDisplayItem() {
    this.display.setItemStack(getItem());
  }

  @Override
  public void clearData() {
    display.remove();
    block.setType(Material.AIR);
  }

  public abstract ItemStack getItem();

  public abstract Material getOriginMaterial();

}
