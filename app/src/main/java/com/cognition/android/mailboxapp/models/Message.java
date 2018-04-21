package com.cognition.android.mailboxapp.models;

import com.google.api.services.gmail.model.MessagePart;
import com.raizlabs.android.dbflow.annotation.ColumnIgnore;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

@Table(database = AppDatabase.class, allFields = true)
public class Message extends BaseModel {

    @PrimaryKey(autoincrement = true)
    private int id;
    private int message_id;
    private String labelsJson;
    private String snippet;
    private String mimetype;
    private String headersJson;
    private String partsJson;
    private String from;
    private String subject;
    private long timestamp;
    private int color;

    @ColumnIgnore
    private List<String> labels;
    @ColumnIgnore
    private Map<String, String> headers;
    @ColumnIgnore
    private List<MessagePart> parts;

    public Message() {
    }

    public Message(List<String> labels, String snippet, String mimetype, Map<String, String> headers, List<MessagePart> parts, long timestamp, int color) throws JSONException {
        this.labels = labels;
        this.snippet = snippet;
        this.mimetype = mimetype;
        this.headers = headers;
        this.parts = parts;
        this.timestamp = timestamp;

        this.from = this.headers.get("From");
        this.subject = this.headers.get("Subject");
        this.color = color;

        if (this.labels != null) {
            JSONArray labelsArr = new JSONArray();
            for (String label : this.labels)
                labelsArr.put(label);
            this.labelsJson = labelsArr.toString();
        }

        if (this.headers != null) {
            JSONObject headersObj = new JSONObject();
            for (Map.Entry<String, String> header : this.headers.entrySet())
                headersObj.put(header.getKey(), header.getValue());
            this.headersJson = headersObj.toString();
        }

        if (this.parts != null) {
            JSONArray partsArr = new JSONArray();
            for (MessagePart messagePart : this.parts)
                partsArr.put(messagePart.toString());
            this.partsJson = partsArr.toString();
        }
    }

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

    public String getLabelsJson() {
        return labelsJson;
    }

    public void setLabelsJson(String labelsJson) {
        this.labelsJson = labelsJson;
    }

    public String getHeadersJson() {
        return headersJson;
    }

    public void setHeadersJson(String headersJson) {
        this.headersJson = headersJson;
    }

    public String getPartsJson() {
        return partsJson;
    }

    public void setPartsJson(String partsJson) {
        this.partsJson = partsJson;
    }
}