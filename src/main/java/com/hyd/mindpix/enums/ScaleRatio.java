package com.hyd.mindpix.enums;

public enum ScaleRatio {
  SCALE_25(0.25, "25%"),
  SCALE_50(0.5, "50%"),
  SCALE_75(0.75, "75%"),
  SCALE_100(1.0, "100%"),
  SCALE_125(1.25, "125%"),
  SCALE_150(1.5, "150%"),
  SCALE_200(2.0, "200%"),
  SCALE_300(3.0, "300%"),
  SCALE_400(4.0, "400%");

  private final double ratio;
  private final String displayName;

  ScaleRatio(double ratio, String displayName) {
    this.ratio = ratio;
    this.displayName = displayName;
  }

  public double getRatio() {
    return ratio;
  }

  public String getDisplayName() {
    return displayName;
  }

  @Override
  public String toString() {
    return displayName;
  }

  public static ScaleRatio getDefault() {
    return SCALE_100;
  }

  public ScaleRatio getNext() {
    ScaleRatio[] values = values();
    int currentIndex = this.ordinal();
    return values[(currentIndex + 1) % values.length];
  }
}
