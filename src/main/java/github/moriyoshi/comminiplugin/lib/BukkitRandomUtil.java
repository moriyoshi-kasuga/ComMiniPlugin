package github.moriyoshi.comminiplugin.lib;

import io.papermc.paper.entity.TeleportFlag;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.jetbrains.annotations.Nullable;

public final class BukkitRandomUtil {

  private final World world;
  private final int bx;
  private final int bz;
  private final int radius;

  @Accessors(chain = true)
  @Setter
  private int maxTry = 100;

  @Accessors(chain = true)
  @Setter
  private boolean shouldChunkLoad = true;

  @Accessors(chain = true)
  @Setter
  private int chunkLoadRadius = 3;

  @Accessors(chain = true)
  @Setter
  private HeightMap heightMap = HeightMap.WORLD_SURFACE;

  @Accessors(chain = true)
  @Setter
  private Predicate<Block> predicate = (block) -> block.isSolid() && block.isCollidable();

  @Accessors(chain = true)
  @Setter
  @Nullable
  private Random random;

  public BukkitRandomUtil(final Location center, final int radius) {
    this(center.getWorld(), center.getBlockX(), center.getBlockZ(), radius);
  }

  public BukkitRandomUtil(final World world, final int bx, final int bz, final int radius) {
    this.world = world;
    this.bx = bx;
    this.bz = bz;
    this.radius = radius;
  }

  public BukkitRandomUtil setRandom() {
    this.random = new Random();
    return this;
  }

  public CompletableFuture<Boolean> randomTeleport(final Entity entity) {
    return randomTopBlock()
        .thenComposeAsync(
            block -> {
              if (block == null) {
                return CompletableFuture.completedStage(false);
              }
              return teleportOnTheBlock(block, entity);
            });
  }

  public static CompletableFuture<Boolean> teleportOnTheBlock(
      final Block block, final Entity entity) {
    return entity.teleportAsync(
        block.getLocation().add(0.5, 1, 0.5),
        TeleportCause.PLUGIN,
        TeleportFlag.Relative.YAW,
        TeleportFlag.Relative.PITCH);
  }

  public CompletableFuture<Block> randomTopBlock() {
    return randomTopBlock(maxTry);
  }

  private CompletableFuture<Block> randomTopBlock(int current) {
    if (current == 0) {
      return CompletableFuture.completedFuture(null);
    }
    val x = random.nextInt(-radius, radius) + bx;
    val z = random.nextInt(-radius, radius) + bz;
    return world
        .getChunkAtAsync(x >> 4, z >> 4)
        .thenComposeAsync(
            chunk -> {
              val block = world.getHighestBlockAt(x, z, heightMap);
              if (predicate.test(block)) {
                if (shouldChunkLoad) {
                  return loadChunks(block, chunkLoadRadius).thenApply(__ -> block);
                }
                return CompletableFuture.completedStage(block);
              }
              return randomTopBlock(current - 1);
            });
  }

  public static CompletableFuture<Void> loadChunks(Block block) {
    return loadChunks(block, 3);
  }

  public static CompletableFuture<Void> loadChunks(Block block, int radius) {
    val x = block.getX() >> 4;
    val z = block.getZ() >> 4;
    val world = block.getWorld();
    return CompletableFuture.allOf(
        IntStream.rangeClosed(-radius, radius)
            .boxed()
            .flatMap(
                first ->
                    IntStream.rangeClosed(-radius, radius)
                        .mapToObj(second -> world.getChunkAtAsync(x + first, z + second)))
            .toArray(CompletableFuture[]::new));
  }

  public static <E>
      Collector<CompletableFuture<E>, List<CompletableFuture<E>>, CompletableFuture<Stream<E>>>
          toFutureOfAll() {
    return Collector.of(
        ArrayList::new,
        List::add,
        (a, b) -> Stream.of(a, b).flatMap(List::stream).collect(Collectors.toList()),
        futures ->
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(nothing -> futures.stream().map(CompletableFuture::join)));
  }
}
