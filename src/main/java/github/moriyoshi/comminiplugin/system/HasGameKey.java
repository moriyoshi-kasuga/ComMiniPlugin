package github.moriyoshi.comminiplugin.system;

import github.moriyoshi.comminiplugin.util.HasKey;

public interface HasGameKey extends HasKey {

  @Override
  AbstractGameKey getKey();
}
