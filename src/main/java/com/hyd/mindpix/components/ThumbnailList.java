package com.hyd.mindpix.components;

import com.hyd.mindpix.Events;
import com.hyd.mindpix.Events.LoadingImagesEvent;
import com.hyd.mindpix.MindPixMain;
import com.hyd.mindpix.utils.ImageUtils;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import lombok.extern.slf4j.Slf4j;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ThumbnailList extends FlowPane {

  private static final Set<String> SUPPORTED_IMAGE_EXTENSIONS = Set.of(
    ".jpg", ".png", ".jpeg", ".gif", ".bmp", ".webp"
  );

  private volatile String currentFolder;

  private Thumbnail currentActiveThumbnail;

  public ThumbnailList() {
    setOnDragOver(this::handleDragOver);
    setOnDragDropped(this::handleDragDropped);
  }

  private List<Thumbnail> getThumbnailList() {
    return getChildrenUnmodifiable().stream()
      .filter(node -> node instanceof Thumbnail).map(node -> (Thumbnail) node)
      .toList();
  }

  public void changeActiveThumbnail(int offset) {
    int currentIndex = -1;
    var thumbnails = getThumbnailList();
    if (currentActiveThumbnail != null) {
      currentIndex = thumbnails.indexOf(currentActiveThumbnail);
    }

    int newIndex = Math.max(0, Math.min(currentIndex + offset, thumbnails.size() - 1));
    if (currentIndex != newIndex) {
      if (currentIndex >= 0) {
        thumbnails.get(currentIndex).setActive(false);
      }
      Thumbnail th = thumbnails.get(newIndex);
      th.setActive(true);
      currentActiveThumbnail = th;
      MindPixMain.publish(new Events.ActiveThumbnailEvent.ActiveThumbnailChanged(th));
    }
  }

  public void changeActiveThumbnail(Thumbnail th) {
    if (th == null || th.isActive()) {
      return;
    }
    if (currentActiveThumbnail != null) {
      currentActiveThumbnail.setActive(false);
    }
    th.setActive(true);
    currentActiveThumbnail = th;
    MindPixMain.publish(new Events.ActiveThumbnailEvent.ActiveThumbnailChanged(th));
  }

  public Thumbnail popupCurrentThumbnail() {
    if (currentActiveThumbnail == null) {
      return null;
    }
    var result = currentActiveThumbnail;
    getChildren().remove(currentActiveThumbnail);
    currentActiveThumbnail = null;
    return result;
  }

  public Thumbnail getCurrentActiveThumbnail() {
    return currentActiveThumbnail;
  }

  /**
   * 是否允许独立加载图片文件夹
   */
  private final BooleanProperty loadable = new SimpleBooleanProperty(true);

  public boolean isLoadable() {
    return loadable.get();
  }

  public void setLoadable(boolean loadable) {
    this.loadable.set(loadable);
  }

  public BooleanProperty loadableProperty() {
    return loadable;
  }

  private void handleDragDropped(DragEvent event) {
    String absolutePath = event.getDragboard().getFiles().getFirst().getAbsolutePath();
    log.info("Opening directory {}", absolutePath);
    openDirectory(absolutePath);
  }

  private void handleDragOver(DragEvent event) {
    if (
      this.isLoadable() &&
        event.getDragboard().hasFiles() &&
        event.getDragboard().getFiles().size() == 1 &&
        event.getDragboard().getFiles().getFirst().isDirectory()
    ) {
      event.acceptTransferModes(TransferMode.MOVE);
    }
    event.consume();
  }

  public void openDirectory(String absolutePath) {

    currentFolder = absolutePath;
    this.getChildren().clear();
    for (File file : Objects.requireNonNull(new File(absolutePath).listFiles())) {
      if (file.isFile() && SUPPORTED_IMAGE_EXTENSIONS.stream().anyMatch(ext -> file.getName().endsWith(ext))) {
        Thumbnail thumbnail = new Thumbnail(file.getAbsolutePath());
        this.getChildren().add(thumbnail);
      }
    }

    Thread.startVirtualThread(() -> {
      var loadingFolder = currentFolder;
      MindPixMain.publish(new LoadingImagesEvent.Started(absolutePath));
      try {

        var total = (int) this.getChildren().stream().filter(child -> child instanceof Thumbnail).count();
        var counter = new AtomicInteger();
        var errorAlerted = new Boolean[]{false};

        for (Node child : new ArrayList<>(this.getChildren())) {
          if (!(loadingFolder.equals(currentFolder))) {
            return;
          }
          if (child instanceof Thumbnail thumbnail) {
            String imagePath = thumbnail.getImagePath();
            try {
              BufferedImage bufferedImage = readAsBufferedImage(imagePath);
              BufferedImage thumbnailImage = ImageUtils.resize(bufferedImage, 180, 180);
              Platform.runLater(() -> thumbnail.setImage(SwingFXUtils.toFXImage(thumbnailImage, null)));
              MindPixMain.publish(new LoadingImagesEvent.Progress(counter.incrementAndGet(), total));
            } catch (IOException e) {
              if (!errorAlerted[0]) {
                Platform.runLater(() -> {
                  // show error alert
                  Alert alert = new Alert(Alert.AlertType.ERROR);
                  alert.setTitle("图片加载错误");
                  alert.setHeaderText("图片加载错误，请检查图片文件是否正确。");
                  alert.setContentText(e.toString());
                  alert.showAndWait();
                  errorAlerted[0] = true;
                });
              }
            }
          }
        }
      } finally {
        MindPixMain.publish(new LoadingImagesEvent.Finished());
      }
    });
  }

  private BufferedImage readAsBufferedImage(String imagePath) throws IOException {
    return javax.imageio.ImageIO.read(new File(imagePath));
  }

  public void removeThumbnail(Thumbnail thumbnail) {
    Platform.runLater(() -> {
      // 确保重置被移除的缩略图状态
      thumbnail.resetState();

      getChildren().remove(thumbnail);
      if (thumbnail == currentActiveThumbnail) {
        currentActiveThumbnail = null;
        // 选择新的活动缩略图
        if (!getChildren().isEmpty()) {
          Node firstNode = getChildren().get(0);
          if (firstNode instanceof Thumbnail) {
            ((Thumbnail) firstNode).setActive(true);
            currentActiveThumbnail = (Thumbnail) firstNode;
          }
        }
      }
    });
  }

  public void addThumbnail(Thumbnail thumbnail) {
    addThumbnail(thumbnail, false);
  }

  /**
   * 添加缩略图到列表
   * @param thumbnail 要添加的缩略图
   * @param autoSelect 是否自动选中该缩略图
   */
  public void addThumbnail(Thumbnail thumbnail, boolean autoSelect) {
    Platform.runLater(() -> {
      getChildren().add(thumbnail);
      if (autoSelect && currentActiveThumbnail == null) {
        // 只有在当前没有选中任何缩略图时才自动选中
        thumbnail.setActive(true);
        currentActiveThumbnail = thumbnail;
      }
    });
  }
}
