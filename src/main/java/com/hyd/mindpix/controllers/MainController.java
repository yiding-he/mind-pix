package com.hyd.mindpix.controllers;

import com.hyd.mindpix.MindPixApplication;
import com.hyd.mindpix.components.ImagePreview;
import com.hyd.mindpix.components.ThumbnailList;
import com.hyd.mindpix.enums.ImageDisplayMode;
import com.hyd.mindpix.enums.ScaleRatio;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Component
public class MainController {

  public ThumbnailList thumbnailList;
  public ImagePreview imagePreview;

  // Display mode controls
  public RadioButton dynamicModeRadio;
  public RadioButton fixedModeRadio;
  public ToggleGroup displayModeGroup;
  public ComboBox<ScaleRatio> scaleComboBox;

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
    imagePreview.displayModeProperty().addListener((observable, oldValue, newValue) -> {
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
