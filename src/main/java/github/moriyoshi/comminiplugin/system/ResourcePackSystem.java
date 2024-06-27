package github.moriyoshi.comminiplugin.system;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.lib.BukkitUtil;
import github.moriyoshi.comminiplugin.lib.HashUUID;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLConnection;
import java.util.UUID;
import lombok.val;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import org.bukkit.scheduler.BukkitRunnable;

public final class ResourcePackSystem {

  private ResourcePackSystem() {}

  private static String oldHash;

  public static void load() {
    oldHash = ResourcePackSystem.getComMiniResourcePackHash();
    new BukkitRunnable() {

      @Override
      public void run() {
        val newHash = ResourcePackSystem.getComMiniResourcePackHash();
        if (!oldHash.equalsIgnoreCase(newHash)) {
          oldHash = newHash;
          ComMiniPlugin.MAIN.broadCast("<red>リソースパックの更新があります!<gray>メニューから更新してください");
        }
      }
    }.runTaskTimer(ComMiniPlugin.getPlugin(), 20 * 10, 20 * 10);
  }

  private static final UUID uuid = HashUUID.v5("ComMiniResourcePack");

  public static void updateComMiniResourcePack(Audience audience) {
    final ResourcePackRequest request =
        ResourcePackRequest.resourcePackRequest()
            .packs(
                ResourcePackInfo.resourcePackInfo()
                    .id(uuid)
                    .uri(
                        URI.create(
                            "https://github.com/moriyoshi-kasuga/ComMiniResoucePack/releases/download/latest/resources.zip"))
                    .hash(oldHash)
                    .build())
            .prompt(BukkitUtil.mm("<red>Please download the resource pack!"))
            .build();

    audience.sendResourcePacks(request);
  }

  public static void removeResourcePack(Audience audience) {
    audience.removeResourcePacks(uuid);
  }

  public static String getComMiniResourcePackHash() {
    try {
      val url =
          URI.create(
                  "https://raw.githubusercontent.com/moriyoshi-kasuga/ComMiniResoucePack/hash/hash.txt")
              .toURL();

      URLConnection conn = url.openConnection();

      BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

      String hash = br.readLine();
      br.close();
      return hash;
    } catch (IOException e) {
      return "2849ace6aa689a8c610907a41c03537310949294";
    }
  }
}
