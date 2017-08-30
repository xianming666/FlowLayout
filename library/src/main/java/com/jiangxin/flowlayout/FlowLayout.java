package com.jiangxin.flowlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by LXM on 2017/8/10.
 */

public class FlowLayout extends ViewGroup {
    public static final int CHILDREN_LAYOUT_MODE_ALIGN = 1;
    public static final int CHILDREN_LAYOUT_MODE_DEFAULT = 0;
    private int childHorizontalMargin = 5;
    private int childVerticalMargin = 5;
    private int childrenLayoutMode = CHILDREN_LAYOUT_MODE_DEFAULT;

    private SparseArray<Row> rows = new SparseArray<>();

    public FlowLayout(Context context) {
        this(context, null);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (getChildCount() <= 0) {
            setMeasuredDimension(0, 0);
            return;
        }
        int measuredWidth = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int measuredHeight = 0;
        int usedWidth = getPaddingLeft() + getPaddingRight();
        int maxChildHeight = 0;//一行中子View最高的Height
        int row = 0, column = 0;//子View的位置，行，列
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            if (usedWidth == getPaddingLeft() + getPaddingRight()) {//该行还没添加子View
                usedWidth += childWidth;
                maxChildHeight = childHeight;
            } else if (usedWidth + childHorizontalMargin + childWidth <= measuredWidth) {
                usedWidth += childHorizontalMargin + childWidth;
                if (childHeight > maxChildHeight) {
                    maxChildHeight = childHeight;
                }
                column++;
            } else {//该行放不下了，另起一行
                if (row == 0) {
                    measuredHeight += maxChildHeight;
                } else {
                    measuredHeight += childVerticalMargin + maxChildHeight;
                }
                rows.put(row, new Row(row, column + 1, maxChildHeight));
                maxChildHeight = childHeight;
                usedWidth = getPaddingLeft() + getPaddingRight() + childWidth;
                row++;
                column = 0;
            }
            child.setTag(R.string.flowlayout_child_position_row, row);
            child.setTag(R.string.flowlayout_child_position_column, column);
        }
        if (row == 0) {
            measuredHeight += maxChildHeight;
        } else {
            measuredHeight += childVerticalMargin + maxChildHeight;
        }
        rows.put(row, new Row(row, column + 1, maxChildHeight));
        setMeasuredDimension(measuredWidth, measuredHeight + getPaddingTop() + getPaddingBottom());
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        layoutChildrenAccordingToMode(l, r);
    }

    private void layoutChildrenAccordingToMode(int l, int r) {
        int usedWidth = getPaddingLeft();
        int usedHeight = getPaddingTop();
        int alignHorizontalSpacing = childHorizontalMargin;
        for (int i = 0; i < this.getChildCount(); i++) {
            View child = this.getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }

            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            int row = (int) child.getTag(R.string.flowlayout_child_position_row);
            int column = (int) child.getTag(R.string.flowlayout_child_position_column);
            int left;
            int top;
            if (column == 0) {
                alignHorizontalSpacing = changeHorizontalSpacing(row, r - l, i);
                usedWidth = getPaddingLeft();
                left = usedWidth;
                if (row > 0) {
                    usedHeight += rows.get(row - 1).rowHeight + childVerticalMargin;
                }
            } else {
                left = alignHorizontalSpacing + usedWidth;
            }
            top = usedHeight;
            int right = left + childWidth;
            usedWidth = right;
            int bottom = top + childHeight;
            child.layout(left, top, right, bottom);

        }
    }

    private int changeHorizontalSpacing(int row, int rowWidth, int currentChildIndex) {
        int alignHorizontalSpacing = childHorizontalMargin;
        if (childrenLayoutMode == CHILDREN_LAYOUT_MODE_DEFAULT) {
            return alignHorizontalSpacing;
        } else {
            int needWidth = 0;
            for (int i = currentChildIndex; i < currentChildIndex + rows.get(row).childCount; i++) {
                needWidth += getChildAt(i).getMeasuredWidth();
            }
            int childCount = rows.get(row).childCount;
            if (childCount > 1) {
                alignHorizontalSpacing = (rowWidth - needWidth - getPaddingRight() - getPaddingLeft()) / (childCount - 1);
            }
            return alignHorizontalSpacing;
        }
    }

    public void setChildHorizontalMargin(int childHorizontalMargin) {
        this.childHorizontalMargin = childHorizontalMargin;
    }

    public void setChildVerticalMargin(int childVerticalMargin) {
        this.childVerticalMargin = childVerticalMargin;
    }

    public void setChildrenLayoutMode(int childrenLayoutMode) {
        this.childrenLayoutMode = childrenLayoutMode;
    }

    public int getChildHorizontalMargin() {
        return childHorizontalMargin;
    }

    public int getChildVerticalMargin() {
        return childVerticalMargin;
    }

    public int getChildrenLayoutMode() {
        return childrenLayoutMode;
    }

    private class Row {
        int row;
        int childCount;
        int rowHeight;

        public Row(int row, int childCount, int rowHeight) {
            this.row = row;
            this.childCount = childCount;
            this.rowHeight = rowHeight;
        }
    }
}
