package com.zj.caseswitcher.utils;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;

/**
 * 提示工具类
 *
 * @author : jie.zhou
 * @date : 2025/7/2
 */
public class NoticeUtils {

    public static Notification notice(String message) {
        return NotificationGroupManager.getInstance()
                .getNotificationGroup("CaseSwitcher")
                .createNotification(message, NotificationType.INFORMATION);
    }

    public static Notification error(String message) {
        return NotificationGroupManager.getInstance()
                .getNotificationGroup("CaseSwitcher")
                .createNotification(message, NotificationType.ERROR);
    }

    public static Notification warn(String message) {
        return NotificationGroupManager.getInstance()
                .getNotificationGroup("CaseSwitcher")
                .createNotification(message, NotificationType.WARNING);
    }

}
