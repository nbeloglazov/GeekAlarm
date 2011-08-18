package com.geekalarm.android;

import android.net.Uri;

public class AlarmSound {
    private String title;
    private Uri uri;
    
    public AlarmSound(String title, Uri uri) {
        this.title = title;
        this.uri = uri;
    }
    public String getTitle() {
        return title;
    }
    public Uri getUri() {
        return uri;
    }
    
    public void setUri(Uri uri) {
        this.uri = uri;
    }
}
