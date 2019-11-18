import 'dart:async';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_box/BoxIteratorItems.dart';
import 'package:flutter_box/flutter_box.dart';
import 'package:image_picker/image_picker.dart';
import 'package:path_provider/path_provider.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return new MaterialApp(
      debugShowCheckedModeBanner: false,
      home: new HomePage(),
    );
  }
}

class HomePage extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return new Scaffold(
      appBar: new AppBar(
        title: const Text('Box Sample'),
      ),
      body: new MyHomePage(),
    );
  }
}

class MyHomePage extends StatefulWidget {
  MyHomePage({Key key, this.title}) : super(key: key);
  final String title;

  @override
  _MyHomePageState createState() => new _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  @override
  void initState() {
    super.initState();
    setState(() {
      isUserAuthenticated();
    });
  }

  String _loginText = 'User is not authenticated!!';
  List<BoxIteratorItems> _responseFromBox = List();
  var folderId;

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> login() async {
    Status status;
    try {
      status = await FlutterBox.initSession;
    } on PlatformException {}
    if (!mounted) return;

    setState(() {
      if (status == Status.SUCCESS) {
        _loginText = "User is logged in!!";
      }
    });
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> logout() async {
    Status status;
    try {
      await FlutterBox.endSession;
    } on PlatformException {}
    if (!mounted) return;
    setState(() {
      if (status == Status.SUCCESS) {
        _loginText = "User is not authenticated!!";
      }
    });
  }

  Future<void> isUserAuthenticated() async {
    var status = await FlutterBox.isUserAuthenticated;
    if (!mounted) return;
    setState(() {
      _loginText = status == Status.SUCCESS
          ? 'User is logged in!!'
          : 'User is not authenticated!!';
    });
  }

  Future<void> loadFromRootFolder() async {
    List<BoxIteratorItems> boxIteratorItems;
    try {
      boxIteratorItems = await FlutterBox.loadRootFolder;
    } on PlatformException catch (e) {
      boxIteratorItems = List();
    }
    setState(() {
      _responseFromBox.clear();
      _responseFromBox.addAll(boxIteratorItems);
    });
  }

  Future<void> loadFromFolder(String folderId) async {
    List<BoxIteratorItems> boxIteratorItems;
    try {
      boxIteratorItems = await FlutterBox.loadFromFolders(folderId);
    } on PlatformException catch (e) {
      boxIteratorItems = List();
    }
    setState(() {
      _responseFromBox.clear();
      _responseFromBox.addAll(boxIteratorItems);
    });
  }

  uploadSampleFile(String filePath) async {
    try {
      var status = await FlutterBox.uploadFile(filePath, folderId);
      if (status == Status.SUCCESS) {
        loadFromFolder(folderId);
      }
    } on PlatformException catch (e) {}
  }

  downloadFile(String fileId) async {
    try {
      Directory appDocDir = await getExternalStorageDirectory();
      String filePath = '${appDocDir.path}';
      print(filePath);
      var status = await FlutterBox.downloadFile(filePath, fileId);
      if (status != null) {
        print("AFTER------");
        print(status);
      }
    } on PlatformException catch (e) {}
  }

  File _image;

  Future getImage() async {
    var image = await ImagePicker.pickImage(source: ImageSource.gallery);
    setState(() {
      _image = image;
      print(_image.path);
      uploadSampleFile(_image.path);
    });
  }

  @override
  Widget build(BuildContext context) {
    return Material(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Text(_loginText),
          RaisedButton(
            child: Text("Login"),
            onPressed: login,
          ),
          RaisedButton(
            child: Text("Logout"),
            onPressed: logout,
          ),
          RaisedButton(
            child: Text("Load Files"),
            onPressed: loadFromRootFolder,
          ),
          RaisedButton(
            child: Text("Upload File"),
            onPressed: getImage,
          ),
          Expanded(child: listViewWidget())
        ],
      ),
    );
  }

  void _onTapItem(BuildContext context, BoxIteratorItems boxIteratorItem) {
    if (boxIteratorItem.isFolder) {
      folderId = boxIteratorItem.id;
      loadFromFolder(boxIteratorItem.id);
    } else {
      downloadFile(boxIteratorItem.id);
    }
  }

  Widget listViewWidget() {
    return Container(
      child: ListView.builder(
          itemCount: _responseFromBox.length,
          padding: const EdgeInsets.all(2.0),
          itemBuilder: (context, position) {
            return Card(
              child: ListTile(
                title: Text(
                  getTextToBeDisplayed(_responseFromBox[position]),
                  style: TextStyle(
                      fontSize: 18.0,
                      color: getFolderColor(_responseFromBox[position]),
                      fontWeight: FontWeight.bold),
                ),
                onTap: () => _onTapItem(context, _responseFromBox[position]),
              ),
            );
          }),
    );
  }

  String getTextToBeDisplayed(BoxIteratorItems responseFromBox) {
    if (responseFromBox.isFolder) {
      return responseFromBox.name;
    } else {
      return responseFromBox.name;
    }
  }

  Color getFolderColor(BoxIteratorItems responseFromBox) {
    if (responseFromBox.isFolder) {
      return Colors.blue;
    } else {
      return Colors.black;
    }
  }
}
