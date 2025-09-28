package com.hyd.mindpix;

import com.hyd.mindpix.components.Thumbnail;

public interface Events {

  interface LoadingImagesEvent {
    record Started(String folderAbsolutePath) {
    }

    record Finished() {
    }

    record Progress(int current, int total) {
      public double progress() {
        return (double) current / total;
      }
    }
  }

  interface NavigationEvent {

    record PrevImage() {
    }

    record NextImage() {
    }

    record GotoImage(Thumbnail thumbnail) {
    }
  }

  interface ActiveThumbnailEvent {
    record ActiveThumbnailChanged(Thumbnail thumbnail) {
    }
  }
}
