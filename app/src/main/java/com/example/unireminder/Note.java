package com.example.unireminder;

import com.google.firebase.Timestamp;

public class Note {
    String title;
    String content;
    Timestamp timestamp;
    String photoPath;

    public Note() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getPhotoPath() {  // Add this getter
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {  // Add this setter
        this.photoPath = photoPath;
    }
}
