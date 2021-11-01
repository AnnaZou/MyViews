package com.annazou.myviews.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.viewpager.widget.ViewPager;

public class PageIndicator extends View implements ViewPager.OnPageChangeListener{

    private static final int DEF_MAX_DOT_COUNT = 7;
    private static final int DEF_DOT_COLOR = 0x000000;
    private static final int DEF_BACKGROUND_DOT_COLOR = 0x666666;
    private static final int DEF_DOT_SIZE = 36;

    private Context mContext;
    private Paint mBackgroundPaint;
    private Paint mFadingPaint;
    private Paint mMovePaint;
    private int mDotSize;
    private int mSpace;
    private int mDotColor;
    private int mBackgroundDotColor;

    private ViewPager mPager;
    private float mScrollX;
    private int mPosition;
    private float mOffset;
    private int mMiddleIndex;
    private int mMaxDotCount;

    public PageIndicator(Context context) {
        super(context);
        mContext = context;
    }

    public PageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setAntiAlias(true);
        mFadingPaint = new Paint();
        mFadingPaint.setAntiAlias(true);
        mMovePaint = new Paint();
        mMovePaint.setAntiAlias(true);
        mDotSize = DEF_DOT_SIZE;
        mSpace = DEF_DOT_SIZE;
        setMaxDotCount(DEF_MAX_DOT_COUNT);

        int[] attrs = new int[]{android.R.attr.colorAccent, android.R.attr.colorBackground};
        TypedArray ta = mContext.obtainStyledAttributes(attrs);
        mDotColor = ta.getColor(0, DEF_DOT_COLOR);
        mBackgroundDotColor = ta.getColor(1,DEF_BACKGROUND_DOT_COLOR);
        applyColor();
    }

    public void setBackgroundDotColor(int color){
        mBackgroundDotColor = color;
        applyColor();
    }

    public void setDotColor(int color){
        mDotColor = color;
        applyColor();
    }

    public void setMaxDotCount(int count){
        mMaxDotCount = count;
        mMiddleIndex = mMaxDotCount / 2;
    }

    private void applyColor(){
        mMovePaint.setColor(mDotColor);
        mBackgroundPaint.setColor(mBackgroundDotColor);
        mFadingPaint.setColor(mBackgroundDotColor);
    }

    public void setDotSize(int size){
        mDotSize = size;
    }

    public void setSpace(int size){
        mSpace = size;
    }

    public void setPager(ViewPager pager){
        mPager = pager;
        mPager.addOnPageChangeListener(this);
    }

    public void setPagerScrollX(int position, float offset){
        mPosition = position;
        mOffset = offset;
        invalidate();
    }

    private int getPageCount(){
        return (mPager == null) || (mPager.getAdapter() == null) ? 0 : mPager.getAdapter().getCount();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension((mDotSize + mSpace) * (Math.min(getPageCount(), mMaxDotCount) + 2) - mSpace, mDotSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mPager.getChildCount() > 0) {
            int count = getPageCount();
            if(count > mMaxDotCount){
                if(mPosition >= mMiddleIndex && mPosition < count - mMiddleIndex - mMaxDotCount % 2){
                    mScrollX = (mDotSize + mSpace) * (mMiddleIndex + 1);
                    float transition = (mDotSize + mSpace) * mOffset;
                    for (int i = 1; i <= mMaxDotCount + 1; i++) {
                        Paint paint = null;
                        if(i == 1){
                            paint = mFadingPaint;
                            paint.setAlpha((int) (255 * (1 - mOffset)));
                        } else if(i == mMaxDotCount + 1) {
                            paint = mFadingPaint;
                            paint.setAlpha((int) (255 * mOffset));
                        } else {
                            paint = mBackgroundPaint;
                        }
                        canvas.drawOval((mDotSize + mSpace) * i - transition, 0, (mDotSize + mSpace) * i + mDotSize - transition, mDotSize, paint);
                    }

                } else {
                    for (int i = 1; i <= mMaxDotCount; i++) {
                        canvas.drawOval((mDotSize + mSpace) * i, 0, (mDotSize + mSpace) * i + mDotSize, mDotSize, mBackgroundPaint);
                    }
                    if(mPosition < mMiddleIndex){
                        mScrollX = (mDotSize + mSpace) * (mPosition + mOffset + 1);

                    } else {
                        mScrollX = (mDotSize + mSpace) * (mMaxDotCount - getPageCount() + mPosition + mOffset + 1);
                    }
                }
            } else {
                mScrollX = (mDotSize + mSpace) * (mPosition + mOffset + 1);
                for (int i = 1; i <= count; i++) {
                    canvas.drawOval((mDotSize + mSpace) * i, 0, (mDotSize + mSpace) * i + mDotSize, mDotSize, mBackgroundPaint);
                }
            }
            canvas.drawOval(mScrollX, 0, mScrollX + mDotSize, mDotSize, mMovePaint);
            canvas.save();
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        setPagerScrollX(position, positionOffset);

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
