package com.annazou.myviews.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

@SuppressLint("AppCompatCustomView")
public class DetailPhotoView extends ImageView{

    static final float minScale = 1;
    static final float maxScale = 3;

    Bitmap mBitmap;
    GestureDetector mDetector;
    ScaleGestureDetector mScaleDetector;
    Matrix mMatrix;
    float mWidth;
    float mHeight;
    float parentWidth;
    float parentHeight;
    float mFirstScale;
    float mMaxScale;
    float mScaleFactor;
    float lastScale;
    float mFirstTranslationX;
    float mFirstTranslationY;

    float mDownX;
    float mDownY;
    float pointX;
    float pointY;
    float translationX;
    float translationY;
    boolean gestureHandled;
    float mFoucusX;
    float mFoucusY;

    DetailPhotoView mCompareView;
    boolean mComparing;
    float[] mCompareViewSize;
    float mCompareScaleX;
    float mCompareScaleY;

    public DetailPhotoView(Context context, PhotoCallBack callBack) {
        super(context);
        initPhotoView(context, callBack);
    }

    public DetailPhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPhotoView(context, null);
    }

    public DetailPhotoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPhotoView(context, null);
    }

    public DetailPhotoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initPhotoView(context, null);
    }

    private void initPhotoView(Context context, PhotoCallBack callBack) {
        mCallback = callBack;
        setScaleType(ImageView.ScaleType.MATRIX);
        mMatrix = new Matrix();
        mDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (mCallback.onSingleTapUp()) return true;
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (mCallback.onDoubleTapUp()) return true;
                if (mScaleFactor != mMaxScale) {
                    mMatrix.postScale(mMaxScale / mScaleFactor, mMaxScale / mScaleFactor, e.getX(), e.getY());
                    if(mCompareView != null) {
                        mCompareView.setScale(mMaxScale / mScaleFactor, e.getX(), e.getY());
                    }
                    mScaleFactor = mMaxScale;
                    setImageMatrix(mMatrix);
                } else {
                    resetPhoto();
                    if(mCompareView != null) {
                        mCompareView.resetPhoto();
                    }
                }
                return true;
            }
        });

        mScaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                mFoucusX = detector.getFocusX();
                mFoucusY = detector.getFocusY();

                if (mWidth * mScaleFactor <= parentWidth) {
                    mFoucusX = parentWidth / 2;
                }
                if (mHeight * mScaleFactor <= parentHeight) {
                    mFoucusY = parentHeight / 2;
                }

                scaleImage(detector.getScaleFactor(), mFoucusX, mFoucusY);
                return super.onScale(detector);
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                lastScale = mScaleFactor;
                mFoucusX = -1;
                mFoucusY = -1;
                return super.onScaleBegin(detector);
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                mScaleFactor *= detector.getScaleFactor();
                if (mScaleFactor > mMaxScale) mScaleFactor = mMaxScale;
                if (mScaleFactor < mFirstScale) mScaleFactor = mFirstScale;
                mFoucusX = -1;
                mFoucusY = -1;
                super.onScaleEnd(detector);
            }

        });
        setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        mDownX = event.getX();
                        mDownX = event.getY();
                        pointX = mDownX;
                        pointY = mDownY;
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                }
                gestureHandled = false;
                if (mDetector.onTouchEvent(event)) gestureHandled = true;
                if (mScaleDetector.onTouchEvent(event)) gestureHandled = true;

                if (mScaleFactor != mFirstScale && !mScaleDetector.isInProgress() && event.getAction() == MotionEvent.ACTION_MOVE) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    float[] maxTranslation = getMaxTranslation(mScaleFactor);
                    float[] value = new float[9];
                    mMatrix.getValues(value);
                    float lastX = value[Matrix.MTRANS_X];
                    float lastY = value[Matrix.MTRANS_Y];
                    float transX = Math.min(Math.max(maxTranslation[0] - lastX, event.getX() - pointX), -lastX);
                    float transY = Math.min(Math.max(maxTranslation[1] - lastY, event.getY() - pointY), -lastY);
                    if (mWidth * mScaleFactor <= parentWidth)
                        transX = (parentWidth - mWidth * mScaleFactor) / 2 - lastX;
                    if (mHeight * mScaleFactor <= parentHeight)
                        transY = (parentHeight - mHeight * mScaleFactor) / 2 - lastY;
                    mMatrix.postTranslate(transX, transY);
                    if(mCompareView != null) {
                        mCompareView.setTranslation(transX, transY);
                    }
                    gestureHandled = true;
                }
                setImageMatrix(mMatrix);
                pointX = event.getX();
                pointY = event.getY();
                if (event.getActionMasked() == MotionEvent.ACTION_POINTER_UP) {
                    int index = event.getActionIndex() == 0 ? 1 : 0;
                    if (event.getPointerCount() == 2) {
                        pointX = event.getX(index);
                        pointY = event.getY(index);
                    }
                }
                return true;
            }

        });
    }

    public interface PhotoCallBack {
        boolean onSingleTapUp();
        boolean onDoubleTapUp();
    }

    PhotoCallBack mCallback;

    public void setCallback(PhotoCallBack callback) {
        mCallback = callback;
    }

    public void setCompareView(DetailPhotoView view){
        mCompareView = view;
        mComparing = view == null ? false : true;
        mCompareViewSize = mCompareView.getBitmapSize();
        mCompareScaleX = mCompareViewSize[0] / mWidth;
        mCompareScaleY = mCompareViewSize[1] / mHeight;
    }

    public float[] getBitmapSize(){
        float[] size = new float[2];
        size[0] = mWidth;
        size[1] = mHeight;
        return  size;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }


    public void setSize(float width, float height) {
        parentWidth = width;
        parentHeight = height;
    }

    public void setPhotoBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        setPhoto();
    }

    private void setPhoto() {
        if (mBitmap == null) {
            return;
        }
        mWidth = mBitmap.getWidth();
        mHeight = mBitmap.getHeight();
        if (mWidth / mHeight > parentWidth / parentHeight) {
            mFirstScale = parentWidth / mWidth;
        } else {
            mFirstScale = parentHeight / mHeight;
        }

        mFirstTranslationX = (parentWidth - mWidth * mFirstScale) / 2;
        mFirstTranslationY = (parentHeight - mHeight * mFirstScale) / 2;

        mMaxScale = mFirstScale * maxScale;
        setImageBitmap(mBitmap);
        resetPhoto();

    }

    public void removePhoto() {
        setImageBitmap(null);
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
        }
        mBitmap = null;
    }

    public void setTranslation(float x, float y){
        mMatrix.postTranslate(x * mCompareScaleX, y * mCompareScaleY);
        setImageMatrix(mMatrix);
    }

    public void setScale(float scale, float focusX, float focusY){
        mMatrix.postScale(scale, scale, focusX * mCompareScaleX, focusY * mCompareScaleY);
        mScaleFactor *= scale;
        setImageMatrix(mMatrix);
    }

    public void scaleImage(double deltaScale, float focusX, float focusY) {
        float newScale = mScaleFactor * (float) deltaScale;
        float scale = newScale / lastScale;
        if (newScale >= mMaxScale) {
            //scale = maxScale;
            //deltaScale = maxScale / origScale;
            return;
        } else if (newScale <= mFirstScale) {
            //scale = mFirstScale;
            //deltaScale = mFirstScale / origScale;
            return;
        }
        lastScale = newScale;
        mMatrix.postScale(scale, scale, focusX, focusY);
        setImageMatrix(mMatrix);
        if(mCompareView != null) {
            mCompareView.setScale(scale, focusX, focusY);
        }
    }

    private float[] getMaxTranslation(float scale) {
        float[] max = new float[2];
        max[0] = -mWidth * scale + parentWidth;
        max[1] = -mHeight * scale + parentHeight;
        return max;
    }

    public void resetPhoto() {
        mMatrix = new Matrix();
        mScaleFactor = mFirstScale;
        mMatrix.setScale(mFirstScale, mFirstScale);
        translationX = mFirstTranslationX;
        translationY = mFirstTranslationY;
        mMatrix.postTranslate(translationX, translationY);
        setImageMatrix(mMatrix);
    }
}
