package com.hyd.mindpix.components;

import com.hyd.mindpix.enums.ImageDisplayMode;
import com.hyd.mindpix.enums.ScaleRatio;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImagePreview extends ScrollPane {

  @Getter
  private final ImageView imageView = new ImageView();

  private final ObjectProperty<ImageDisplayMode> displayMode = new SimpleObjectProperty<>(ImageDisplayMode.DYNAMIC);
  private final ObjectProperty<ScaleRatio> currentScale = new SimpleObjectProperty<>(ScaleRatio.getDefault());

  public ImagePreview() {
    this.setContent(imageView);
    this.setOnMouseClicked(this::handleMouseClick);
    this.setPannable(true);
    this.setBackground(new Background(new BackgroundFill(Color.web("#999999"), CornerRadii.EMPTY, Insets.EMPTY)));
    this.setFitToWidth(true);
    this.setFitToHeight(true);

    // Set initial properties
    imageView.setPreserveRatio(true);
    imageView.setSmooth(true);
    imageView.setCache(true);

    // Add property listeners
    displayMode.addListener((_, _, _) -> updateImageViewProperties());
    currentScale.addListener((_, _, _) -> updateImageViewProperties());

    updateImageViewProperties();
  }

  private void handleMouseClick(MouseEvent event) {
    if (event.getClickCount() == 2) {
      if (displayMode.get() == ImageDisplayMode.DYNAMIC) {
        // Switch from dynamic mode to fixed 100% mode
        setDisplayMode(ImageDisplayMode.FIXED);
        setScaleRatio(ScaleRatio.SCALE_100);
      } else {
        // Switch from fixed mode back to dynamic mode
        setDisplayMode(ImageDisplayMode.DYNAMIC);
      }
    }
  }

  private void updateImageViewProperties() {
    if (this.imageView.getImage() == null) {
      return;
    }

    if (displayMode.get() == ImageDisplayMode.DYNAMIC) {
      this.setFitToWidth(true);
      this.setFitToHeight(true);
      imageView.setFitWidth(0);
      imageView.setFitHeight(0);
    } else {
      // Fixed mode - use scale ratio
      this.setFitToWidth(false);
      this.setFitToHeight(false);
      Image image = imageView.getImage();
      if (image != null) {
        double scaledWidth = image.getWidth() * currentScale.get().getRatio();
        double scaledHeight = image.getHeight() * currentScale.get().getRatio();
        imageView.setFitWidth(scaledWidth);
        imageView.setFitHeight(scaledHeight);
      } else {
        // No image loaded, remove size constraints
        imageView.setFitWidth(0);
        imageView.setFitHeight(0);
      }
    }
  }

  public void setImage(Image image) {
    log.info("Changing image to {}", image);
    imageView.setImage(image);
    updateImageViewProperties();
  }

  // --------------------------------------------

  public ImageDisplayMode getDisplayMode() {
    return displayMode.get();
  }

  public void setDisplayMode(ImageDisplayMode displayMode) {
    this.displayMode.set(displayMode);
  }

  public ObjectProperty<ImageDisplayMode> displayModeProperty() {
    return displayMode;
  }

  public ScaleRatio getScaleRatio() {
    return currentScale.get();
  }

  public void setScaleRatio(ScaleRatio scaleRatio) {
    this.currentScale.set(scaleRatio);
  }

  public ObjectProperty<ScaleRatio> scaleRatioProperty() {
    return currentScale;
  }
}
