package com.hyd.mindpix.components;

import com.hyd.mindpix.Events;
import com.hyd.mindpix.MindPixMain;
import com.hyd.mindpix.enums.ImageDisplayMode;
import com.hyd.mindpix.enums.ScaleRatio;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImagePreview extends ScrollPane {

  private static final Background IMG_BACKGROUND =
    new Background(new BackgroundFill(Color.web("#999999"), CornerRadii.EMPTY, Insets.EMPTY));

  @Getter
  private final ImageView imageView = new ImageView();

  private double imageWidth = 0;

  private double imageHeight = 0;

  private final ChangeListener<Bounds> vpListenerForDynamic = (_, _, newValue) -> {
    if (newValue != null) {
      repaintDynamicImage(newValue);
    }
  };

  private final ObjectProperty<ImageDisplayMode> displayMode = new SimpleObjectProperty<>(ImageDisplayMode.DYNAMIC);

  private final ObjectProperty<ScaleRatio> currentScale = new SimpleObjectProperty<>(ScaleRatio.getDefault());

  public ImagePreview() {
    StackPane imageViewWrapper = new StackPane(imageView);
    imageViewWrapper.setBackground(IMG_BACKGROUND);
    this.setContent(imageViewWrapper);
    this.setOnMouseClicked(this::handleMouseClick);
    this.setPannable(true);
    this.setFitToWidth(true);
    this.setFitToHeight(true);

    // Set initial properties
    imageView.setPreserveRatio(true);
    imageView.setSmooth(true);
    imageView.setCache(true);

    // Add property listeners
    displayMode.addListener((_, _, _) -> repaintImage());
    currentScale.addListener((_, _, _) -> repaintImage());

    // Add key listeners
    this.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
      if (event.getCode() == KeyCode.PAGE_UP) {
        MindPixMain.publish(new Events.NavigationEvent.PrevImage());
      } else if (event.getCode() == KeyCode.PAGE_DOWN) {
        MindPixMain.publish(new Events.NavigationEvent.NextImage());
      }
    });

    repaintImage();
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

  private void repaintImage() {
    if (this.imageView.getImage() == null) {
      return;
    }

    ObjectProperty<Bounds> vpBounds = this.viewportBoundsProperty();

    // 先清理 vpListenerForDynamic 免得 add 的时候出现重复添加
    // 该操作在 if 和 else 分支里面都是第一步，所以提取到外面来
    vpBounds.removeListener(vpListenerForDynamic);

    if (displayMode.get() == ImageDisplayMode.DYNAMIC) {
      vpBounds.addListener(vpListenerForDynamic);
      this.repaintDynamicImage(this.getViewportBounds());
    } else {
      if (this.imageWidth > 0 && this.imageHeight > 0) {
        double scaledWidth = this.imageWidth * currentScale.get().getRatio();
        double scaledHeight = this.imageHeight * currentScale.get().getRatio();
        imageView.setFitWidth(scaledWidth);
        imageView.setFitHeight(scaledHeight);
      } else {
        imageView.setFitWidth(0);
        imageView.setFitHeight(0);
      }
    }
  }

  private void repaintDynamicImage(Bounds viewportBounds) {
    double viewportWidth = viewportBounds.getWidth();
    double viewportHeight = viewportBounds.getHeight();
    imageView.setFitWidth(viewportWidth);
    imageView.setFitHeight(viewportHeight);
  }

  public void setImage(Image image) {
    imageView.setImage(image);
    if (image!= null) {
      imageWidth = image.getWidth();
      imageHeight = image.getHeight();
    } else {
      imageWidth = 0;
      imageHeight = 0;
    }
    repaintImage();
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
