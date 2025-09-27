package com.hyd.mindpix.utils;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.imgscalr.Scalr;

import java.awt.image.BufferedImage;

public class ImageUtils {

  /**
   * 重写 resize 方法，使用 BufferedImage 实现缩略图生成，避免依赖 JavaFX 渲染线程
   * 同时保留高斯模糊功能以提高缩略图质量
   *
   * @param source       原始图像
   * @param targetWidth  目标宽度
   * @param targetHeight 目标高度
   * @return 缩放后的图像
   */
  public static Image resize(Image source, int targetWidth, int targetHeight) {
    // 将 JavaFX Image 转换为 BufferedImage
    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(source, null);

    // 计算原始图像的宽高比
    double originalRatio = (double) bufferedImage.getWidth() / bufferedImage.getHeight();
    double targetRatio = (double) targetWidth / targetHeight;

    // 根据纵横比调整目标尺寸
    int adjustedWidth, adjustedHeight;
    if (originalRatio > targetRatio) {
      // 原始图像更宽，以宽度为准
      adjustedWidth = targetWidth;
      adjustedHeight = (int) (targetWidth / originalRatio);
    } else {
      // 原始图像更高，以高度为准
      adjustedHeight = targetHeight;
      adjustedWidth = (int) (targetHeight * originalRatio);
    }

    // 使用 imgscalr 进行高质量缩放
    BufferedImage scaledImage = Scalr.resize(bufferedImage, Scalr.Method.QUALITY,
      Scalr.Mode.FIT_EXACT, adjustedWidth, adjustedHeight,
      Scalr.OP_ANTIALIAS);

    // 将 BufferedImage 转换回 JavaFX Image 并返回
    return SwingFXUtils.toFXImage(scaledImage, null);
  }

}
