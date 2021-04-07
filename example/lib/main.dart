import 'dart:typed_data';

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';

import 'package:video_thumbnail/video_thumbnail.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      home: Material(
        child: GenThumbnailImage(),
      ),
    );
  }
}

class GenThumbnailImage extends StatefulWidget {
  @override
  _GenThumbnailImageState createState() => _GenThumbnailImageState();
}

class _GenThumbnailImageState extends State<GenThumbnailImage> {
  Uint8List bytes;

  @override
  void initState() {
    super.initState();
    stream();
  }

  stream() async {
    String link = "https://www.w3schools.com/html/mov_bbb.mp4";
    await VideoThumbnail.setKey("test");
    VideoThumbnail.streamVideo("test", link).listen((event) {
      setState(() {
        bytes = event;
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return bytes == null
        ? Center(
            child: Text("Loading..."),
          )
        : Container(
            alignment: Alignment.center,
            height: 300,
            child: Image(
              image: MemoryImage(bytes),
              gaplessPlayback: true,
            ),
          );
  }
}
