import 'dart:async';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

class VideoThumbnail {
  static const MethodChannel _channel = const MethodChannel('video_buffer');

  static getId() async {
    return _channel.invokeMethod("getId");
  }

  static Stream<dynamic> streamVideo(int id, String video) {
    final EventChannel _eventChannel = EventChannel('video_event/$id');
    final reqMap = <String, dynamic>{'video': video};
    return _eventChannel.receiveBroadcastStream(reqMap);
  }
}
