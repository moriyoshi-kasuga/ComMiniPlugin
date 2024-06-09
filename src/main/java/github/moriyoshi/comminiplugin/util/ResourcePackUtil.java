package github.moriyoshi.comminiplugin.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;

public final class ResourcePackUtil {

  private ResourcePackUtil() {}

  public static void updateComMiniResoucePack(Audience audience) {
    final ResourcePackRequest request =
        ResourcePackRequest.resourcePackRequest()
            .packs(buildComMiniResourcePack())
            .prompt(Util.mm("<red>Please download the resource pack!"))
            .required(true)
            .build();

    audience.sendResourcePacks(request);
  }

  public static String getComMiniResourcePackHash() {
    String hash;
    try {
      System.setProperty("http.keepAlive", "false");
      HttpURLConnection connection =
          (HttpURLConnection)
              new URI(
                      "https://raw.githubusercontent.com/moriyoshi-kasuga/ComMiniResoucePack/hash/hash.txt"
                          + "?_="
                          + System.currentTimeMillis())
                  .toURL()
                  .openConnection();
      connection.setRequestMethod("GET");
      connection.setConnectTimeout(1000);
      connection.setReadTimeout(1000);
      connection.setDoInput(true);
      connection.setUseCaches(false);

      // キャッシュ無効化のためのヘッダーを追加
      connection.setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
      connection.setRequestProperty("Pragma", "no-cache");
      connection.setRequestProperty("Expires", "0");

      connection.connect();
      InputStream in = connection.getInputStream();
      final InputStreamReader inReader = new InputStreamReader(in);
      final BufferedReader bufReader = new BufferedReader(inReader);
      hash = bufReader.readLine();

      connection.disconnect();
      bufReader.close();
      inReader.close();
      in.close();
    } catch (Exception ex) {
      ex.printStackTrace();
      hash = "2849ace6aa689a8c610907a41c03537310949294";
    } finally {
      System.setProperty("http.keepAlive", "true");
    }
    return hash;
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
