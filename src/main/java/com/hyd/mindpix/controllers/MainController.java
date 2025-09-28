package com.hyd.mindpix.controllers;

import com.hyd.mindpix.Events;
import com.hyd.mindpix.MindPixApplication;
import com.hyd.mindpix.components.ImageCollectionTab;
import com.hyd.mindpix.components.ImagePreview;
import com.hyd.mindpix.components.ThumbnailList;
import com.hyd.mindpix.enums.ImageDisplayMode;
import com.hyd.mindpix.enums.ScaleRatio;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.image.Image;
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

  public ImagePreview imagePreview;

  // Display mode controls
  public RadioButton dynamicModeRadio;

  public RadioButton fixedModeRadio;

  public ToggleGroup displayModeGroup;

  public ComboBox<ScaleRatio> scaleComboBox;

  public ProgressBar readingProgressBar;

  public HBox readingProgressPane;

  public Label readingProgressLabel;

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

    ImageCollectionTab defaultCollection = new ImageCollectionTab("[默认集合]");
    defaultCollection.setClosable(false);
    this.collectionsTabPane.getTabs().add(defaultCollection);
  }

  //----------------------------------------------------

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

  private ThumbnailList getCurrentThumbnailList() {
    Tab selectedTab = collectionsTabPane.getSelectionModel().getSelectedItem();
    if (selectedTab == null) {
      return null;
    }
    return ((ImageCollectionTab) selectedTab).getThumbnailList();
  }

  private ImageCollectionTab getCurrentCollectionTab() {
    Tab selectedTab = collectionsTabPane.getSelectionModel().getSelectedItem();
    if (selectedTab == null) {
      return null;
    }
    return (ImageCollectionTab) selectedTab;
  }

  @EventListener
  public void onPrevImage(Events.NavigationEvent.PrevImage ignoredEvent) {
    var thumbnailList = getCurrentThumbnailList();
    if (thumbnailList != null) {
      thumbnailList.changeActiveThumbnail(-1);
    }
  }

  @EventListener
  public void onNextImage(Events.NavigationEvent.NextImage ignoredEvent) {
    var thumbnailList = getCurrentThumbnailList();
    if (thumbnailList != null) {
      thumbnailList.changeActiveThumbnail(1);
    }
  }

  @EventListener
  public void onGotoImage(Events.NavigationEvent.GotoImage event) {
    var thumbnailList = getCurrentThumbnailList();
    if (thumbnailList != null) {
      thumbnailList.changeActiveThumbnail(event.thumbnail());
    }
  }

  @EventListener
  public void onActiveThumbnailChanged(Events.ActiveThumbnailEvent.ActiveThumbnailChanged event) {
    ImageCollectionTab collectionTab = getCurrentCollectionTab();
    if (collectionTab != null) {
      collectionTab.scrollToActiveThumbnail(event.thumbnail());
    }
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

    this.collectionsTabPane.getSelectionModel().select(0);

    DirectoryChooser directoryChooser = new DirectoryChooser();
    directoryChooser.setTitle("选择图片文件夹");

    Stage stage = (Stage) collectionsTabPane.getScene().getWindow();
    File dir = directoryChooser.showDialog(stage);
    if (dir != null && dir.isDirectory()) {
      ThumbnailList currentThumbnailList = getCurrentThumbnailList();
      if (currentThumbnailList != null) {
        currentThumbnailList.openDirectory(dir.getAbsolutePath());
      }
    }
  }
}
