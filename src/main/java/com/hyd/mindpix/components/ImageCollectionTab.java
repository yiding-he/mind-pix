package com.hyd.mindpix.components;

import com.hyd.mindpix.Events;
import com.hyd.mindpix.MindPixMain;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.input.KeyCode;
import lombok.Getter;

public class ImageCollectionTab extends Tab {

  private final String title;

  @Getter
  private final ScrollPane scrollPane = new ScrollPane();

  @Getter
  private final ThumbnailList thumbnailList = new ThumbnailList();

  public ImageCollectionTab(String title) {
    this.title = title;

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
      }
    });


    this.setText(title);
    this.setContent(this.scrollPane);
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
