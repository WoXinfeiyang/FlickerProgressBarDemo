package com.fxj.flickerprogressbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;




/**
 * Created by Administrator on 2017/4/28.
 */

public class FlickerProgressBar extends View implements Runnable{

        private String tag="FlikerProgressBar";

        /*XML布局文件中定义属性对于参数*/
        /**精度条文本大小*/
        private int textSize;
        /**进度条下载颜色*/
        private int loadingColor;
        /**进度条停止颜色*/
        private int stopColor;
        /**进度条矩形圆角半径*/
        private int radius;
        /**进度条矩形边框宽度*/
        private int borderWidth;

        /**进度条颜色*/
        private int progressColor;

        /**进度条默认高度*/
        private int Default_Height_Dp=35;

        /**最大进度值*/
        private float maxProgress=100f;
        /**当前进度值*/
        private float progress=40f;
        /**下载结束标志*/
        private boolean isFinish;
        /**停止下载标志*/
        private boolean isStop;


        /**背景画笔Paint对象*/
        private Paint bgPaint;
        /**背景矩形*/
        private RectF bgRectF;


        /**进度条画笔Paint对象*/
        private Paint pgPaint;
        /**进度条Bitmap对象*/
        private Bitmap pgBitmap;
        /**进度条画布Canvas对象*/
        private Canvas pgCanvas;


        /**进度条文本画笔Paint对象*/
        private Paint textPaint;
        /**文本矩形*/
        private Rect textRect;
        /**进度条文本String对象*/
        private String progressText;

        /**位图渲染器*/
        private BitmapShader bitmapShader;

        /**滑块Bitmap对象*/
        private Bitmap flickBitmap;
        /**滑块移动最左边的位置，用于控制滑块移动*/
        private float flickLeft;
        /**图片相交模式*/
        private PorterDuffXfermode xfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP);
        /**控制滑块移动的线程对象*/
        Thread thread;

    public FlickerProgressBar(Context context) {
        this(context,null,0);
    }

    public FlickerProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }


    public FlickerProgressBar(Context context, AttributeSet attrs, int defStyleRes) {
        super(context, attrs,defStyleRes);
        initAttrs(attrs);
    }

    /**初始化XML布局文件中的属性*/
    private void initAttrs(AttributeSet attrs) {
        TypedArray ta=getContext().obtainStyledAttributes(attrs,R.styleable.FlickerProgressBar);
        try {
            this.textSize = (int) ta.getDimension(R.styleable.FlickerProgressBar_textSize, 12);
            this.loadingColor = ta.getColor(R.styleable.FlickerProgressBar_loadingColor, Color.parseColor("#40c4ff"));
            this.stopColor = ta.getColor(R.styleable.FlickerProgressBar_stopColor, Color.parseColor("#ff9800"));
            this.radius=(int)ta.getDimension(R.styleable.FlickerProgressBar_radius,0);
            this.borderWidth = (int) ta.getDimension(R.styleable.FlickerProgressBar_borderWidth, 1);
        }finally {
            ta.recycle();;
        }
    }


    private void init() {
        /*设置背景边框Paint对象抗锯齿*/
        this.bgPaint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);
        /*设置背景边框Paint对象的填充风格*/
        this.bgPaint.setStyle(Paint.Style.STROKE);
        /*设置画笔的笔触宽度*/
        this.bgPaint.setStrokeWidth(this.borderWidth);
        /*初始化背景矩形*/
        this.bgRectF =new RectF(borderWidth,borderWidth,getMeasuredWidth()-borderWidth,getMeasuredHeight()-borderWidth);

        /*设置进度条画笔Paint对象抗锯齿*/
        pgPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        /*设置进度条画笔Paint对象填充风格*/
        pgPaint.setStyle(Paint.Style.FILL);

        /*设置文本画笔Paint抗锯齿*/
        textPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        /*给画笔设置绘制文本时的文字大小*/
        textPaint.setTextSize(textSize);
        /*初始化文本矩形*/
        textRect=new Rect();


       /*根据下载状态设置进度条颜色*/
       if(this.isStop){
           this.progressColor=stopColor;
       }else{
           this.progressColor=loadingColor;
       }

       /*初始化与滑块有关的参数*/
       /*创建滑块Bitmap对象*/
       flickBitmap= BitmapFactory.decodeResource(getResources(),R.drawable.flicker);
       /*初始化滑块最左边位置*/
       flickLeft=-flickBitmap.getWidth();


       initPgBitmap();
    }

    private void initPgBitmap() {
        /*初始化创建进度条Bitmap对象*/
        pgBitmap=Bitmap.createBitmap(getMeasuredWidth()-borderWidth,getMeasuredHeight()-borderWidth,Bitmap.Config.ARGB_8888);
        /*将进度条画布对象与进度条Bitmap对象绑定*/
        pgCanvas=new Canvas(pgBitmap);

        thread=new Thread(this);
        thread.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthSize=MeasureSpec.getSize(widthMeasureSpec);
        int heightSize=MeasureSpec.getSize(heightMeasureSpec);
        int heightMode=MeasureSpec.getMode(heightMeasureSpec);
        int height=0;
        switch (heightMode){
            /*当height设置为wrap_content其高度值设为默认值*/
            case MeasureSpec.AT_MOST:
                height=dp2px(this.Default_Height_Dp);
                break;

            case MeasureSpec.EXACTLY:
            case MeasureSpec.UNSPECIFIED:
                 height=heightMeasureSpec;
                 break;
        }

        setMeasuredDimension(widthSize,height);

        if (this.pgBitmap==null){
            init();
        }
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackGround(canvas);
        drawProgress(canvas);
        drawProgressText(canvas);
        drawColorProgressText(canvas);
    }




    /**绘制背景边框*/
    private void drawBackGround(Canvas canvas) {
        /*给背景画笔Paint对象设置画笔颜色*/
        bgPaint.setColor(progressColor);
        canvas.drawRoundRect(bgRectF, radius, radius,bgPaint);
    }

    /**绘制进度条*/
    private void drawProgress(Canvas canvas) {
        /*给进度条画笔Paint对象设置画笔颜色*/
        pgPaint.setColor(progressColor);
        float progressWidth=(progress/maxProgress)*getMeasuredWidth();

        pgCanvas.save(Canvas.CLIP_SAVE_FLAG);
        /*裁剪一个矩形区域*/
        pgCanvas.clipRect(0,0,progressWidth,getMeasuredHeight());
        /*染色*/
        pgCanvas.drawColor(progressColor);
        pgCanvas.restore();


        if(!isStop){
            /*给进度条画笔Paint对象设置图片相交模式*/
            pgPaint.setXfermode(xfermode);
            /*在进度条画布Canvas对象上绘制滑块Bitmap对象*/
            pgCanvas.drawBitmap(flickBitmap,flickLeft,0,pgPaint);
            pgPaint.setXfermode(null);
        }


        bitmapShader=new BitmapShader(pgBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        /*给进度条画笔Paint对象设置渲染效果Shader对象*/
        pgPaint.setShader(bitmapShader);
//        canvas.drawBitmap(pgBitmap,0,0,pgPaint);
        /*使用渲染效果Shader对象填充图形*/
        canvas.drawRoundRect(bgRectF,radius,radius,pgPaint);
    }

    /**绘制进度条文本*/
    private void drawProgressText(Canvas canvas) {
        /*给进度条文本画笔Paint对象设置画笔颜色*/
        textPaint.setColor(progressColor);
        progressText=getProgressText();
        /*返回包围字符串的最小矩形*/
        textPaint.getTextBounds(progressText,0,progressText.length(),textRect);
        float textWidth=textRect.width();
        float textHeight=textRect.height();
        float xCoordinate=(getMeasuredWidth()-textWidth)/2;
        float yCoordinate=(getMeasuredHeight()+textHeight)/2;
        /*绘制字符串*/
        canvas.drawText(progressText,xCoordinate,yCoordinate,textPaint);
    }

    /**获取进度条文本字符串*/
    private String getProgressText() {
        if(!isFinish){/*当没有下载完成时*/
            if(!isStop){/*当正在下载时*/
                return "下载中"+progress+"%";
            }else{/*当停止下载时*/
                return "继续";
            }
        }else{/*当下载完成时*/
            return "下载完成";
        }

    }

    /**进度条文本变色处理*/
    private void drawColorProgressText(Canvas canvas) {
        /*给进度条文本画笔设置颜色*/
        textPaint.setColor(Color.BLACK);
        /*获取文本矩形的宽和高*/
        float textWidth=textRect.width();
        float textHeight=textRect.height();
        /*获取进度条文本的坐标*/
        float xCoordinate=(getMeasuredWidth()-textWidth)/2;
        float yCoordinate=(getMeasuredHeight()+textHeight)/2;
        /*进度条宽度*/
        float progressWidth=(progress/maxProgress)*getMeasuredWidth();

        /*当进度条宽度大于进度条文本x坐标时才需要变色处理*/
        if(progressWidth>xCoordinate){
            /*需要进行变色处理的文字长度*/
            float colorWidth=Math.min(progressWidth,xCoordinate+textWidth);
            canvas.save(Canvas.CLIP_SAVE_FLAG);

            /*裁剪矩形画布*/
            canvas.clipRect(xCoordinate,0,colorWidth,getMeasuredHeight());
            /*绘制字符串*/
            canvas.drawText(progressText,xCoordinate,yCoordinate,textPaint);

            canvas.restore();
        }
    }


    @Override
    public void run() {
        float flickBitmapWidth=flickBitmap.getWidth();

        while(!isStop&&!isFinish&&!thread.isInterrupted()){
            flickLeft+=dp2px(5);

            float progressWidth=(progress/maxProgress)*getMeasuredWidth();

            if(flickLeft>=progressWidth){
                flickLeft=-flickBitmapWidth;
            }

            /*在工作线程中刷新界面*/
            postInvalidate();

            try {
                Thread.sleep(35);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    /**设置进度值*/
    public void setProgress(float progress){
        this.progress=progress;
        invalidate();
    }

    /**停止当前的进度条*/
    public void setStop(boolean stop){
        this.isStop=stop;
        if(this.isStop){/*停止当前进度条*/
            /*设置当前进度条的颜色*/
            progressColor=stopColor;
            /*将控制滑块移动的线程打断*/
            thread.interrupt();
        }else{/*没有停止当前进度条，当前进度条仍然继续运行*/
            progressColor=loadingColor;
//            thread.resume();
            thread=new Thread(this);
            thread.start();
        }

        /*在UI线程中刷新View，调用该方法会触发View的onDraw方法*/
        invalidate();
    }

    /**结束耗时任务*/
    public void finishTask(){
        this.isFinish=true;
        setStop(true);
    }

    public void toggle(){
        if(!isFinish){
            if(isStop){
                setStop(false);
            }else {
                setStop(true);
            }
        }
    }

    /**获取当前进度条的进度值*/
    public float getProgress(){
        return this.progress;
    }

    /**获取当前进度条是否停止*/
    public boolean isStop(){
        return isStop;
    }

    /**获取当前进度条是否结束*/
    public boolean isFinish(){
        return isFinish;
    }

    /**重置*/
    public void reset(){
        progress=0;
        isFinish=false;
        isStop=false;
        progressColor=loadingColor;
        progressText="";
        flickLeft=-flickBitmap.getWidth();
        initPgBitmap();
    }

    private int dp2px(int dp) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * density);
    }
}
