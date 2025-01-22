package com.messaging.rcs.rbm.messages;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sbsingh on Nov/20/2021.
 */
public class RichCards {

    private String title;
    private String description;
    private String imageUrl;
    private String height;
    private String orientation;
    private SuggestedReplies suggestedReplies;

    public SuggestedReplies getSuggestedReplies() {
        return suggestedReplies;
    }

    public void setSuggestedReplies(SuggestedReplies suggestedReplies) {
        this.suggestedReplies = suggestedReplies;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }


    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }
}
