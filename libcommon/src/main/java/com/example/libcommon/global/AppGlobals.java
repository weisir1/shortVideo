package com.example.libcommon.global;

import android.app.Application;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AppGlobals {
    private static Application sApplication;
    public  static Application getsApplication(){
        if (sApplication == null){
            try {
                //ActivityThread中currentApplication会返回一个currentApplication类
                Method method = Class.forName("android.app.ActivityThread").getDeclaredMethod("currentApplication");
                 sApplication = (Application) method.invoke(null, (Object[]) null);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

        }
        return sApplication;

    }
}
