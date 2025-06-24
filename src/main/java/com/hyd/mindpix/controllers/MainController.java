package com.hyd.mindpix.controllers;

import com.hyd.mindpix.MindPixApplication;
import com.hyd.mindpix.components.ImagePreview;
import com.hyd.mindpix.components.ThumbnailList;
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

  public void initialize() {
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
