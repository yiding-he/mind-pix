package com.hyd.mindpix.controllers;

import com.hyd.mindpix.Events;
import com.hyd.mindpix.MindPixApplication;
import com.hyd.mindpix.components.ImagePreview;
import com.hyd.mindpix.components.Thumbnail;
import com.hyd.mindpix.components.ThumbnailList;
import com.hyd.mindpix.enums.ImageDisplayMode;
import com.hyd.mindpix.enums.ScaleRatio;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
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

  private void scrollToActiveThumbnail(Thumbnail thumbnail) {
    double contentHeight = thumbnailList.localToScene(thumbnailList.getBoundsInLocal()).getHeight();
    var vpBounds = this.thumbnailScrollPane.getViewportBounds();
    var thBounds = thumbnail.localToScene(thumbnail.getBoundsInLocal());
    var vValue = this.thumbnailScrollPane.getVvalue();
    double newV = Double.MIN_VALUE;
    if (thBounds.getMinY() < 0) {
      newV = vValue - ((-thBounds.getMinY() + 100) / contentHeight);
    } else if (thBounds.getMaxY() > vpBounds.getHeight()) {
      newV = vValue + ((thBounds.getMaxY() - vpBounds.getHeight() + 50) / contentHeight);
    }
    if (newV != Double.MIN_VALUE) {
      double fixedNewV = Double.min(this.thumbnailScrollPane.getVmax(),
        Double.max(this.thumbnailScrollPane.getVmin(), newV)
      );
      log.info("Scrolling to active thumbnail, v={}, vmin={}, vmax={}",
        fixedNewV, this.thumbnailScrollPane.getVmin(), this.thumbnailScrollPane.getVmax()
      );
      this.thumbnailScrollPane.setVvalue(fixedNewV);
    }
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
