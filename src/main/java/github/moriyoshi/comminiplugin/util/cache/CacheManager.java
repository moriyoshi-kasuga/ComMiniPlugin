package github.moriyoshi.comminiplugin.util.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * cacheを保存するためのクラス
 *
 * @param <K> キーのクラス
 * @param <V> 値のクラス {@link CacheData}
 */
@SuppressWarnings("unchecked")
public class CacheManager<K, V extends CacheData<K>> {

  private final Map<K, Map<Class<? extends V>, V>> cache = new HashMap<>();

  /**
   * このclassのcacheを持っているか確認します
   *
   * @param key キー
   * @param clazz cacheのクラス
   * @return cacheがある場合trueを返します
   */
  public boolean hasCache(K key, Class<? extends V> clazz) {
    return getCacheData(key).containsKey(clazz);
  }

  /**
   * classのcacheを取得します
   *
   * @param key キー
   * @param clazz cacheのクラス
   * @return cacheのクラスのインスタンスを返します
   */
  public <T extends V> T getCache(K key, Class<T> clazz) {
    if (!hasCache(key, clazz)) {
      return null;
    }
    return (T) getCacheData(key).get(clazz);
  }

  /**
   * cacheを保存します
   *
   * @param key キー
   * @param playerData cacheのインスタンス
   */
  public <T extends V> void putCache(K key, T playerData) {
    getCacheData(key).put((Class<T>) playerData.getClass(), playerData);
  }

  /**
   * cacheがない場合にその値を保存して返しますある場合はあるcacheを取得して返します
   *
   * @param key キー
   * @param playerData cacheが保存されていない場合に保存し返す値
   * @return cache
   */
  public <T extends V> T computeGetCache(K key, Class<T> clazz, Supplier<T> playerData) {
    return (T) getCacheData(key).computeIfAbsent(clazz, tClass -> playerData.get());
  }

  /**
   * cacheの削除
   *
   * @param key キー
   * @param playerData cacheのクラス
   */
  public void removeCache(K key, Class<? extends V> playerData) {
    getCacheData(key).remove(playerData);
  }

  /**
   * キーのすべてのcacheのデータを取得します
   *
   * @param key キー
   * @return キーのすべてのcache
   */
  public Map<Class<? extends V>, V> getCacheData(K key) {
    return cache.computeIfAbsent(key, o -> new HashMap<>());
  }
}
