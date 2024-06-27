package github.moriyoshi.comminiplugin.util;

import java.util.Collection;
import java.util.function.Predicate;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.Enchantment;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class NMSUtil {

  public static ServerPlayer getServerPlayer(Player player) {
    return ((CraftPlayer) player).getHandle();
  }

  /**
   * 特定のプレイヤーにパケットを送ります
   *
   * @param player target
   * @param packet send packet
   */
  public static void sendPacket(final Player player, final Packet<?> packet) {
    ((CraftPlayer) player).getHandle().connection.send(packet);
  }

  /**
   * collection のプレイヤーにパケットを送ります
   *
   * @param collection targets
   * @param packet send packet
   */
  public static void sendPacket(final Collection<Player> collection, final Packet<?> packet) {
    collection.forEach(player -> ((CraftPlayer) player).getHandle().connection.send(packet));
  }

  /**
   * predicate のプレイヤーにパケットを送ります
   *
   * @param predicate targets
   * @param packet send packet
   */
  public static void sendPacket(final Predicate<Player> predicate, final Packet<?> packet) {
    Bukkit.getOnlinePlayers().stream()
        .filter(predicate)
        .forEach(player -> ((CraftPlayer) player).getHandle().connection.send(packet));
  }

  /**
   * 全てのプレイヤーにパケットを送ります
   *
   * @param packet send packet
   */
  public static void sendPacket(final Packet<?> packet) {
    Bukkit.getOnlinePlayers()
        .forEach(player -> ((CraftPlayer) player).getHandle().connection.send(packet));
  }

  public static void sendEntityRemovePacket(Player player, int... entityIds) {
    ClientboundRemoveEntitiesPacket packet = new ClientboundRemoveEntitiesPacket(entityIds);
    sendPacket(player, packet);
  }

  public static void sendEntityRemovePacket(Collection<Player> collection, int... entityIds) {
    ClientboundRemoveEntitiesPacket packet = new ClientboundRemoveEntitiesPacket(entityIds);
    sendPacket(collection, packet);
  }

  public static void sendEntityRemovePacket(Predicate<Player> predicate, int... entityIds) {
    ClientboundRemoveEntitiesPacket packet = new ClientboundRemoveEntitiesPacket(entityIds);
    sendPacket(predicate, packet);
  }

  public static void sendEntityRemovePacket(int... entityIds) {
    ClientboundRemoveEntitiesPacket packet = new ClientboundRemoveEntitiesPacket(entityIds);
    sendPacket(packet);
  }

  private NMSUtil() {}
}
