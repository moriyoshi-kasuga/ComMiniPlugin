package github.moriyoshi.comminiplugin.system;

import github.moriyoshi.comminiplugin.lib.HasKey;

public interface HasGameKey extends HasKey {

  @Override
  AbstractGameKey getKey();
}
