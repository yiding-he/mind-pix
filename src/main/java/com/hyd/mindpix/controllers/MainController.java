package com.hyd.mindpix.controllers;

import com.hyd.mindpix.Events;
import com.hyd.mindpix.MindPixApplication;
import com.hyd.mindpix.MindPixMain;
import com.hyd.mindpix.components.ImagePreview;
import com.hyd.mindpix.components.Thumbnail;
import com.hyd.mindpix.components.ThumbnailList;
import com.hyd.mindpix.enums.ImageDisplayMode;
import com.hyd.mindpix.enums.ScaleRatio;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Component
@Slf4j
public class MainController {

  public ThumbnailList thumbnailList;

  public ImagePreview imagePreview;

  // Display mode controls
  public RadioButton dynamicModeRadio;

  public RadioButton fixedModeRadio;

  public ToggleGroup displayModeGroup;

  public ComboBox<ScaleRatio> scaleComboBox;

  public ScrollPane thumbnailScrollPane;

  public ProgressBar readingProgressBar;

  public HBox readingProgressPane;

  public Label readingProgressLabel;

  public Tab defaultTab;

  public TabPane collectionsTabPane;

  public void initialize() {
    // Initialize scale combo box
    scaleComboBox.setItems(FXCollections.observableArrayList(ScaleRatio.values()));
    scaleComboBox.setValue(ScaleRatio.getDefault());

    // Bind scale combo box to image preview scale property
    scaleComboBox.valueProperty().bindBidirectional(imagePreview.scaleRatioProperty());

    // Bind scale combo box disable state to display mode
    scaleComboBox.disableProperty().bind(
      imagePreview.displayModeProperty().isEqualTo(ImageDisplayMode.DYNAMIC)
    );

    // Synchronize radio buttons with ImagePreview display mode property
    setupDisplayModeBinding();

    // Set up image change listener
    MindPixApplication.CURRENT_IMAGE.addListener((_, _, imagePath) -> {
      if (imagePath != null) {
        try {
          imagePreview.setImage(new Image(new FileInputStream(imagePath)));
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      } else {
        imagePreview.setImage(null);
      }
    });

    // Add key listeners
    this.thumbnailScrollPane.setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.RIGHT) {
        MindPixMain.publish(new Events.NavigationEvent.NextImage());
      } else if (event.getCode() == KeyCode.LEFT) {
        MindPixMain.publish(new Events.NavigationEvent.PrevImage());
      }
    });
  }

  private void setupDisplayModeBinding() {
    // Listen to ImagePreview display mode changes and update radio buttons
    imagePreview.displayModeProperty().addListener((_, _, newValue) -> {
      if (newValue == ImageDisplayMode.DYNAMIC) {
        dynamicModeRadio.setSelected(true);
      } else {
        fixedModeRadio.setSelected(true);
      }
    });

    // Initialize radio button state based on current display mode
    if (imagePreview.getDisplayMode() == ImageDisplayMode.DYNAMIC) {
      dynamicModeRadio.setSelected(true);
    } else {
      fixedModeRadio.setSelected(true);
    }
  }

  @EventListener
  public void onPrevImage(Events.NavigationEvent.PrevImage ignoredEvent) {
    this.thumbnailList.changeActiveThumbnail(-1);
  }

  @EventListener
  public void onNextImage(Events.NavigationEvent.NextImage ignoredEvent) {
    this.thumbnailList.changeActiveThumbnail(1);
  }

  @EventListener
  public void onGotoImage(Events.NavigationEvent.GotoImage event) {
    this.thumbnailList.changeActiveThumbnail(event.thumbnail());
  }

  @EventListener
  public void onActiveThumbnailChanged(Events.ActiveThumbnailEvent.ActiveThumbnailChanged event) {
    scrollToActiveThumbnail(event.thumbnail());
  }

  @EventListener
  public void onLoadingStarted(Events.LoadingImagesEvent.Started event) {
    log.info("Loading images started.");
    Platform.runLater(() -> readingProgressPane.setMaxHeight(-1));
  }

  @EventListener
  public void onLoadingFinished(Events.LoadingImagesEvent.Finished event) {
    log.info("Loading images finished, progressBar {}", readingProgressBar == null ? "not exists" : "exists");
    Platform.runLater(() -> readingProgressPane.setMaxHeight(0));
  }

  @EventListener
  public void onLoadingProgress(Events.LoadingImagesEvent.Progress event) {
    Platform.runLater(() -> {
      if (readingProgressPane.getMaxHeight() != -1) {
        readingProgressPane.setMaxHeight(-1);
      }
      readingProgressBar.setProgress(event.progress());
      readingProgressLabel.setText(event.current() + "/" + event.total());
    });
  }

  private void scrollToActiveThumbnail(Thumbnail thumbnail) {
    // 获取thumbnail在thumbnailList中的位置（使用父容器坐标系）
    double thumbnailY = thumbnail.getBoundsInParent().getMinY();
    double thumbnailHeight = thumbnail.getBoundsInParent().getHeight();

    // 获取viewport的高度
    double viewportHeight = thumbnailScrollPane.getViewportBounds().getHeight();

    // 获取当前滚动位置和范围
    double vmin = thumbnailScrollPane.getVmin();
    double vmax = thumbnailScrollPane.getVmax();

    // 计算内容总高度（通过viewport高度和vmax-vmin范围来推算）
    double contentHeight = thumbnailList.getHeight();

    // 如果内容高度为0，说明布局尚未完成，暂时不滚动
    if (contentHeight <= 0) {
      return;
    }

    // 计算当前视口在内容中的位置
    double viewportPosition = thumbnailScrollPane.getVvalue() * (contentHeight - viewportHeight);

    // 计算需要滚动到的目标位置
    double targetViewportPosition = viewportPosition;

    // 如果thumbnail顶部在可视区域上方
    if (thumbnailY < viewportPosition) {
      targetViewportPosition = thumbnailY - 10;
    }
    // 如果thumbnail底部在可视区域下方
    else if (thumbnailY + thumbnailHeight > viewportPosition + viewportHeight) {
      targetViewportPosition = thumbnailY + thumbnailHeight - viewportHeight + 10;
    }

    // 转换为目标vvalue并限制在有效范围内
    double targetVvalue = Math.max(vmin, Math.min(vmax, targetViewportPosition / (contentHeight - viewportHeight)));

    // 执行滚动
    thumbnailScrollPane.setVvalue(targetVvalue);
  }


  public void onDisplayModeChanged() {
    ImageDisplayMode mode = dynamicModeRadio.isSelected() ?
      ImageDisplayMode.DYNAMIC : ImageDisplayMode.FIXED;

    imagePreview.setDisplayMode(mode);
    // Note: scaleComboBox disable state is automatically handled by property binding
  }

  public void onScaleChanged() {
    // Note: scale changes are automatically handled by property binding
    // This method can be used for additional logic if needed
  }

  public void openFolder() {
    Stage stage = (Stage) thumbnailList.getScene().getWindow();

    DirectoryChooser directoryChooser = new DirectoryChooser();
    directoryChooser.setTitle("选择图片文件夹");

    File dir = directoryChooser.showDialog(stage);
    if (dir != null && dir.isDirectory()) {
      this.thumbnailList.openDirectory(dir.getAbsolutePath());
    }
  }
}
