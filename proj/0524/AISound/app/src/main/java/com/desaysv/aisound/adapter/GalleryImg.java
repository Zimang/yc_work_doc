package com.desaysv.aisound.adapter;

import androidx.annotation.DrawableRes;

public class GalleryImg {
    public int id;
    @DrawableRes
    public int pic;
    @DrawableRes
    public int mirrorPic;
    public String name;
    public boolean isOpen;
    public int volumn = 50;
    public int mode = 1;
    public int x = 0, y = 0, z = 0;
    public String mediaFile = null;

    public GalleryImg(@DrawableRes int pic, @DrawableRes int mirrorPic, String name) {
        this.pic = pic;
        this.mirrorPic = mirrorPic;  // Use same image for mirror effect
        this.name = name;
        this.id = pic;
    }

    public GalleryImg(@DrawableRes int pic, @DrawableRes int mirrorPic, String name, String mediaFile) {
        this.pic = pic;
        this.mirrorPic = mirrorPic;  // Use same image for mirror effect
        this.name = name;
        this.id = pic;
        this.mediaFile = mediaFile;
    }
}
