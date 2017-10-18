package com.tape_hencoder;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.OverScroller;

/**
 * Created by zhaozhibo on 2017/10/16.
 */

public class TapeView extends View {
    private int gapWidth;
    private int width;
    private int height;
    private int tapeHeight;
    /**
     * 刻度值宽度
     */
    private int tapeWidth;
    /**
     * 这个是指示器距离左边那个刻度的距离（0,tapeWidth）
     * 绘制的时候要注意这个数值
     */
    private int gapOffset;
    private int numberMarginTop;
    private int numberMarginBottom;
    private int numberHeight;
    private int numberSize;
    private int indicatorWidth;
    private int indicatorHeight;


    private int currentValue;
    private int initValue;
    private int maxValue;
    private int minValue;

    private int tapeColor;
    private int indicatorColor;
    private int numberColor;

    private Paint tapePaint;
    private Paint textPaint;
    private Paint indicatorPaint;

    private Rect textRect;


    private VelocityTracker velocityTracker;
    private OverScroller scroller;
    private float maxVelocity;
    private float minVelocity;


    private float lastX;
    private float moveX;
    /**
     * 防止move是触发computeScroll中的else
     */
    private boolean isCanScroll;
    /**
     * 防止循环调用
     */
    private boolean isCanJustScroll;

    public TapeView(Context context) {
        this(context, null);
    }

    public TapeView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TapeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TapeView, 0, 0);
        try {
            initValue = a.getInteger(R.styleable.TapeView_init_value, 50);
            gapWidth = a.getDimensionPixelOffset(R.styleable.TapeView_gap_width, 35);
            numberMarginTop = a.getDimensionPixelOffset(R.styleable.TapeView_number_margin_top, 80);
            tapeHeight = a.getDimensionPixelOffset(R.styleable.TapeView_tape_height, 60);
            tapeColor = a.getColor(R.styleable.TapeView_tape_color, Color.parseColor("#E6E8E5"));
            indicatorColor = a.getColor(R.styleable.TapeView_indicator_color, Color.parseColor("#6DB67C"));
            numberColor = a.getColor(R.styleable.TapeView_number_color, Color.parseColor("#555855"));
            numberSize = a.getDimensionPixelOffset(R.styleable.TapeView_number_size, 48);
            indicatorWidth = a.getDimensionPixelOffset(R.styleable.TapeView_indicator_width, 10);
            indicatorHeight = a.getDimensionPixelOffset(R.styleable.TapeView_indicator_height, 130);
            tapeWidth = a.getDimensionPixelOffset(R.styleable.TapeView_tape_width, 5);
            minValue = a.getInteger(R.styleable.TapeView_min_value, -100);
            maxValue = a.getInteger(R.styleable.TapeView_max_value, 100);
            numberMarginBottom = a.getDimensionPixelOffset(R.styleable.TapeView_number_margin_bottom, 80);
        } finally {
            a.recycle();
        }
        init();
    }

    private void init() {


        initPaint();
        currentValue = initValue;


        textRect = new Rect();

        ViewConfiguration vc = ViewConfiguration.get(getContext());
        maxVelocity = vc.getScaledMaximumFlingVelocity();
        minVelocity = vc.getScaledMinimumFlingVelocity();

        scroller = new OverScroller(getContext());

        //直接获取一下数字高度
        textPaint.getTextBounds("1", 0, 1, textRect);
        numberHeight = textRect.height();
    }

    private void initPaint() {
        tapePaint = new Paint();
        tapePaint.setColor(tapeColor);
        tapePaint.setAntiAlias(true);
        tapePaint.setStyle(Paint.Style.FILL);
        tapePaint.setStrokeWidth(tapeWidth);


        textPaint = new TextPaint();
        textPaint.setTextSize(numberSize);
        textPaint.setAntiAlias(true);
        textPaint.setColor(numberColor);

        indicatorPaint = new Paint();
        indicatorPaint.setColor(indicatorColor);
        indicatorPaint.setStrokeWidth(indicatorWidth);
        indicatorPaint.setAntiAlias(true);
        indicatorPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        int heightModel = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (heightModel == MeasureSpec.AT_MOST) {
            height = tapeHeight * 2 + numberHeight + numberMarginTop + numberMarginBottom;
        } else {
            height = heightSize;
        }
        setMeasuredDimension(width, height);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (gapOffset < 0 || gapOffset > gapWidth) {
            throw new IllegalStateException("gapOffset is illegalState");
        }
        drawLeft(canvas);
        drawRight(canvas);
        drawIndicator(canvas);

    }

    private void drawIndicator(Canvas canvas) {
        canvas.drawLine(width / 2, 0, width / 2, indicatorHeight, indicatorPaint);
    }

    private void drawLeft(Canvas canvas) {
        int drawLength = gapOffset;
        int currentValue = this.currentValue;
        int totalWidth = width / 2;
        //多加一个gapWidth 防止数字消失太突然
        while (drawLength < width / 2 + gapWidth) {
            if (currentValue<minValue)
                break;
            if (currentValue % 10 == 0) {
                canvas.drawLine(totalWidth - drawLength, 0, totalWidth - drawLength, tapeHeight * 2, tapePaint);
                drawText(canvas, currentValue, totalWidth - drawLength);
            } else {
                canvas.drawLine(totalWidth - drawLength, 0, totalWidth - drawLength, tapeHeight, tapePaint);
            }
            drawLength += gapWidth;
            currentValue--;
        }

    }

    private void drawRight(Canvas canvas) {
        int drawLength = gapWidth - gapOffset;
        int currentValue = this.currentValue + 1;
        int totalWidth = width / 2;
        //多加一个gapWidth 防止数字消失太突然
        while (drawLength < width / 2 + gapWidth) {
            if (currentValue>maxValue)
                break;
            if (currentValue % 10 == 0) {
                canvas.drawLine(totalWidth + drawLength, 0, totalWidth + drawLength, tapeHeight * 2, tapePaint);
                drawText(canvas, currentValue, totalWidth + drawLength);
            } else {
                canvas.drawLine(totalWidth + drawLength, 0, totalWidth + drawLength, tapeHeight, tapePaint);
            }
            drawLength += gapWidth;
            currentValue++;
        }

    }

    private void drawText(Canvas canvas, int value, int x) {
        String drawStr = value + "";
        textPaint.getTextBounds(drawStr, 0, drawStr.length(), textRect);
        float width = textPaint.measureText(drawStr);
        canvas.drawText(drawStr, x - width / 2, tapeHeight * 2 + numberHeight + numberMarginTop, textPaint);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        acquireVelocityTracker(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                scroller.forceFinished(true);
                isCanScroll = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - lastX;
                moveX += dx;

                //根据moveX的值判断是否超出最大最小值范围，分情况处理
                if ((moveX > (initValue - minValue) * gapWidth && dx > 0)) {
                    moveX = (initValue - minValue) * gapWidth;
                    currentValue = minValue;
                    gapOffset = 0;
                } else if ((moveX < -(maxValue - initValue) * gapWidth && dx < 0)) {
                    moveX = -(maxValue - initValue) * gapWidth;
                    currentValue = maxValue;
                    gapOffset = 0;
                } else {
                    float moveValue = initValue - (moveX / gapWidth);
                    //处理负数情况
                    if (moveValue > 0) {
                        currentValue = (int) moveValue;
                    } else {
                        currentValue = (int) moveValue - 1;
                    }
                    //根据左右移动判断gapOffset大小
                    if (moveX > 0) {
                        gapOffset = (int) (gapWidth - Math.abs(moveX % gapWidth));
                    } else {
                        gapOffset = (int) Math.abs(moveX % gapWidth);
                    }
                }
                if (valueListener != null) {
                    if (gapOffset > gapWidth / 2) {
                        valueListener.OnValue(currentValue + 1);
                    } else {
                        valueListener.OnValue(currentValue);

                    }
                }


                invalidate();
                isCanScroll = false;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                velocityTracker.computeCurrentVelocity(1000, maxVelocity);
                float xVelocity = velocityTracker.getXVelocity();

                isCanJustScroll = true;

                if (Math.abs(xVelocity) > minVelocity) {
                    scroller.fling((int) event.getX(), 0, (int) xVelocity, 0, Integer.MIN_VALUE,
                            Integer.MAX_VALUE, 0, 0);
                } else {
                    if (gapOffset > gapWidth / 2) {
                        adjustScroll((int) event.getX(), -(gapWidth - gapOffset));
                    } else {
                        adjustScroll((int) event.getX(), gapOffset);
                    }
                }
                isCanScroll = true;
                break;
            default:
                break;
        }
        lastX = event.getX();
        return true;

    }

    private void acquireVelocityTracker(final MotionEvent event) {
        if (null == velocityTracker) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(event);
    }




    @Override
    public void computeScroll() {
        super.computeScroll();
        if (scroller.computeScrollOffset()) {

            float dx = scroller.getCurrX() - lastX;
            moveX += dx;
            //根据moveX的值判断是否超出最大最小值范围，分情况处理
            if ((moveX > (initValue - minValue) * gapWidth && dx > 0)) {
                moveX = (initValue - minValue) * gapWidth;
                currentValue = minValue;
                gapOffset = 0;
            } else if ((moveX < -(maxValue - initValue) * gapWidth && dx < 0)) {
                moveX = -(maxValue - initValue) * gapWidth;
                currentValue = maxValue;
                gapOffset = 0;
            } else {
                float moveValue = initValue - (moveX / gapWidth);
                //处理负数情况
                if (moveValue > 0) {
                    currentValue = (int) moveValue;
                } else {
                    currentValue = (int) moveValue - 1;
                }
                //根据左右移动判断gapOffset大小
                if (moveX > 0) {
                    gapOffset = (int) (gapWidth - Math.abs(moveX % gapWidth));
                } else {
                    gapOffset = (int) Math.abs(moveX % gapWidth);
                }
                if (valueListener != null) {
                    if (gapOffset > gapWidth / 2) {
                        valueListener.OnValue(currentValue + 1);
                    } else {
                        valueListener.OnValue(currentValue);

                    }
                }
            }
            lastX = scroller.getCurrX();
            if (valueListener != null) {
                if (gapOffset > gapWidth / 2) {
                    valueListener.OnValue(currentValue + 1);
                } else {
                    valueListener.OnValue(currentValue);

                }
            }


            invalidate();

        } else if (isCanScroll && isCanJustScroll) {
            Log.i("scroll", "isCanScroll");
            isCanJustScroll = false;

            if (gapOffset > gapWidth / 2) {
                adjustScroll(scroller.getFinalX(), -(gapWidth - gapOffset));
            } else {
                adjustScroll(scroller.getFinalX(), gapOffset);
            }

        }
    }

    /**
     * 用于滑动后 指示器没有刚好指到刻度值上 进行微调
     * @param start 起始X坐标
     * @param distance 移动差值
     */
    private void adjustScroll(int start, int distance) {
        scroller.startScroll(start, 0, distance, 0);
        invalidate();
    }

    private ValueListener valueListener;

    public interface ValueListener {
        void OnValue(int value);
    }

    /**
     * 监听刻度值
     * @param valueListener
     */
    public void setValueListener(ValueListener valueListener) {
        this.valueListener = valueListener;
    }

    public int getCurrentValue() {
        return this.currentValue;
    }
}
