package com.zj.caseswitcher.utils.log;

import com.intellij.openapi.project.ProjectManager;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author : jie.zhou
 * @date : 2025/9/11
 */
public class Logger {
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
        log.info(message);
    }

    private void errorNotice(String message) {
        log.error(message);
    }

    public void warn(String message) {
        log.warn(message);
    }
}
