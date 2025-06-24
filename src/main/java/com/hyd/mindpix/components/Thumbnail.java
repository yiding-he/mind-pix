package com.hyd.mindpix.components;

import com.hyd.mindpix.MindPixApplication;
import com.hyd.mindpix.utils.FilenameUtils;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public class Thumbnail extends StackPane {

  public static final Image PLACEHOLDER_IMAGE = new Image(Objects.requireNonNull(
    Thumbnail.class.getResourceAsStream("/placeholder.png")
  ));

  @Getter
  private final ImageView imageView;

  @Getter
  private final Label label;

  @Getter
  private final String imagePath;

  public Thumbnail(String imagePath) {
    this(PLACEHOLDER_IMAGE, imagePath);
  }

  public Thumbnail(Image image, String imagePath) {
    this.setMaxWidth(180);
    this.setMaxHeight(180);

    this.imagePath = imagePath;
    this.imageView = new ImageView(image);
    this.label = new Label(FilenameUtils.getFileNameFromPath(imagePath));

    imageView.setPreserveRatio(true);
    imageView.setFitHeight(180);
    imageView.setFitWidth(180);

    label.setPadding(new Insets(5));
    label.setAlignment(Pos.BOTTOM_CENTER);
    label.setWrapText(true);

    StackPane.setAlignment(label, Pos.BOTTOM_CENTER);
    getChildren().addAll(imageView, label);

    this.focusedProperty().addListener((_, _, focused) -> {
      if (focused) {
        log.info("Thumbnail instance get focused: {}", this);
        focusedStyle();
        MindPixApplication.CURRENT_IMAGE.set(this.imagePath);
      } else {
        log.info("Thumbnail instance lost focus: {}", this);
        defaultStyle();
        if (MindPixApplication.CURRENT_IMAGE.get().equals(this.imagePath)) {
          MindPixApplication.CURRENT_IMAGE.set(null);
        }
      }
    });
    defaultStyle();

    // 这里必须用 Platform.runLater 否则无法获得焦点
    this.setFocusTraversable(true);
    this.setOnMousePressed(_ -> Platform.runLater(this::requestFocus));
  }

  private void defaultStyle() {
    this.setStyle("""
      -fx-background-color: transparent;
      -fx-background-insets: 0;
      -fx-padding: 0;
      -fx-border-color: transparent;
      -fx-border-width: 2;""");
    this.label.setStyle("""
      -fx-background-color: #00000080;
      -fx-background-insets: 0;
      -fx-padding: 5;
      -fx-text-fill: #ffffff;""");
  }

  private void focusedStyle() {
    this.setStyle("""
      -fx-background-color: #00000020;
      -fx-background-insets: 0;
      -fx-padding: 0;
      -fx-border-color: #61a6df;
      -fx-border-width: 2;""");
  }

  public void setImage(Image image) {
    this.imageView.setImage(image);
  }
}
