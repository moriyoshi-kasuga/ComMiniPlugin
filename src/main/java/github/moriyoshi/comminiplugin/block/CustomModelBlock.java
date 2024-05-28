package github.moriyoshi.comminiplugin.block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Display.Billboard;
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
  private final ItemDisplay display;

  public CustomModelBlock(Location location) {
    super(location);
    location.getBlock().setType(getOriginMaterial());
    display = location.getWorld().spawn(location, ItemDisplay.class, display -> {
      display.setItemStack(getItem());
      display.setTransformation(
          new Transformation(new Vector3f(), new AxisAngle4f(), new Vector3f(1.001f), new AxisAngle4f()));
      display.setBillboard(Billboard.FIXED);
      display.setItemDisplayTransform(ItemDisplayTransform.FIXED);
      display.setRotation(0, 0);
      display.setPersistent(true);
    });
  }

  public CustomModelBlock(Location location, JsonObject data) {
    this(location);
  }

  public CustomModelBlock(Location location, Player player) {
    this(location);
  }

  @Override
  public void clearData(Location location) {
    display.remove();
    location.getBlock().setType(Material.AIR);
  }

  public abstract ItemStack getItem();

  public abstract Material getOriginMaterial();

}
