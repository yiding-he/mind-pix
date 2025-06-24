package com.hyd.mindpix.components;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;

@Slf4j
public class ThumbnailList extends FlowPane {

  private volatile String currentFolder;

  public ThumbnailList() {
    setOnDragOver(this::handleDragOver);
    setOnDragDropped(this::handleDragDropped);
  }

  private void handleDragDropped(DragEvent event) {
    String absolutePath = event.getDragboard().getFiles().getFirst().getAbsolutePath();
    log.info("Opening directory {}", absolutePath);
    openDirectory(absolutePath);
  }

  private void handleDragOver(DragEvent event) {
    if (
      event.getDragboard().hasFiles() &&
        event.getDragboard().getFiles().size() == 1 &&
        event.getDragboard().getFiles().getFirst().isDirectory()) {
      event.acceptTransferModes(TransferMode.MOVE);
    }
    event.consume();
  }

  public void openDirectory(String absolutePath) {

    currentFolder = absolutePath;
    this.getChildren().clear();
    for (File file : Objects.requireNonNull(new File(absolutePath).listFiles())) {
      if (file.isFile() && (
        file.getName().endsWith(".jpg") || file.getName().endsWith(".png") || file.getName().endsWith(".jpeg") ||
          file.getName().endsWith(".gif") || file.getName().endsWith(".bmp")
      )) {
        Thumbnail thumbnail = new Thumbnail(file.getAbsolutePath());
        this.getChildren().add(thumbnail);
      }
    }

    Thread.startVirtualThread(() -> {
      var loadingFolder = currentFolder;
      for (Node child : this.getChildren()) {
        if (!(loadingFolder.equals(currentFolder))) {
          return;
        }
        if (child instanceof Thumbnail thumbnail) {
          String imagePath = thumbnail.getImagePath();
          try (FileInputStream fis = new FileInputStream(imagePath)) {
            Image thumbnailImage = new Image(fis, 180, 180, true, true);
            Platform.runLater(() -> thumbnail.setImage(thumbnailImage));
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
      }
    });

  }
}
