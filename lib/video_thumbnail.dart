import 'dart:async';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

class VideoThumbnail {
  static const MethodChannel _channel = const MethodChannel('video_buffer');

  static setKey(String key) async {
    return _channel.invokeMethod("setKey", key);
  }

  static Stream<dynamic> streamVideo(String key, String video) {
    final EventChannel _eventChannel = EventChannel('video_event/$key');
    final reqMap = <String, dynamic>{'video': video};
    return _eventChannel.receiveBroadcastStream(reqMap);
  }
}
