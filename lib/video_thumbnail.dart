import 'dart:async';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

class VideoThumbnail {
  static const EventChannel _eventChannel =
      const EventChannel('video_event');

  static Stream<dynamic> thumbnailVideo({@required String video}) {
    assert(video != null && video.isNotEmpty);
    final reqMap = <String, dynamic>{'video': video};
    return _eventChannel.receiveBroadcastStream(reqMap);
  }
}
