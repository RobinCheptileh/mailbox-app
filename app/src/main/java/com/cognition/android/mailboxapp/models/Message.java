package com.cognition.android.mailboxapp.models;

import com.google.api.services.gmail.model.MessagePart;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.List;
import java.util.Map;

@Table(database = AppDatabase.class)
public class Message extends BaseModel {

    public Message() {
    }

    public Message(List<String> labels, String snippet, String mimetype, Map<String, String> headers, List<MessagePart> parts, long timestamp, int color) {
        this.labels = labels;
        this.snippet = snippet;
        this.mimetype = mimetype;
        this.headers = headers;
        this.parts = parts;

        this.timestamp = timestamp;

        this.from = this.headers.get("From");
        this.subject = this.headers.get("Subject");
        this.color = color;
    }

    @PrimaryKey(autoincrement = true)
    private int id;
    @Column
    private int message_id;
    private List<String> labels;
    @Column
    private String snippet;
    @Column
    private String mimetype;
    private Map<String, String> headers;
    private List<MessagePart> parts;
    @Column
    private String from;
    @Column
    private String subject;
    @Column
    private long timestamp;
    private int color;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMessage_id() {
        return message_id;
    }

    public void setMessage_id(int message_id) {
        this.message_id = message_id;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public String getMimetype() {
        return mimetype;
    }

    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public List<MessagePart> getParts() {
        return parts;
    }

    public void setParts(List<MessagePart> parts) {
        this.parts = parts;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}