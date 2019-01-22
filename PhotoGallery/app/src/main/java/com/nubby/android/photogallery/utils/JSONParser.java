package com.nubby.android.photogallery.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nubby.android.photogallery.model.GalleryItem;
import com.nubby.android.photogallery.model.GsonCheme;

import java.util.List;

public final class JSONParser {
    private JSONParser() {}

    public static List<GalleryItem> parseBody(String body) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        List<GalleryItem> list = gson
                .fromJson(body, GsonCheme.class)
                .getPhotos()
                .getGalleryItem();
        return list;
    }
}
