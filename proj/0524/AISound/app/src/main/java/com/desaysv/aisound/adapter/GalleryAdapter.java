package com.desaysv.aisound.adapter;

import static com.desaysv.aisound.BaseApplication.BASE_TAG;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.desaysv.aisound.R;
import com.desaysv.sceneengine.util.PxUtil;

import java.util.ArrayList;
import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {
    public static final String TAG = BASE_TAG + "GalleryAdapter";
    private List<GalleryImg> imageList = new ArrayList<>();
    private boolean hasBtn = false;
    private boolean hasBg = false;

    public void initImages(List<GalleryImg> list) {
        imageList = list;
    }

    public List<GalleryImg> getImageList() {
        return imageList;
    }

    public void setHasBtn(boolean has) {
        hasBtn = has;
    }

    public void setHasBg(boolean has) {
        hasBg = has;
    }

    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery, parent, false);
        return new GalleryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {
        GalleryImg image = imageList.get(position);
        holder.galleryItem.setTag(R.id.gallery_item, image);
        holder.imageView.setImageResource(image.pic);
        //holder.imageView.setLayoutParams(new FrameLayout.LayoutParams(image.width, image.height));
        holder.titleText.setText(image.name);
        holder.titleText.setTextSize(PxUtil.pxTodp(24));
        if (hasBtn) {
            holder.btnView.setVisibility(View.VISIBLE);
            holder.btnView.setImageResource(image.isOpen ? R.mipmap.yx_417_9_2 : R.mipmap.yx_417_9_1);
        } else {
            holder.btnView.setVisibility(View.GONE);
        }
        if (hasBg) {
            //holder.imageBg.setBackgroundResource(R.mipmap.yx_415_6);
        } else {
            holder.imageBg.setBackground(null);
        }
        holder.reflectionView.setImageResource(image.mirrorPic);
        //holder.reflectionView.setLayoutParams(new LinearLayout.LayoutParams(image.mirrorWidth, image.mirrorHeight));
        //Log.i(TAG,String.format("position=%s,pic width=%s,height=%s",position,PxUtil.pxTodp(image.width),PxUtil.pxTodp(image.height)));
        // Create reflection effect
        holder.imageView.post(() -> {
            holder.imageView.setDrawingCacheEnabled(true);
            Bitmap originalImage = holder.imageView.getDrawingCache();
            if (originalImage != null) {
                Bitmap reflectionImage = createReflection(originalImage);
                holder.reflectionView.setImageBitmap(reflectionImage);
            }
            holder.imageView.setDrawingCacheEnabled(false);
        });
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    private Bitmap createReflection(Bitmap originalImage) {
        final int reflectionGap = 0;
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        Matrix matrix = new Matrix();
        matrix.preScale(1, -1);

        Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0, height / 2,
                width, height / 2, matrix, false);

        Bitmap finalBitmap = Bitmap.createBitmap(width,
                height / 2, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(finalBitmap);
        canvas.drawBitmap(reflectionImage, 0, reflectionGap, null);

        Paint paint = new Paint();
        LinearGradient shader = new LinearGradient(0, 0, 0,
                finalBitmap.getHeight(), 0x70ffffff, 0x00ffffff,
                Shader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        canvas.drawRect(0, 0, width, finalBitmap.getHeight(), paint);

        return finalBitmap;
    }

    static class GalleryViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView reflectionView;
        TextView titleText;
        ImageView btnView;
        FrameLayout imageBg;
        LinearLayout galleryItem;

        GalleryViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.gallery_image);
            reflectionView = itemView.findViewById(R.id.gallery_mirror);
            titleText = itemView.findViewById(R.id.gallery_name);
            btnView = itemView.findViewById(R.id.gallery_btn);
            imageBg = itemView.findViewById(R.id.gallery_image_bg);
            galleryItem = itemView.findViewById(R.id.gallery_item);
        }
    }

}
