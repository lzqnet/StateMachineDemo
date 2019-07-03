package com.example.statemachinedemo;

import android.os.Bundle;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "StateMachineDemo";
    StateMachineDemo demo;
    EditText editText;
    EditText editTextvalue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        HandlerThread thread = new HandlerThread("test");
        thread.start();
        demo = new StateMachineDemo(thread.getLooper(), this);
        demo.start();
        editText = findViewById(R.id.edit);
        editTextvalue = findViewById(R.id.value);
    }

    public void open(View view) {
        Log.d(TAG, "open: ");
        demo.handleCacheOpen();
    }

    public void closed(View view) {
        Log.d(TAG, "closed: ");
        demo.handleCacheClose();
    }

    public void get(View view) {
        Log.d(TAG, "get: ");
        StateMachineDemo.CacheGetEntity entity = new StateMachineDemo.CacheGetEntity(CacheManager.generateKeyForDiskLRU(editText.getText().toString()), new IDataCallback() {
            @Override
            public void onDataFetched(Data data) {
                Log.d(TAG, "onDataFetched: data=" + data.toString());
            }
        });
        demo.handleCacheGet(entity);
    }

    public void put(View view) {
        Log.d(TAG, "put: ");
        StateMachineDemo.CachePutEntity entity = new StateMachineDemo.CachePutEntity(
                CacheManager.generateKeyForDiskLRU(editText.getText().toString()), new Data(editTextvalue.getText().toString()));
        demo.handleCachePut(entity);
    }

    public void shutdown(View view) {
        Log.d(TAG, "shutdown: ");
    }
}
