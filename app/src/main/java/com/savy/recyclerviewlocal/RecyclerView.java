package com.savy.recyclerviewlocal;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.List;

public class RecyclerView extends ViewGroup {

    //协调adapter和回收池之间的工作

    private Adapter adapter;
    private RecyclerViewPool recyclerViewPool;

    private VelocityTracker velocityTracker;

    //当前recyclerview的高度
    private int width;

    private Flinger flinger;

    //当前recyclerview的宽度
    private int height;

    //每一个item测量高度，简单起见，先写死每一个item的高度
    private int[] heights;

    //item的数量
    private int rowCount;

    private boolean needLayout;

    //最小滑动距离
    private int touchSlop;

    private int currentY;

    private List<ViewHolder> viewHolderList;

    //第一个可见元素左上角顶端距离屏幕左边顶端的距离
    private int scrollY = 0;

    //第一个可见元素在viewHolderList中的index索引值，默认为0
    private int firstRow = 0;

    private int miniVelocity;

    public RecyclerView(Context context) {
        super(context);
    }

    public RecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        viewHolderList = new ArrayList<>();
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        touchSlop = viewConfiguration.getScaledTouchSlop();
        miniVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
        flinger = new Flinger(context);
    }

    public RecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public void setAdapter(Adapter adapter) {
        this.adapter = adapter;
        needLayout = true;
        recyclerViewPool = new RecyclerViewPool();
        requestLayout();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (needLayout || changed) {
            needLayout = false;

            //走到重新摆放子控件的位置
            if (adapter != null) {
                width = r - l;
                height = b - t;
            }

            rowCount = adapter.getItemCount();
            heights = new int[rowCount];

            for (int i = 0; i < rowCount; i++) {
                heights[i] = adapter.getHeight(i);
            }

            int left = 0, top = 0;
            int right, bottom;

            Log.e("DEBUG", "height ->" + height);

            for (int i = 0; i < rowCount && top < height; i++) {
                bottom = top + heights[i];

                //生成view
                ViewHolder viewHolder = makeViewHolder(i, left, top, width, bottom);
                viewHolderList.add(viewHolder);

                top = bottom;
            }
        }
    }

    private ViewHolder makeViewHolder(int row, int left, int top, int right, int bottom) {
        ViewHolder viewHolder = obtainViewHolder(row, right - left, bottom - top);
        viewHolder.itemView.layout(left, top, right, bottom);

        return viewHolder;
    }

    private ViewHolder obtainViewHolder(int row, int width, int height) {
        int itemType = adapter.getItemViewType(row);
        ViewHolder viewHolder = recyclerViewPool.getRecyclerView(itemType);

        //第一屏幕
        if (viewHolder == null) {
            viewHolder = adapter.onCreateViewHolder(this, itemType);
        }

        //更新数据
        adapter.onBindViewHolder(viewHolder, row);
        viewHolder.setItemType(itemType);
        viewHolder.itemView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));

        addView(viewHolder.itemView, 0);

        return viewHolder;
    }

    /***
     * 容器不处理点击事件，只处理滑动事件，要做定向拦截
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }

        velocityTracker.addMovement(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE: {

                int y2 = (int) event.getRawY();
                int diff = (int) (currentY - event.getRawY());
                scrollBy(0, diff);

                break;
            }

            case MotionEvent.ACTION_UP: {
                velocityTracker.computeCurrentVelocity(1000);

                int velocityY = (int) velocityTracker.getYVelocity();
                int initY = scrollY + sumArray(heights, 1, firstRow);
                int maxY = Math.max(0, sumArray(heights, 0, heights.length)) - height;
                if (Math.abs(velocityY) > miniVelocity) {
                    //惯性滑动，需要用额外的线程来控制
                    flinger.start(0, initY,0, velocityY,0, maxY);
                } else {
                    if (this.velocityTracker != null) {
                        this.velocityTracker.recycle();
                        this.velocityTracker = null;
                    }
                }
                break;
            }
        }

        return super.onTouchEvent(event);
    }

    class Flinger implements Runnable {

        private int initY;
        private Scroller scroller;

        public void start(int initX, int initY,
                   int initVelocityX, int initVelocityY,
                   int maxX, int maxY) {
            scroller.fling(initX, initY, initVelocityX, initVelocityY, 0, maxX, 0, maxY);
            this.initY = initY;
            post(this);
        }

        Flinger(Context context) {
            scroller = new Scroller(context);
        }

        @Override
        public void run() {
            if (scroller != null && scroller.isFinished()) {
                return;
            }
            boolean more = scroller.computeScrollOffset();

            int y = scroller.getCurrY();
            int diff = initY - y;
            if (diff != 0) {
                scrollBy(0, diff);
                initY = y;
            }

            if (more) {
                post(this);
            }
        }
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        boolean allowInterception = false;

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                currentY = (int) ev.getRawY();
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                int dis = (int) Math.abs(currentY - ev.getRawY());
                if (dis > touchSlop) {
                    allowInterception = true;
                }
                break;
            }

        }

        return allowInterception;
    }


    /***
     * 默认的scrollBy只对canvas做滚动操作,所以这里需要重新摆放每一个子控件，
     * 计算控件的滑入滑出，并且和回收池之间的联系，然后再滑动的过程中，去摆放每一个子空间的位置
     * @param x
     * @param y
     */
    @Override
    public void scrollBy(int x, int y) {
        //super.scrollBy(x, y);

        scrollY += y;

        scrollBounds();

        if (scrollY > 0) {
            //向上滑动
            while (heights[firstRow] < scrollY) {
                //丢回回收池中 firstRow++
                if (!viewHolderList.isEmpty()) {
                    removeView(viewHolderList.remove(0));
                }
                scrollY -= heights[firstRow];
                firstRow += 1;
                Log.e("DEBUG", "removing an item");
            }


            while ( height > getFilledHeight()) {
                //需要被添加

                int dataIndex = firstRow + viewHolderList.size();
                ViewHolder viewHolder = obtainViewHolder(dataIndex, width, heights[dataIndex]);
                viewHolderList.add(viewHolderList.size(), viewHolder);
                Log.e("DEBUG", "adding an item");
            }
        }

        else {//往下滑动
            while (scrollY < 0) {
                firstRow --;
                ViewHolder viewHolder = obtainViewHolder(firstRow, width, heights[firstRow]);
                scrollY += heights[firstRow];
                viewHolderList.add(0, viewHolder);
            }

            while (!viewHolderList.isEmpty() &&  getFilledHeight() - heights[firstRow + viewHolderList.size() - 1] > height) {
                removeView(viewHolderList.remove(viewHolderList.size() - 1));
            }
        }

        repositionViews();
        //顶端移除一个元素的高度，底部不会添加的情况

    }

    private void scrollBounds() {
        if (scrollY < 0) {
            //firstRow变成了0，一开始的不能允许向下加载
            scrollY = Math.max(scrollY, -sumArray(heights, 0, firstRow));
        } else {
            if (sumArray(heights, firstRow, heights.length - firstRow) - scrollY < height) {
                //scrollY应该是一个固定值，如果在底部
                scrollY = sumArray(heights, firstRow, heights.length - firstRow) - height;
            }
        }
    }


    private void repositionViews() {

        int left = 0, top, right = width, bottom, i = 0;
        top = -scrollY;

        for (ViewHolder viewHolder : viewHolderList) {
            bottom = top + heights[i];
            i++;
            viewHolder.itemView.layout(left, top, width, bottom);
            top = bottom;
        }

    }


    private int getFilledHeight() {
        return sumArray(heights, firstRow, viewHolderList.size()) - scrollY;
    }


    private int sumArray(int array[], int firstIndex, int count) {
        int sum = 0;
        count += firstIndex;

        for (int i = firstIndex; i < count; i++) {
            sum += array[i];
        }

        return sum;
    }


    private void removeView(ViewHolder remove) {
        recyclerViewPool.putRecyclerView(remove);
        removeView(remove.itemView);
    }


    interface Adapter<VH extends ViewHolder> {

        VH onCreateViewHolder(ViewGroup parent, int viewType);
        void onBindViewHolder(VH viewHolder, int position);

        //item的类型
        int getItemViewType(int position);
        int getItemCount();
        public int getHeight(int index);
    }
}
