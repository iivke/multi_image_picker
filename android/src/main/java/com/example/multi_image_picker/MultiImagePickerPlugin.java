package com.example.multi_image_picker;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;



import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;


import androidx.core.content.ContextCompat;

import com.sangcomz.fishbun.FishBun;
import com.sangcomz.fishbun.FishBunCreator;
import com.sangcomz.fishbun.adapter.GlideAdapter;
import com.sangcomz.fishbun.ui.camera.CameraActivity;
import com.sangcomz.fishbun.util.Define;
import com.sangcomz.fishbun.util.MediaInfoData;
import com.sangcomz.fishbun.util.PermissionCheck;
import com.sangcomz.fishbun.util.DisplayImage;
import com.sangcomz.fishbun.util.MediaCompress;
import com.sangcomz.fishbun.util.MediaThumbData;
import com.sangcomz.fishbun.util.ProxyCacheUtils;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import android.content.Context;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.BinaryMessenger;

/**
 * MultiImagePickerPlugin
 */
public class MultiImagePickerPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
    private static final String CHANNEL_NAME = "multi_image_picker";

    private static final String FETCH_MEDIA_THUMB_DATA = "fetchMediaThumbData";
    private static final String FETCH_MEDIA_INFO = "fetchMediaInfo";
    private static final String REQUEST_MEDIA_DATA = "requestMediaData";
    private static final String REQUEST_COMPRESS_MEDIA = "requestCompressMedia";
    private static final String REQUEST_TAKE_PICTURE = "requestTakePicture";
    private static final String REQUEST_FILE_PATH = "requestFilePath";
    private static final String REQUEST_FILE_SIZE = "requestFileSize";
    private static final String REQUEST_FILE_DIMEN = "requestFileDimen";
    private static final String REQUEST_THUMB_DIRECTORY = "requestThumbDirectory";
    private static final String FETCH_CACHED_VIDEO_PATH = "cachedVideoPath";
    private static final String FETCH_CACHED_VIDEO_Directory = "cachedVideoDirectory";
    private static final String PICK_IMAGES = "pickImages";
    private static final String MAX_IMAGES = "maxImages";
    private static final String THUMB = "thumb";
    private static final String IDENTIFY = "identifier";
    private static final String FILE_TYPE = "fileType";
    private static final String LIMIT = "limit";
    private static final String OFFSET = "offset";
    private static final String SELECTED_ASSETS = "selectedAssets";
    private static final String DEFAULT_ASSETS = "defaultAsset";
    private static final String SELECT_TYPE = "mediaSelectTypes";
    private static final String MEDIA_SHOW_TYPES = "mediaShowTypes";
    private static final String DONE_BUTTON_TEXT = "doneButtonText";
    private static final String ANDROID_OPTIONS = "androidOptions";
    private static final String THEME_COLOR = "themeColor";
    private static final String PERMISSIONERROR = "PERMISSION_PERMANENTLY_DENIED";
    private static final String PERMISSIONDESC = "NO PERMISSION";
    private static final String GETFAILD = "GET FAILED";
    private static final int REQUEST_CODE_CHOOSE = 1001;
    private static final int REQUEST_CODE_TAKE = 1002;
    private  Activity activity;
    private static Result currentPickerResult;

    private MethodChannel channel;
    private Context context;

//    public MultiImagePickerPlugin(Activity activity, Context context) {
//        this.activity = activity;
//        this.context = context;
//    }

    /**
     * Plugin registration.
     */
//    public static void registerWith(Registrar registrar) {
//        final MethodChannel channel = new MethodChannel(registrar.messenger(), CHANNEL_NAME);
//        MultiImagePickerPlugin instance = new MultiImagePickerPlugin(registrar.activity(), registrar.context());
//        registrar.addActivityResultListener(instance);
//        channel.setMethodCallHandler(instance);
//    }

    @Override
    public void onAttachedToEngine(FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), CHANNEL_NAME);
        channel.setMethodCallHandler(this);
        context = flutterPluginBinding.getApplicationContext();
    }
    @Override
    public void onDetachedFromEngine(FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

    @Override
    public void onAttachedToActivity(@NonNull final ActivityPluginBinding binding) {
        activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {

    }

    @Override
    public void onDetachedFromActivity() {

    }

    boolean checkPermission(boolean checkCamera, boolean checkRecord, boolean checkStorage) {
        PermissionCheck permissionCheck = new PermissionCheck(activity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean result = true;
            if (checkCamera) {
                result = result && permissionCheck.CheckCameraPermission();
            }
            if (checkRecord) {
                result = result && permissionCheck.CheckRecordAudioPermission();
            }
            if (checkStorage) {
                result = result && permissionCheck.CheckStoragePermission();
            }
            return result;
        } else {
            return true;
        }
    }

    @Override
    public void onMethodCall(final MethodCall call, final Result result) {
        try {
            switch (call.method) {
                case PICK_IMAGES: {
                    if (checkPermission(false, false, true)) {
                        if (currentPickerResult != null) {
                            currentPickerResult.error("TIME OUT NEW PICKER COME IN", "", null);
                        }
                        currentPickerResult = result;
                        presentPicker(call);
                    }else {
                        if (currentPickerResult != null) {
                            currentPickerResult.error(PERMISSIONERROR, PERMISSIONDESC, null);
                            currentPickerResult = null;
                        }else {
                            result.error(PERMISSIONERROR, PERMISSIONDESC, null);
                        }
                    }
                    break;
                }
                case FETCH_MEDIA_INFO: {
                    if (checkPermission(false, false, true)) {
                        int limit = call.argument(LIMIT);
                        int offset = call.argument(OFFSET);
                        ArrayList<String> selectMedias = call.argument(SELECTED_ASSETS);
                        selectMedias = selectMedias == null ? new ArrayList<String>() : selectMedias;
                        DisplayImage displayImage = new DisplayImage((long) 0, selectMedias, "all", activity);
                        displayImage.setRequestHashMap(true);
                        displayImage.setLimit(limit);
                        displayImage.setOffset(offset);
                        displayImage.setRequestVideoDimen(!selectMedias.isEmpty());
                        displayImage.setListener(new DisplayImage.DisplayImageListener() {
                            @Override
                            public void OnDisplayImageDidSelectFinish(ArrayList medias) {
                                result.success(medias);
                            }
                        });
                        displayImage.execute();
                    }else {
                        result.error(PERMISSIONERROR, PERMISSIONDESC, null);
                    }
                    break;
                }
                case REQUEST_FILE_PATH:{
                    String identify = call.argument(IDENTIFY);
                    ArrayList<String> selectMedias = new ArrayList<>();
                    selectMedias.add(identify);
                    DisplayImage displayImage = new DisplayImage((long) 0, selectMedias, "all", activity);
                    displayImage.setRequestHashMap(true);
                    displayImage.setRequestVideoDimen(false);
                    displayImage.setListener(new DisplayImage.DisplayImageListener() {
                        @Override
                        public void OnDisplayImageDidSelectFinish(ArrayList medias) {
                            if (medias.size() > 0) {
                                HashMap hashMap = new HashMap();
                                hashMap.put("filePath", ((HashMap) medias.get(0)).get("filePath"));
                                result.success(hashMap);
                            }else {
                                result.error(GETFAILD, GETFAILD, "get media failed");
                            }
                        }
                    });
                    displayImage.execute();
                    break;
                }
                case REQUEST_TAKE_PICTURE: {
                    if (checkPermission(true, true, true)) {
                        if (CameraActivity.isTakingPicture) {
                            if (currentPickerResult != null) {
                                currentPickerResult.error("TAKING PICTURE", "", null);
                                currentPickerResult = null;
                            }
                        }else {
                            if (currentPickerResult != null) {
                                currentPickerResult.error("TIME OUT NEW PICKER COME IN", "", null);
                            }
                            String color = call.argument(THEME_COLOR);
                            int themeColor = color == null || color.isEmpty() ? 0xFF00CC00 : Color.parseColor(color);
                            currentPickerResult = result;
                            CameraActivity.isTakingPicture = true;
                            Intent i = new Intent(activity, CameraActivity.class);
                            i.putExtra(THEME_COLOR, themeColor);
                            activity.startActivityForResult(i, REQUEST_CODE_TAKE);
                        }
                    }else  {
                        if (currentPickerResult != null) {
                            currentPickerResult.error(PERMISSIONERROR, PERMISSIONDESC, null);
                            currentPickerResult = null;
                        }else {
                            result.error(PERMISSIONERROR, PERMISSIONDESC, null);
                        }
                    }
                    break;
                }
                case FETCH_MEDIA_THUMB_DATA: {
                    if (checkPermission(false, false, true)) {
                        String identify = call.argument(IDENTIFY);
                        String fileType = call.argument(FILE_TYPE);
                        MediaThumbData mediaThumbData = new MediaThumbData(identify, fileType, activity);
                        mediaThumbData.setListener(new MediaThumbData.MediaThumbDataListener() {
                            @Override
                            public void mediaThumbDataDidFinish(byte[] bytes) {
                                result.success(bytes);
                            }
                        });
                        mediaThumbData.execute();
                    }else {
                        result.error(PERMISSIONERROR, PERMISSIONDESC, null);
                    }
                    break;
                }
                case REQUEST_FILE_SIZE: {
                    if (checkPermission( false, false, true)) {
                        String identify = call.argument(IDENTIFY);
                        MediaInfoData mediaInfoData = new MediaInfoData(identify, activity);
                        mediaInfoData.setListener(new MediaInfoData.MediaInfoDataListener() {
                            @Override
                            public void mediaInfoDataDidFinish(HashMap hashMap) {
                                result.success(hashMap.get("size"));
                            }
                        });
                        mediaInfoData.execute();
                    }else {
                        result.error(PERMISSIONERROR, PERMISSIONDESC, null);
                    }
                    break;
                }
                case REQUEST_FILE_DIMEN: {
                    if (checkPermission( false, false, true)) {
                        String identify = call.argument(IDENTIFY);
                        MediaInfoData mediaInfoData = new MediaInfoData(identify, activity);
                        mediaInfoData.setListener(new MediaInfoData.MediaInfoDataListener() {
                            @Override
                            public void mediaInfoDataDidFinish(HashMap hashMap) {
                                result.success(hashMap);
                            }
                        });
                        mediaInfoData.execute();
                    }else {
                        result.error(PERMISSIONERROR, PERMISSIONDESC, null);
                    }
                    break;
                }
                case REQUEST_THUMB_DIRECTORY: {
                    result.success(context.getCacheDir().getAbsolutePath() + "/multi_image_pick/thumb/");
                    break;
                }
                case REQUEST_COMPRESS_MEDIA: {
                    if (checkPermission(false, false, true)) {
                        boolean thumb = call.argument("thumb");
                        String fileType = call.argument("fileType");
                        List<String> selectMedias = call.argument("fileList");
                        MediaCompress mediaCompress = new MediaCompress(thumb, selectMedias, fileType, activity);
                        mediaCompress.setListener(new MediaCompress.MediaCompressListener() {
                            @Override
                            public void mediaCompressDidFinish(ArrayList<HashMap> results) {
                                result.success(results);
                            }
                        });
                        mediaCompress.execute();
                    }else {
                        result.error(PERMISSIONERROR, PERMISSIONDESC, null);
                    }
                    break;
                }
                case REQUEST_MEDIA_DATA: {
                    if (checkPermission(false, false, true)) {
                        boolean thumb = call.argument("thumb");
                        List<String> selectMedias = call.argument("selectedAssets");
                        MediaCompress mediaCompress = new MediaCompress(thumb, selectMedias, activity);
                        mediaCompress.setListener(new MediaCompress.MediaCompressListener() {
                            @Override
                            public void mediaCompressDidFinish(ArrayList<HashMap> results) {
                                result.success(results);
                            }
                        });
                        mediaCompress.execute();
                    }else {
                        result.error(PERMISSIONERROR, PERMISSIONDESC, null);
                    }
                    break;
                }
                case FETCH_CACHED_VIDEO_PATH: {
                    try{
                        String url = call.argument("url");
                        if (!TextUtils.isEmpty(url)) {
                            result.success(getVideoCacheDir(context, url).getAbsolutePath());
                        }else {
                            result.success("");
                        }
                    }catch (Exception e) {
                        result.success("");
                    }
                    break;
                }
                case FETCH_CACHED_VIDEO_Directory:{
                    try{
                        File file = new File(context.getExternalCacheDir(), "video-cache");
                        result.success(file.getAbsolutePath());
                    }catch (Exception e) {
                        result.success("");
                    }
                    break;
                }
            }
        } catch (Exception e) {
            if (currentPickerResult != null) {
                currentPickerResult.error(e.getMessage(), e.toString(), null);
                currentPickerResult = null;
            }else {
                result.error(e.getMessage(), e.toString(), null);
            }
        }
    }

    public File getVideoCacheDir(Context context, String url) {
        return new File(new File(context.getExternalCacheDir(), "video-cache"), generate(url));
    }

    public String generate(String url) {
        String extension = getExtension(url);
        String name = ProxyCacheUtils.computeMD5(url);
        return TextUtils.isEmpty(extension) ? name : name + "." + extension;
    }

    private String getExtension(String url) {
        int dotIndex = url.lastIndexOf('.');
        int slashIndex = url.lastIndexOf('/');
        return dotIndex != -1 && dotIndex > slashIndex && dotIndex + 2 + 4 > url.length() ?
                url.substring(dotIndex + 1, url.length()) : "";
    }

    private void presentPicker(MethodCall call) {
        final HashMap<String, String> options = call.argument(ANDROID_OPTIONS);
        int maxImages = call.argument(MAX_IMAGES);
        String thumb = call.argument(THUMB);
        ArrayList<String> selectMedias = call.argument(SELECTED_ASSETS);
        selectMedias = selectMedias == null ? new ArrayList<String>() : selectMedias;
        String defaultAsset = call.argument(DEFAULT_ASSETS);
        defaultAsset = TextUtils.isEmpty(defaultAsset) ? "" : defaultAsset;

        String selectType = call.argument(SELECT_TYPE);
        selectType = TextUtils.isEmpty(selectType) ? "" : selectType;
        String doneButtonText = call.argument(DONE_BUTTON_TEXT);
        doneButtonText = TextUtils.isEmpty(doneButtonText) ? "" : doneButtonText;

        String showMediaType = call.argument(MEDIA_SHOW_TYPES);
        showMediaType = TextUtils.isEmpty(showMediaType) ? "" : showMediaType;

        String actionBarTitle = options.get("actionBarTitle");
        String allViewTitle =  options.get("allViewTitle");
        String selectCircleStrokeColor = options.get("selectCircleStrokeColor");
        String selectionLimitReachedText = options.get("selectionLimitReachedText");
        String textOnNothingSelected = options.get("textOnNothingSelected");
        String backButtonDrawable = options.get("backButtonDrawable");
        String okButtonDrawable = options.get("okButtonDrawable");

        FishBunCreator fishBun = FishBun.with(MultiImagePickerPlugin.this.activity)
                .setImageAdapter(new GlideAdapter())
                .setMaxCount(maxImages)
                .setThumb(thumb.equalsIgnoreCase("thumb"))
                .setHiddenThumb(thumb.equalsIgnoreCase("file"))
                .setPreSelectMedia(defaultAsset)
                .setPreSelectMedias(selectMedias)
                .setRequestCode(REQUEST_CODE_CHOOSE)
                .setShowMediaType(showMediaType)
                .setSelectType(selectType)
                .setDoneButtonText(doneButtonText);

        if (!textOnNothingSelected.isEmpty()) {
            fishBun.textOnNothingSelected(textOnNothingSelected);
        }

        if (!backButtonDrawable.isEmpty()) {
            int id = context.getResources().getIdentifier(backButtonDrawable, "drawable", context.getPackageName());
            fishBun.setHomeAsUpIndicatorDrawable(ContextCompat.getDrawable(context, id));
        }

        if (!okButtonDrawable.isEmpty()) {
            int id = context.getResources().getIdentifier(okButtonDrawable, "drawable", context.getPackageName());
            fishBun.setDoneButtonDrawable(ContextCompat.getDrawable(context, id));
        }

        if (actionBarTitle != null && !actionBarTitle.isEmpty()) {
            fishBun.setActionBarTitle(actionBarTitle);
        }

        if (selectionLimitReachedText != null && !selectionLimitReachedText.isEmpty()) {
            fishBun.textOnImagesSelectionLimitReached(selectionLimitReachedText);
        }

        if (selectCircleStrokeColor != null && !selectCircleStrokeColor.isEmpty()) {
            fishBun.setSelectCircleStrokeColor(Color.parseColor(selectCircleStrokeColor));
        }

        if (allViewTitle != null && !allViewTitle.isEmpty()) {
            fishBun.setAllViewTitle(allViewTitle);
        }

        fishBun.startAlbum();
    }

//    @Override
//    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
//        try {
//            if (requestCode == REQUEST_CODE_CHOOSE) {
//                if (resultCode == Activity.RESULT_OK) {
//                    if (currentPickerResult != null) {
//                        Serializable result = data.getSerializableExtra(Define.INTENT_RESULT);
//                        currentPickerResult.success(result != null ? result : new HashMap());
//                        currentPickerResult = null;
//                    }
//                }else if (resultCode == Define.FINISH_DETAIL_RESULT_CODE) {
//                    if (currentPickerResult != null) {
//                        Serializable result = data.getSerializableExtra(Define.INTENT_RESULT);
//                        currentPickerResult.success(result);
//                        currentPickerResult = null;
//                    }
//                }else if (resultCode == Activity.RESULT_CANCELED) {
//                    if (currentPickerResult != null) {
//                        ArrayList result = data != null ? data.getParcelableArrayListExtra(Define.INTENT_RESULT) : new ArrayList();
//                        Boolean thumb = data != null ? data.getBooleanExtra(Define.INTENT_THUMB, true) : true;
//                        HashMap <String, Object> t = new HashMap<>();
//                        t.put("assets", result);
//                        t.put("thumb", thumb);
//                        currentPickerResult.error("CANCELLED", "", t);
//                        currentPickerResult = null;
//                    }
//                }else {
//                    if (currentPickerResult != null) {
//                        currentPickerResult.error("CANCELLED", "", new ArrayList<>());
//                        currentPickerResult = null;
//                    }
//                }
//                return true;
//            } else if (requestCode == REQUEST_CODE_TAKE) {
//                if (resultCode == Define.ENTER_TAKE_RESULT_CODE) {
//                    if (currentPickerResult != null) {
//                        HashMap result = (HashMap) data.getSerializableExtra(Define.INTENT_RESULT);
//                        currentPickerResult.success(result);
//                        currentPickerResult = null;
//                    }
//                }else {
//                    if (currentPickerResult != null) {
//                        currentPickerResult.error("CANCELLED", "", new HashMap<>());
//                        currentPickerResult = null;
//                    }
//                }
//                return true;
//            }else {
//                if (currentPickerResult != null) {
//                    currentPickerResult.success(new Object());
//                    currentPickerResult = null;
//                }
//                return true;
//            }
//        } catch (Exception e) {
//            if (currentPickerResult != null) {
//                currentPickerResult.error("CANCELLED", "", new ArrayList<>());
//                currentPickerResult = null;
//            }
//            return true;
//        }
//    }
}
