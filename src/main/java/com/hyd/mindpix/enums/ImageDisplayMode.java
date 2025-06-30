package com.hyd.mindpix.enums;

public enum ImageDisplayMode {
  DYNAMIC("动态适应"),
  FIXED("固定尺寸");

  private final String displayName;

  ImageDisplayMode(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

  @Override
  public String toString() {
    return displayName;
  }
}
