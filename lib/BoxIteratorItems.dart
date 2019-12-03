class BoxIteratorItems {
  String id;
  String name;
  int createdAt;
  int modifiedAt;
  bool isFolder;

  BoxIteratorItems(
      this.id, this.name, this.isFolder, this.createdAt, this.modifiedAt);

  BoxIteratorItems.fromJson(Map<String, dynamic> json)
      : id = json['id'],
        name = json['name'],
        isFolder = json['is_folder'],
        createdAt = json['created_at'],
        modifiedAt = json['modified_at'];

  Map<String, dynamic> toJson() => {
        'id': id,
        'name': name,
        'is_folder': isFolder,
        'created_at': createdAt,
        'modified_at': modifiedAt,
      };
}
