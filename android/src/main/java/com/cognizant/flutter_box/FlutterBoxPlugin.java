package com.cognizant.flutter_box;

import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.box.androidsdk.content.BoxApiFile;
import com.box.androidsdk.content.BoxApiFolder;
import com.box.androidsdk.content.BoxConfig;
import com.box.androidsdk.content.BoxConstants;
import com.box.androidsdk.content.BoxException;
import com.box.androidsdk.content.BoxFutureTask;
import com.box.androidsdk.content.auth.BoxAuthentication;
import com.box.androidsdk.content.models.BoxDownload;
import com.box.androidsdk.content.models.BoxFolder;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxIteratorItems;
import com.box.androidsdk.content.models.BoxOrder;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.content.requests.BoxRequestsFile;
import com.box.androidsdk.content.requests.BoxResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import io.flutter.Log;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * FlutterBoxPlugin
 */
public class FlutterBoxPlugin implements MethodCallHandler {
    private static final String TAG = "FlutterBoxPlugin";
    private static Registrar registrar;
    private BoxSession mSession;
    private BoxApiFolder mFolderApi;
    private BoxApiFile mFileApi;
    private MethodCall call;
    private Result result;

    private static final String INIT_SESSION = "initSession";
    private static final String END_SESSION = "endSession";
    private static final String IS_AUTHENTICATED = "isAuthenticated";
    private static final String LOAD_ROOT_FOLDER = "loadRootFolder";
    private static final String LOAD_FOLDER_ITEMS = "loadFolderItems";
    private static final String UPLOAD_FILE = "uploadFile";
    private static final String DOWNLOAD_FILE = "downloadFile";

    private static final String FAILURE = "FAILURE";
    private static final String SUCCESS = "SUCCESS";

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        configureClient();
        FlutterBoxPlugin.registrar = registrar;
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "box_integration");
        channel.setMethodCallHandler(new FlutterBoxPlugin());
    }

    /**
     * Set required config parameters. Use values from your application settings in the box developer console.
     */
    private static void configureClient() {
        BoxConfig.IS_LOG_ENABLED = true;
        BoxConfig.CLIENT_ID = "jaga5873tij9j2cv1jrbexuniwj26580";
        BoxConfig.CLIENT_SECRET = "aIbcqFNiqgmdWKaQ6JzpCrHlfRKg3zg4";
        // needs to match redirect uri in developer settings if set.
        BoxConfig.REDIRECT_URL = "http://127.0.0.1";
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        this.call = call;
        this.result = result;
        String method = call.method;
        if (method.equals(INIT_SESSION)) {
            initSession();
        } else if (method.equals(END_SESSION)) {
            endSession();
        } else if (method.equals(IS_AUTHENTICATED)) {
            result.success(isAuthenticated() ? SUCCESS : FAILURE);
        } else if (method.equals(LOAD_ROOT_FOLDER)) {
            if (mSession == null) {
                initSession();
            } else {
                loadFolderItems(null);
            }
        } else if (method.equals(LOAD_FOLDER_ITEMS)) {
            if (mSession == null) {
                initSession();
            } else {
                String folderId = call.arguments();
                loadFolderItems(folderId);
            }
        } else if (method.equals(UPLOAD_FILE)) {
            if (mSession == null) {
                initSession();
            } else {
                String filePath = call.argument("filePath");
                String fileName = call.argument("fileName");
                String folderId = call.argument("folderId");
                uploadFile(filePath, fileName, folderId);
            }
        } else if (method.equals(DOWNLOAD_FILE)) {
            if (mSession == null) {
                initSession();
            } else {
                String fileId = call.argument("fileId");
                String targetFilePath = call.argument("targetFilePath");
                downloadFile(targetFilePath, fileId);
            }
        } else {
            result.notImplemented();
        }
    }

    /**
     * Create a BoxSession and authenticate.
     */
    private void initSession() {
        mSession = new BoxSession(registrar.activeContext());
        mSession.setSessionAuthListener(new BoxAuthentication.AuthListener() {
            @Override
            public void onRefreshed(BoxAuthentication.BoxAuthenticationInfo info) {

            }

            @Override
            public void onAuthCreated(BoxAuthentication.BoxAuthenticationInfo info) {
                mFolderApi = new BoxApiFolder(mSession);
                mFileApi = new BoxApiFile(mSession);
                if (!TextUtils.isEmpty(call.method)) {
                    if (call.method.equals(INIT_SESSION)) {
                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                result.success(SUCCESS);
                            }
                        });
                    } else if (call.method.equals(LOAD_ROOT_FOLDER)) {
                        loadFolderItems(null);
                    } else if (call.method.equals(LOAD_FOLDER_ITEMS)) {
                        String folderId = call.arguments();
                        loadFolderItems(folderId);
                    } else if (call.method.equals(UPLOAD_FILE)) {
                        String filePath = call.argument("filePath");
                        String folderId = call.argument("folderId");
                        String fileName = call.argument("fileName");
                        uploadFile(filePath, fileName, folderId);
                    } else if (call.method.equals(DOWNLOAD_FILE)) {
                        String fileId = call.argument("fileId");
                        String targetFilePath = call.argument("targetFilePath");
                        downloadFile(targetFilePath, fileId);
                    }
                }
            }

            @Override
            public void onAuthFailure(BoxAuthentication.BoxAuthenticationInfo info, Exception ex) {
                result.success(SUCCESS);
            }

            @Override
            public void onLoggedOut(BoxAuthentication.BoxAuthenticationInfo info, Exception ex) {
//                result.success(SUCCESS);
//                mSession = null;
            }
        });
        mSession.authenticate(registrar.activeContext());
    }


    private void endSession() {
        if (mSession != null) {
            BoxFutureTask<BoxSession> logout = mSession.logout();
            logout.addOnCompletedListener(new BoxFutureTask.OnCompletedListener<BoxSession>() {
                @Override
                public void onCompleted(BoxResponse<BoxSession> response) {
                    if (response.isSuccess()) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                result.success(SUCCESS);
                            }
                        });
                    } else {
                        result.success(FAILURE);
                    }
                }
            });
        }
    }

    private boolean isAuthenticated() {
        return BoxAuthentication.getInstance().getStoredAuthInfo(registrar.context()).keySet().size() > 0;
    }

    //Method to demonstrate fetching folder items from the root folder
    private void loadFolderItems(final String folderId) {
        new Thread() {
            @Override
            public void run() {
                try {
                    //Api to fetch root folder
                    BoxIteratorItems folderItems;
                    if (TextUtils.isEmpty(folderId)) {
                        folderItems = mFolderApi.getItemsRequest(BoxConstants.ROOT_FOLDER_ID).send();
                    } else {
                        folderItems = mFolderApi.getFolderWithAllItems(folderId).send().getItemCollection();
                    }
                    ArrayList<BoxOrder> sortOrders = folderItems.getSortOrders();
                    final JSONArray jsonArray = new JSONArray();
                    for (BoxItem boxItem : folderItems) {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("id", boxItem.getId());
                            jsonObject.put("name", boxItem.getName());
//                            jsonObject.put("created_at", boxItem.getCreatedAt().getTime());
//                            jsonObject.put("modified_at", boxItem.getModifiedAt().getTime());
                            jsonObject.put("is_folder", boxItem instanceof BoxFolder);
                            jsonArray.put(jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    registrar.activity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            result.success(jsonArray.toString());
                        }
                    });
                } catch (BoxException e) {
                    e.printStackTrace();
                    result.error(FAILURE, "Unable to load folder items", null);
                }
            }
        }.start();
    }

    private void uploadFile(final String filePath, final String fileName, final String destinationFolderId) {
        new Thread() {
            @Override
            public void run() {
                try {
                    String folderId = destinationFolderId;
                    if (TextUtils.isEmpty(folderId)) {
                        folderId = "0";
                    }
                    BoxRequestsFile.UploadFile request;
                    if (!TextUtils.isEmpty(filePath)) {
                        request = mFileApi.getUploadRequest(new File(filePath), folderId).setFileName(TextUtils.isEmpty(fileName) ?
                                new File(filePath).getName() : fileName);
                        request.send();
                        registrar.activity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                result.success(SUCCESS);
                            }
                        });
                    } else {
                        registrar.activity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                result.error(FAILURE, "Upload failure!", null);
                            }
                        });
                    }
                } catch (BoxException e) {
                    registrar.activity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            result.error(FAILURE, "Upload failure!", null);
                        }
                    });
                }
            }
        }.start();

    }

    private void downloadFile(final String targetFile, final String fileId) {
        new Thread() {
            @Override
            public void run() {
                Log.d(TAG, "Download File" + targetFile);
                try {
                    final BoxDownload download = mFileApi.getDownloadRequest(new File(targetFile), fileId)
                            .send();
                    registrar.activity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            result.success(download.getOutputFile().getAbsolutePath());
                        }
                    });
                } catch (BoxException e) {
                    result.error(FAILURE, "Upload failure!", null);
                    e.printStackTrace();
                } catch (IOException e) {
                    result.error(FAILURE, "Upload failure!", null);
                    e.printStackTrace();
                }
            }
        }.start();
    }


    // url = file path or whatever suitable URL you want.
    public String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

}
