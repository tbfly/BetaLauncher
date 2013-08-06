/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lennox.launcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Region.Op;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

/**
 * An icon on a PagedView, specifically for items in the launcher's paged view (with compound
 * drawables on the top).
 */
public class PagedViewIcon extends TextView {
    /** A simple callback interface to allow a PagedViewIcon to notify when it has been pressed */
    public static interface PressedCallback {
        void iconPressed(PagedViewIcon icon);
    }

    @SuppressWarnings("unused")
    private static final String TAG = "PagedViewIcon";
    private static final float PRESS_ALPHA = 0.4f;

    private PagedViewIcon.PressedCallback mPressedCallback;
    private boolean mLockDrawableState = false;

    private Bitmap mIcon;

    private int mOriginalTextSize;

    private boolean mTextVisible = true;
    private CharSequence mVisibleText;

    public PagedViewIcon(Context context) {
        this(context, null);
    }

    public PagedViewIcon(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PagedViewIcon(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mOriginalTextSize = (int) getTextSize();
    }

    public void applyFromApplicationInfo(ApplicationInfo info, float scale,
            PagedViewIcon.PressedCallback cb) {
        mIcon = info.iconBitmap;
        int width = (int)((float)mIcon.getWidth() * scale);
        int height = (int)((float)mIcon.getHeight() * scale);
        FastBitmapDrawable d = new FastBitmapDrawable(Bitmap.createScaledBitmap(mIcon,
                width, height, true));
        mPressedCallback = cb;
        setCompoundDrawablesWithIntrinsicBounds(null, d, null, null);
        setCompoundDrawablePadding(0);
        setText(info.title);
        setTag(info);
    }

    public void setIconScale(float scale) {
        Drawable d = getCompoundDrawables()[1];
        Bitmap b = ((FastBitmapDrawable)d).getBitmap();
        int width = (int)((float)b.getWidth() * scale);
        int height = (int)((float)b.getHeight() * scale);
        d.setBounds(new Rect(0, 0, width, height));
        setCompoundDrawables(null,
                d, null, null);
    }

    public void lockDrawableState() {
        mLockDrawableState = true;
    }

    public void resetDrawableState() {
        mLockDrawableState = false;
        post(new Runnable() {
            @Override
            public void run() {
                refreshDrawableState();
            }
        });
    }

    public void setTextScale(float scale, boolean padding) {
        setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) mOriginalTextSize * (float) scale);
        if (padding) {
            setEllipsize(TextUtils.TruncateAt.END);
            setSingleLine(true);
        } else {
            setEllipsize(TextUtils.TruncateAt.MARQUEE);
            setSingleLine(false);
            setMaxLines(2);
        }
    }

    public boolean getTextVisible() {
        return mTextVisible;
    }

    public void setTextVisible(boolean visible) {
        if (mTextVisible == visible) return;
        mTextVisible = visible;
        if (visible) {
            setText(mVisibleText);
        } else {
            mVisibleText = getText();
            setText("");
        }
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();

        // We keep in the pressed state until resetDrawableState() is called to reset the press
        // feedback
        if (isPressed()) {
            setAlpha(PRESS_ALPHA);
            if (mPressedCallback != null) {
                mPressedCallback.iconPressed(this);
            }
        } else if (!mLockDrawableState) {
            setAlpha(1f);
        }
    }
}
