package com.android.arijit.firebase.todoapp;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ToDo implements Serializable {
//    private int id;
    private String id, task;
    private boolean completed = false;
    private String cdate;

    public ToDo() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        cdate = sdf.format(new java.util.Date());
    }

    public ToDo(String id, String task, boolean completed) {
        this.id = id;
        this.task = task;
        this.completed = completed;
    }

    public ToDo(String id, String task) {
        this.id = id;
        this.task = task;
    }

    public ToDo(String id, String task,  String cdate, boolean completed) {
        this.id = id;
        this.task = task;
        this.completed = completed;
        this.cdate = cdate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getCdate() {
        return cdate;
    }

    public void setCdate(String cdate) {
        this.cdate = cdate;
    }
}
