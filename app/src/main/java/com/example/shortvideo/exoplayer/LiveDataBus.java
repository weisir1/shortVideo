package com.example.shortvideo.exoplayer;

import android.icu.text.CaseMap;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import java.util.concurrent.ConcurrentHashMap;

public class LiveDataBus {
    private static class Lazy {
        static LiveDataBus liveDataBus = new LiveDataBus();
    }

    public static LiveDataBus get() {
        return Lazy.liveDataBus;
    }

    //保证线程同步
    private ConcurrentHashMap<String, StickyLiveData> hashMap = new ConcurrentHashMap<String, StickyLiveData>();

    public StickyLiveData with(String eventName) {
        StickyLiveData liveData = hashMap.get(eventName);
        if (liveData == null) {
            liveData = new StickyLiveData(eventName);
            hashMap.put(eventName, liveData);
        }
        return liveData;
    }

    public class StickyLiveData<T> extends LiveData<T> {
        //        标记事件名称
        private String eventName;
        //      事件
        private T stickyData;    //粘性事件,如果没有观察者,暂时存储
        //      标记发送的事件次数
        private int version = 0;

        public StickyLiveData(String eventName) {

            this.eventName = eventName;
        }

        @Override
        public void setValue(T value) {
            version++;
            super.setValue(value);
        }

        @Override
        public void postValue(T value) {
            version++;
            super.postValue(value);
        }

        //      支持同步发送一条粘性事件
        public void setStickyData(T stickyData) {
            this.stickyData = stickyData;
            setValue(stickyData);
        }

        //        支持异步发送一条粘性事件
        public void postStickyData(T stickyData) {
            this.stickyData = stickyData;
            postValue(stickyData);
        }

        @Override
        public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
            observerSticky(owner, observer, false);
        }

        public void observerSticky(LifecycleOwner owner, Observer<? super T> observer, boolean sticky) {
//            包装一下回调的observer
            super.observe(owner, new WrapperObserver<>(this, observer, sticky));
//            当宿主状态发生改变时,移除掉
            owner.getLifecycle().addObserver(new LifecycleEventObserver() {
                @Override
                public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                    if (event == Lifecycle.Event.ON_DESTROY) {
                        hashMap.remove(eventName);
                    }
                }
            });
        }

        private class WrapperObserver<T> implements Observer<T> {
            private StickyLiveData liveData;
            private Observer<T> observer;
            private boolean sticky;
            private int lastVersion = 0;

            public WrapperObserver(StickyLiveData liveData, Observer<T> observer, boolean sticky) {
                this.liveData = liveData;
                this.observer = observer;
                this.sticky = sticky;
                lastVersion = liveData.version;
            }

            @Override
            public void onChanged(T t) {
//               粘性事件 可以理解为先发送消息 后注册 先发送的消息
                if (lastVersion >= liveData.version) {
//                    如果是粘性事件 ,livedata会在下一次有观察者注册时,回调其change(),livedata天生是一个粘性事件
                    if (sticky && liveData.stickyData != null) {
                        observer.onChanged((T)liveData.stickyData);
                    }
                    return;
                }
//               小于发送的版本,则发送后续事件
                lastVersion = liveData.version;
                observer.onChanged(t);
            }
        }
    }
}
