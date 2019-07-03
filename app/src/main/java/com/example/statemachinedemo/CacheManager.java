package com.example.statemachinedemo;


import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.example.statemachinedemo.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by zhiqing on 2018/12/25.
 */

public class CacheManager {

    public static final String DISKLRUCACHE_NAME = "cache_demo";
    public static final long DISKLRUCACHE_MAX_SIZE = 10 * 1024;
    public static final int CACHE_VERSION = 1;
    private final static String TAG = "CacheManager";
    private static final Object sLock = new Object();
    private static CacheManager sDM;
    private DiskLruCache mDiskLruCache = null;
    private Context mApplicationContext;

    /**
     * Private constructor, used from {load()}.
     *
     * @param context Should be the application context (or something that will live for the
     *                lifetime of the application).
     */
    private CacheManager(final Context context) {
        mApplicationContext = context;

    }

    /**
     * Get a  instance of {@link CacheManager}, creating one if there isn't one yet.
     * This is the only public method for getting a new instance of the class.
     *
     * @param context Should be the application context (or something that will live for the
     *                lifetime of the application).
     */
    public static void init(final Context context) {
        Log.d(TAG, "load: ");
        synchronized (sLock) {
            Log.d(TAG, "load: new");
            sDM = new CacheManager(context);
        }
    }

    public static CacheManager get() throws Exception {
        if (sDM == null) {
            throw new RuntimeException("data base no init");
        } else {
            return sDM;
        }
    }

    public static String generateKeyForDiskLRU(String token) {
        return String.valueOf((token).hashCode());
    }

    public void close() {
        try {
            if (mDiskLruCache != null) {
                mDiskLruCache.flush();
                mDiskLruCache.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "onTerminate: ", e);
        }
        mDiskLruCache = null;

    }

    public void open() {
        Log.w(TAG, "open: ");
        try {
            String cachePath = mApplicationContext.getCacheDir().getPath();
            File cacheDir = new File(cachePath + File.separator + DISKLRUCACHE_NAME);
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            mDiskLruCache = DiskLruCache.open(cacheDir, CACHE_VERSION, 1, DISKLRUCACHE_MAX_SIZE);
        } catch (Exception e) {
            Log.e(TAG, "open: ", e);

        }
    }

    public Data loadDataFromDiskLRUCache(String key) {
        if (TextUtils.isEmpty(key)) {
            Log.e(TAG, "saveData: data is invalid ");
            return new Data();
        }
        try {
            Log.w(TAG, "loadDataFromDiskLRUCache:");
            long mStartMillis = SystemClock.uptimeMillis();
            DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
            if (snapshot == null) {
                Log.w(TAG, "loadDataFromDiskLRUCache: snapshot is null ");
                return new Data();
            }
            InputStream in = snapshot.getInputStream(0);
            ObjectInputStream ois = new ObjectInputStream(in);
            Data data = (Data) ois.readObject();
            ois.close();
            in.close();
            long elapsedMs = SystemClock.uptimeMillis() - mStartMillis;
            Log.w(TAG, "loadDataFromDiskLRUCache apply: time=" + elapsedMs);
            return data;
        } catch (Exception e) {
            Log.e(TAG, "loadDataFromDiskLRUCache: ", e);
            return new Data();

        }

    }

    public void saveData(String key, Data data) {
        if (data == null) {
            Log.e(TAG, "saveData: data is null ");
            return;
        }
        if (TextUtils.isEmpty(data.getTest())) {
            Log.e(TAG, "saveData: data is invalid data= " + data.toString());
            return;
        }
        Log.w(TAG, "saveData: data=" + data.toString());

        try {
            DiskLruCache.Editor editor = mDiskLruCache.edit(key);
            if (editor != null) {
                ObjectOutputStream outputStream = new ObjectOutputStream(editor.newOutputStream(0));
                outputStream.writeObject(data);
                outputStream.close();
                editor.commit();
            }
        } catch (IOException e) {
            Log.e(TAG, "saveData: ", e);
        }


    }


}
