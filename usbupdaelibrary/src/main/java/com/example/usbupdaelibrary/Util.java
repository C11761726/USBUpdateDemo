package com.example.usbupdaelibrary;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.storage.StorageManager;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.util.List;

public class Util {
    private static String appName = "";

    public static void deleteAllFiles(String path) {
        //LogUtil.d(TAG, "delete file");
        File root = new File(path);
        if (!root.exists()) {
            root.mkdirs();
            return;
        }
        File files[] = root.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) { // 判断是否为文件夹
                    deleteAllFiles(f.getAbsolutePath());
                    try {
                        f.delete();
                    } catch (Exception e) {
                    }
                } else {
                    if (f.exists()) { // 判断是否存在
                        deleteAllFiles(f.getAbsolutePath());
                        try {
                            f.delete();
                        } catch (Exception e) {
                        }
                    }
                }
            }
        }
    }

    public static String getPackageName(String absPath, Context context) {
        ApplicationInfo appInfo = apkInfo(absPath, context);
        return appInfo.packageName;
    }


    /**
     * 获取apk包的信息：版本号，名称，图标等
     *
     * @param //absPath  apk包的绝对路径
     * @param //context 
     */
    public static ApplicationInfo apkInfo(String absPath, Context context) {
        ApplicationInfo appInfo = null;
        String version = "";
        PackageManager pm = context.getPackageManager();
        PackageInfo pkgInfo = pm.getPackageArchiveInfo(absPath, PackageManager.GET_ACTIVITIES);
        if (pkgInfo != null) {
            appInfo = pkgInfo.applicationInfo;
            /* 必须加这两句，不然下面icon获取是default icon而不是应用包的icon */
            appInfo.sourceDir = absPath;
            appInfo.publicSourceDir = absPath;
            String appName = pm.getApplicationLabel(appInfo).toString();// 得到应用名 
            String packageName = appInfo.packageName; // 得到包名
            version = pkgInfo.versionName; // 得到版本信息
            /* icon1和icon2其实是一样的 */
            //获取u盘里面的文件图片会导致文件占用
            //Drawable icon1 = pm.getApplicationIcon(appInfo);// 得到图标信息
            //Drawable icon2 = appInfo.loadIcon(pm);
            String pkgInfoStr = String.format("PackageName:%s, Vesion: %s, AppName: %s", packageName, version, appName);
            Log.i("apkInfo", String.format("PkgInfo: %s", pkgInfoStr));
        }
        return appInfo;
    }

    public static boolean doCheckPackageName(Context context, String app) {
        //测试代码
        //String packageName = "";
        String packageName = getPackageName(app, context);
        String cur_package_name = getCurPackageName(context);
        if (cur_package_name.equals(packageName)) {
            return true;
        } else {
            Log.d("doCheckPackageName", "包名不一致，不需要更新");
        }
        return false;
    }

    private static String getCurPackageName(Context context) {
        //当前应用pid
        int pid = android.os.Process.myPid();
        //任务管理类
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //遍历所有应用
        List<ActivityManager.RunningAppProcessInfo> infos = manager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : infos) {
            if (info.pid == pid)//得到当前应用
                return info.processName;//返回包名
        }
        return "";
    }

    /**
     * 判断路径是否存在
     *
     * @param path 需要判断的路径
     * @return true 是存在，false 是不存在
     */
    public static boolean isPathExist(String path) {
        File file = new File(path);
        if (file.exists() && file.isDirectory()) {
            return true;
        }
        return false;
    }

    /**
     * 判断文件是否存在
     *
     * @param name 需要判断的路径
     * @return true 是存在，false 是不存在
     */
    public static boolean isFileExist(String name) {
        File file = new File(name);
        if (file.exists() && file.isFile()) {
            return true;
        }
        return false;
    }

    public static String getStoragePath(Context context) {
        String storagePath = null;
        StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        try {
            Class storeManagerClazz = Class.forName("android.os.storage.StorageManager");
            Method getVolumesMethod = storeManagerClazz.getMethod("getVolumes");
            List<?> volumeInfos = (List<?>) getVolumesMethod.invoke(mStorageManager);//获取到了VolumeInfo的列表
            Class volumeInfoClazz = Class.forName("android.os.storage.VolumeInfo");
            Method getFsUuidMethod = volumeInfoClazz.getMethod("getFsUuid");
            Field pathField = volumeInfoClazz.getDeclaredField("path");
            if (volumeInfos != null) {
                for (Object volumeInfo : volumeInfos) {
                    String uuid = (String) getFsUuidMethod.invoke(volumeInfo);
                    if (uuid != null) {
                        storagePath = (String) pathField.get(volumeInfo);
                    }
                }
            }

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return storagePath;
    }

    public static boolean copyFileUsingFileChannels(File source, File dest) {
        boolean b_ret = true;
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            fileInputStream = new FileInputStream(source);
            fileOutputStream = new FileOutputStream(dest);
            inputChannel = fileInputStream.getChannel();
            outputChannel = fileOutputStream.getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
            fileOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("copyFile", "copyFileUsingFileChannels IOException 错误！！！");
            b_ret = false;
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                if (inputChannel != null) {
                    inputChannel.close();
                }
                if (outputChannel != null) {
                    outputChannel.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                b_ret = false;
                Log.e("copyFile", "copyFileUsingFileChannels IO 错误！！！");
            }
        }
        return b_ret;
    }

    public static void doUpdate(Context context, String app) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(app)), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    public static void doRestartApp(Activity context) {
        Intent intent = context.getBaseContext().getPackageManager().getLaunchIntentForPackage(context.getBaseContext().getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * 获取apk的版本号
     *
     * @param context
     * @param absPath apk所在路径
     * @return
     */
    public static long getAppVersion(Context context, String absPath) {
        PackageManager pm = context.getPackageManager();
        PackageInfo pkgInfo = pm.getPackageArchiveInfo(absPath, PackageManager.GET_ACTIVITIES);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return pkgInfo.getLongVersionCode();
        } else {
            return pkgInfo.versionCode;
        }
    }

    /**
     * 获取当前本地apk的版本
     * 获取软件版本号，对应AndroidManifest.xml下android:versionCode
     *
     * @param mContext
     * @return
     */
    public static long getVersionCode(Context mContext) {
        int versionCode = 0;
        try {
            PackageInfo pkgInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                return pkgInfo.getLongVersionCode();
            } else {
                return pkgInfo.versionCode;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * 获取版本号名称
     *
     * @param context 上下文
     * @return
     */
    public static String getVerName(Context context) {
        String verName = "";
        try {
            verName = context.getPackageManager().
                    getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return verName;
    }

    public static String getRandom() {
        return String.valueOf(Math.random() * 10000);
    }

    public static FileInfoStruct getFileInfoStruct(File file) throws IOException {
        FileInfoStruct fileInfoStruct = new FileInfoStruct();
        //str = currentFiles[j].getName();
        fileInfoStruct.setFileName(file.getName());
        //str = currentFiles[j].getPath();
        fileInfoStruct.setFromPath(file.getPath());

        //测试代码
        //if (fileInfoStruct!=null) return fileInfoStruct;

        FileInputStream is = null;
        String md5 = null;
        int ret_count = 0;
        try {
            is = new FileInputStream(file.getPath());
            //APK 不用取md5值，用随机数代替
            md5 = getRandom();
            fileInfoStruct.setMd5(md5);
            fileInfoStruct.setDestPath(Constant.TARGET_PATH_UPDATE);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("getFileInfoStruct", "getFileInfo IO 错误==>>！");
            ret_count = -2;
        } finally {
            if (is != null) {
                Log.e("getFileInfoStruct", "准备关闭is==>>" + fileInfoStruct.getFileName());
                is.close();
            }
            if (ret_count == -2) {
                Log.e("getFileInfoStruct", "IO 错误 out getFileInfo =ret_count=>>" + ret_count);
            }
        }
        return fileInfoStruct;
    }
}
