import 'dart:async';
import 'dart:convert';
import 'dart:developer';

import 'package:flutter/services.dart';

import 'BoxIteratorItems.dart';

class FlutterBox {
  static const MethodChannel _channel = const MethodChannel('box_integration');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<Status> get initSession async {
    Status status = Status.FAILURE;
    try {
      final String sessionStatus = await _channel.invokeMethod('initSession');
      if (sessionStatus != null && sessionStatus == "SUCCESS") {
        status = Status.SUCCESS;
      }
    } on PlatformException catch (e) {
      print(e.message);
    }
    return status;
  }

  static Future<Status> get endSession async {
    Status status = Status.FAILURE;
    try {
      log('data: clicked');
      final String sessionStatus = await _channel.invokeMethod('endSession');
      log('data: $sessionStatus');
      if (sessionStatus != null && sessionStatus == "SUCCESS") {
        status = Status.SUCCESS;
      }
    } on PlatformException catch (e) {
      print(e.message);
    }
    return status;
  }

  static Future<Status> get isUserAuthenticated async {
    Status status = Status.FAILURE;
    try {
      final String sessionStatus =
          await _channel.invokeMethod('isAuthenticated');
      if (sessionStatus != null && sessionStatus == "SUCCESS") {
        status = Status.SUCCESS;
      }
    } on PlatformException catch (e) {
      print(e.message);
    }
    return status;
  }

  static Future<Status> uploadFile(String filePath, String folderId) async {
    Status status = Status.FAILURE;
    try {
      final String sessionStatus = await _channel.invokeMethod(
          'uploadFile', {"filePath": filePath, "folderId": folderId});
      if (sessionStatus != null && sessionStatus == "SUCCESS") {
        status = Status.SUCCESS;
      }
    } on PlatformException catch (e) {
      print(e.message);
    }
    return status;
  }

  static Future<String> downloadFile(
      String targetFilePath, String fileId) async {
    String targetFile;
    try {
      final String sessionStatus = await _channel.invokeMethod(
          'downloadFile', {"targetFilePath": targetFilePath, "fileId": fileId});
      if (sessionStatus != null) {
        targetFile = sessionStatus;
      }
    } on PlatformException catch (e) {
      print(e.message);
    }
    return targetFile;
  }

  static Future<List<BoxIteratorItems>> get loadRootFolder async {
    List<BoxIteratorItems> boxIteratorItems;
    try {
      final String sessionStatus =
          await _channel.invokeMethod('loadRootFolder');
      List<dynamic> list = json.decode(sessionStatus);
      boxIteratorItems = list
          .map<BoxIteratorItems>((json) => BoxIteratorItems.fromJson(json))
          .toList();
    } on PlatformException catch (e) {
      print(e.message);
      boxIteratorItems = List();
    }
    return boxIteratorItems;
  }

  static Future<List<BoxIteratorItems>> loadFromFolders(String folderId) async {
    List<BoxIteratorItems> boxIteratorItems;
    try {
      final String sessionStatus =
          await _channel.invokeMethod('loadFolderItems', folderId);
      List<dynamic> list = json.decode(sessionStatus);
      boxIteratorItems = list
          .map<BoxIteratorItems>((json) => BoxIteratorItems.fromJson(json))
          .toList();
    } on PlatformException catch (e) {
      print(e.message);
      boxIteratorItems = List();
    }
    return boxIteratorItems;
  }
}

enum Status { SUCCESS, FAILURE }
