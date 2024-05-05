package github.moriyoshi.comminiplugin.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.BlockStateArgument;
import dev.jorel.commandapi.arguments.EnchantmentArgument;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import github.moriyoshi.comminiplugin.util.FormatterUtil;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import github.moriyoshi.comminiplugin.util.PrefixUtil;
import github.moriyoshi.comminiplugin.util.Util;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

public class ItemEditCommand extends CommandAPICommand {

  private static final PrefixUtil IE = new PrefixUtil(
      Util.mm("<gold>[<aqua>IE<gold>] "));

  private static final String HASITEM = "<red>アイテムを手に持ってください";
  private static final String RENAME = "<green>名前を<reset>[ <reset>{name}<reset> ]<green>にしました";
  private static final String ONLYLORE = "<blue>{lore}<red>列しかアイテムにはありません";
  private static final String ADDLORE = "<green>loreに<reset>[ <reset>{name}<reset> ]<green>を追加しました";
  private static final String SETLORE = "<green>loreの<blue>{lore}<green>行目を<reset>[ <reset>{name}<reset> ]<green>にしました";
  private static final String INSERTLORE = "<green>loreの<blue>{lore}<green>行目に<reset>[ <reset>{name}<reset> ]<green>を挿入しました";
  private static final String REMOVELORE = "<green>loreの<blue>{lore}<green>行目を削除しました";
  private static final String CUSTOMMODELDATA = "<green>カスタムモデルデータを<blue>{data}<green>にしました";
  private static final String GETSKULL = "<blue>{player}<green>の頭にしました";
  private static final String DURABILITY = "<green>耐久値を<blue>{durability}<green>にしました";
  private static final String MAXDURABILITY = "<red>アイテムの最大耐久値より以下にしてください";
  private static final String UNBREAKABLE = "<green>不可解にしました";
  private static final String BREAKABLE = "<green>不可解を解除しました";
  private static final String TYPE = "<blue>{type}<green>にしました";
  private static final String GLOW = "<green>アイテムを光らせました";
  private static final String UNGLOW = "<green>アイテムの光を解除しました";
  private static final String ENCHANT_SET = "<blue>{level}<green>Lvl <blue>{enchant}<green>エンチャントを付けました";
  private static final String ENCHANT_REMOVE = "<blue>{enchant}<green>エンチャントを削除しました";

  private static final List<String> help = new ArrayList<>() {
    {
      add("<green>/rename [文字] <red>(名前を[文字]に変更します)");
      add("<green>/addlore [文字] <red>(loreに[文字]を追加します)");
      add("<green>/setlore [行数] [文字] <red>([行数]に[文字]を設定します)");
      add("<green>/insertlore [行数] [文字] <red>([行数]に[文字]を追加にします)");
      add("<green>/removelore [行数] <red>([行数]を削除します)");
      add("<green>/ie custommodeldata [整数値] <red>(custommodeldataを[整数値]にします)");
      add("<green>/ie durability [整数値] <red>(耐久値を[整数値]にします)");
      add("<green>/ie unbreakable <red>(不可解にします)");
      add("<green>/ie breakable <red>(不可解を解除します)");
      add("<green>/ie type [material] <red>(typeを[material]にします)");
      add("<green>/ie getskull [player] <red>([player]の頭を渡します)");
      add("<green>/ie glow <red>(アイテムを光らせます)");
      add("<green>/ie unglow <red>(アイテムを光りを解除します)");
      add("<green>/ie enchant set [enchant] [level] <red>([enchant][level]を付けます)");
      add("<green>/ie enchant remove [enchant] <red>([enchant]を削除します)");
    }
  };

  public ItemEditCommand() {
    super("itemedit");
    setAliases(new String[] { "ie" });
    withPermission(CommandPermission.OP);
    withSubcommand(new CommandAPICommand("help")
        .executesPlayer((player, objects) -> {
          help.forEach(s -> IE.send(player, s));
        }));
    withSubcommand(
        new CommandAPICommand("removelore")
            .withArguments(new IntegerArgument("line", 1))
            .withArguments(new GreedyStringArgument("name"))
            .executesPlayer((sender, args) -> {
              itemEdit(sender, builder -> {
                int arg = (int) args.get(0);
                if (builder.getLore().size() < arg) {
                  IE.send(sender,
                      FormatterUtil.format(ONLYLORE, Map.of("lore", builder.getLore().size())));
                  return builder;
                }
                IE.send(sender, FormatterUtil.format(REMOVELORE, Map.of("lore", arg)));
                return builder.removeLore(arg - 1);
              });
            }));
    withSubcommand(
        new CommandAPICommand("addlore")
            .withArguments(new GreedyStringArgument("name"))
            .executesPlayer((sender, args) -> {
              String s = args.get(0).toString();
              itemEdit(sender, builder -> {
                IE.send(sender, FormatterUtil.format(ADDLORE, Map.of("name", s)));
                return builder.addLore(Util.mm(s));
              });
            }));
    withSubcommand(
        new CommandAPICommand("setlore")
            .withArguments(new IntegerArgument("line", 1))
            .withArguments(new GreedyStringArgument("name"))
            .executesPlayer((player, objects) -> {
              itemEdit(player, builder -> {
                int i = (int) objects.get(0);
                String s = objects.get(1).toString();
                IE.send(player,
                    FormatterUtil.format(SETLORE, Map.of("lore", i, "name", s)));
                return builder.setLore(i - 1, Util.mm(s));
              });
            }));
    withSubcommand(
        new CommandAPICommand("insertlore")
            .withArguments(new IntegerArgument("line", 1))
            .withArguments(new GreedyStringArgument("name"))
            .executesPlayer((player, objects) -> {
              itemEdit(player, builder -> {
                int i = (int) objects.get(0);
                if (builder.getLore().size() < i) {
                  IE.send(player,
                      FormatterUtil.format(ONLYLORE, Map.of("lore", builder.getLore().size())));
                  return builder;
                }
                String s = objects.get(1).toString();
                IE.send(player,
                    FormatterUtil.format(INSERTLORE, Map.of("lore", i, "name", s)));
                return builder.insertLore(i - 1, Util.mm(s));
              });
            }));
    withSubcommand(
        new CommandAPICommand("rename")
            .withPermission(CommandPermission.OP)
            .withArguments(new GreedyStringArgument("name"))
            .executesPlayer((sender, args) -> {
              itemEdit(sender, builder -> {
                String s = args.get(0).toString();
                IE.send(sender, FormatterUtil.format(RENAME, Map.of("name", s)));
                return builder.name(Util.mm(s));
              });
            }));
    withSubcommand(
        new CommandAPICommand("getskull")
            .withArguments(new EntitySelectorArgument.OnePlayer("player"))
            .executesPlayer((player, objects) -> {
              String s = ((Player) objects.get("player")).getName();
              IE.send(player, FormatterUtil.format(GETSKULL, Map.of("player", s)));
              player.getInventory().addItem(ItemBuilder.createSkull(s).build());
            }));
    withSubcommand(
        new CommandAPICommand("custommodeldata")
            .withArguments(new IntegerArgument("value"))
            .executesPlayer((player, objects) -> {
              itemEdit(player, builder -> {
                int object = (int) objects.get(0);
                IE.send(player,
                    FormatterUtil.format(CUSTOMMODELDATA, Map.of("data", object)));
                return builder.changeItemMeta(itemMeta -> itemMeta.setCustomModelData(object));
              });
            }));
    withSubcommand(
        new CommandAPICommand("unbreakable")
            .executesPlayer((player, objects) -> {
              itemEdit(player, builder -> {
                IE.send(player, UNBREAKABLE);
                return builder.unbreakable(true);
              });
            }));
    withSubcommand(
        new CommandAPICommand("breakable")
            .executesPlayer((player, objects) -> {
              itemEdit(player, builder -> {
                IE.send(player, BREAKABLE);
                return builder.unbreakable(false);
              });
            }));
    withSubcommand(new CommandAPICommand("enchant")
        .withSubcommand(
            new CommandAPICommand("set")
                .withArguments(new EnchantmentArgument("enchant"))
                .executesPlayer((player, objects) -> {
                  itemEdit(player, builder -> {
                    Enchantment object = (Enchantment) objects.get(0);
                    IE.send(player,
                        FormatterUtil.format(ENCHANT_SET,
                            Map.of("level", 1, "enchant", object.getKey().getKey())));
                    return builder.enchant(object, 1);
                  });
                }))
        .withSubcommand(
            new CommandAPICommand("set")
                .withArguments(new EnchantmentArgument("enchant"))
                .withArguments(new IntegerArgument("level", 0))
                .executesPlayer((player, objects) -> {
                  itemEdit(player, builder -> {
                    Enchantment object = (Enchantment) objects.get(0);
                    int i = (int) objects.get(1);
                    IE.send(player,
                        FormatterUtil.format(ENCHANT_SET,
                            Map.of("level", i, "enchant", object.getKey()
                                .getKey())));
                    return builder.enchant(object, i);
                  });
                }))
        .withSubcommand(
            new CommandAPICommand("remove")
                .withArguments(new EnchantmentArgument("enchant"))
                .executesPlayer((player, objects) -> {
                  itemEdit(player, builder -> {
                    Enchantment object = (Enchantment) objects.get(0);
                    IE.send(player,
                        FormatterUtil.format(ENCHANT_REMOVE,
                            Map.of("enchant", object.getKey().getKey())));
                    return builder.unEnchant(object);
                  });
                })));
    withSubcommand(
        new CommandAPICommand("type")
            .withArguments(new BlockStateArgument("material"))
            .executesPlayer((sender, args) -> {
              itemEdit(sender, builder -> {
                Material material = ((BlockState) args.get("material")).getType();
                IE.send(sender, FormatterUtil.format(TYPE, Map.of("type", material.name())));
                return builder.type(material);
              });
            }));
    withSubcommand(
        new CommandAPICommand("durability")
            .withArguments(new IntegerArgument("durability"))
            .executesPlayer((sender, args) -> {
              itemEdit(sender, builder -> {
                int max = builder.build().getType().getMaxDurability();
                int size = (int) args.get(0);
                if (size > max) {
                  IE.send(sender, MAXDURABILITY);
                  return builder;
                }
                IE.send(sender,
                    FormatterUtil.format(DURABILITY,
                        Map.of("durability", size)));
                return builder.changeMeta(
                    (Consumer<Damageable>) consumer -> consumer.setDamage(max - size));
              });
            }));
    withSubcommand(new CommandAPICommand("glow")
        .executesPlayer((player, objects) -> {
          itemEdit(player, builder -> {
            IE.send(player, GLOW);
            return builder.glow();
          });
        }));
    withSubcommand(new CommandAPICommand("unglow")
        .executesPlayer((player, objects) -> {
          itemEdit(player, builder -> {
            IE.send(player, UNGLOW);
            return builder.unGlow();
          });
        }));
  }

  private void itemEdit(Player player, Function<ItemBuilder, ItemBuilder> function) {
    ItemStack item = player.getInventory().getItemInMainHand();
    if (item.getType() == Material.AIR) {
      IE.send(player, HASITEM);
      return;
    }
    function.apply(new ItemBuilder(item));
  }
}
