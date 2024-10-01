package com.java.udpmailclient.Model;


public class MailItem {
    private String from;
    private String to;
    private String subject;
    private String content;
    private String time;

    public MailItem(String from, String to, String subject, String content, String time) {
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.content = content;
        this.time = time;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getSubject() {
        return subject;
    }

    public String getContent() {
        return content;
    }

    public String getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "MailItem{" +
                "from=" + from +
                ", to=" + to +
                ", subject='" + subject + '\'' +
                ", content='" + content + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}
