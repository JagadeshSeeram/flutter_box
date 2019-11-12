# flutter_box

A Flutter plugin for using the native Box SDKs to access the Box Storage


## How do I use it? 

· initSession() – this will launch the login page, where the user needs to give his credentials to access his box storage

· endSession() – logout the session(current session).

· isUserAuthenticated() – to check whether the user is logged in or not(current session is active or not).

· loadRootFolder() – To fetch the root folder details(files/folders)

· loadFromFolders(String folderId) – to fetch the details from the particular folder(folderId).

· uploadFile(String folderId, String fileName) – upload the file into the particular folder(folderId), if the folderId is empty then it will upload to the root folder

