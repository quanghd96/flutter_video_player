import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  const MethodChannel channel = MethodChannel('video_thumbnail');

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      final m = methodCall.method;
      final a = methodCall.arguments;

      return '$m=${a["video"]}:${a["path"]}:${a["format"]}:${a["maxhow"]}:${a["quality"]}';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });
}
