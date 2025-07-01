package com.hyd.mindpix;

import com.hyd.mindpix.components.Thumbnail;

public interface Events {

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
