package com.hyd.mindpix.components;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

public class ImagePreview extends BorderPane {

  private final ImageView imageView = new ImageView();

  public ImagePreview() {
     this.setCenter(imageView);
  }

  public void setImage(Image image) {
    imageView.setImage(image);
    setCenter(imageView);
  }
}
