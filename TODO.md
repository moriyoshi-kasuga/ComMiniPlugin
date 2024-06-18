# TOOD

ちょいと `Mysql` を使おう

## System

今ある 修正すべき System

- minigame
- game (big game)
- AbstractGame の showPlayer と hidePlayer の javadoc と命名を見直す
- あと interfacegame に show と hide とか utility method を
  もうちょい作る

## rename

単に GameListner とかだと base かssとかbrとか重複しないゲームなのかがわかりにくい

- Game -> BigGame
- AbstractGame -> Game
- MiniGame はそのまま
