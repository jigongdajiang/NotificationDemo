package gjg.com.notificationdemo.base;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;


import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author gaojigong
 * @version V1.0
 * @Description: 4.3以下，由于系统使用的AIDL机制中无法通过反射去改变其检测是否为系统应用的逻辑，所以无法得到通知开关状态
 * 这个是系统强制要求，所以该方法只适用于4.3及其以上，以下默认返回true
 * @date 17/06/12.
 */
public class NotificationCheckUtil {
    private static final String CHECK_OP_NO_THROW = "checkOpNoThrow";
    private static final String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";

    public static boolean notificationIsOpen(Context context) {
        //API19+
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT || notificationCheckFor19Up(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static boolean notificationCheckFor19Up(Context context) {
        AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        String pkg = context.getApplicationContext().getPackageName();
        int uid = applicationInfo.uid;
        Class appOpsClass;

        try {
            appOpsClass = Class.forName(AppOpsManager.class.getName());
            Method checkOpNoThrowMethod = appOpsClass.getMethod(CHECK_OP_NO_THROW, Integer.TYPE, Integer.TYPE, String.class);
            Field opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION);
            int op = (int) opPostNotificationValue.get(Integer.class);
            return ((int) checkOpNoThrowMethod.invoke(appOpsManager, op, uid, pkg) == AppOpsManager.MODE_ALLOWED);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public interface NotificationCheckResultListener {
        void checkResult(boolean hasOpenNotification);
    }

    public static void checkNotificationOpend(final Activity context,
                                              NotificationCheckResultListener listener,
                                              boolean jump2Setting,
                                              FragmentManager fm) {
        if (notificationIsOpen(context)) {
            if (null != listener) {
                listener.checkResult(true);
            }
        } else {
            if (jump2Setting) {
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                context.startActivity(intent);
            } else {
                listener.checkResult(false);
            }
        }
    }
}