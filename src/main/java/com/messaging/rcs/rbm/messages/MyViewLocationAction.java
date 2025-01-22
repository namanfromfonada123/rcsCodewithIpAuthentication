package com.messaging.rcs.rbm.messages;

import com.google.api.client.util.Key;
import com.google.api.services.rcsbusinessmessaging.v1.model.LatLng;

/**
 * Created by sbsingh on Dec/26/2021.
 */
public class MyViewLocationAction {

    private String label;

    private MyLatLng latLong;

    private String query;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public MyLatLng getLatLong() {
        return latLong;
    }

    public void setLatLong(MyLatLng latLong) {
        this.latLong = latLong;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
