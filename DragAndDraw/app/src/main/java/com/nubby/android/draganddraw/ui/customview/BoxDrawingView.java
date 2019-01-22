package com.nubby.android.draganddraw.ui.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.nubby.android.draganddraw.R;
import com.nubby.android.draganddraw.ui.model.Box;

import java.util.ArrayList;

import androidx.annotation.Nullable;

public class BoxDrawingView extends View {
    private static final String TAG = "BoxDrawindView";
    private static final String BUNDLE_BOXES = "bundle_boxes";
    private static final String BUNDLE_SUPER = "bundle_super";

    private Box mCurrentBox;
    private ArrayList<Box> mBoxes = new ArrayList<>();
    private Paint mBoxPaint;
    private Paint mBackgroundPaint;

    public BoxDrawingView(Context context) {
        this(context, null);
    }

    public BoxDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mBoxPaint = new Paint();
        mBoxPaint.setColor(getResources().getColor(R.color.boxColor));

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(getResources().getColor(R.color.backgroundColor));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        PointF current = new PointF(event.getX(), event.getY());
        //event.getActionIndex()
        String action = "";
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                action = "action down";
                if (event.getActionIndex() == 0) {
                    mCurrentBox = new Box(current);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                action = "action move";
                if (mCurrentBox != null && event.getActionIndex() == 0) {
                    mCurrentBox.setCurrent(current);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                action = "action up";
                if (event.getActionIndex() == 0) {
                    mBoxes.add(mCurrentBox);
                    invalidate();
                    mCurrentBox = null;
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                action = "action pointer down";
                if (mCurrentBox != null) {
                    mCurrentBox = rotateBox(mCurrentBox);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                action = "action pointer up";
                break;
            case MotionEvent.ACTION_CANCEL:
                action = "action cancel";
                if (event.getActionIndex() == 0) {
                    mCurrentBox = null;
                }
                break;
        }
        Log.i(TAG, action + " at x = " + current.x + ", y = " + current.y);
        return true;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPaint(mBackgroundPaint);

        for (Box box: mBoxes) {
            drawBox(canvas, box);
        }

        if (mCurrentBox != null) {
            drawBox(canvas, mCurrentBox);
        }
    }

    private void drawBox(Canvas canvas, Box box) {
        float left = Math.min(box.getCurrent().x, box.getOrigin().x);
        float right = Math.max(box.getCurrent().x, box.getOrigin().x);
        float top = Math.min(box.getCurrent().y, box.getOrigin().y);
        float bottom = Math.max(box.getCurrent().y, box.getOrigin().y);
        canvas.drawRect(left, top, right, bottom, mBoxPaint);
    }

    private Box rotateBox(Box box) {
        float xLen = Math.abs(box.getCurrent().x - box.getOrigin().x);
        float yLen = Math.abs(box.getCurrent().y - box.getOrigin().y);

        PointF newOrigin = null;
        PointF current = box.getCurrent();

        if (box.getOrigin().x >= current.x ) {
            if (box.getOrigin().y >= current.y) {
                newOrigin = new PointF(current.x - yLen, current.y + xLen);
            } else {
                newOrigin = new PointF(current.x + yLen, current.y + xLen);
            }
        } else {
            if (box.getOrigin().y >= current.y) {
                newOrigin = new PointF(current.x - yLen, current.y - xLen);
            } else {
                newOrigin = new PointF(current.x + yLen, current.y - xLen);
            }
        }

        Box rotatedBox = new Box(newOrigin);
        rotatedBox.setCurrent(current);
        return rotatedBox;
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable state = super.onSaveInstanceState();
        Bundle bundle = new Bundle();
        bundle.putParcelable(BUNDLE_SUPER, state);
        bundle.putParcelableArrayList(BUNDLE_BOXES, mBoxes);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        mBoxes = ((Bundle) state).getParcelableArrayList(BUNDLE_BOXES);
        Parcelable superState = ((Bundle) state).getParcelable(BUNDLE_SUPER);
        super.onRestoreInstanceState(superState);
    }
}
