package com.example.statemachinedemo;

import android.content.Context;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.example.statemachinedemo.StateMachine.State;
import com.example.statemachinedemo.StateMachine.StateMachine;
/**
 * Created by zhiqing on 2018/12/25.
 */

public class StateMachineDemo extends StateMachine {
    private final static String TAG = "StateMachineDemo";
    private static final int EVENT_CACHE_OPEN = 1;
    private static final int EVENT_CACHE_GET = 2;
    private static final int EVENT_CACHE_PUT = 3;
    private static final int EVENT_CACHE_CLOSE = 4;
    private static final int EVENT_CACHE_SHUTDOWN = 5;
    private final DefaultState mDefaultState = new DefaultState();
    private final StartupState mStartupState = new StartupState();
    private final IdleState mIdleState = new IdleState();
    private final OpenState mOpenState = new OpenState();
    private final ShutDowningState mShutDowningState = new ShutDowningState();
    private final ShutDownedState mShutDownedState = new ShutDownedState();
    private Context mContext;

    public StateMachineDemo(Looper looper, Context context) {
        super(TAG, looper);
        mContext = context;
        addState(mDefaultState);
        addState(mStartupState, mDefaultState);
        addState(mIdleState, mDefaultState);
        addState(mOpenState, mDefaultState);
        addState(mShutDowningState, mDefaultState);
        addState(mShutDownedState, mShutDowningState);
        setInitialState(mStartupState);
    }

    public void handleCacheOpen() {
        sendMessage(EVENT_CACHE_OPEN);
    }

    public void handleCacheGet(CacheGetEntity entity) {
        sendMessage(EVENT_CACHE_GET, entity);
    }

    public void handleCachePut(CachePutEntity entity) {
        sendMessage(EVENT_CACHE_PUT, entity);
    }

    public void handleCacheClose() {
        sendMessage(EVENT_CACHE_CLOSE);
    }

    public void handleCacheShutdown() {
        sendMessage(EVENT_CACHE_SHUTDOWN);
    }

    public static class CacheGetEntity {
        String key;
        IDataCallback callback;

        public CacheGetEntity(String key, IDataCallback callback) {
            this.key = key;
            this.callback = callback;
        }
    }

    public static class CachePutEntity {
        String key;
        Data data;

        public CachePutEntity(String key, Data data) {
            this.key = key;
            this.data = data;
        }
    }

    private class DefaultState extends BaseState {
        @Override
        public void enter() {
            Log.w(TAG, "DefaultState.enter:  ");
        }

        @Override
        public void exit() {
            Log.w(TAG, "DefaultState.exit:  ");
        }

        @Override
        public boolean processMessage(Message msg) {
            switch (msg.what) {
                default:
                    Log.w(TAG, "DefaultState.processMessage: no handled " + msg.toString());
            }
            return HANDLED;
        }
    }

    private class IdleState extends BaseState {
        @Override
        public void enter() {
            Log.w(TAG, "IdleState.enter:  ");

        }

        @Override
        public void exit() {
            Log.w(TAG, "IdleState.exit:  ");
        }

        @Override
        public boolean processMessage(Message msg) {
            Log.w(TAG, "IdleState.processMessage: no handled " + msg.toString());

            switch (msg.what) {
                case EVENT_CACHE_OPEN:
                    transitionTo(mOpenState);
                    break;
                case EVENT_CACHE_CLOSE:{
                    return HANDLED;
                }


                default:
                    super.processMessage(msg);
            }
            return HANDLED;
        }
    }


    private class ShutDowningState extends BaseState {
        @Override
        public void enter() {
            Log.w(TAG, "ShutDowningState.enter:  ");
            transitionTo(mShutDownedState);
        }

        @Override
        public void exit() {
            Log.w(TAG, "ShutDowningState.exit:  ");
        }
    }

    private class ShutDownedState extends BaseState {
        @Override
        public void enter() {
            Log.w(TAG, "ShutDownedState.enter:  ");
        }

        @Override
        public void exit() {
            Log.w(TAG, "ShutDownedState.exit:  ");
        }

        @Override
        public boolean processMessage(Message msg) {
            Log.w(TAG, "processMessage: " + msg.toString());
            return HANDLED;
        }
    }

    private class StartupState extends BaseState {
        @Override
        public void enter() {
            Log.w(TAG, "StartupState.enter:  ");
            try {
                CacheManager.init(mContext);
                transitionTo(mIdleState);
            } catch (Exception e) {
                Log.e(TAG, "enter: ", e);
            }
        }

        @Override
        public void exit() {
            Log.w(TAG, "StartupState.exit:  ");
        }

        @Override
        public boolean processMessage(Message msg) {
            return super.processMessage(msg);
        }
    }

    private class OpenState extends BaseState {
        private int openCount = 0;

        @Override
        public void enter() {
            Log.w(TAG, "OpenState.enter:  ");
            ++openCount;
            try {
                CacheManager.get().open();
            } catch (Exception e) {
                Log.e(TAG, "enter: ", e);
            }

        }

        @Override
        public void exit() {
            Log.w(TAG, "OpenState.exit:  ");
            try {
                CacheManager.get().close();
            } catch (Exception e) {
                Log.e(TAG, "exit: ", e);
            }
        }

        @Override
        public boolean processMessage(Message msg) {
            Log.d(TAG, "processMessage: openCount=" + openCount);
            switch (msg.what) {
                case EVENT_CACHE_OPEN: {
                    openCount++;
                    Log.d(TAG, "processMessage: EVENT_CACHE_OPEN openCount=" + openCount);

                    break;
                }
                case EVENT_CACHE_GET: {
                    CacheGetEntity entity = (CacheGetEntity) msg.obj;
                    IDataCallback callback = entity.callback;
                    try {
                        Data data = CacheManager.get().loadDataFromDiskLRUCache(entity.key);
                        callback.onDataFetched(data);
                    } catch (Exception e) {

                    }


                    break;
                }
                case EVENT_CACHE_PUT: {
                    CachePutEntity entity = (CachePutEntity) msg.obj;
                    try {
                        CacheManager.get().saveData(entity.key, entity.data);
                    } catch (Exception e) {

                    }
                    break;
                }
                case EVENT_CACHE_CLOSE: {
                    if (--openCount == 0) {
                        transitionTo(mIdleState);
                    }
                    break;
                }
                default:
                    return super.processMessage(msg);

            }
            return HANDLED;
        }
    }

    private class BaseState extends State {
        @Override
        public void enter() {
        }

        @Override
        public void exit() {
        }

        @Override
        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case EVENT_CACHE_OPEN:
                    deferMessage(msg);
                    break;
                case EVENT_CACHE_GET:
                    deferMessage(msg);
                    break;
                case EVENT_CACHE_PUT:
                    deferMessage(msg);
                    break;
                case EVENT_CACHE_CLOSE:
                    deferMessage(msg);
                    break;
                case EVENT_CACHE_SHUTDOWN:
                    transitionTo(mShutDowningState);
                    break;


                default:
            }
            return HANDLED;
        }
    }
}
