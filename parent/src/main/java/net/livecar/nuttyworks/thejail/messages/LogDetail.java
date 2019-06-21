package net.livecar.nuttyworks.thejail.messages;

import java.util.Date;

public class LogDetail {
    public Date logDateTime;
    public String logContent;

    public LogDetail(String logContent) {
        logDateTime = new Date();
        this.logContent = logContent;
    }
}