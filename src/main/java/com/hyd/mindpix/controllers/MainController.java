package com.hyd.mindpix.controllers;

import com.hyd.mindpix.Events;
import com.hyd.mindpix.MindPixApplication;
import com.hyd.mindpix.components.ImageCollectionTab;
import com.hyd.mindpix.components.ImagePreview;
import com.hyd.mindpix.components.Thumbnail;
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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

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
        // 用户点击缩略图查看图片，此时图片文件可能已经不在了
        Path imageFile = Path.of(imagePath);
        if (!Files.exists(imageFile)) {
          return;
        }
        try (InputStream fis = Files.newInputStream(imageFile)) {
          imagePreview.setImage(new Image(fis));
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

    // 设置Tab选择监听器来更新CURRENT_TAB
    collectionsTabPane.getSelectionModel().selectedItemProperty().addListener((_, _, newTab) -> {
      if (newTab instanceof ImageCollectionTab imageCollectionTab) {
        MindPixApplication.CURRENT_TAB.set(imageCollectionTab);
      }
    });
    // 初始化CURRENT_TAB为默认Tab
    MindPixApplication.CURRENT_TAB.set(defaultCollection);
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
    MindPixApplication.CURRENT_FOLDER.set(event.folderAbsolutePath());
    Platform.runLater(() -> readingProgressPane.setMaxHeight(-1));
  }

  @EventListener
  public void onLoadingFinished(Events.LoadingImagesEvent.Finished event) {
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

  @EventListener
  public void onTransferImageToTab(Events.TransferImageEvent.TransferToTab event) {
    int tabNumber = event.tabNumber();
    Thumbnail thumbnail = event.thumbnail();

    Platform.runLater(() -> {
      // 查找目标标签页
      ImageCollectionTab targetTab = findTabByNumber(tabNumber);

      // 如果目标标签页不存在，创建新的
      if (targetTab == null) {
        targetTab = new ImageCollectionTab(String.valueOf(tabNumber));
        collectionsTabPane.getTabs().add(targetTab);
        // 设置新建的Tab为CURRENT_TAB（这里不需要，因为TabPane的监听器会自动处理）
      }

      // 获取当前标签页
      ImageCollectionTab sourceTab = getCurrentCollectionTab();
      if (sourceTab == null || sourceTab == targetTab) {
        return; // 无法转移或不需要转移
      }

      // 从源标签页移除缩略图（removeThumbnail会自动重置状态）
      sourceTab.getThumbnailList().removeThumbnail(thumbnail);

      // 添加到目标标签页（不自动选中）
      targetTab.getThumbnailList().addThumbnail(thumbnail, false);

      log.info("Transferred image from tab '{}' to tab '{}' : {}",
               sourceTab.getText(), targetTab.getText(), thumbnail.getImagePath());
    });
  }

  private ImageCollectionTab findTabByNumber(int number) {
    String tabTitle = String.valueOf(number);
    return collectionsTabPane.getTabs().stream()
        .filter(tab -> tab instanceof ImageCollectionTab)
        .map(tab -> (ImageCollectionTab) tab)
        .filter(tab -> tabTitle.equals(tab.getText()))
        .findFirst()
        .orElse(null);
  }

  @EventListener
  public void onTransferImageToFirstTab(Events.TransferImageEvent.TransferToFirstTab event) {
    Thumbnail thumbnail = event.thumbnail();

    Platform.runLater(() -> {
      // 获取第一个标签页（索引为0的Tab）
      Tab firstTab = collectionsTabPane.getTabs().get(0);
      if (!(firstTab instanceof ImageCollectionTab targetTab)) {
        log.warn("First tab is not an ImageCollectionTab");
        return;
      }

      // 获取当前标签页
      ImageCollectionTab sourceTab = getCurrentCollectionTab();
      if (sourceTab == null || sourceTab == targetTab) {
        return; // 无法转移或不需要转移
      }

      // 从源标签页移除缩略图（removeThumbnail会自动重置状态）
      sourceTab.getThumbnailList().removeThumbnail(thumbnail);

      // 添加到第一个标签页（不自动选中）
      targetTab.getThumbnailList().addThumbnail(thumbnail, false);

      log.info("Transferred image from tab '{}' to first tab: {}",
               sourceTab.getText(), thumbnail.getImagePath());
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

    // 只允许第一个标签页加载文件夹
    this.collectionsTabPane.getSelectionModel().select(0);

    DirectoryChooser directoryChooser = new DirectoryChooser();
    directoryChooser.setTitle("选择图片文件夹");
    if (MindPixApplication.CURRENT_FOLDER.get() != null) {
      directoryChooser.setInitialDirectory(new File(MindPixApplication.CURRENT_FOLDER.get()));
    }

    // CURRENT_FOLDER 属性不在这个方法中赋值，而是通过事件侦听赋值，
    // 因为触发 LoadingImagesEvent.Started 事件的方式有多种，
    // 所以 CURRENT_FOLDER 赋值统一在 LoadingImagesEvent.Started 事件中进行
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
