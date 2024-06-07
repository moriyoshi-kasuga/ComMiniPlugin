package github.moriyoshi.comminiplugin.util;

public enum JumpState {
  // 壁や地面につくまで動けません
  FIXED,
  // 上向きに飛ぶときは動けずに、下に向かい始めると動けます (FIXED と同様、壁や地面に当たると動けます)
  DOWN,
  // 自由に動けます
  FREE,
}
