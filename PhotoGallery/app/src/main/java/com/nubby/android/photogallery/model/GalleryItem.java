package com.nubby.android.photogallery.model;

import android.net.Uri;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class GalleryItem {

    @SerializedName("id")
    @Expose
    private String mId;
    @SerializedName("owner")
    @Expose
    private String mOwner;
    @SerializedName("secret")
    @Expose
    private String mSecret;
    @SerializedName("server")
    @Expose
    private String mServer;
    @SerializedName("farm")
    @Expose
    private Integer mFarm;
    @SerializedName("title")
    @Expose
    private String mTitle;
    @SerializedName("ispublic")
    @Expose
    private Integer mIsPublic;
    @SerializedName("isfriend")
    @Expose
    private Integer mIsFriend;
    @SerializedName("isfamily")
    @Expose
    private Integer mIsFamily;
    @SerializedName("url_s")
    @Expose
    private String mUrlS;

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        this.mId = id;
    }

    public String getOwner() {
        return mOwner;
    }

    public void setOwner(String owner) {
        this.mOwner = owner;
    }

    public String getSecret() {
        return mSecret;
    }

    public void setSecret(String secret) {
        this.mSecret = secret;
    }

    public String getServer() {
        return mServer;
    }

    public void setServer(String server) {
        this.mServer = server;
    }

    public Integer getFarm() {
        return mFarm;
    }

    public void setFarm(Integer farm) {
        this.mFarm = farm;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public Integer getIspublic() {
        return mIsPublic;
    }

    public void setIspublic(Integer ispublic) {
        this.mIsPublic = ispublic;
    }

    public Integer getIsfriend() {
        return mIsFriend;
    }

    public void setIsfriend(Integer isfriend) {
        this.mIsFriend = isfriend;
    }

    public Integer getIsfamily() {
        return mIsFamily;
    }

    public void setIsfamily(Integer isfamily) {
        this.mIsFamily = isfamily;
    }

    public String getUrl_s() {
        return mUrlS;
    }

    public void setUrl_s(String url_s) {
        this.mUrlS = url_s;
    }

    public String toString() {
        return mTitle;
    }

    public Uri getPhotoPageUri() {
        return  Uri
                .parse("https://www.flickr.com/photos/")
                .buildUpon()
                .appendPath(mOwner)
                .appendPath(mId)
                .build();
    }
}
