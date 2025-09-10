package com.hyd.mindpix.utils;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ImageUtils {

  public static Image resize(Image source, int targetWidth, int targetHeight, boolean preserveRatio) {
    ImageView imageView = new ImageView(source);
    imageView.setPreserveRatio(preserveRatio);
    imageView.setSmooth(true);
    imageView.setFitWidth(targetWidth);
    imageView.setFitHeight(targetHeight);
    return imageView.snapshot(null, null);
  }
}
