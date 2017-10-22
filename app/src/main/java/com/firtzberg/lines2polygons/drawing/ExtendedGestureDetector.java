package com.firtzberg.lines2polygons.drawing;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Created by hrvoje on 15.10.17..
 * Gesture detector additionally detecting a finger up gesture.
 */
public class ExtendedGestureDetector extends GestureDetector {
    ExtendedGestureDetector.OnGestureListener fListener;

    public ExtendedGestureDetector(Context context, OnGestureListener listener) {
        super(context, listener);
        fListener = listener;
    }

    public ExtendedGestureDetector(Context context, GestureDetector.OnGestureListener listener, OnGestureListener fListener) {
        super(context, listener);
        this.fListener = fListener;
    }

    public ExtendedGestureDetector(Context context, GestureDetector.OnGestureListener listener, Handler handler, OnGestureListener fListener) {
        super(context, listener, handler);
        this.fListener = fListener;
    }

    public ExtendedGestureDetector(Context context, GestureDetector.OnGestureListener listener, Handler handler, boolean unused, OnGestureListener fListener) {
        super(context, listener, handler, unused);
        this.fListener = fListener;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent ev) {
        if (super.onTouchEvent(ev)) return true;
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            return fListener.onFingerUp(ev);
        }
        return false;
    }

    public interface OnGestureListener extends GestureDetector.OnGestureListener {
        boolean onFingerUp(MotionEvent e);
    }

    public static class SimpleOnGestureListener extends GestureDetector.SimpleOnGestureListener implements ExtendedGestureDetector.OnGestureListener {
        @Override
        public boolean onFingerUp(MotionEvent e) {
            return false;
        }
    }
}