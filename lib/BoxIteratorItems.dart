class BoxIteratorItems {
  String id;
  String name;
  bool isFolder;

  BoxIteratorItems(this.id, this.name, this.isFolder);

  BoxIteratorItems.fromJson(Map<String, dynamic> json)
      : id = json['id'],
        name = json['name'],
        isFolder = json['is_folder'];

  Map<String, dynamic> toJson() =>
      {
        'id' : id,
        'name': name,
        'is_folder': isFolder,
      };
}