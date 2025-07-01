package com.hyd.mindpix.components;

import com.hyd.mindpix.Events;
import com.hyd.mindpix.MindPixApplication;
import com.hyd.mindpix.MindPixMain;
import com.hyd.mindpix.utils.FilenameUtils;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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

  private final SimpleBooleanProperty active = new SimpleBooleanProperty(false);

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

    this.activeProperty().addListener((_, _, active) -> {
      if (active) {
        activeStyle();
        MindPixApplication.CURRENT_IMAGE.set(this.imagePath);
      } else {
        defaultStyle();
      }
    });
    defaultStyle();

    // 这里必须用 Platform.runLater 否则无法获得焦点
    this.setOnMousePressed(_ -> MindPixMain.publish(new Events.NavigationEvent.GotoImage(this)));
  }

  private void defaultStyle() {
    this.setStyle("""
      -fx-background-color: transparent;
      -fx-background-insets: 0;
      -fx-padding: 0;
      -fx-border-color: transparent;
      -fx-border-radius: 5;
      -fx-border-width: 5;""");
    this.label.setStyle("""
      -fx-background-color: #00000080;
      -fx-background-insets: 0;
      -fx-padding: 5;
      -fx-text-fill: #ffffff;""");
  }

  private void activeStyle() {
    this.setStyle("""
      -fx-background-color: #00000020;
      -fx-background-insets: 0;
      -fx-padding: 0;
      -fx-border-color: #fb6934;
      -fx-border-radius: 5;
      -fx-border-width: 5;""");
  }

  public void setImage(Image image) {
    this.imageView.setImage(image);
  }

  public void setActive(boolean active) {
    this.active.set(active);
  }

  public boolean isActive() {
    return this.active.get();
  }

  public BooleanProperty activeProperty() {
    return this.active;
  }
}
