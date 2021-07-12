package com.android.arijit.firebase.todoapp;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Comment implements Serializable {
    private String cid;
    private String cmtext, cmdate;

    public Comment() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        cmdate = sdf.format(new java.util.Date());
    }

    public Comment(String cid, String cmtext, String cmdate) {
        this.cid = cid;
        this.cmtext = cmtext;
        this.cmdate = cmdate;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getCmtext() {
        return cmtext;
    }

    public void setCmtext(String cmtext) {
        this.cmtext = cmtext;
    }

    public String getCmdate() {
        return cmdate;
    }

    public void setCmdate(String cmdate) {
        this.cmdate = cmdate;
    }
}
