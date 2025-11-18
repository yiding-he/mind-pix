package com.hyd.mindpix.components;

import com.hyd.mindpix.Events;
import com.hyd.mindpix.MindPixMain;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImageCollectionTab extends Tab {

  @Getter
  private final String title;
  private Label tabLabel; // 用于显示标题的Label，支持右键菜单

  @Getter
  private final ScrollPane scrollPane = new ScrollPane();

  @Getter
  private final ThumbnailList thumbnailList = new ThumbnailList();

  public ImageCollectionTab(String title) {
    this.title = title;

    // 移除关闭按钮
    this.setClosable(false);

    this.thumbnailList.setHgap(10);
    this.thumbnailList.setVgap(10);
    this.thumbnailList.setPadding(new Insets(10));

    this.scrollPane.setContent(this.thumbnailList);
    this.scrollPane.setFitToWidth(true);
    this.scrollPane.setFitToHeight(true);
    this.scrollPane.setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.RIGHT) {
        MindPixMain.publish(new Events.NavigationEvent.NextImage());
      } else if (event.getCode() == KeyCode.LEFT) {
        MindPixMain.publish(new Events.NavigationEvent.PrevImage());
      } else if (event.getCode().isDigitKey()) {
        String digitText = event.getCode().getChar();
        try {
          int tabNumber = Integer.parseInt(digitText);
          if (tabNumber >= 1 && tabNumber <= 9) {
            var currentThumbnail = thumbnailList.getCurrentActiveThumbnail();
            if (currentThumbnail != null) {
              log.debug("Transfer image to tab {}: {}", tabNumber, currentThumbnail.getImagePath());
              MindPixMain.publish(new Events.TransferImageEvent.TransferToTab(tabNumber, currentThumbnail));
              event.consume();
            }
          }
        } catch (NumberFormatException e) {
          log.warn("Invalid digit key: {}", digitText);
        }
      } else if (event.getCode() == KeyCode.BACK_QUOTE) {
        var currentThumbnail = thumbnailList.getCurrentActiveThumbnail();
        if (currentThumbnail != null) {
          log.debug("Transfer image to first tab: {}", currentThumbnail.getImagePath());
          MindPixMain.publish(new Events.TransferImageEvent.TransferToFirstTab(currentThumbnail));
          event.consume();
        }
      }
    });


    this.setContent(this.scrollPane);

    // 设置自定义标题标签以支持右键菜单（不要同时调用setText，否则会导致重复显示）
    setupTabLabel(title);
  }

  private void setupTabLabel(String title) {
    // 创建用于显示标题的Label
    tabLabel = new Label(title);

    // 创建右键菜单
    ContextMenu contextMenu = new ContextMenu();
    MenuItem closeItem = new MenuItem("关闭");

    // 设置关闭菜单项的事件处理
    closeItem.setOnAction(event -> {
      // 只允许关闭非默认集合的Tab
      if (!MindPixMain.DEFAULT_COLLECTION_NAME.equals(title) && getTabPane() != null) {
        getTabPane().getTabs().remove(this);
      }
    });

    // 默认禁用第一个Tab的关闭功能
    closeItem.setDisable(MindPixMain.DEFAULT_COLLECTION_NAME.equals(title));

    contextMenu.getItems().add(closeItem);

    // 为Label设置右键菜单
    tabLabel.setContextMenu(contextMenu);

    // 将Label设置为Tab的图形
    this.setGraphic(tabLabel);

    // 监听标题变化，更新Label（注意：不要调用setText，否则又会导致重复）
    textProperty().addListener((obs, oldText, newText) -> {
      if (tabLabel != null) {
        tabLabel.setText(newText);
        // 重要：不要调用setText，因为我们使用graphic来显示标题
      }
    });
  }

  public void scrollToActiveThumbnail(Thumbnail thumbnail) {
    // 获取thumbnail在thumbnailList中的位置（使用父容器坐标系）
    double thumbnailY = thumbnail.getBoundsInParent().getMinY();
    double thumbnailHeight = thumbnail.getBoundsInParent().getHeight();

    // 获取viewport的高度
    double viewportHeight = scrollPane.getViewportBounds().getHeight();

    // 获取当前滚动位置和范围
    double vmin = scrollPane.getVmin();
    double vmax = scrollPane.getVmax();

    // 计算内容总高度（通过viewport高度和vmax-vmin范围来推算）
    double contentHeight = thumbnailList.getHeight();

    // 如果内容高度为0，说明布局尚未完成，暂时不滚动
    if (contentHeight <= 0) {
      return;
    }

    // 计算当前视口在内容中的位置
    double viewportPosition = scrollPane.getVvalue() * (contentHeight - viewportHeight);

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
    scrollPane.setVvalue(targetVvalue);
  }

}
