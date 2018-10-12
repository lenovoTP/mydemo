package com.tzutalin.dlibtest;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.Trace;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import junit.framework.Assert;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.example.xjl.mydemo.R;
import com.example.xjl.mydemo.tools.AnimationsContainer;
import com.tzutalin.dlib.Constants;
import com.tzutalin.dlib.FaceDet;
import com.tzutalin.dlib.VisionDetRet;


public class OnGetImageListener implements ImageReader.OnImageAvailableListener{
    private static final boolean SAVE_PREVIEW_BITMAP = false;

    //324, 648, 972, 1296, 224, 448, 672, 976, 1344
    private static final int INPUT_SIZE = 976;
    private static final String TAG = "OnGetImageListener";

    private int mScreenRotation = 90;

    //相关变量
    /**
     private List<VisionDetRet> reults  探测结果
     private int  mPreviewWdith         悬浮窗宽度
     private int mPreviewHeight         悬浮窗高度
     private byte[][] mYUVBytes         相机原始预览图
     private int[] mRGBBytes            dlib预处理图源
     private Bitmap mRGBframeBitmap     原始图片
     private Bitmap mCroppedBitmap      重绘后的图片
     private Bitmap mResizedBitmap      调整尺寸后的bitmap图片
     private Bitmap mInversedBipmap     bitmap1的转置bitmap
     */
    private List<VisionDetRet> results;
    private int mPreviewWdith = 0;
    private int mPreviewHeight = 0;
    private byte[][] mYUVBytes;
    private int[] mRGBBytes = null;
    private Bitmap mRGBframeBitmap = null;
    private Bitmap mCroppedBitmap = null;
    private Bitmap mResizedBitmap = null;
    private Bitmap mInversedBipmap = null;
    /*
    private boolean mIsComputing        是否经过转换
    private Handler mInferenceHandler;  后台处理handler
    private Context mContext;           activity的context
    private FaceDet mFaceDet;           dlib的面部识别
    private TrasparentTitleView mTransparentTitleView;  上面可以变得titleview 用作显示处理时间
    private ImageView mImageView;           播放动画的imageview
    private FloatingCameraWindow mWindow;   悬浮窗本体
    private Paint mFaceLandmardkPaint;      java绘制类
     private  AnimationsContainer.FramesSequenceAnimation animation; 帧动画容器
    private int mframeNum                   你猜
    */
    private boolean mIsComputing = false;
    private Handler mInferenceHandler;

    private Context mContext;
    private FaceDet mFaceDet;
    private TrasparentTitleView mTransparentTitleView;
    private ImageView mImageView;
    private FloatingCameraWindow mWindow;
    private Paint mFaceLandmardkPaint;
    private int emotional;
    private  AnimationsContainer.FramesSequenceAnimation animation;

    private int mframeNum = 0;

    //这个是listener的初始化
    public void initialize(
            final Context context,
            final AssetManager assetManager,
            final TrasparentTitleView scoreView,
            final Handler handler) {
        this.mContext = context;
        this.mTransparentTitleView = scoreView;
        this.mInferenceHandler = handler;
        mFaceDet = new FaceDet(Constants.getFaceShapeModelPath());
        mWindow = new FloatingCameraWindow(mContext);

        mFaceLandmardkPaint = new Paint();
        mFaceLandmardkPaint.setColor(Color.GREEN);
        mFaceLandmardkPaint.setStrokeWidth(2);
        mFaceLandmardkPaint.setStyle(Paint.Style.STROKE);
    }

    //集成界面用的初始化函数，少了原始的TransparentTitleView
    public void initialize(final Context context,
                           final AssetManager assetManager,
                           final ImageView imageView,
                           final Handler handler){
        this.mContext = context;
        this.mInferenceHandler = handler;
        this.mImageView = imageView;
        mFaceDet = new FaceDet(Constants.getFaceShapeModelPath());
        mWindow = new FloatingCameraWindow(mContext);

        mFaceLandmardkPaint = new Paint();
        mFaceLandmardkPaint.setColor(Color.GREEN);
        mFaceLandmardkPaint.setStrokeWidth(2);
        mFaceLandmardkPaint.setStyle(Paint.Style.STROKE);
    }

    //用完之后释放，省的出现leak和bufferqueue abandon报错（其实已经出现了）
    public void deInitialize() {
        synchronized (OnGetImageListener.this) {
            if (mFaceDet != null) {
                mFaceDet.release();
            }

            if (mWindow != null) {
                mWindow.release();
            }
        }
    }

    //src：原始bitmap，dst目标bitmap
    private void drawResizedBitmap(final Bitmap src, final Bitmap dst) {
        //获得屏幕长宽
        Display getOrient = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int orientation = Configuration.ORIENTATION_UNDEFINED;
        Point point = new Point();
        Log.d("point", "point x is"+point.x+",point y is "+point.y);
        getOrient.getSize(point);
        int screen_width = point.x;
        int screen_height = point.y;
        Log.d(TAG, String.format("screen size (%d,%d)", screen_width, screen_height));
        //旋转横竖屏
        if (screen_width < screen_height) {
            orientation = Configuration.ORIENTATION_PORTRAIT;
            mScreenRotation = -90;
        } else {
            orientation = Configuration.ORIENTATION_LANDSCAPE;
            mScreenRotation = 0;
        }

        Assert.assertEquals(dst.getWidth(), dst.getHeight());
        final float minDim = Math.min(src.getWidth(), src.getHeight());

        final Matrix matrix = new Matrix();

        // We only want the center square out of the original rectangle.
        final float translateX = -Math.max(0, (src.getWidth() - minDim) / 2);
        final float translateY = -Math.max(0, (src.getHeight() - minDim) / 2);
        matrix.preTranslate(translateX, translateY);

        final float scaleFactor = dst.getHeight() / minDim;
        matrix.postScale(scaleFactor, scaleFactor);

        // Rotate around the center if necessary.
        if (mScreenRotation != 0) {
            matrix.postTranslate(-dst.getWidth() / 2.0f, -dst.getHeight() / 2.0f);
            matrix.postRotate(mScreenRotation);
            matrix.postTranslate(dst.getWidth() / 2.0f, dst.getHeight() / 2.0f);
        }

        final Canvas canvas = new Canvas(dst);
        canvas.drawBitmap(src, matrix, null);
    }

    public Bitmap imageSideInversion(Bitmap src){
        Matrix sideInversion = new Matrix();
        sideInversion.setScale(-1, 1);
        Bitmap inversedImage = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), sideInversion, false);
        return inversedImage;
    }

    @Override
    public void onImageAvailable(final ImageReader reader) {
        Image image = null;
        try {
            image = reader.acquireLatestImage();

            if (image == null) {
                return;
            }

            // No mutex needed as this method is not reentrant.
            if (mIsComputing) {
                image.close();
                return;
            }
            mIsComputing = true;

            Trace.beginSection("imageAvailable");

            final Image.Plane[] planes = image.getPlanes();

            // Initialize the storage bitmaps once when the resolution is known.
            if (mPreviewWdith != image.getWidth() || mPreviewHeight != image.getHeight()) {
                mPreviewWdith = image.getWidth();
                mPreviewHeight = image.getHeight();

                //Log.d(TAG, String.format("Initializing at size %dx%d", mPreviewWdith, mPreviewHeight));
                mRGBBytes = new int[mPreviewWdith * mPreviewHeight];
                mRGBframeBitmap = Bitmap.createBitmap(mPreviewWdith, mPreviewHeight, Bitmap.Config.ARGB_8888);
                mCroppedBitmap = Bitmap.createBitmap(INPUT_SIZE, INPUT_SIZE, Bitmap.Config.ARGB_8888);

                mYUVBytes = new byte[planes.length][];
                for (int i = 0; i < planes.length; ++i) {
                    mYUVBytes[i] = new byte[planes[i].getBuffer().capacity()];
                }
            }

            for (int i = 0; i < planes.length; ++i) {
                planes[i].getBuffer().get(mYUVBytes[i]);
            }

            final int yRowStride = planes[0].getRowStride();
            final int uvRowStride = planes[1].getRowStride();
            final int uvPixelStride = planes[1].getPixelStride();
            ImageUtils.convertYUV420ToARGB8888(
                    mYUVBytes[0],
                    mYUVBytes[1],
                    mYUVBytes[2],
                    mRGBBytes,
                    mPreviewWdith,
                    mPreviewHeight,
                    yRowStride,
                    uvRowStride,
                    uvPixelStride,
                    false);
            image.close();
        } catch (final Exception e) {
            if (image != null) {
                image.close();
            }
            //Log.e(TAG, "Exception!", e);
            Trace.endSection();
            return;
        }

        mRGBframeBitmap.setPixels(mRGBBytes, 0, mPreviewWdith, 0, 0, mPreviewWdith, mPreviewHeight);
        drawResizedBitmap(mRGBframeBitmap, mCroppedBitmap);

        mInversedBipmap = imageSideInversion(mCroppedBitmap);
        mResizedBitmap = Bitmap.createScaledBitmap(mInversedBipmap, (int)(INPUT_SIZE/4.5), (int)(INPUT_SIZE/4.5), true);

        //dlib检测人脸68个点
        mInferenceHandler.post(
                new Runnable() {
                    @Override
                    public void run() {

                        if (!new File(Constants.getFaceShapeModelPath()).exists()) {
                            //mTransparentTitleView.setText("Copying landmark model to " + Constants.getFaceShapeModelPath());
                            Toast.makeText(mContext,"Copying landmark model to " + Constants.getFaceShapeModelPath(),Toast.LENGTH_SHORT).show();
                            FileUtils.copyFileFromRawToOthers(mContext, R.raw.shape_predictor_68_face_landmarks, Constants.getFaceShapeModelPath());
                        }
                        //人脸68个点识别
                        if(mframeNum % 3 == 0){
                            long startTime = System.currentTimeMillis();
                            synchronized (OnGetImageListener.this) {
                                results = mFaceDet.detect(mResizedBitmap);
                                //TODO：向服务器发送坐标并接受结果
                            }
                            //TODO：判断结果并播放动画
                            switch (emotional){
                                default:
                                    break;
                                case 1://smile
                                    animation = AnimationsContainer.getInstance(R.array.loading_anim_smile, 100).createProgressDialogAnim(mImageView);
                                    break;
                                case 2://c
                                    animation = AnimationsContainer.getInstance(R.array.loading_anim, 100).createProgressDialogAnim(mImageView);
                                    break;
                                case 3://angry
                                    break;
                                case 4://laugh
                                    break;
                                case 5://shy
                                    break;
                            }
                            animation.start();
                            //long endTime = System.currentTimeMillis();
                            //mTransparentTitleView.setText("Time cost: " + String.valueOf((endTime - startTime) / 1000f) + " sec");
                        }

                        // Draw on bitmap
                        if (results.size() != 0) {
                            for (final VisionDetRet ret : results) {
                                float resizeRatio = 4.5f;
                                Canvas canvas = new Canvas(mInversedBipmap);

                                // Draw landmark
                                ArrayList<Point> landmarks = ret.getFaceLandmarks();
                                for (Point point : landmarks) {
                                    int pointX = (int) (point.x * resizeRatio);
                                    int pointY = (int) (point.y * resizeRatio);
                                    canvas.drawCircle(pointX, pointY, 4, mFaceLandmardkPaint);
                                }
                            }
                        }

                        mframeNum++;
                        mWindow.setRGBBitmap(mInversedBipmap);
                        mIsComputing = false;
                    }

                });

        Trace.endSection();
    }
}
