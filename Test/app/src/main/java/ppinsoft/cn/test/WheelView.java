package ppinsoft.cn.test;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.renderscript.ScriptIntrinsicLUT;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: wangjie
 * Email: tiantian.china.2@gmail.com
 * Date: 7/1/14.
 */
public class WheelView extends HorizontalScrollView {
    public static final String TAG = WheelView.class.getSimpleName();

    public static class OnWheelViewListener {
        public void onSelected(int selectedIndex, String item) {
        }
    }


    private Context context;
//    private ScrollView scrollView;

    private LinearLayout views;

    public WheelView(Context context) {
        super(context);
        init(context);
    }

    public WheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public WheelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    //    String[] items;
    List<String> items;

    private List<String> getItems() {
        return items;
    }

    public void setItems(List<String> list) {
        if (null == items) {
            items = new ArrayList<String>();
        }
        items.clear();
        items.addAll(list);

        // 前面和后面补全
        for (int i = 0; i < offset; i++) {
            items.add(0, "");
            items.add("");
        }

        initData();

    }


    public static final int OFF_SET_DEFAULT = 1;
    int offset = OFF_SET_DEFAULT; // 偏移量（需要在最前面和最后面补全）

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    int displayItemCount; // 每页显示的数量

    int selectedIndex = 1;


    private void init(Context context) {
        this.context = context;

//        scrollView = ((ScrollView)this.getParent());
//        Log.d(TAG, "scrollview: " + scrollView);
        Log.d(TAG, "parent: " + this.getParent());
//        this.setOrientation(VERTICAL);
        this.setHorizontalScrollBarEnabled(false);

        views = new LinearLayout(context);
        views.setOrientation(LinearLayout.HORIZONTAL);
        this.addView(views);

         scrollerTask = new Runnable() {

            public void run() {

//                int newY = getScrollY();
                int newX = getScrollX();
                if (initialX - newX == 0) { // stopped
                    final int remainder = initialX % itemWidth;
                    final int divided = initialX / itemWidth;
                    Log.d(TAG, "initialX: " + initialX);
                    Log.d(TAG, "remainder: " + remainder + ", divided: " + divided);
                    if (remainder == 0) {
                        selectedIndex = divided + offset;

                        onSeletedCallBack();
                    } else {
                        if (remainder > itemWidth / 2) {
                            WheelView.this.post(new Runnable() {
                                @Override
                                public void run() {
                                    Log.i("移动的距离1",initialX -remainder + itemWidth+"");
                                    WheelView.this.smoothScrollTo(initialX -remainder + itemWidth   ,0);
                                    selectedIndex = divided + offset + 1;
                                    onSeletedCallBack();
                                }
                            });
                        } else {
                            WheelView.this.post(new Runnable() {
                                @Override
                                public void run() {
                                    Log.i("移动的距离2",initialX -remainder+"");
                                    WheelView.this.smoothScrollTo(initialX - remainder ,0);
                                    selectedIndex = divided + offset;
                                    onSeletedCallBack();
                                }
                            });
                        }


                    }


                } else {
//                    initialY = getScrollY();
                    initialX = getScrollX();
                    WheelView.this.postDelayed(scrollerTask, newCheck);
                }
            }
        };


    }

//    int initialY;
    int initialX;

    Runnable scrollerTask;
    int newCheck = 50;

    public void startScrollerTask() {

//        initialY = getScrollY();
        initialX = getScrollX();
        this.postDelayed(scrollerTask, newCheck);
    }

    private void initData() {
        displayItemCount = offset * 2 + 1;

        for (String item : items) {
            views.addView(createView(item));
        }

        refreshItemView(0);
    }

//    int itemHeight = 0;
    int itemWidth = 0;
    private TextView createView(String item) {
        TextView tv = new TextView(context);
        tv.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        tv.setSingleLine(true);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        tv.setWidth(dip2px(60));//由于横屏滑动，必须设置固定宽度
        tv.setText(item);
//        tv.setBackgroundColor(Color.RED);
        tv.setGravity(Gravity.CENTER);
//        int padding = dip2px(25);
//        tv.setPadding(padding, padding, padding, padding);
        if (0 == itemWidth) {
//            itemHeight = getViewMeasuredHeight(tv);
            itemWidth = getViewMeasuredWidth(tv);
            Log.d(TAG, "itemWidth: " + itemWidth);
            views.setLayoutParams(new LayoutParams(itemWidth * displayItemCount,ViewGroup.LayoutParams.MATCH_PARENT));
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) this.getLayoutParams();
            this.setLayoutParams(new LinearLayout.LayoutParams(itemWidth * displayItemCount,lp.height));
        }
        return tv;
    }


    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        refreshItemView(l);
    }

    private void refreshItemView(int y) {
        int position = y / itemWidth + offset;
        int remainder = y % itemWidth;
        int divided = y / itemWidth;

        if (remainder == 0) {
            position = divided + offset;
        } else {
            if (remainder > itemWidth / 2) {
                position = divided + offset + 1;
            }
        }

        int childSize = views.getChildCount();
        for (int i = 0; i < childSize; i++) {
            TextView itemView = (TextView) views.getChildAt(i);
            if (null == itemView) {
                return;
            }
            if (position == i) {
                itemView.setTextColor(Color.parseColor("#0288ce"));
                itemView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
            } else {
                itemView.setTextColor(Color.parseColor("#bbbbbb"));
                itemView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            }
        }
    }

    /**
     * 获取选中区域的边界
     */
    int[] selectedAreaBorder;

    private int[] obtainSelectedAreaBorder() {
        if (null == selectedAreaBorder) {
            selectedAreaBorder = new int[2];
            selectedAreaBorder[0] = itemWidth * offset;
            selectedAreaBorder[1] = itemWidth * (offset + 1);
        }
        return selectedAreaBorder;
    }


    Paint paint;
//    int viewWidth;
    int viewHeigth;

    @Override
    public void setBackgroundDrawable(Drawable background) {

        if (viewHeigth == 0) {
//            viewWidth = ((Activity) context).getWindowManager().getDefaultDisplay().getWidth();
            viewHeigth = ((Activity) context).getWindowManager().getDefaultDisplay().getHeight();
            Log.d(TAG, "viewWidth: " + viewHeigth);
        }

        if (null == paint) {
            paint = new Paint();
            paint.setColor(Color.parseColor("#83cde6"));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dip2px(2f));
        }

        background = new Drawable() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void draw(Canvas canvas) {
                canvas.drawArc(obtainSelectedAreaBorder()[0],(viewHeigth-obtainSelectedAreaBorder()[1]+obtainSelectedAreaBorder()[0]) / 2,  obtainSelectedAreaBorder()[1],(viewHeigth+obtainSelectedAreaBorder()[1]-obtainSelectedAreaBorder()[0]) / 2,270,450,true, paint);

//                canvas.drawArc((viewWidth-obtainSelectedAreaBorder()[1]+obtainSelectedAreaBorder()[0]) / 2, obtainSelectedAreaBorder()[0],(viewWidth+obtainSelectedAreaBorder()[1]-obtainSelectedAreaBorder()[0]) / 2, obtainSelectedAreaBorder()[1],270,450,true, paint);
//                canvas.drawLine(viewWidth * 1 / 6, obtainSelectedAreaBorder()[0], viewWidth * 5 / 6, obtainSelectedAreaBorder()[0], paint);
//                canvas.drawLine(viewWidth * 1 / 6, obtainSelectedAreaBorder()[1], viewWidth * 5 / 6, obtainSelectedAreaBorder()[1], paint);
            }

            @Override
            public void setAlpha(int alpha) {

            }

            @Override
            public void setColorFilter(ColorFilter cf) {

            }

            @Override
            public int getOpacity() {
                return PixelFormat.UNKNOWN;
            }
        };


        super.setBackgroundDrawable(background);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d(TAG, "w: " + w + ", h: " + h + ", oldw: " + oldw + ", oldh: " + oldh);
//        viewWidth = w;
        viewHeigth = h;
        setBackgroundDrawable(null);
    }

    /**
     * 选中回调
     */
    private void onSeletedCallBack() {
        if (null != onWheelViewListener) {
            onWheelViewListener.onSelected(selectedIndex, items.get(selectedIndex));
        }

    }

    public void setSeletion(int position) {
        final int p = position;
        selectedIndex = p + offset;
        this.post(new Runnable() {
            @Override
            public void run() {
                WheelView.this.smoothScrollTo(p * itemWidth,0);
            }
        });

    }

    public String getSeletedItem() {
        return items.get(selectedIndex);
    }

    public int getSeletedIndex() {
        return selectedIndex - offset;
    }


    @Override
    public void fling(int velocityY) {
        super.fling(velocityY / 3);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {

            startScrollerTask();
        }
        return super.onTouchEvent(ev);
    }

    private OnWheelViewListener onWheelViewListener;

    public OnWheelViewListener getOnWheelViewListener() {
        return onWheelViewListener;
    }

    public void setOnWheelViewListener(OnWheelViewListener onWheelViewListener) {
        this.onWheelViewListener = onWheelViewListener;
    }

    private int dip2px(float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private int getViewMeasuredHeight(View view) {
        int width = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        view.measure(width, expandSpec);
        return view.getMeasuredHeight();
    }
    private int getViewMeasuredWidth(View view) {
        int height = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        view.measure(height, expandSpec);
        return view.getMeasuredWidth();
    }
}
