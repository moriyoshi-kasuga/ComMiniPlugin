package github.moriyoshi.comminiplugin.system.game;

public enum GameButtonType {
  BEFORE_START,
  AFTER_START,
  ALREADY,
  ENABLE,
  DISABLE;
  // TODO: abstractGameにisCanAddPlayer() -> this class を実装してそこで
  // addspecbuttonとかでonClickの中で実装してあとはgenerateItem(ItemStack item) とかでdescription
  // をうわがきするとか
}
