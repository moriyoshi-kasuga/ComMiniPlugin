package github.moriyoshi.comminiplugin.util;

import com.google.common.collect.Multimap;
import de.tr7zw.changeme.nbtapi.NBT;
import github.moriyoshi.comminiplugin.item.CustomItemFlag;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.val;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

/** アイテムを作成やmodifyするクラス */
public class ItemBuilder {

  /**
   * プレイヤーの頭を作成するクラス
   *
   * @param skullOwner プレイヤーの名前
   * @return 作成したアイテムスタック
   */
  @SuppressWarnings("deprecation")
  public static ItemBuilder createSkull(final String skullOwner) {
    return new ItemBuilder(Material.PLAYER_HEAD)
        .changeMeta((Consumer<SkullMeta>) skullMeta -> skullMeta.setOwner(skullOwner));
  }

  private final ItemStack itemStack;

  /**
   * 引数のMaterialでItemStackを作成します
   *
   * @param material アイテムのマテリアル
   */
  public ItemBuilder(final Material material) {
    this.itemStack = new ItemStack(Objects.requireNonNull(material, "Material cannot be null"));
  }

  /**
   * 渡されたアイテムに対して直接変更を適用します
   *
   * <p>(変更を適用せずに新しく作りたい場合は {@code ItemStack#clone()} を使ってください)
   *
   * @param itemStack 変更を適用するアイテム
   */
  public ItemBuilder(final ItemStack itemStack) {
    Objects.requireNonNull(itemStack, "ItemStack cannot be null");
    this.itemStack = itemStack;
  }

  /**
   * アイテムの数を変更します
   *
   * @param amount 変更する量
   * @return new instance (use {@link #build()} to create)
   */
  public ItemBuilder amount(final int amount) {
    return change(i -> i.setAmount(amount));
  }

  /**
   * エンチャントを設定
   *
   * @param enchantment 追加するエンチャント
   * @param level エンチャントのレベル (6とか100とかでもok)
   * @return new instance (use {@link #build()} to create)
   */
  public ItemBuilder enchant(final Enchantment enchantment, final int level) {
    return change(i -> i.addUnsafeEnchantment(enchantment, level));
  }

  /**
   * エンチャントを削除
   *
   * @param enchantment 削除するエンチャント
   * @return new instance (use {@link #build()} to create)
   */
  public ItemBuilder unEnchant(final Enchantment enchantment) {
    return change(i -> i.removeEnchantment(enchantment));
  }

  /**
   * 耐久値のダメージを設定
   *
   * @param damage ダメージ
   * @return new instance (use {@link #build()} to create)
   */
  public ItemBuilder damage(final int damage) {
    return changeItemMeta(meta -> ((Damageable) meta).setDamage(damage));
  }

  /**
   * 耐久値の設定
   *
   * @param damage 耐久値
   * @return new instance (use {@link #build()} to create)
   */
  public ItemBuilder durability(final int damage) {
    return changeItemMeta(
        meta ->
            ((Damageable) meta)
                .setDamage(Math.abs(itemStack.getType().getMaxDurability() - damage)));
  }

  /**
   * マテリアルの変更
   *
   * @param type 変更するマテリアル
   * @return new instance (use {@link #build()} to create)
   */
  @SuppressWarnings("deprecation")
  public ItemBuilder type(final Material type) {
    return change(i -> i.setType(type));
  }

  /**
   * アイテムの名前を変更
   *
   * @param displayName 変更するアイテム名
   * @return new instance (use {@link #build()} to create)
   */
  public ItemBuilder name(final Object displayName) {
    return changeItemMeta(meta -> meta.displayName(Util.mm(displayName)));
  }

  /**
   * loreの変更
   *
   * @param lore 変更するlore
   * @return new instance (use {@link #build()} to create)
   */
  public ItemBuilder lore(@NotNull final List<?> lore) {
    return changeItemMeta(meta -> meta.lore(Util.ListMM(lore)));
  }

  /**
   * loreの変更
   *
   * @param lore 変更するlore
   * @return new instance (use {@link #build()} to create)
   */
  public ItemBuilder lore(@NotNull final Object... lore) {
    return lore(Arrays.asList(lore));
  }

  /**
   * loreの追加
   *
   * @param str 追加するloreの文
   * @return new instance (use {@link #build()} to create)
   */
  public ItemBuilder addLore(final Object str) {
    final List<Component> lore = getLore();
    lore.add(Util.mm(str));
    return lore(lore);
  }

  /**
   * loreの {@code line} 行目に {@code str} を挿入します
   *
   * @param line loreの行 (0の場合は一番最初に入ります)
   * @param str 挿入するloreの文
   * @return new instance (use {@link #build()} to create)
   */
  public ItemBuilder insertLore(final int line, final Object str) {
    final List<Component> lore = getLore();
    lore.add(line, Util.mm(str));
    return lore(lore);
  }

  /**
   * loreの {@code line} 行目に {@code str} を上書きします
   *
   * @param line 上書きする行
   * @param str 上書きする文
   * @return new instance (use {@link #build()} to create)
   */
  public ItemBuilder setLore(final int line, final Object str) {
    final List<Component> lore = getLore();
    while (lore.size() <= line) {
      lore.add(Util.mm(" "));
    }
    lore.set(line, Util.mm(str));
    return lore(lore);
  }

  /**
   * loreから {@code line} 行目 を削除
   *
   * @param line 削除する行
   * @return new instance (use {@link #build()} to create)
   */
  public ItemBuilder removeLore(final int line) {
    final List<Component> lore = getLore();
    lore.remove(line);
    return lore(lore);
  }

  /**
   * {@link #lore(List)} を使用するために簡単にloreを取得できるように
   *
   * @return このアイテムのlore
   */
  public List<Component> getLore() {
    final ItemMeta itemMeta = itemStack.getItemMeta();
    if (itemMeta.hasLore()) {
      return itemMeta.lore();
    }
    final List<Component> lore = new ArrayList<>();
    itemMeta.lore(lore);
    return lore;
  }

  /**
   * アイテムをエンチャントで光らせます
   *
   * @return new instance (use {@link #build()} to create)
   */
  public ItemBuilder glow() {
    return enchant(Enchantment.LURE, 1).flags(ItemFlag.HIDE_ENCHANTS);
  }

  /**
   * アイテムを {@link #glow()} で光らせてる場合に削除します
   *
   * @return new instance (use {@link #build()} to create)
   */
  public ItemBuilder unGlow() {
    return unEnchant(Enchantment.LURE).unFlags(ItemFlag.HIDE_ENCHANTS);
  }

  /**
   * アイテムを壊せるかどうか設定します
   *
   * @param unbreakable 壊せないかどうか (true = 不可壊)
   * @return new instance (use {@link #build()} to create)
   */
  public ItemBuilder unbreakable(final boolean unbreakable) {
    return changeItemMeta(meta -> meta.setUnbreakable(unbreakable));
  }

  /**
   * {@link ItemFlag} をアイテムに追加します
   *
   * @param flags 追加するフラッグたち
   * @return new instance (use {@link #build()} to create)
   */
  public ItemBuilder flags(final ItemFlag... flags) {
    return changeItemMeta(meta -> meta.addItemFlags(flags));
  }

  /**
   * {@link ItemFlag} をアイテムから削除します
   *
   * @param flags 削除するフラッグたち
   * @return new instance (use {@link #build()} to create)
   */
  public ItemBuilder unFlags(final ItemFlag... flags) {
    return changeItemMeta(meta -> meta.removeItemFlags(flags));
  }

  /**
   * {@link ItemMeta#setAttributeModifiers(Multimap)} を見てください
   *
   * @param attributeModifiers arg
   * @return new instance (use {@link #build()} to create)
   */
  public ItemBuilder attributes(final Multimap<Attribute, AttributeModifier> attributeModifiers) {
    return changeItemMeta(meta -> meta.setAttributeModifiers(attributeModifiers));
  }

  /**
   * {@link Attribute} を追加します
   *
   * @param attribute 追加する対象
   * @param attributeModifier 詳細設定
   * @return new instance (use {@link #build()} to create)
   */
  public ItemBuilder addAttribute(
      final Attribute attribute, final AttributeModifier attributeModifier) {
    return changeItemMeta(meta -> meta.addAttributeModifier(attribute, attributeModifier));
  }

  /**
   * マップで {@link #addAttribute(Attribute, AttributeModifier)} をできるように
   *
   * @param attributeModifiers arg
   * @return new instance (use {@link #build()} to create)
   */
  public ItemBuilder addAttributes(
      final Multimap<Attribute, AttributeModifier> attributeModifiers) {
    return attributeModifiers.entries().stream()
        .reduce(
            this,
            (itemBuilder, entry) -> itemBuilder.addAttribute(entry.getKey(), entry.getValue()),
            (first, second) -> second);
  }

  /**
   * Entryで {@link #addAttribute(Attribute, AttributeModifier)} をできるように
   *
   * @param attributeModifiers arg
   * @return new instance (use {@link #build()} to create)
   */
  @SafeVarargs
  public final ItemBuilder addAttributes(
      final Map.Entry<Attribute, AttributeModifier>... attributeModifiers) {
    ItemBuilder result = this;
    for (val entry : attributeModifiers) {
      result = result.addAttribute(entry.getKey(), entry.getValue());
    }
    return result;
  }

  /**
   * カスタムモデルデータを設定します
   *
   * @param customModelData 値
   * @return new instance (use {@link #build()} to create)
   */
  public ItemBuilder customModelData(final Integer customModelData) {
    return changeItemMeta(meta -> meta.setCustomModelData(customModelData));
  }

  /**
   * {@link #createSkull(String)} のように先にItemMetaのキャストをしたいときに使用してください
   *
   * @param consumer 実行する処理
   * @param <IM> ItemMeta の サブクラス
   * @return new instance (use {@link #build()} to create)
   */
  @SuppressWarnings("unchecked")
  public <IM extends ItemMeta> ItemBuilder changeMeta(final Consumer<IM> consumer) {
    return changeItemMeta(m -> consumer.accept((IM) m));
  }

  /**
   * meta を変更する時に使用します
   *
   * @param consumer 実行する処理
   * @return new instance (use {@link #build()} to create)
   */
  public ItemBuilder changeItemMeta(final Consumer<? super ItemMeta> consumer) {
    return change(
        i -> {
          final ItemMeta meta = i.getItemMeta();
          consumer.accept(meta);
          i.setItemMeta(meta);
        });
  }

  public ItemBuilder customItemFlag(final String flag, boolean isEnable) {
    return change(
        i ->
            NBT.modify(
                i,
                nbt -> {
                  nbt.getOrCreateCompound("customitemflag").setBoolean(flag, isEnable);
                }));
  }

  public ItemBuilder customItemFlag(final CustomItemFlag flag, boolean isEnable) {
    return customItemFlag(flag.id, isEnable);
  }

  /**
   * アイテムを変更する際に使用します
   *
   * @param consumer 変更する処理
   * @return new instance (use {@link #build()} to create)
   */
  public ItemBuilder change(final Consumer<? super ItemStack> consumer) {
    consumer.accept(this.itemStack);
    return this;
  }

  /**
   * アイテム自体で処理する際に使用します
   *
   * @param function 処理
   * @return new instance (use {@link #build()} to create)
   */
  public ItemBuilder map(final Function<? super ItemStack, ? extends ItemStack> function) {
    return new ItemBuilder(function.apply(build()));
  }

  public ItemStack build() {
    return itemStack;
  }
}
