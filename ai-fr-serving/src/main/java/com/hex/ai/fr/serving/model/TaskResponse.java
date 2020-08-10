package com.hex.ai.fr.serving.model;

import java.io.Serializable;

public class TaskResponse implements Serializable {
    private String code;
    private boolean status;
    private String message;
    protected String objectId;
    public TaskResponse(boolean status, String message) {
        this.status = status;
        this.message = message;
    }

    public TaskResponse() {
    }

    public TaskResponse(boolean status, String message, String objectId) {
        this.status = status;
        this.message = message;
        this.objectId = objectId;
    }

    public TaskResponse(String code, boolean status, String message) {
        this.status = status;
        this.message = message;
        this.code = code;
    }

    public TaskResponse(String code, boolean status, String message, String objectId) {
        this.code = code;
        this.status = status;
        this.message = message;
        this.objectId = objectId;
    }

    public TaskResponse(boolean status) {
        this.status = status;
    }


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }
}
