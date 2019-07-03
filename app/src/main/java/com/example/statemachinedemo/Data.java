package com.example.statemachinedemo;


import android.support.annotation.NonNull;

import java.io.Serializable;

import static com.example.statemachinedemo.CacheManager.CACHE_VERSION;

/**
 * Created by zhiqing on 2018/05/07.
 */
class Data implements Serializable {
    private static final long serialVersionUID = CACHE_VERSION;

    @NonNull
    private String test;


    public Data() {
    }

    public Data(@NonNull String test) {
        this.test = test;

    }

    @NonNull
    public String getTest() {
        return test;
    }

    public void setTest(@NonNull String test) {
        this.test = test;
    }

    @Override
    public String toString() {
        return "Data{" +
                "test='" + test + '\'' +
                '}';
    }

}
