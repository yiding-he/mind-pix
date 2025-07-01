package com.hyd.mindpix.components;

import com.hyd.mindpix.Events;
import com.hyd.mindpix.MindPixMain;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Slf4j
public class ThumbnailList extends FlowPane {

  private volatile String currentFolder;

  public ThumbnailList() {
    setOnDragOver(this::handleDragOver);
    setOnDragDropped(this::handleDragDropped);

    // Add key listeners
    this.setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.RIGHT) {
        MindPixMain.publish(new Events.NavigationEvent.PrevImage());
      } else if (event.getCode() == KeyCode.LEFT) {
        MindPixMain.publish(new Events.NavigationEvent.NextImage());
      }
    });
  }

  private List<Thumbnail> getThumbnailList() {
    return getChildrenUnmodifiable().stream()
      .filter(node -> node instanceof Thumbnail).map(node -> (Thumbnail) node)
      .toList();
  }

  public void changeActiveThumbnail(int offset) {
    var thumbnails = getThumbnailList();
    int activeIndex = -1;
    for (int i = 0; i < thumbnails.size(); i++) {
      var thumbnail = thumbnails.get(i);
      if (thumbnail.isActive()) {
        activeIndex = i;
      }
    }

    int newIndex = activeIndex + offset;
    if (newIndex >= 0 && newIndex < getChildren().size()) {
      if (activeIndex >= 0 && activeIndex != newIndex) {
        thumbnails.get(activeIndex).setActive(false);
      }
      Thumbnail th = thumbnails.get(newIndex);
      th.setActive(true);
      MindPixMain.publish(new Events.ActiveThumbnailEvent.ActiveThumbnailChanged(th));
    }
  }

  public void changeActiveThumbnail(Thumbnail th) {
    var thumbnails = getThumbnailList();
    int activeIndex = -1;
    for (int i = 0; i < thumbnails.size(); i++) {
      var t = thumbnails.get(i);
      if (t.isActive()) {
        activeIndex = i;
      }
    }
    if (activeIndex >= 0) {
      thumbnails.get(activeIndex).setActive(false);
    }
    th.setActive(true);
    MindPixMain.publish(new Events.ActiveThumbnailEvent.ActiveThumbnailChanged(th));
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
