package com.zj.caseswitcher.utils.log;

import com.intellij.openapi.project.ProjectManager;
import com.zj.caseswitcher.utils.NoticeUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

/**
 * @author : jie.zhou
 * @date : 2025/9/11
 */
public class Logger {
    /**
     * 是否打印日志到Notifications
     */
    private static final boolean LOG_ENABLE;

    static {
        LOG_ENABLE = true;
    }

    private final com.intellij.openapi.diagnostic.Logger log;

    private Logger(com.intellij.openapi.diagnostic.Logger log) {
        this.log = log;
    }

    public static Logger getInstance(Class<?> clazz) {
        return new Logger(com.intellij.openapi.diagnostic.Logger.getInstance(clazz));
    }

    public void log(String msg) {
        info(msg);
    }

    public void info(String message) {
        notice(message);
    }

    public void error(Throwable throwable) {
        ProjectManager.getInstance().getDefaultProject();
        try (StringWriter sw = new StringWriter();
             PrintWriter pw = new PrintWriter(sw)) {
            throwable.printStackTrace(pw);
            error(sw.toString());
        } catch (IOException e) {
            error("printStackTrace error " + e.getMessage());
        }
    }

    public void error(String msg) {
        errorNotice(msg);
    }

    private void notice(String message) {
        // 仅调试时开启，信息显示在打开的第一个project里
        // 发布分支需关闭
        if (LOG_ENABLE) {
            Optional.of(ProjectManager.getInstance().getOpenProjects())
                    .filter(openProjects -> openProjects.length > 0)
                    .map(openProjects -> openProjects[0])
                    .ifPresent(project ->
                            NoticeUtils.notice(message).notify(project)
                    );
        }

        log.info(message);
    }

    private void errorNotice(String message) {
        // 仅调试时开启，信息显示在打开的第一个project里
        if (LOG_ENABLE) {
            Optional.of(ProjectManager.getInstance().getOpenProjects())
                    .filter(openProjects -> openProjects.length > 0)
                    .map(openProjects -> openProjects[0])
                    .ifPresent(project ->
                            NoticeUtils.error(message).notify(project)
                    );
        }

        log.error(message);
    }

    public void warn(String message) {
        // 仅调试时开启，信息显示在打开的第一个project里
        if (LOG_ENABLE) {
            Optional.of(ProjectManager.getInstance().getOpenProjects())
                    .filter(openProjects -> openProjects.length > 0)
                    .map(openProjects -> openProjects[0])
                    .ifPresent(project ->
                            NoticeUtils.warn(message).notify(project)
                    );
        }

        log.warn(message);
    }
}
