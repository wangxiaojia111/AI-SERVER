package com.hex.ai.fr.serving.model;

import java.io.Serializable;

public class TableRetResult implements Serializable {
    private String code;
    private boolean status;
    private String message;
    private Object table;
    private String image;

    public TableRetResult(boolean status, String message) {
        this.code = "1";
        this.status = status;
        this.message = message;
    }

    public TableRetResult() {
    }

    public TableRetResult(boolean status, String message, String table) {
        this.status = status;
        this.message = message;
        this.table = table;
    }

    public TableRetResult(String code, boolean status, String message) {
        this.status = status;
        this.message = message;
        this.code = code;
    }

    public TableRetResult(String code, boolean status, String message, String table, String image) {
        this.code = code;
        this.status = status;
        this.message = message;
        this.image = image;
        this.table = table;
    }

    public TableRetResult(boolean status) {
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

    public Object getTable() {
        return table;
    }

    public void setTable(Object table) {
        this.table = table;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}