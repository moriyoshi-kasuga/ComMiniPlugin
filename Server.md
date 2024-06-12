spigot.yml の `entity-tracking-range` をこの値に変えてください (最大描画距離です)

```yml
entity-tracking-range:
  players: 300
  animals: 100
  monsters: 100
  misc: 100
  display: 100
  other: 100
```

bukkit.yml の `connection-throttle` を `-1` に変えてください

```
  connection-throttle: -1
```

paper-global.yml の `velocity` の enabled を true にして secret を velocity/forwarding.secret の値にしてください
