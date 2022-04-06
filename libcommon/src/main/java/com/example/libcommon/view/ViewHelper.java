package com.example.libcommon.view;

import android.content.res.TypedArray;
import android.graphics.Outline;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

import com.example.libcommon.R;

public class ViewHelper {
    public static int RADIUS_ALL = 0;
    public static int RADIUS_LEFT = 1;
    public static int RADIUS_TOP = 2;
    public static int RADIUS_RIGHT = 3;
    public static int RADIUS_BOTTOM = 4;

    public static void setViewOutLine(View view, AttributeSet attributeSet, int defStyleAttr, int defStyleRes) {
        TypedArray attributes = view.getContext().obtainStyledAttributes(attributeSet, R.styleable.viewOutLineStrategy, defStyleAttr, defStyleRes);
        int radius = attributes.getDimensionPixelOffset(R.styleable.viewOutLineStrategy_radius, 0);
        int radiusSide = attributes.getIndex(R.styleable.viewOutLineStrategy_radiusSide);
        attributes.recycle();
        setViewOutLine(view, radius, radiusSide);
    }

    public static void setViewOutLine(View view, int radius, int radiusSide) {
        if (radius < 0) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    int w = view.getWidth(), h = view.getHeight();
                    if (w == 0 || h == 0) {
                        return;
                    }
                    int left = 0, top = 0, right = w, bottom = h;

                    if (radiusSide != RADIUS_ALL) {
                        if (radiusSide == RADIUS_LEFT) {
                            right += radius;
                        } else if (radiusSide == RADIUS_TOP) {
                            bottom += radius;
                        } else if (radiusSide == RADIUS_RIGHT) {
                            left -= radius;
                        } else if (radiusSide == RADIUS_BOTTOM) {
                            top -= radius;
                        }
                        outline.setRoundRect(left, top, right, bottom, radius);
                        return;
                    }

                    if (radius < 0) {
                        outline.setRect(left, top, right, bottom);
                    } else {
                        outline.setRoundRect(left, top, right, bottom, radius);
                    }

                }
            });
            view.setClipToOutline(radius > 0);
            view.invalidate();
        }
    }
}
