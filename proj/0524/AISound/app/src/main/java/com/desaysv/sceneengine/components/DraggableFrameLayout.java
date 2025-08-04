package com.desaysv.sceneengine.components;

import static com.desaysv.aisound.BaseApplication.BASE_TAG;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.desaysv.aisound.MainActivity;
import com.desaysv.aisound.Coordinate;
import com.desaysv.sceneengine.util.PxUtil;

public class DraggableFrameLayout extends FrameLayout {

    public static final String TAG = BASE_TAG + "DraggableFrameLayout";
    private View mCurrentChild;
    private float mInitialTouchX;
    private float mInitialTouchY;
    private float mLastTouchX;
    private float mLastTouchY;

    private Coordinate centerPoint = Coordinate.getZcCenter();

    public DraggableFrameLayout(Context context) {
        super(context);
    }

    public DraggableFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DraggableFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                mLastTouchX = x;
                mLastTouchY = y;
                mInitialTouchX = x;
                mInitialTouchY = y;

                // Find which child was touched
                mCurrentChild = null;
                for (int i = 0; i < getChildCount(); i++) {
                    View child = getChildAt(i);
                    if (isPointInView(child, x, y)) {
                        mCurrentChild = child;
                        break;
                    }
                }
                return true;
            }

            case MotionEvent.ACTION_MOVE: {
                if (mCurrentChild != null) {
                    final float dx = x - mLastTouchX;
                    final float dy = y - mLastTouchY;

                    Log.i(TAG, "updateImageCoordinates x,y=" + x + "," + y);
                    //Log.i(TAG, "updateImageCoordinates " + (int) PxUtil.dpTOpx(884) + "," + (int) PxUtil.dpTOpx(484) + "," + (int) PxUtil.dpTOpx(96) + "," + (int) PxUtil.dpTOpx(40));

                    if (x < (int) PxUtil.dpTOpx(980) / 2) {
                        if (y < (int) PxUtil.dpTOpx(100)) {
                            return true;
                        }
                        if (y > (int) PxUtil.dpTOpx(406)) {
                            return true;
                        }
                    }

                    if (x > (int) PxUtil.dpTOpx(980) / 2) {
                        if (y < (int) PxUtil.dpTOpx(40)) {
                            return true;
                        }
                        if (y > (int) PxUtil.dpTOpx(346)) {
                            return true;
                        }
                    }

                    if (x > (int) PxUtil.dpTOpx(980) - (int) PxUtil.dpTOpx(96) || x < (int) PxUtil.dpTOpx(200)) {
                        return true;
                    }

                    mCurrentChild.setTranslationX(mCurrentChild.getTranslationX() + dx);
                    mCurrentChild.setTranslationY(mCurrentChild.getTranslationY() + dy);

                    mLastTouchX = x;
                    mLastTouchY = y;

                    // Update coordinates in MainActivity
                    ((MainActivity) getContext()).updateImageCoordinates(mCurrentChild.getId(), mLastTouchX, mLastTouchY);
                }
                return true;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                mCurrentChild = null;
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    private boolean isPointInView(View view, float x, float y) {
        // Convert coordinates to local view space, accounting for translation
        float localX = x - (view.getLeft() + view.getTranslationX());
        float localY = y - (view.getTop() + view.getTranslationY());

        return (localX >= 0 && localX < view.getWidth() &&
                localY >= 0 && localY < view.getHeight());
    }

}