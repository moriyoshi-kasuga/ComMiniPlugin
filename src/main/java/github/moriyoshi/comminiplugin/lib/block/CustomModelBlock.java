package github.moriyoshi.comminiplugin.lib.block;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import github.moriyoshi.comminiplugin.lib.BukkitUtil;
import lombok.Getter;
import lombok.val;
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

@Getter
public abstract class CustomModelBlock extends CustomBlock {

  protected final ItemDisplay display;

  public CustomModelBlock(Block block) {
    super(block);

    display =
        block
            .getWorld()
            .spawn(
                block.getLocation().toCenterLocation(),
                ItemDisplay.class,
                display -> {
                  display.setItemStack(getItem());
                  display.setTransformation(
                      new Transformation(
                          new Vector3f(),
                          new AxisAngle4f(0, 0, 0, 1),
                          new Vector3f(1.001f),
                          new AxisAngle4f(0, 0, 0, 1)));
                  display.setBillboard(Billboard.FIXED);
                  display.setBrightness(new Brightness(0, 15));
                  display.setItemDisplayTransform(ItemDisplayTransform.NONE);
                  display.setRotation(0, 0);
                  display.setPersistent(true);
                });
  }

  public CustomModelBlock(Block block, JsonElement dataElement) {
    this(block);
    val data = dataElement.getAsJsonObject();
    if (data.has("face")) {
      setFace(BlockFace.valueOf(data.get("face").getAsString()));
    }
  }

  public CustomModelBlock(Block block, Player player) {
    this(block);
    setFace(player.getFacing().getOppositeFace());
  }

  public CustomModelBlock(Block block, BlockFace face) {
    this(block);
    setFace(face);
  }

  @Override
  public JsonElement getBlockData() {
    JsonObject json = new JsonObject();
    if (block.getBlockData() instanceof org.bukkit.block.data.Directional directional) {
      json.addProperty("face", directional.getFacing().name());
    }
    return json;
  }

  @Override
  public boolean setFace(BlockFace face) {
    if (super.setFace(face)) {
      display.setRotation(BukkitUtil.convertBlockFaceToYaw(face), 0);
      return false;
    }
    return true;
  }

  public void updateDisplayItem() {
    this.display.setItemStack(getItem());
  }

  @Override
  public void clearData() {
    display.remove();
  }

  public abstract ItemStack getItem();
}
