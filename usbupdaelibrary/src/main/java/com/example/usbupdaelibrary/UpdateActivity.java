package com.example.usbupdaelibrary;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class UpdateActivity extends Activity {
    public static final String TAG = "UpdateActivity";
    private TextView tv_copying, tv_count, tv_number, tv_update, tv_update_time;
    private LinearLayout ll_update_layout;
    private Timer timer;
    private String update_app;
    private FileInfoStruct fileInfoStruct;
    private Map<String, FileInfoStruct> fileInfoStructMap;
    public static String from_path_root = null;
    private int count = 0;
    private int cursor = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.update_activity);

        initViews();
        initDatas();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    doNext();
                } catch (IOException e) {
                    Log.e(TAG, "IO 错误！！！");
                }
            }
        }).start();
    }

    private void initViews() {
        tv_copying = findViewById(R.id.tv_copying);
        tv_count = findViewById(R.id.tv_count);
        tv_number = findViewById(R.id.tv_number);
        ll_update_layout = findViewById(R.id.ll_update_layout);
        tv_update = findViewById(R.id.tv_update);
        tv_update_time = findViewById(R.id.tv_update_time);
    }

    private void initDatas() {
        Intent intent = getIntent();
        from_path_root = intent.getStringExtra("path");
        fileInfoStructMap = new HashMap<>();
    }

    private void doNext() throws IOException {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                tv_copying.setText("正在检查文件....");
                tv_count.setText("");
                tv_number.setText("");
            }
        });
        //将找到的文件放入fileInfoStructMap
        count = getFileInfoUpdate(from_path_root + "/mile_resource/");
//        Log.d(TAG, "视频文件总数为：" + count + "=from_path_root=>>" + from_path_root);
        if (count == 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(UpdateActivity.this, "该U盘中没有可更新的文件", Toast.LENGTH_SHORT).show();
                }
            });
            finish();
        } else if (count < 0) {
            Log.d(TAG, "有错误发生");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(UpdateActivity.this, "更新升级失败！", Toast.LENGTH_SHORT).show();
                    UpdateActivity.this.finish();
                }
            });
        } else {
//            //===============================================>>>
            count = 0;
            //isHasAPK 标识是否有需要升级的apk，如果有，将目标文件夹中的apk删除
            boolean isHasAPK = false;
            for (FileInfoStruct fileInfoStruct : fileInfoStructMap.values()) {
                if (!fileInfoStruct.isHasSame()) {
                    count++;
                }
                if (fileInfoStruct.getFileName().endsWith(".apk")) {
                    isHasAPK = true;
                }
            }
            if (isHasAPK) {
                Log.d(TAG, "发现有需要升级的apk，将目标文件夹清空");
                Util.deleteAllFiles(Constant.TARGET_PATH_UPDATE);
            }
            if (count == 0) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        tv_copying.setText("该U盘中没有可更新的文件");
                        Toast.makeText(UpdateActivity.this, "该U盘中没有可更新的文件", Toast.LENGTH_SHORT).show();
                    }
                });
                finish();
            } else {
                //遍列map，进行copy
                for (FileInfoStruct fileInfoStruct : fileInfoStructMap.values()) {
                    Log.d(TAG, "开始拷贝");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            tv_copying.setText("正在复制 ");
                            tv_count.setText("/" + count + "个文件");
                            tv_number.setText(cursor + "");
                        }
                    });

                    String str_source = fileInfoStruct.getFromPath();
                    String str_dest = fileInfoStruct.getDestPath() + "/" + fileInfoStruct.getFileName();
                    if (!fileInfoStruct.isHasSame()) {
                        if (!Util.copyFileUsingFileChannels(new File(str_source), new File(str_dest))) {
                            Log.d(TAG, "拷贝文件有错误发生");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(UpdateActivity.this, "更新升级失败！", Toast.LENGTH_SHORT).show();
                                }
                            });
                            UpdateActivity.this.finish();
                        }
                    }
                }
                doFinish();
            }
            //finish();
        }
    }

    private void doFinish() {
        boolean isHasAPK = false;
        for (FileInfoStruct fileInfoStruct : fileInfoStructMap.values()) {
            if (fileInfoStruct.getFileName().endsWith(".apk")) {
                this.fileInfoStruct = fileInfoStruct;
                this.update_app = fileInfoStruct.getDestPath() + "/" + fileInfoStruct.getFileName();
                isHasAPK = true;
            }
        }

        if (isHasAPK) {
            Log.d(TAG, "发现需要更新apk，准备升级");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ll_update_layout.setVisibility(View.VISIBLE);
                    tv_copying.setText("更新文件已经复制完成，正在检查后续...");
                    tv_count.setText("");
                    tv_number.setText("");

                    tv_update.setText("找到apk，10秒后准备升级！");

                }
            });
            timer = new Timer();
            timer.schedule(timerTask, 1000, 1000);
        }
    }

    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (Util.isFileExist(fileInfoStruct.getFromPath())) {
                        int left_time = Integer.parseInt(tv_update_time.getText().toString()) - 1;
                        Log.d(TAG, "剩余时间==>>" + left_time);
                        tv_update_time.setText(String.valueOf(left_time));
                        if (left_time == 0) {
                            Log.d(TAG, "update_app==>>" + update_app);
                            timer.cancel();
                            Util.doUpdate(UpdateActivity.this, update_app);
                            finish();
                        }
                    } else {
                        Log.d(TAG, "u盘已拔出，文件已不在");
                        timer.cancel();
                        finish();
                    }
                }
            });
        }
    };


    private int getFileInfoUpdate(String path) throws IOException {
        //测试代码
        //if (!path.isEmpty()) return 0;

        File[] currentFiles;
        File root = new File(path);
        //判断文件是否存在
        //如果不存在则 return出去
        if (!root.exists()) {
            return -1;
        }
        Map<Long, FileInfoStruct> apkMap = new HashMap<>();
        //如果存在则获取当前目录下的全部文件 填充数组
        currentFiles = root.listFiles();
        for (int j = 0; j < currentFiles.length; j++) {
            //是apk文件就存在临时map中，获取版本最高的
            if (currentFiles[j].isFile() && currentFiles[j].getName().endsWith(".apk"))//如果当前项文件，则判断是否为apk文件，判断版本号，取最高的一个
            {
                //LogUtil.d(TAG, "file: "+f.getName());
                if (Util.doCheckPackageName(UpdateActivity.this, currentFiles[j].getAbsolutePath())) {
                    long version = Util.getAppVersion(UpdateActivity.this, currentFiles[j].getAbsolutePath());
                    FileInfoStruct fileInfoStruct = Util.getFileInfoStruct(currentFiles[j]);
                    apkMap.put(version, fileInfoStruct);
                }
            }
        }
        if (apkMap.size() > 0) {
            Object[] key = apkMap.keySet().toArray();
            Arrays.sort(key);
            FileInfoStruct fileInfoStruct = apkMap.get(key[key.length - 1]);
            long version = Util.getAppVersion(UpdateActivity.this, fileInfoStruct.getFromPath());
            long cur_version = Util.getVersionCode(UpdateActivity.this);
            if (version > cur_version) {
                fileInfoStructMap.put(fileInfoStruct.getMd5(), fileInfoStruct);
            } else {
                Log.d(TAG, "apk版本一致，不需要升级");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(UpdateActivity.this, "版本相同或小于当前版本，app无需升级！", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
        return 1;
    }
}
