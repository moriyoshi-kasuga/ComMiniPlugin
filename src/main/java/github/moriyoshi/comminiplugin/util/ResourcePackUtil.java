package github.moriyoshi.comminiplugin.util;

import github.moriyoshi.comminiplugin.lib.BukkitUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLConnection;
import lombok.val;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;

public final class ResourcePackUtil {

  private ResourcePackUtil() {}

  public static void updateComMiniResourcePack(Audience audience) {
    final ResourcePackRequest request =
        ResourcePackRequest.resourcePackRequest()
            .packs(buildComMiniResourcePack())
            .prompt(BukkitUtil.mm("<red>Please download the resource pack!"))
            .required(true)
            .build();

    audience.sendResourcePacks(request);
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

  public static ResourcePackInfo buildComMiniResourcePack() {
    return ResourcePackInfo.resourcePackInfo()
        .uri(
            URI.create(
                "https://github.com/moriyoshi-kasuga/ComMiniResoucePack/releases/download/latest/resources.zip"))
        .hash(getComMiniResourcePackHash())
        .build();
  }
}
