package github.moriyoshi.comminiplugin.dependencies.ui.mask;

import static github.moriyoshi.comminiplugin.dependencies.ui.mask.Shapes.chest;
import static github.moriyoshi.comminiplugin.dependencies.ui.mask.Shapes.combine;
import static github.moriyoshi.comminiplugin.dependencies.ui.mask.Shapes.generic;
import static github.moriyoshi.comminiplugin.dependencies.ui.mask.Shapes.grid;

import java.util.Arrays;
import java.util.Objects;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Mule;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.AbstractHorseInventory;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.LlamaInventory;

/**
 * Represents the shape of an inventory.
 */
public interface Shape {

  // InventoryType shapes
  Shape ANVIL = combine(generic(2, SlotType.CRAFTING), generic(1, SlotType.RESULT));
  Shape BARREL = grid(9, 3, SlotType.CONTAINER);
  Shape BEACON = generic(1, SlotType.CRAFTING);
  Shape BLAST_FURNACE = combine(generic(1, SlotType.CRAFTING), generic(1, SlotType.FUEL),
      generic(1, SlotType.RESULT));
  Shape BREWING = combine(generic(3, SlotType.RESULT), generic(1, SlotType.CRAFTING),
      generic(1, SlotType.FUEL));
  Shape CARTOGRAPHY = combine(generic(2, SlotType.CRAFTING), generic(1, SlotType.RESULT));
  Shape CHEST1 = chest(1);
  Shape CHEST2 = chest(2);
  Shape CHEST3 = chest(3);
  Shape CHEST4 = chest(4);
  Shape CHEST5 = chest(5);
  Shape CHEST6 = chest(6);
  Shape CHISELED_BOOKSHELF = generic(6, SlotType.CONTAINER);
  Shape COMPOSTER = generic(1, SlotType.CONTAINER);
  Shape CRAFTING = combine(grid(2, 2, SlotType.CRAFTING), generic(1, SlotType.RESULT));
  Shape CREATIVE = grid(9, 1, SlotType.QUICKBAR);
  Shape DISPENSER = grid(3, 3, SlotType.CONTAINER);
  Shape DROPPER = grid(3, 3, SlotType.CONTAINER);
  Shape ENCHANTING = generic(2, SlotType.CRAFTING);
  Shape ENDER_CHEST = chest(3);
  Shape FURNACE = combine(generic(1, SlotType.CRAFTING), generic(1, SlotType.FUEL),
      generic(1, SlotType.RESULT));
  Shape GRINDSTONE = combine(generic(2, SlotType.CRAFTING), generic(1, SlotType.RESULT));
  Shape HOPPER = grid(5, 1, SlotType.CONTAINER);
  Shape JUKEBOX = generic(1, SlotType.CONTAINER);
  Shape LECTERN = generic(1, SlotType.CONTAINER);
  Shape LOOM = combine(generic(3, SlotType.CRAFTING), generic(1, SlotType.RESULT));
  Shape MERCHANT = combine(generic(2, SlotType.CRAFTING), generic(1, SlotType.RESULT));
  Shape PLAYER = combine(grid(9, 1, SlotType.QUICKBAR), grid(9, 3, SlotType.CONTAINER),
      generic(4, SlotType.ARMOR),
      generic(1, SlotType.CONTAINER /* off hand */));
  Shape SHULKER_BOX = grid(9, 3, SlotType.CONTAINER);
  Shape SMITHING = combine(generic(2, SlotType.CRAFTING), generic(1, SlotType.RESULT));
  Shape SMITHING_NEW = combine(generic(3, SlotType.CRAFTING), generic(1, SlotType.RESULT));
  Shape SMOKER = combine(generic(1, SlotType.CRAFTING), generic(1, SlotType.FUEL),
      generic(1, SlotType.RESULT));
  Shape STONECUTTER = combine(generic(1, SlotType.CRAFTING), generic(1, SlotType.RESULT));
  Shape WORKBENCH = combine(grid(3, 3, SlotType.CRAFTING), generic(1, SlotType.RESULT));

  // entity inventory shapes
  Shape HORSE = generic(2, SlotType.ARMOR);
  Shape MULE = generic(1, SlotType.ARMOR);
  Shape CHEST_MULE = combine(MULE, grid(5, 3, SlotType.CONTAINER));
  Shape LLAMA = generic(1, SlotType.ARMOR);
  Shape CHEST_LLAMA = combine(LLAMA, grid(3, 3, SlotType.CONTAINER));
  Shape VILLAGER = generic(8, SlotType.CONTAINER);

  static Shape determine(Inventory inventory) {
    switch (inventory.getType()) {
      case ANVIL:
        return ANVIL;
      case BARREL:
        return BARREL;
      case BEACON:
        return BEACON;
      case BLAST_FURNACE:
        return BLAST_FURNACE;
      case BREWING:
        return BREWING;
      case CARTOGRAPHY:
        return CARTOGRAPHY;
      case CHEST:
        final int size = inventory.getSize();
        switch (size) {
          case 9:
            return CHEST1;
          case 18:
            return CHEST2;
          case 27:
            return CHEST3;
          case 36:
            return CHEST4;
          case 45:
            return CHEST5;
          case 54:
            return CHEST6;
          default:
            if (size % 9 == 0) {
              return chest(size / 9);
            } else
            // can't be sure that it's actually a grid. just return a generic shape.
            {
              return generic(size, SlotType.CONTAINER);
            }
        }
      case CHISELED_BOOKSHELF:
        return CHISELED_BOOKSHELF;
      case COMPOSTER:
        return COMPOSTER;
      case CRAFTING:
        return CRAFTING;
      case CREATIVE:
        return CREATIVE;
      case DISPENSER:
        return DISPENSER;
      case DROPPER:
        return DROPPER;
      case ENCHANTING:
        return ENCHANTING;
      case ENDER_CHEST:
        return ENDER_CHEST;
      case FURNACE:
        return FURNACE;
      case GRINDSTONE:
        return GRINDSTONE;
      case HOPPER:
        return HOPPER;
      case JUKEBOX:
        return JUKEBOX;
      case LECTERN:
        return LECTERN;
      case LOOM:
        return LOOM;
      case MERCHANT:
        return MERCHANT;
      case PLAYER:
        return PLAYER;
      case SHULKER_BOX:
        return SHULKER_BOX;
      case SMITHING:
        return SMITHING;
      case SMOKER:
        return SMOKER;
      case STONECUTTER:
        return STONECUTTER;
      case WORKBENCH:
        return WORKBENCH;
      default:
        // there does not seem to be any horse- or llama inventory type.
        // I think bukkit should have these. (with isCreatable() returning false).

        if (inventory instanceof LlamaInventory) {
          if (((Llama) inventory.getHolder()).isCarryingChest()) {
            return CHEST_LLAMA;
          } else {
            return LLAMA;
          }
        } else if (inventory instanceof HorseInventory) {
          return HORSE;
        } else if (inventory instanceof AbstractHorseInventory) {
          if (((Mule) inventory.getHolder()).isCarryingChest()) {
            return CHEST_MULE;
          } else {
            return MULE;
          }
        }

        // fallback
        assert false;
        return generic(inventory.getSize(), SlotType.CONTAINER);
    }
  }

  int size();

  Pattern<SlotType> toPattern();

}

class Shapes {

  private Shapes() {
  }

  static GridShape chest(int rows) {
    return grid(9, rows, SlotType.CONTAINER);
  }

  static GenericShape generic(int size, SlotType slotType) {
    return new GenericShape(size, slotType);
  }

  static CombinedShape combine(Shape... shapes) {
    Objects.requireNonNull(shapes, "shapes cannot be null");
    if (shapes.length <= 1) {
      throw new IllegalArgumentException("It is non-sensical to combine less than 2 shapes");
    }

    return new CombinedShape(shapes);
  }

  static GridShape grid(int width, int height, SlotType slotType) {
    return new GridShape(height, width, slotType);
  }
}

final class GenericShape implements Shape {

  private final int size;
  private final SlotType slotType;

  GenericShape(int size, SlotType slotType) {
    this.size = size;
    this.slotType = slotType;
  }

  @Override
  public int size() {
    return getSize();
  }

  @Override
  public Pattern<SlotType> toPattern() {
    return i -> {
      if (0 <= i && i < size()) {
        return getSlotType();
      } else {
        return SlotType.OUTSIDE;
      }
    };
  }

  int getSize() {
    return size;
  }

  public SlotType getSlotType() {
    return slotType;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof GenericShape that)) {
      return false;
    }

    return this.getSize() == that.getSize();
  }

  @Override
  public int hashCode() {
    return size;
  }

  @Override
  public String toString() {
    return "GenericShape(size=" + getSize() + ")";
  }
}

record CombinedShape(Shape... shapes) implements Shape {

  @Override
  public int size() {
    return Arrays.stream(shapes).mapToInt(Shape::size).sum();
  }

  @Override
  public Pattern<SlotType> toPattern() {
    return slotIndex -> {
      if (slotIndex < 0) {
        return SlotType.OUTSIDE;
      }

      int sum = 0;
      int shapeIndex = 0;

      do {
        int lastSum = sum;

        Shape shape = shapes[shapeIndex];
        sum += shape.size();

        if (slotIndex < sum) {
          int innerIndex = slotIndex - lastSum;
          return shape.toPattern().getSymbol(innerIndex);
        }

        shapeIndex += 1;
      } while (shapeIndex < shapes.length);

      return SlotType.OUTSIDE;
    };
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof CombinedShape that)) {
      return false;
    }

    return Arrays.equals(this.shapes(), that.shapes());
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(shapes);
  }

  @Override
  public String toString() {
    return "CombinedShape(shapes=" + Arrays.toString(shapes) + ")";
  }
}

record GridShape(int rows, int columns, SlotType slotType) implements Shape {

  @Override
  public int size() {
    return rows() * columns();
  }

  @Override
  public Pattern<SlotType> toPattern() {
    return slotIndex -> {
      if (slotIndex < 0 || slotIndex >= size()) {
        return SlotType.OUTSIDE;
      } else {
        return slotType();
      }
    };
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof GridShape that)) {
      return false;
    }

    return this.rows() == that.rows()
        && this.columns() == that.columns();
  }

  @Override
  public int hashCode() {
    return Objects.hash(rows(), columns());
  }

  @Override
  public String toString() {
    return "GridShape(rows=" + rows() + ",columns=" + columns() + ")";
  }
}
