package com.example.shortvideo;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    public static void main(String[] args) {
        Looper.prepare();
        Looper.loop();
        Handler handler;
        Handler handlers = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                Message message = handler.obtainMessage();
                handler.sendMessage(message);
                return false;
            }
        });
        Looper.myLooper().quitSafely();
    }
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }
}