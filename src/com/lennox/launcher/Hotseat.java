/*
 * Copyright (C) 2011 The Android Open Source Project
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
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.lennox.launcher.R;
import com.lennox.launcher.preference.PreferencesProvider;

public class Hotseat extends PagedView {
    private int mCellCount;

    private int mHotseatPages;
    private int mDefaultPage;

    private boolean mTransposeLayoutWithOrientation;
    private boolean mIsLandscape;
    private boolean mScrollTransformsDirty = false;
    private boolean mOverscrollTransformsDirty = false;
    private int mCameraDistance;

    private boolean mStretchCells;
    private boolean mFitToCells;

    boolean mIsDragOccuring = false;

    private float[] mTempCellLayoutCenterCoordinates = new float[2];
    private Matrix mTempInverseMatrix = new Matrix();

    private static final int DEFAULT_PAGE = 0;

    private static final int DEFAULT_CELL_COUNT = 5;

    public enum TransitionEffect {
        Standard,
        Flip,
        Accordion,
        CarouselLeft,
        CarouselRight,
        Shutter
    }
    private TransitionEffect mTransitionEffect = TransitionEffect.Standard;

    public Hotseat(Context context) {
        this(context, null);
    }

    public Hotseat(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Hotseat(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        final Resources res = getResources();

        mIsLandscape = res.getConfiguration().orientation ==
            Configuration.ORIENTATION_LANDSCAPE;

        mTransitionEffect = PreferencesProvider.Interface.Dock.Scrolling.getTransitionEffect(
                res.getString(R.string.config_dockDefaultTransitionEffect));

        mFadeInAdjacentScreens = PreferencesProvider.Interface.Dock.getFadeInAdjacentScreens();
        mHandleScrollIndicator = true;

        mCameraDistance = res.getInteger(R.integer.config_cameraDistance);

        mHotseatPages = PreferencesProvider.Interface.Dock.getNumberPages(mIsLandscape);
        int defaultPage = PreferencesProvider.Interface.Dock.getDefaultPage(DEFAULT_PAGE, mIsLandscape);
        if (defaultPage >= mHotseatPages) {
            defaultPage = mHotseatPages / 2;
        }

        mCurrentPage = mDefaultPage = defaultPage;

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.Hotseat, defStyle, 0);
        mTransposeLayoutWithOrientation =
                res.getBoolean(R.bool.hotseat_transpose_layout_with_orientation);
        mCellCount = a.getInt(R.styleable.Hotseat_cellCount, DEFAULT_CELL_COUNT);
        mCellCount = PreferencesProvider.Interface.Dock.getNumberIcons(mCellCount, mIsLandscape);
        setBackgroundResource(R.drawable.hotseat_background);

        LauncherModel.updateHotseatLayoutCells(mCellCount);

        mVertical = hasVerticalHotseat();

        mStretchCells = PreferencesProvider.Interface.Dock.getStretchScreens(mIsLandscape);
        mFitToCells = PreferencesProvider.Interface.Dock.getFitToCells(mIsLandscape);
        mInfiniteScrolling = PreferencesProvider.Interface.Dock.Scrolling.getInfiniteScrolling(mIsLandscape);

        boolean hideDockIconLabels = PreferencesProvider.Interface.Dock.getHideIconLabels(mIsLandscape) ||
                (mVertical && !LauncherApplication.isScreenLarge());

        float childrenScale = PreferencesProvider.Interface.Dock.getIconScale(mIsLandscape) / 100f;
        float textScale = PreferencesProvider.Interface.Dock.getTextScale(mIsLandscape) / 100f;
        boolean textPadding = PreferencesProvider.Interface.Dock.getTextPadding(mIsLandscape);

        LayoutInflater inflater =
                (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i = 0; i < mHotseatPages; i++) {
            CellLayout cl = (CellLayout) inflater.inflate(R.layout.hotseat_page, null);
            cl.setChildrenScale(childrenScale);
            cl.setGridSize((!hasVerticalHotseat() ? mCellCount : 1), (hasVerticalHotseat() ? mCellCount : 1), mVertical);
            cl.setStretchCells(mStretchCells, mFitToCells, hideDockIconLabels, mVertical);
            cl.setTextScale(textScale, textPadding);

            addView(cl);
        }

        // No data needed
        setDataIsReady();

        setOnKeyListener(new HotseatIconKeyEventListener());
    }

    public void setDragOccuring(boolean isOccuring) {
        mIsDragOccuring = isOccuring;
    }

    public boolean hasPage(View view) {
        for (int i = 0; i < getChildCount(); i++) {
            if (view == getChildAt(i)) {
                return true;
            }
        }
        return false;
    }

    boolean hasVerticalHotseat() {
        return (mIsLandscape && mTransposeLayoutWithOrientation);
    }

    /* Get the orientation invariant order of the item in the hotseat for persistence. */
    int getOrderInHotseat(int x, int y) {
        return hasVerticalHotseat() ? (mCellCount - y - 1) : x;
    }
    /* Get the orientation specific coordinates given an invariant order in the hotseat. */
    int getCellXFromOrder(int rank) {
        return hasVerticalHotseat() ? 0 : rank;
    }
    int getCellYFromOrder(int rank) {
        return hasVerticalHotseat() ? (mCellCount - rank - 1) : 0;
    }
    int getInverterCellXFromOrder(int rank) {
        return hasVerticalHotseat() ? (mCellCount - rank - 1) : 0;
    }
    int getInverterCellYFromOrder(int rank) {
        return hasVerticalHotseat() ? 0 : rank;
    }
    int getScreenFromOrder(int screen) {
        return hasVerticalHotseat() ? (getChildCount() - screen - 1) : screen;
    }

    int[] getDatabaseCellsFromLayout(int[] lpCells) {
        if (!hasVerticalHotseat()) {
            return lpCells;
        }
        // On landscape with vertical hotseat, the items are stored in y axis and from up to down,
        // so we need to convert to x axis and left to right prior to save to database. In screen
        // the item has the right coordinates
        return new int[]{mCellCount - lpCells[1] - 1, lpCells[0]};
    }

    /*
     *
     * Convert the 2D coordinate xy from the parent View's coordinate space to this CellLayout's
     * coordinate space. The argument xy is modified with the return result.
     *
     * if cachedInverseMatrix is not null, this method will just use that matrix instead of
     * computing it itself; we use this to avoid redundant matrix inversions in
     * findMatchingPageForDragOver
     *
     */
    void mapPointFromSelfToChild(View v, float[] xy, Matrix cachedInverseMatrix) {
        if (cachedInverseMatrix == null) {
            v.getMatrix().invert(mTempInverseMatrix);
            cachedInverseMatrix = mTempInverseMatrix;
        }
        int scrollX = getScrollX();
        if (mNextPage != INVALID_PAGE) {
            scrollX = mScroller.getFinalX();
        }
        xy[0] = xy[0] + scrollX - v.getLeft();
        xy[1] = xy[1] + getScrollY() - v.getTop();
        cachedInverseMatrix.mapPoints(xy);
    }

    /**
     * Convert the 2D coordinate xy from this CellLayout's coordinate space to
     * the parent View's coordinate space. The argument xy is modified with the return result.
     */
    void mapPointFromChildToSelf(View v, float[] xy) {
        v.getMatrix().mapPoints(xy);
        int scrollX = getScrollX();
        if (mNextPage != INVALID_PAGE) {
            scrollX = mScroller.getFinalX();
        }
        xy[0] -= (scrollX - v.getLeft());
        xy[1] -= (getScrollY() - v.getTop());
    }

    /**
     * This method returns the CellLayout that is currently being dragged to. In order to drag
     * to a CellLayout, either the touch point must be directly over the CellLayout, or as a second
     * strategy, we see if the dragView is overlapping any CellLayout and choose the closest one
     *
     * Return null if no CellLayout is currently being dragged over
     */
    CellLayout findMatchingPageForDragOver(float originX, float originY, boolean exact) {
        // We loop through all the screens (ie CellLayouts) and see which ones overlap
        // with the item being dragged and then choose the one that's closest to the touch point
        final int screenCount = getChildCount();
        CellLayout bestMatchingScreen = null;
        float smallestDistSoFar = Float.MAX_VALUE;

        for (int i = 0; i < screenCount; i++) {
            CellLayout cl = (CellLayout) getChildAt(i);

            final float[] touchXy = {originX, originY};
            // Transform the touch coordinates to the CellLayout's local coordinates
            // If the touch point is within the bounds of the cell layout, we can return immediately
            cl.getMatrix().invert(mTempInverseMatrix);
            mapPointFromSelfToChild(cl, touchXy, mTempInverseMatrix);

            if (touchXy[0] >= 0 && touchXy[0] <= cl.getWidth() &&
                    touchXy[1] >= 0 && touchXy[1] <= cl.getHeight()) {
                return cl;
            }

            if (!exact) {
                // Get the center of the cell layout in screen coordinates
                final float[] cellLayoutCenter = mTempCellLayoutCenterCoordinates;
                cellLayoutCenter[0] = cl.getWidth()/2;
                cellLayoutCenter[1] = cl.getHeight()/2;
                mapPointFromChildToSelf(cl, cellLayoutCenter);

                touchXy[0] = originX;
                touchXy[1] = originY;

                // Calculate the distance between the center of the CellLayout
                // and the touch point
                float dist = Workspace.squaredDistance(touchXy, cellLayoutCenter);

                if (dist < smallestDistSoFar) {
                    smallestDistSoFar = dist;
                    bestMatchingScreen = cl;
                }
            }
        }
        return bestMatchingScreen;
    }

    public void setChildrenOutlineAlpha(float alpha) {
        for (int i = 0; i < getChildCount(); i++) {
            CellLayout cl = (CellLayout) getChildAt(i);
            cl.setBackgroundAlpha(alpha);
        }
    }

    /**
     * Return the current {@link CellLayout}, correctly picking the destination
     * screen while a scroll is in progress.
     */
    public CellLayout getCurrentDropLayout() {
        return (CellLayout) getChildAt(getNextPage());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        resetLayout();
    }

    // Transition effects
    @Override
    protected void screenScrolled(int screenScroll) {
        super.screenScrolled(screenScroll);

        boolean isInOverscroll = !mVertical ? (mOverScrollX < 0 || mOverScrollX > mMaxScrollX) :
                (mOverScrollY < 0 || mOverScrollY > mMaxScrollY);
        if (isInOverscroll && !mOverscrollTransformsDirty) {
            mScrollTransformsDirty = true;
        }
        if (!isInOverscroll || mScrollTransformsDirty || mInfiniteScrolling) {
            // Limit the "normal" effects to mScrollX/Y
            int scroll = !mVertical ? getScrollX() : getScrollY();

            // Reset transforms when we aren't in overscroll
            if (mOverscrollTransformsDirty && !mInfiniteScrolling) {
                mOverscrollTransformsDirty = false;
                View v0 = getPageAt(0);
                View v1 = getPageAt(getChildCount() - 1);
                if (!mVertical) {
                    v0.setTranslationX(0);
                    v1.setTranslationX(0);
                    v0.setRotationY(0);
                    v1.setRotationY(0);
                } else {
                    v0.setTranslationY(0);
                    v1.setTranslationY(0);
                    v0.setRotationX(0);
                    v1.setRotationX(0);
                }
                v0.setCameraDistance(mDensity * 1280);
                v1.setCameraDistance(mDensity * 1280);
                v0.setPivotX(v0.getMeasuredWidth() / 2);
                v1.setPivotX(v1.getMeasuredWidth() / 2);
                v0.setPivotY(v0.getMeasuredHeight() / 2);
                v1.setPivotY(v1.getMeasuredHeight() / 2);
            }

            if (mIsDragOccuring) {
                screenScrolledStandard(scroll);
                mScrollTransformsDirty = false;
                return;
            }

            switch (mTransitionEffect) {
                case Standard:
                    screenScrolledStandard(scroll);
                    break;
                case Flip:
                    screenScrolledFlip(scroll);
                    break;
                case Accordion:
                    screenScrolledAccordion(scroll);
                    break;
                case CarouselLeft:
                    screenScrolledCarousel(scroll, true);
                    break;
                case CarouselRight:
                    screenScrolledCarousel(scroll, false);
                    break;
                case Shutter:
                    screenScrolledShutter(scroll, mVertical, false);
                    break;
            }
            mScrollTransformsDirty = false;
        }

        if (isInOverscroll && !mInfiniteScrolling) {
            int index = (!mVertical ? mOverScrollX : mOverScrollY) < 0 ? 0 : getChildCount() - 1;
            View v = getPageAt(index);
            if (v != null) {
                float scrollProgress = getScrollProgress(screenScroll, v, index);
                float rotation = -24f * scrollProgress;
                if (!mOverscrollTransformsDirty) {
                    mOverscrollTransformsDirty = true;
                    if (!mVertical) {
                        v.setPivotX(v.getMeasuredWidth() * (index == 0 ? 0.65f : 1 - 0.65f));
                        v.setPivotY(v.getMeasuredHeight() * 0.5f);
                        v.setTranslationX(0);
                    } else {
                        v.setPivotX(v.getMeasuredWidth() * 0.5f);
                        v.setPivotY(v.getMeasuredHeight() * (index == 0 ? 0.65f : 1 - 0.65f));
                        v.setTranslationY(0);
                    }
                }
                if (!mVertical) {
                    v.setRotationY(rotation);
                } else {
                    v.setRotationX(-rotation);
                }
            }
        }
    }

    private void screenScrolledCarousel(int screenScroll, boolean left) {
        for (int i = 0; i < getChildCount(); i++) {
            View v = getPageAt(i);
            if (v != null) {
                float scrollProgress = getScrollProgress(screenScroll, v, i);
                float rotation = 90.0f * scrollProgress;

                if (scrollProgress >= -0.98f && scrollProgress <= 0.98f) {
                v.setCameraDistance(mDensity * mCameraDistance);
                if (!mVertical) {
                    v.setTranslationX(v.getMeasuredWidth() * scrollProgress);
                    v.setPivotX(left ? 0f : v.getMeasuredWidth());
                    v.setPivotY(v.getMeasuredHeight() / 2);
                    v.setRotationY(-rotation);
                } else {
                    v.setTranslationY(v.getMeasuredHeight() * scrollProgress);
                    v.setPivotX(v.getMeasuredWidth() / 2);
                    v.setPivotY(left ? 0f : v.getMeasuredHeight());
                    v.setRotationX(rotation);
                }
                    if (v.getVisibility() != VISIBLE) {
                        v.setVisibility(VISIBLE);
                    }
                    if (mFadeInAdjacentScreens) {
                        float alpha = 1 - Math.abs(scrollProgress);
                        v.setAlpha(alpha);
                    }
                } else {
                    v.setVisibility(INVISIBLE);
                }
                invalidate();
            }
        }
    }

    private void screenScrolledFlip(int screenScroll) {
        for (int i = 0; i < getChildCount(); i++) {
            View v = getPageAt(i);
            if (v != null) {
                float scrollProgress = getScrollProgress(screenScroll, v, i);
                float rotation = -180.0f * scrollProgress;

                if (scrollProgress >= -0.5f && scrollProgress <= 0.5f) {
                    v.setPivotX(v.getMeasuredWidth() * 0.5f);
                    v.setPivotY(v.getMeasuredHeight() * 0.5f);
                    if (!mVertical) {
                        v.setTranslationX(v.getMeasuredWidth() * scrollProgress);
                        v.setRotationY(rotation);
                    } else {
                        v.setTranslationY(v.getMeasuredHeight() * scrollProgress);
                        v.setRotationX(-rotation);
                    }
                    if (v.getVisibility() != VISIBLE) {
                        v.setVisibility(VISIBLE);
                    }
                    if (mFadeInAdjacentScreens) {
                        float alpha = 1 - Math.abs(scrollProgress);
                        v.setAlpha(alpha);
                    }
                } else {
                    v.setVisibility(INVISIBLE);
                }
                invalidate();
            }
        }
    }

    private void screenScrolledAccordion(int screenScroll) {
        for (int i = 0; i < getChildCount(); i++) {
            View v = getPageAt(i);
            if (v != null) {
                float scrollProgress = getScrollProgress(screenScroll, v, i);
                float scale = 1.0f - Math.abs(scrollProgress);

                if (scrollProgress >= -0.98f && scrollProgress <= 0.98f) {
                    if (!mVertical) {
                        v.setPivotX(scrollProgress < 0 ? 0 : v.getMeasuredWidth());
                        v.setScaleX(scale);
                    } else {
                        v.setPivotY(scrollProgress < 0 ? 0 : v.getMeasuredHeight());
                        v.setScaleY(scale);
                    }
                    if (v.getVisibility() != VISIBLE) {
                        v.setVisibility(VISIBLE);
                    }
                    if (mFadeInAdjacentScreens) {
                        float alpha = 1 - Math.abs(scrollProgress);
                        v.setAlpha(alpha);
                    }
                } else {
                    v.setVisibility(INVISIBLE);
                }
                invalidate();
            }
        }
    }

    private void screenScrolledStandard(int screenScroll) {
        for (int i = 0; i < getChildCount(); i++) {
            View v = getPageAt(i);
            if (v != null) {
                float scrollProgress = getScrollProgress(screenScroll, v, i);
                if (mFadeInAdjacentScreens) {
                    float alpha = 1 - Math.abs(scrollProgress);
                    v.setAlpha(alpha);
                }
                invalidate();
            }
        }
    }

    private void screenScrolledShutter(int screenScroll, boolean vertical, boolean reverse) {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getPageAt(i);
            if (view == null) return;
            if (view instanceof PagedViewCellLayout) {
                PagedViewCellLayout cl = (PagedViewCellLayout) view;
                float scrollProgress = getScrollProgress(screenScroll, cl, i);
                float rotation = (reverse ? 180.0f : -180.0f) * scrollProgress;
                if (scrollProgress >= -0.5f && scrollProgress <= 0.5f) {
                    if (!vertical) {
                        cl.setTranslationX(cl.getMeasuredWidth() * scrollProgress);
                    } else {
                        cl.setTranslationY(cl.getMeasuredHeight() * scrollProgress);
                    }
                    PagedViewCellLayoutChildren children = cl.getChildrenLayout();
                    for (int j = 0; j < children.getChildCount(); j++) {
                        View v = children.getChildAt(j);
                        v.setPivotX(v.getMeasuredWidth() * 0.5f);
                        v.setPivotY(v.getMeasuredHeight() * 0.5f);
                        if (!vertical) {
                            v.setTranslationX(0);
                            v.setRotationY(rotation);
                        } else {
                            v.setTranslationY(0);
                            v.setRotationX(rotation);
                        }
                    }
                    if (cl.getVisibility() != VISIBLE) {
                        cl.setVisibility(VISIBLE);
                    }
                    if (mFadeInAdjacentScreens) {
                        float alpha = 1 - Math.abs(scrollProgress);
                        cl.setAlpha(alpha);
                    }
                } else {
                    cl.setVisibility(INVISIBLE);
                }
            } else if (view instanceof PagedViewGridLayout) {
                PagedViewGridLayout cl = (PagedViewGridLayout) view;
                float scrollProgress = getScrollProgress(screenScroll, cl, i);
                float rotation = (reverse ? 180.0f : -180.0f) * scrollProgress;
                if (scrollProgress >= -0.5f && scrollProgress <= 0.5f) {
                    if (!vertical) {
                        cl.setTranslationX(cl.getMeasuredWidth() * scrollProgress);
                    } else {
                        cl.setTranslationY(cl.getMeasuredHeight() * scrollProgress);
                    }
                    for (int j = 0; j < cl.getChildCount(); j++) {
                        View v = cl.getChildAt(j);
                        v.setPivotX(v.getMeasuredWidth() * 0.5f);
                        v.setPivotY(v.getMeasuredHeight() * 0.5f);
                        if (!vertical) {
                            v.setTranslationX(0);
                            v.setRotationY(rotation);
                        } else {
                            v.setTranslationY(0);
                            v.setRotationX(rotation);
                        }
                    }
                    if (cl.getVisibility() != VISIBLE) {
                        cl.setVisibility(VISIBLE);
                    }
                    if (mFadeInAdjacentScreens) {
                        float alpha = 1 - Math.abs(scrollProgress);
                        cl.setAlpha(alpha);
                    }
                } else {
                    cl.setVisibility(INVISIBLE);
                }
            } else if (view instanceof CellLayout) {
                CellLayout cl = (CellLayout) view;
                float scrollProgress = getScrollProgress(screenScroll, cl, i);
                float rotation = (reverse ? 180.0f : -180.0f) * scrollProgress;
                if (scrollProgress >= -0.5f && scrollProgress <= 0.5f) {
                    if (!vertical) {
                        cl.setTranslationX(cl.getMeasuredWidth() * scrollProgress);
                    } else {
                        cl.setTranslationY(cl.getMeasuredHeight() * scrollProgress);
                    }
                    ShortcutAndWidgetContainer children = cl.getShortcutsAndWidgets();
                    for (int j = 0; j < children.getChildCount(); j++) {
                        View v = children.getChildAt(j);
                        v.setPivotX(v.getMeasuredWidth() * 0.5f);
                        v.setPivotY(v.getMeasuredHeight() * 0.5f);
                        if (!vertical) {
                            v.setTranslationX(0);
                            v.setRotationY(rotation);
                        } else {
                            v.setTranslationY(0);
                            v.setRotationX(rotation);
                        }
                    }
                    if (cl.getVisibility() != VISIBLE) {
                        cl.setVisibility(VISIBLE);
                    }
                    if (mFadeInAdjacentScreens) {
                        float alpha = 1 - Math.abs(scrollProgress);
                        cl.setAlpha(alpha);
                    }
                } else {
                    cl.setVisibility(INVISIBLE);
                }
                invalidate();
            }
        }
    }

    void resetLayout() {
        for (int i = 0; i < getChildCount(); i++) {
            CellLayout cl = (CellLayout) getPageAt(i);
            cl.removeAllViewsInLayout();
        }
    }

    void moveToDefaultScreen(boolean animate) {
        int page = hasVerticalHotseat() ? (mHotseatPages - mDefaultPage - 1) : mDefaultPage;
        if (animate) {
            snapToPage(page);
        } else {
            setCurrentPage(page);
        }
        getChildAt(page).requestFocus();
    }

    CellLayout getLayout() {
        return (CellLayout) getPageAt(mCurrentPage);
    }

    @Override
    public void syncPages() {
    }

    @Override
    public void syncPageItems(int page, boolean immediate) {
    }

    @Override
    protected void loadAssociatedPages(int page) {
    }
    @Override
    protected void loadAssociatedPages(int page, boolean immediateAndOnly) {
    }
}
