package github.moriyoshi.comminiplugin.util;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ServerPlayer;
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
   * @param collection targets
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

  public static void sendPlayerHidePackt(Player player, List<UUID> profileIds) {
    final ClientboundPlayerInfoRemovePacket packet =
        new ClientboundPlayerInfoRemovePacket(profileIds);
    sendPacket(player, packet);
  }

  public static void sendPlayerHidePackt(Collection<Player> collection, List<UUID> profileIds) {
    final ClientboundPlayerInfoRemovePacket packet =
        new ClientboundPlayerInfoRemovePacket(profileIds);
    sendPacket(collection, packet);
  }

  public static void sendPlayerHidePackt(Predicate<Player> predicate, List<UUID> profileIds) {
    final ClientboundPlayerInfoRemovePacket packet =
        new ClientboundPlayerInfoRemovePacket(profileIds);
    sendPacket(predicate, packet);
  }

  public static void sendPlayerHidePackt(List<UUID> profileIds) {
    final ClientboundPlayerInfoRemovePacket packet =
        new ClientboundPlayerInfoRemovePacket(profileIds);
    sendPacket(packet);
  }

  public static void sendPlayerShowPackt(Player player, Collection<Player> hidePlayers) {
    final ClientboundPlayerInfoUpdatePacket packet =
        ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(
            hidePlayers.stream().map(p -> ((CraftPlayer) p).getHandle()).toList());
    sendPacket(player, packet);
  }

  public static void sendPlayerShowPackt(
      Collection<Player> collection, Collection<Player> hidePlayers) {
    final ClientboundPlayerInfoUpdatePacket packet =
        ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(
            hidePlayers.stream().map(p -> ((CraftPlayer) p).getHandle()).toList());
    sendPacket(collection, packet);
  }

  public static void sendPlayerShowPackt(
      Predicate<Player> predicate, Collection<Player> hidePlayers) {
    final ClientboundPlayerInfoUpdatePacket packet =
        ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(
            hidePlayers.stream().map(p -> ((CraftPlayer) p).getHandle()).toList());
    sendPacket(predicate, packet);
  }

  public static void sendPlayerShowPackt(Collection<Player> hidePlayers) {
    final ClientboundPlayerInfoUpdatePacket packet =
        ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(
            hidePlayers.stream().map(p -> ((CraftPlayer) p).getHandle()).toList());
    sendPacket(packet);
  }

  public static void sendNMSPlayerShowPackt(Player player, Collection<ServerPlayer> hidePlayers) {
    final ClientboundPlayerInfoUpdatePacket packet =
        ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(hidePlayers);
    sendPacket(player, packet);
  }

  public static void sendNMSPlayerShowPackt(
      Collection<Player> collection, Collection<ServerPlayer> hidePlayers) {
    final ClientboundPlayerInfoUpdatePacket packet =
        ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(hidePlayers);
    sendPacket(collection, packet);
  }

  public static void sendNMSPlayerShowPackt(
      Predicate<Player> predicate, Collection<ServerPlayer> hidePlayers) {
    final ClientboundPlayerInfoUpdatePacket packet =
        ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(hidePlayers);
    sendPacket(predicate, packet);
  }

  public static void sendNMSPlayerShowPackt(Collection<ServerPlayer> hidePlayers) {
    final ClientboundPlayerInfoUpdatePacket packet =
        ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(hidePlayers);
    sendPacket(packet);
  }

  private NMSUtil() {}
}
