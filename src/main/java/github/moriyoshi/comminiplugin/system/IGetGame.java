package github.moriyoshi.comminiplugin.system;

public interface IGetGame<T extends AbstractGame> {

  @SuppressWarnings("unchecked")
  default T getGame() {
    return (T) GameSystem.getNowGame();
  }

}
