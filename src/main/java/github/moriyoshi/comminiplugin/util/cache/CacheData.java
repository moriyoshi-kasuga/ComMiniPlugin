package github.moriyoshi.comminiplugin.util.cache;

public interface CacheData<T> {

  /**
   * {@link CacheManager} の キーの値を返します
   *
   * @return 保存されているcacheのキーの値を返します
   */
  T getKey();
}
