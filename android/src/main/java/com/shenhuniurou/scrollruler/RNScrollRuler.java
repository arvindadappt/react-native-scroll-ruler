package com.shenhuniurou.scrollruler;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;

import android.media.MediaPlayer;
import  android.graphics.Path;

/**
 * @author shenhuniurou
 * @email shenhuniurou@gmail.com
 * @date 2018/5/23 02:10
 * @description
 */

public class RNScrollRuler extends View  {
    private static final String TAG = "RulerView";
    /**
     * 2个大刻度之间间距，默认为1
     */
    private int scaleLimit = 10;
    /**
     * 尺子高度
     */
    private int rulerHeight = 80;

    /**
     * 尺子和屏幕顶部以及结果之间的高度
     */
    private int rulerToResultgap = rulerHeight / 4;
    /**
     * 刻度平分多少份
     */
    private int scaleCount = 10;  //刻度评分多少份
    /**
     * 刻度间距
     */
    private int scaleGap = 10;
    /**
     * 刻度最小值
     */
    private int minScale = 0;
    /**
     * 第一次显示的刻度
     */
    private float firstScale = 50;
    /**
     * 刻度最大值
     */
    private int maxScale = 100;

    /**
     * kg颜色
     */
    private String unit = "cm";
    /**
     * kg颜色
     */
    private int unitColor = 0xff333333;

    /**
     * 单位字体大小
     */
    private int unitTextSize = 14;

    /**
     * 背景颜色
     */
    private int bgColor = 0xfffcfffc;
    /**
     * 小刻度的颜色
     */
    private int smallScaleColor = 0xffcccccc;
    /**
     * 中刻度的颜色
     */
    private int midScaleColor = 0xffcccccc;
    /**
     * 大刻度的颜色
     */
    private int largeScaleColor = 0xffff0000;
    /**
     * 刻度颜色
     */
    private int scaleNumColor = 0xffcccccc;
    /**
     * 结果值颜色
     */
    private int resultNumColor = 0xff333333;
    /**
     * 小刻度粗细大小
     */
    private int smallScaleStroke = 1;
    /**
     * 中刻度粗细大小
     */
    private int midScaleStroke = 1;
    /**
     * 大刻度粗细大小
     */
    private int largeScaleStroke = 1;
    /**
     * 结果字体大小
     */
    private int resultNumTextSize = 20;
    /**
     * 刻度字体大小
     */
    private int scaleNumTextSize = 14;
    /**
     * 是否显示刻度结果
     */
    private boolean showScaleResult = true;
    /**
     * 是否背景显示圆角
     */
    private boolean isBgRoundRect = false;

    /**
     * 结果回调
     */
    private OnChooseResulterListener onChooseResulterListener;
    /**
     * 滑动选择刻度
     */
    private float computeScale = -1;
    /**
     * 当前刻度
     */
    public float currentScale = firstScale;

    private boolean isTime = false;

    private ValueAnimator valueAnimator;
    private VelocityTracker velocityTracker = VelocityTracker.obtain();
    private String resultText = String.valueOf(firstScale);
    private Paint bgPaint;
    private Paint horzitalLinePaint;
    private Paint smallScalePaint;
    private Paint midScalePaint;
    private Paint lagScalePaint;
    private Paint scaleNumPaint;
    private Paint resultNumPaint;
    private Paint kgPaint;
    private Rect scaleNumRect;
    private Rect resultNumRect;
    private Rect kgRect;
    private RectF bgRect;
    private int height, width;
    private int smallScaleHeight;
    private int midScaleHeight;
    private int lagScaleHeight;
    private int rulerRight = 0;
    private int resultNumRight;
    private float downX;
    private float moveX = 0;
    private float currentX;
    private float lastMoveX = 0;
    private boolean isUp = false;
    private int leftScroll;
    private int rightScroll;
    private int xVelocity;
    private Rect correctAnswerRect;
    private static Context sContext;
    private Paint resultRectPaint;

    private String  markerTextColor = "#ffffff";
    private String markerColor  = "#ff8d2a";

    MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.ticker);

    public static void setContext(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context cannot be null!");
        }

        // In order to avoid memory leak, you should use application context rather than the `activiy`
        context = context.getApplicationContext();
        if (context == null) {
            throw new IllegalArgumentException("context cannot be null!");
        }

        sContext = context;
    }


    public RNScrollRuler(Context context) {
        this(context, null);
    }

    public RNScrollRuler(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RNScrollRuler(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setAttr(attrs, defStyleAttr);
        init();
    }

    public void setOnChooseResulterListener(OnChooseResulterListener onChooseResulterListener) {
        this.onChooseResulterListener = onChooseResulterListener;
    }

    private void setAttr(AttributeSet attrs, int defStyleAttr) {

        TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.RulerView, defStyleAttr, 0);

        scaleLimit = a.getInt(R.styleable.RulerView_scaleLimit, scaleLimit);

        rulerHeight = a.getDimensionPixelSize(R.styleable.RulerView_rulerHeight, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, rulerHeight, getResources().getDisplayMetrics()));

        rulerToResultgap = a.getDimensionPixelSize(R.styleable.RulerView_rulerToResultgap, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, rulerToResultgap, getResources().getDisplayMetrics()));

        scaleCount = a.getInt(R.styleable.RulerView_scaleCount, scaleCount);

        scaleGap = a.getDimensionPixelSize(R.styleable.RulerView_scaleGap, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, scaleGap, getResources().getDisplayMetrics()));

        minScale = a.getInt(R.styleable.RulerView_minScale, minScale) / scaleLimit;

        firstScale = a.getFloat(R.styleable.RulerView_firstScale, firstScale) / scaleLimit;

        maxScale = a.getInt(R.styleable.RulerView_maxScale, maxScale) / scaleLimit;

        bgColor = a.getColor(R.styleable.RulerView_bgColor, bgColor);

        smallScaleColor = a.getColor(R.styleable.RulerView_smallScaleColor, smallScaleColor);

        midScaleColor = a.getColor(R.styleable.RulerView_midScaleColor, midScaleColor);

        largeScaleColor = a.getColor(R.styleable.RulerView_largeScaleColor, largeScaleColor);

        scaleNumColor = a.getColor(R.styleable.RulerView_scaleNumColor, scaleNumColor);

        resultNumColor = a.getColor(R.styleable.RulerView_resultNumColor, resultNumColor);

        unitColor = a.getColor(R.styleable.RulerView_unitColor, unitColor);

        smallScaleStroke = a.getDimensionPixelSize(R.styleable.RulerView_smallScaleStroke, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, smallScaleStroke, getResources().getDisplayMetrics()));

        midScaleStroke = a.getDimensionPixelSize(R.styleable.RulerView_midScaleStroke, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, midScaleStroke, getResources().getDisplayMetrics()));
        largeScaleStroke = a.getDimensionPixelSize(R.styleable.RulerView_largeScaleStroke, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, largeScaleStroke, getResources().getDisplayMetrics()));
        resultNumTextSize = a.getDimensionPixelSize(R.styleable.RulerView_resultNumTextSize, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, resultNumTextSize, getResources().getDisplayMetrics()));

        scaleNumTextSize = a.getDimensionPixelSize(R.styleable.RulerView_scaleNumTextSize, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, scaleNumTextSize, getResources().getDisplayMetrics()));

        unitTextSize = a.getDimensionPixelSize(R.styleable.RulerView_unitTextSize, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, unitTextSize, getResources().getDisplayMetrics()));


        showScaleResult = a.getBoolean(R.styleable.RulerView_showScaleResult, showScaleResult);
        isBgRoundRect = a.getBoolean(R.styleable.RulerView_isBgRoundRect, isBgRoundRect);
        isTime = a.getBoolean(R.styleable.RulerView_isTime, isTime);

        a.recycle();
    }


    private void init() {
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        horzitalLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        smallScalePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        midScalePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lagScalePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        scaleNumPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        resultNumPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        kgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        bgPaint.setColor(bgColor);
        horzitalLinePaint.setColor(smallScaleColor);
        smallScalePaint.setColor(smallScaleColor);
        midScalePaint.setColor(midScaleColor);
        lagScalePaint.setColor(largeScaleColor);
        scaleNumPaint.setColor(getResources().getColor(R.color.black));
        resultNumPaint.setColor(resultNumColor);
        resultNumPaint.setFakeBoldText(true);

        kgPaint.setColor(unitColor);
        kgPaint.setFakeBoldText(true);

        resultNumPaint.setStyle(Paint.Style.FILL);

        kgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setStyle(Paint.Style.FILL);
        horzitalLinePaint.setStyle(Paint.Style.FILL);
        smallScalePaint.setStyle(Paint.Style.FILL);
        midScalePaint.setStyle(Paint.Style.FILL);
        lagScalePaint.setStyle(Paint.Style.FILL);

//        lagScalePaint.setStrokeCap(Paint.Cap.ROUND);
//        midScalePaint.setStrokeCap(Paint.Cap.ROUND);
//        smallScalePaint.setStrokeCap(Paint.Cap.ROUND);

        smallScalePaint.setStrokeWidth(smallScaleStroke);
        midScalePaint.setStrokeWidth(midScaleStroke);
        lagScalePaint.setStrokeWidth(largeScaleStroke);
        horzitalLinePaint.setStrokeWidth(largeScaleStroke);

        resultNumPaint.setTextSize(resultNumTextSize);

        kgPaint.setTextSize(unitTextSize);
        scaleNumPaint.setTextSize(scaleNumTextSize);

        bgRect = new RectF();
        resultNumRect = new Rect();
        correctAnswerRect = new Rect();

        scaleNumRect = new Rect();
        kgRect = new Rect();

        resultNumPaint.getTextBounds(resultText, 0, resultText.length(), resultNumRect);


        kgPaint.getTextBounds(resultText, 0, 1, kgRect);

        smallScaleHeight = rulerHeight / 4;
        midScaleHeight = rulerHeight / 2;
        lagScaleHeight = rulerHeight / 2 + 5;
        valueAnimator = new ValueAnimator();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int heightModule = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        switch (heightModule) {
            case MeasureSpec.AT_MOST:
                height = rulerHeight + (showScaleResult ? resultNumRect.height() : 0) + rulerToResultgap * 2 + getPaddingTop() + getPaddingBottom();
                break;
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.EXACTLY:
                height = heightSize + getPaddingTop() + getPaddingBottom();
                break;
        }

        width = widthSize + getPaddingLeft() + getPaddingRight();

        setMeasuredDimension(width, height);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawBg(canvas);
        drawScaleAndNum(canvas);
        drawResultText(canvas, resultText);
        //drawCorrectAnswer(canvas ,  String.valueOf((int) 20));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        currentX = event.getX();
        isUp = false;
        velocityTracker.computeCurrentVelocity(500);
        velocityTracker.addMovement(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // this.playSound();
                //按下时如果属性动画还没执行完,就终止,记录下当前按下点的位置
                if (valueAnimator != null && valueAnimator.isRunning()) {
                    valueAnimator.end();
                    valueAnimator.cancel();
                }
                downX = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                //滑动时候,通过假设的滑动距离,做超出左边界以及右边界的限制。
                mp.start();

                moveX = currentX - downX + lastMoveX;
                if (moveX >= width / 2) {
                    moveX = width / 2;
                } else if (moveX <= getWhichScalMovex(maxScale)) {
                    moveX = getWhichScalMovex(maxScale);
                }
                break;
            case MotionEvent.ACTION_UP:
                //手指抬起时候制造惯性滑动
                mp.pause();
                lastMoveX = moveX;
                xVelocity = (int) velocityTracker.getXVelocity();
                autoVelocityScroll(xVelocity);
                velocityTracker.clear();
                break;
        }
        invalidate();
        return true;
    }

    private void autoVelocityScroll(int xVelocity) {
        //惯性滑动的代码,速率和滑动距离,以及滑动时间需要控制的很好,应该网上已经有关于这方面的算法了吧。。这里是经过N次测试调节出来的惯性滑动
        if (Math.abs(xVelocity) < 50) {
            isUp = true;
            return;
        }
        if (valueAnimator.isRunning()) {
            return;
        }
        valueAnimator = ValueAnimator.ofInt(0, xVelocity / 20).setDuration(Math.abs(xVelocity / 10));
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                moveX += (int) animation.getAnimatedValue();
                if (moveX >= width / 2) {
                    moveX = width / 2;
                } else if (moveX <= getWhichScalMovex(maxScale)) {
                    moveX = getWhichScalMovex(maxScale);
                }
                lastMoveX = moveX;
                invalidate();
            }

        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isUp = true;
                invalidate();
            }
        });

        valueAnimator.start();
    }

    private float getWhichScalMovex(float scale) {
        return width / 2 - scaleGap * scaleCount * (scale - minScale);
    }

    private String transformSecondsToMinutes (float timeInSec ){
        double minutes = Math.floor(timeInSec / 60);
        double seconds = timeInSec - minutes * 60;
        int min = (int) minutes;
        int sec = (int) seconds;
        String secStr = (sec < 10) ? ('0' + String.valueOf(sec)) : String.valueOf(sec);
        return  (int)minutes + ":" + secStr;
    };

    private void drawScaleAndNum(Canvas canvas) {
        canvas.translate(0, (showScaleResult ? resultNumRect.height() : 0) + rulerToResultgap);//移动画布到结果值的下面

        // 先画横线

        canvas.drawLine(0, midScaleHeight, width, midScaleHeight, horzitalLinePaint);

        int num1;//确定刻度位置
        float num2;

        if (firstScale != -1) {   //第一次进来的时候计算出默认刻度对应的假设滑动的距离moveX
            moveX = getWhichScalMovex(firstScale);          //如果设置了默认滑动位置，计算出需要滑动的距离
            lastMoveX = moveX;
            firstScale = -1;                                //将结果置为-1，下次不再计算初始位置
        }

        if (computeScale != -1) {//弹性滑动到目标刻度
            lastMoveX = moveX;
            if (valueAnimator != null && !valueAnimator.isRunning()) {
                valueAnimator = ValueAnimator.ofFloat(getWhichScalMovex(currentScale), getWhichScalMovex(computeScale));
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        moveX = (float) animation.getAnimatedValue();
                        lastMoveX = moveX;
                        invalidate();
                    }
                });
                valueAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        //这里是滑动结束时候回调给使用者的结果值
                        computeScale = -1;
                    }
                });
                valueAnimator.setDuration(Math.abs((long) ((getWhichScalMovex(computeScale) - getWhichScalMovex(currentScale)) / 100)));
                valueAnimator.start();
            }
        }

        num1 = -(int) (moveX / scaleGap);                   //滑动刻度的整数部分
        num2 = (moveX % scaleGap);                         //滑动刻度的小数部分

        canvas.save();                                      //保存当前画布

        rulerRight = 0;                                    //准备开始绘制当前屏幕,从最左面开始

        if (isUp) {   //这部分代码主要是计算手指抬起时，惯性滑动结束时，刻度需要停留的位置
            num2 = ((moveX - width / 2 % scaleGap) % scaleGap);     //计算滑动位置距离整点刻度的小数部分距离
            if (num2 <= 0) {
                num2 = scaleGap - Math.abs(num2);
            }
            leftScroll = (int) Math.abs(num2);                        //当前滑动位置距离左边整点刻度的距离
            rightScroll = (int) (scaleGap - Math.abs(num2));         //当前滑动位置距离右边整点刻度的距离

            float moveX2 = num2 <= scaleGap / 2 ? moveX - leftScroll : moveX + rightScroll; //最终计算出当前位置到整点刻度位置需要滑动的距离

            if (valueAnimator != null && !valueAnimator.isRunning()) {      //手指抬起，并且当前没有惯性滑动在进行，创建一个惯性滑动
                valueAnimator = ValueAnimator.ofFloat(moveX, moveX2);
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        moveX = (float) animation.getAnimatedValue();            //不断滑动去更新新的位置
                        lastMoveX = moveX;
                        invalidate();
                    }
                });
                valueAnimator.addListener(new AnimatorListenerAdapter() {       //增加一个监听，用来返回给使用者滑动结束后的最终结果刻度值
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        //这里是滑动结束时候回调给使用者的结果值
                        if (onChooseResulterListener != null) {
                            onChooseResulterListener.onEndResult(resultText);
                        }
                    }
                });
                valueAnimator.setDuration(300);
                valueAnimator.start();
                isUp = false;
            }

            num1 = (int) -(moveX / scaleGap);                //重新计算当前滑动位置的整数以及小数位置
            num2 = (moveX % scaleGap);
        }


        canvas.translate(num2, 0);    //不加该偏移的话，滑动时刻度不会落在0~1之间只会落在整数上面,其实这个都能设置一种模式了，毕竟初衷就是指针不会落在小数上面

        //这里是滑动时候不断回调给使用者的结果值
        currentScale = new WeakReference<>(new BigDecimal(((width / 2 - moveX) / (scaleGap * scaleCount) + minScale) * scaleLimit)).get().setScale(0, BigDecimal.ROUND_HALF_UP).intValue();

        resultText = String.valueOf((int) currentScale);

        if (onChooseResulterListener != null) {
            onChooseResulterListener.onScrollResult(resultText); //接口不断回调给使用者结果值
        }
        //绘制当前屏幕可见刻度,不需要裁剪屏幕,while循环只会执行·屏幕宽度/刻度宽度·次,大部分的绘制都是if(curDis<width)这样子内存暂用相对来说会比较高。。
        while (rulerRight < width) {
            if (num1 % scaleCount == 0) {    //绘制整点刻度以及文字
                if ((moveX >= 0 && rulerRight < moveX - scaleGap) || width / 2 - rulerRight <= getWhichScalMovex(maxScale + 1) - moveX) {
                    //当滑动出范围的话，不绘制，去除左右边界
                } else {
                    //绘制刻度，绘制刻度数字
                    canvas.drawLine(0, 0, 0, midScaleHeight, midScalePaint);
                    if (num1 == 0 && minScale == 0) {
                        scaleNumPaint.getTextBounds("0", 0, "0".length(), scaleNumRect);
                         // checking time paramter and drawing the scale value
                        //canvas.drawText("0", -scaleNumRect.width() / 2, lagScaleHeight + (rulerHeight - lagScaleHeight) / 2 + scaleNumRect.height(), scaleNumPaint);
                        canvas.drawText("0",  -scaleNumRect.width() / 2 , -resultNumRect.height(), scaleNumPaint);
                    } else {
                        int rulerValue = (num1 / scaleCount + minScale) * scaleLimit;
                        scaleNumPaint.getTextBounds(num1 / scaleGap + minScale + "", 0, (num1 / scaleGap + minScale + "").length(), scaleNumRect);

                        // checking time paramter and drawing the scale value
                        if(this.isTime){
                            String newFormatedValue =  this.transformSecondsToMinutes(rulerValue);
                            //canvas.drawText( newFormatedValue, -scaleNumRect.width() / 2, lagScaleHeight + (rulerHeight - lagScaleHeight) / 2 + scaleNumRect.height(), scaleNumPaint);

                            // reposition the scale value
                            canvas.drawText( newFormatedValue, (-scaleNumRect.width() / 2) - 18 , -resultNumRect.height(), scaleNumPaint);
                        }else{
                            // canvas.drawText( (num1 / scaleCount + minScale) * scaleLimit + "", -scaleNumRect.width() / 2, lagScaleHeight + (rulerHeight - lagScaleHeight) / 2 + scaleNumRect.height(), scaleNumPaint);

                            // reposition the scale vlaue
                            canvas.drawText( (num1 / scaleCount + minScale) * scaleLimit + "", (-scaleNumRect.width() / 2) - 18 , -resultNumRect.height(), scaleNumPaint);
                            /**
                             * Code for making color change of current scale

                             Log.i("CREATE FILE", "currentScale "+(int) currentScale + "resultText "+ resultText);

                             String compareResultText  =  String.valueOf((int) currentScale);
                             if( resultText.equals(compareResultText)){
                             scaleNumPaint.setColor(getResources().getColor(R.color.result_text_color));
                             canvas.drawText( (num1 / scaleCount + minScale) * scaleLimit + "", (-scaleNumRect.width() / 2) - 18 , -resultNumRect.height() - rulerToResultgap / 6, scaleNumPaint);
                             }else{
                             canvas.drawText( (num1 / scaleCount + minScale) * scaleLimit + "", (-scaleNumRect.width() / 2) - 18 , -resultNumRect.height() - rulerToResultgap / 6, scaleNumPaint);
                             }
                             */

                        }
                    }
                }

            } else {   //绘制小数刻度
                if ((moveX >= 0 && rulerRight < moveX) || width / 2 - rulerRight < getWhichScalMovex(maxScale) - moveX) {
                    //当滑动出范围的话，不绘制，去除左右边界
                } else {
                    //绘制小数刻度
                    canvas.drawLine(0, midScaleHeight, 0, smallScaleHeight, smallScalePaint);
                }
            }
            ++num1;  //刻度加1
            rulerRight += scaleGap;  //绘制屏幕的距离在原有基础上+1个刻度间距
            canvas.translate(scaleGap, 0); //移动画布到下一个刻度
        }

        canvas.restore();
        //绘制屏幕中间用来选中刻度的最大刻度

        // reposition the bottom line and color
        //lagScalePaint.setColor(getResources().getColor(R.color.result_text_color));
        lagScalePaint.setColor(Color.parseColor(this.markerColor));
        canvas.drawLine(width / 2, resultNumRect.height()- 180, width / 2, midScaleHeight , lagScalePaint);

    }

//    //绘制上面的结果 结果值+单位
//    private void drawResultText(Canvas canvas, String resultText) {
//        if (!showScaleResult) {   //判断用户是否设置需要显示当前刻度值，如果否则取消绘制
//            return;
//        }
//        canvas.translate(0, -resultNumRect.height() - rulerToResultgap / 2);  //移动画布到正确的位置来绘制结果值
//        if (resultText.equals("0")) {
//            resultText = "0";
//            resultNumPaint.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20, getResources().getDisplayMetrics()));
//            resultNumPaint.getTextBounds(resultText, 0, resultText.length(), resultNumRect);
//            canvas.drawText(resultText, width / 2 - resultNumRect.width() / 2, resultNumRect.height()- 100,  resultNumPaint); //绘制当前刻度结果值
//            resultNumPaint.setColor(getResources().getColor(R.color.result_text_color));
//        } else {
//            resultNumPaint.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20, getResources().getDisplayMetrics()));
//            resultNumPaint.getTextBounds(resultText, 0, resultText.length(), resultNumRect);
//            resultNumPaint.setColor(getResources().getColor(R.color.result_text_color));
//            if(this.isTime){
//                String newFormatedValue =  this.transformSecondsToMinutes( Integer.valueOf(resultText) );
//                canvas.drawText(newFormatedValue, width / 2 - resultNumRect.width() / 2, resultNumRect.height() - 100, //绘制当前刻度结果值
//                        resultNumPaint);
//            }else{
//                canvas.drawText(resultText, width / 2 - resultNumRect.width() / 2, resultNumRect.height()- 100,resultNumPaint); //绘制当前刻度结果值
//            }
//            resultNumRight = width / 2 + resultNumRect.width() / 2 + 5;
//
//            canvas.drawText(unit, resultNumRight, kgRect.height() + 8, kgPaint);            //在当前刻度结果值的又面10px的位置绘制单位
//        }
//    }

    public void drawTriangle(Canvas canvas, Paint paint, int x, int y, int width) {
        int halfWidth = width / 2;
        Matrix matrix = new Matrix();

        Path path = new Path();
        path.moveTo(x, y - halfWidth); // Top
        path.lineTo(x - halfWidth, y + halfWidth); // Bottom left
        path.lineTo(x + halfWidth, y + halfWidth); // Bottom right
        path.lineTo(x, y - halfWidth); // Back to Top

        Path oval = new Path();
        matrix.postRotate(60, x+halfWidth, y);
        path.transform(matrix, path);


        canvas.drawPath(path, paint);
    }

    private void drawResultText(Canvas canvas, String resultText) {
        if (!showScaleResult) {   //判断用户是否设置需要显示当前刻度值，如果否则取消绘制
            return;
        }
        canvas.translate(0, -resultNumRect.height() - rulerToResultgap / 2);  //移动画布到正确的位置来绘制结果值

        //CHANGES
        resultRectPaint = new Paint();
        resultRectPaint.setStyle(Paint.Style.FILL);
       // resultRectPaint.setColor(getResources().getColor(R.color.result_text_color));
        resultRectPaint.setColor(Color.parseColor(this.markerColor));

        resultRectPaint.setStrokeWidth(4);
        Rect rectangle = new Rect(width / 2 + 160, -180, width / 2 - 150, resultNumRect.height()- 100 );
        canvas.drawRect(rectangle, resultRectPaint);

        Paint paint = new Paint();
        //paint.setColor(getResources().getColor(R.color.result_text_color));
        paint.setColor(Color.parseColor(this.markerColor));

        drawTriangle(canvas, paint, width / 2 - 10,  resultNumRect.height()- 100, 150);
        if (resultText.equals("0")) {
            resultText = "0";
            resultNumPaint.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20, getResources().getDisplayMetrics()));
            resultNumPaint.getTextBounds(resultText, 0, resultText.length(), resultNumRect);
            //resultNumPaint.setColor(getResources().getColor(R.color.white));
            resultNumPaint.setColor(Color.parseColor(this.markerTextColor));
           // canvas.drawText(resultText, width / 2 - resultNumRect.width() / 2, resultNumRect.height(), resultNumPaint);
            canvas.drawText(resultText, width / 2  - resultNumRect.width() / 2 - 2, resultNumRect.height() - 145, resultNumPaint);

        } else {
            resultNumPaint.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20, getResources().getDisplayMetrics()));
            resultNumPaint.getTextBounds(resultText, 0, resultText.length(), resultNumRect);
           // resultNumPaint.setColor(getResources().getColor(R.color.white));
            resultNumPaint.setColor(Color.parseColor(this.markerTextColor));

            // canvas.drawText(resultText, width / 2  - resultNumRect.width() / 2 - 2, resultNumRect.height() - 120, resultNumPaint);
            if(this.isTime){
                String newFormatedValue =  this.transformSecondsToMinutes( Integer.valueOf(resultText) );
                canvas.drawText(newFormatedValue, width / 2  - resultNumRect.width() / 2 - 2, resultNumRect.height() - 145, resultNumPaint);
            }else{
                canvas.drawText(resultText, width / 2  - resultNumRect.width() / 2 - 2, resultNumRect.height() - 145, resultNumPaint);
            }

            kgPaint.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, unitTextSize, getResources().getDisplayMetrics()));
            kgPaint.setColor(unitColor);
            resultNumRight = width / 2 - unitTextSize;
            canvas.drawText(unit, resultNumRight, -10, kgPaint);            //在当前刻度结果值的又面10px的位置绘制单位
        }
    }



    private void drawBg(Canvas canvas) {
        bgRect.set(0, 0, width, height);
        //bgPaint.setColor(getResources().getColor(R.color.yellow));
        if (isBgRoundRect) {
            canvas.drawRoundRect(bgRect, 50, 50, bgPaint); //20->椭圆的用于圆形角x-radius
        } else {
            canvas.drawRect(bgRect, bgPaint);
        }
    }

    public void computeScrollTo(float scale) {
        scale = scale / scaleLimit;
        if (scale < minScale || scale > maxScale) {
            return;
        }

        computeScale = scale;
        invalidate();

    }

    public interface OnChooseResulterListener {
        void onEndResult(String result);

        void onScrollResult(String result);
    }

    public void setRulerHeight(int rulerHeight) {
        this.rulerHeight = rulerHeight;
        invalidate();
    }

    public void setRulerToResultgap(int rulerToResultgap) {
        this.rulerToResultgap = rulerToResultgap;
        invalidate();
    }

    public void setScaleCount(int scaleCount) {
        this.scaleCount = scaleCount;
        invalidate();
    }

    public void setScaleGap(int scaleGap) {
        this.scaleGap = scaleGap;
        invalidate();
    }

    public void setMinScale(int minScale) {
        this.minScale = minScale / scaleLimit;
        invalidate();
    }

    public void setFirstScale(float firstScale) {
        this.firstScale = firstScale / scaleLimit;
        invalidate();
    }

    public void setMaxScale(int maxScale) {
        this.maxScale = maxScale / scaleLimit;
        invalidate();
    }

    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
        invalidate();
    }

    public void setSmallScaleColor(int smallScaleColor) {
        this.smallScaleColor = smallScaleColor;
        invalidate();
    }

    public void setMidScaleColor(int midScaleColor) {
        this.midScaleColor = midScaleColor;
        invalidate();
    }

    public void setLargeScaleColor(int largeScaleColor) {
        this.largeScaleColor = largeScaleColor;
    }

    public void setScaleNumColor(int scaleNumColor) {
        this.scaleNumColor = scaleNumColor;
        invalidate();
    }

    public void setResultNumColor(int resultNumColor) {
        this.resultNumColor = resultNumColor;
        invalidate();
    }

    public void setSmallScaleStroke(int smallScaleStroke) {
        this.smallScaleStroke = smallScaleStroke;
        invalidate();
    }

    public void setMidScaleStroke(int midScaleStroke) {
        this.midScaleStroke = midScaleStroke;
        invalidate();
    }

    public void setLargeScaleStroke(int largeScaleStroke) {
        this.largeScaleStroke = largeScaleStroke;
        invalidate();
    }

    public void setResultNumTextSize(int resultNumTextSize) {
        this.resultNumTextSize = resultNumTextSize;
        invalidate();
    }

    public void setScaleNumTextSize(int scaleNumTextSize) {
        this.scaleNumTextSize = scaleNumTextSize;
        invalidate();
    }

    public void setShowScaleResult(boolean showScaleResult) {
        this.showScaleResult = showScaleResult;
        invalidate();
    }

    public void setIsBgRoundRect(boolean bgRoundRect) {
        isBgRoundRect = bgRoundRect;
        invalidate();
    }

    public void setScaleLimit(int scaleLimit) {
        this.scaleLimit = scaleLimit;
        invalidate();
    }

    public void setUnit(String unit) {
        this.unit = unit;
        invalidate();
    }

    public void setUnitColor(int unitColor) {
        this.unitColor = unitColor;
        invalidate();
    }

    public void checkIsTime(boolean isTime) {
        this.isTime = isTime;
        invalidate();
    }

    public void setSelectedTextColor(String markerTextColor) {
        this.markerTextColor = markerTextColor;
        invalidate();
    }

    public void setMarkerColor(String markerColor) {
        this.markerColor = markerColor;
        invalidate();
    }
}
