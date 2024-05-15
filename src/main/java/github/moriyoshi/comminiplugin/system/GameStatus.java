package github.moriyoshi.comminiplugin.system;

public enum GameStatus {
  // まだ開始されおらず、参加ができる
  BEFORE_START,
  // すでに開始されていて、参加ができない
  AFTER_START,
  // すでに参加している
  ALREADY,
  // 常に参加できる
  ENABLE,
  // 常に参加できない
  DISABLE,
  // ゲームが開催されていない
  NON;
}
