package com.example.changshi.calendartest;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by changshi on 2016/8/19.
 */
public class CustomCalendarView extends FrameLayout {
    /**
     * 空位，无数据展示
     */
    public final static int VIEW_TYPE_PLACE_HOLDER = -1;

    /**
     * 月标题，展示当前月的年月数据
     */
    public final static int VIEW_TYPE_MONTH_TITLE = 0;

    /**
     * 一天，展示一天的号数信息
     */
    public final static int VIEW_TYPE_DAY = 1;

    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy年-MM月-dd日");

    private TextView mTopLastMonthTxv;
    private TextView mTopNextMonthTxv;
    private LinearLayout mTopMonthLayout;
    private RecyclerView mCalendarView;
    private GridLayoutManager mLayoutManager;

    private float mTopMonthViewPosition1;
    private float mTopMonthViewPosition2;
    private float mTopMonthViewPosition3;
    private int mChooseDateMonthPositionFlag;
    private int mChooseDateMonthPosition;

    private boolean mIsFirst = true;

    private List<CalendarItem> mViewItemData;
    private OnDayClickListener mDayClickListener;
    private int mChoosePosition;
    private Context mContext;

    public CustomCalendarView(Context context) {
        this(context, null);
    }

    public CustomCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        View view = LayoutInflater.from(mContext).inflate(R.layout.calendar_view_layout, null);
        addView(view, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        mCalendarView = (RecyclerView) findViewById(R.id.rv_calendar_view);
        mTopLastMonthTxv = (TextView) findViewById(R.id.tv_last_month);
        mTopNextMonthTxv = (TextView) findViewById(R.id.tv_next_month);
        mTopMonthLayout = (LinearLayout) findViewById(R.id.ll_top_month_title);
    }

    public void setData(List<CalendarItemProperty> calendarData) {

        if (calendarData == null || calendarData.size() == 0) {
            return;
        }

        // 初始化顶部月份标题
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(calendarData.get(0).date);
        mTopLastMonthTxv.setText(mDateFormat.format(calendar.getTime()).substring(0, 9));
        calendar.add(Calendar.MONTH, +1);
        mTopNextMonthTxv.setText(mDateFormat.format(calendar.getTime()).substring(0, 9));

        // 构造列表需要的界面数据
        genViewItem(calendarData);

        mLayoutManager = new GridLayoutManager(mContext, 7);
        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (mViewItemData.get(position).type == VIEW_TYPE_MONTH_TITLE) {
                    return 7;
                } else {
                    return 1;
                }

            }
        });
        final CalendarAdapter adapter = new CalendarAdapter(mContext, mViewItemData);
        adapter.setItemOnClickListener(new CalendarAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                if (mViewItemData.get(position).data.isCanBeChoose) {
                    mViewItemData.get(mChoosePosition).data.isChecked = false;
                    mViewItemData.get(position).data.isChecked = true;
                    mChoosePosition = position;
                    if (mDayClickListener != null) {
                        mDayClickListener.onClick(mViewItemData.get(mChoosePosition).data.date);
                    }
                    adapter.setDatas(mViewItemData);
                }
            }
        });
        mCalendarView.setLayoutManager(mLayoutManager);
        mCalendarView.setAdapter(adapter);
        mCalendarView.addOnScrollListener(new ScrollListenerMove());
        mLayoutManager.scrollToPosition(mChooseDateMonthPosition);
    }

    public Date getChoosedDate() {
        return mViewItemData.get(mChoosePosition).data.date;
    }

    public void setOnDayClickListener(OnDayClickListener listener) {
        mDayClickListener = listener;
    }

    /**
     * 纯粹的日期数据转换为日历界面的数据，增加了每月标题和空白占位
     *
     * @param calendarData
     */
    private void genViewItem(List<CalendarItemProperty> calendarData) {

        Calendar calendar = Calendar.getInstance();
        CalendarItem placeItem = new CalendarItem();
        placeItem.type = VIEW_TYPE_PLACE_HOLDER;

        mViewItemData = new ArrayList<>();
        mChooseDateMonthPositionFlag = 0;

        // 第一个月前面部分处理
        {

            // 增加月标题
            calendar.setTime(calendarData.get(0).date);
            CalendarItem monthTitleItem = new CalendarItem();
            monthTitleItem.type = VIEW_TYPE_MONTH_TITLE;
            monthTitleItem.dateText = mDateFormat.format(calendar.getTime()).substring(0, 9);
            monthTitleItem.data = calendarData.get(0);
            mViewItemData.add(monthTitleItem);

            // 增加空白占位
            int weekFlag = calendar.get(Calendar.DAY_OF_WEEK);
            for (int i = 1; i < weekFlag; i++) {
                mViewItemData.add(placeItem);
            }
        }

        for (int i = 0; i < calendarData.size(); i++) {

            calendar.setTime(calendarData.get(i).date);

            // 新的一个月
            if (calendar.get(Calendar.DAY_OF_MONTH) == 1 && i != 0) {

                int weekFlag = calendar.get(Calendar.DAY_OF_WEEK);

                // 增加上月末空白占位
                if (weekFlag != 1) {
                    for (int j = weekFlag; j <= 7; j++) {
                        mViewItemData.add(placeItem);
                    }
                }

                // 增加月标题
                CalendarItem monthTitleItem = new CalendarItem();
                monthTitleItem.type = VIEW_TYPE_MONTH_TITLE;
                monthTitleItem.dateText = mDateFormat.format(calendar.getTime()).substring(0, 9);
                monthTitleItem.data = calendarData.get(i);
                mViewItemData.add(monthTitleItem);
                mChooseDateMonthPositionFlag = mViewItemData.size() - 1;

                // 增加本月初空白占位
                for (int j = 1; j < weekFlag; j++) {
                    mViewItemData.add(placeItem);
                }
            }

            CalendarItem dayItem = new CalendarItem();
            dayItem.type = VIEW_TYPE_DAY;
            dayItem.data = calendarData.get(i);
            dayItem.dateText = calendar.get(Calendar.DAY_OF_MONTH) + "";
            mViewItemData.add(dayItem);

            if (calendarData.get(i).isChecked == true) {
                mChoosePosition = mViewItemData.size() - 1;
                mChooseDateMonthPosition = mChooseDateMonthPositionFlag;
            }

        }

    }

    /**
     * 顶部标题动画切换效果
     *
     * @param isUP
     */
    private void startTopMonthAnim(boolean isUP) {
        long duration = 200;

        if (mIsFirst) {
            mTopMonthViewPosition2 = mTopLastMonthTxv.getY();
            mTopMonthViewPosition3 = mTopNextMonthTxv.getY();
            mTopMonthViewPosition1 = mTopMonthViewPosition2 - (mTopMonthViewPosition3 - mTopMonthViewPosition2);
            mIsFirst = false;
        }

        // 上滑
        if (isUP) {
            ObjectAnimator anim1 = ObjectAnimator.ofFloat(mTopLastMonthTxv, "y", mTopMonthViewPosition1, mTopMonthViewPosition2).setDuration(duration);
            anim1.setInterpolator(new AccelerateDecelerateInterpolator());
            anim1.start();

            ObjectAnimator anim2 = ObjectAnimator.ofFloat(mTopNextMonthTxv, "y", mTopMonthViewPosition2, mTopMonthViewPosition3).setDuration(duration);
            anim2.setInterpolator(new AccelerateDecelerateInterpolator());
            anim2.start();
        }

        // 下滑
        else {
            ObjectAnimator anim1 = ObjectAnimator.ofFloat(mTopLastMonthTxv, "y", mTopMonthViewPosition2, mTopMonthViewPosition1).setDuration(duration);
            anim1.setInterpolator(new AccelerateDecelerateInterpolator());
            anim1.start();

            ObjectAnimator anim2 = ObjectAnimator.ofFloat(mTopNextMonthTxv, "y", mTopMonthViewPosition3, mTopMonthViewPosition2).setDuration(duration);
            anim2.setInterpolator(new AccelerateDecelerateInterpolator());
            anim2.start();
        }
    }

    /**
     * 在此实现了顶部标题移动透明度切换效果,可在{@link #setData(List)}方法mCalendarView.addOnScrollListener处切换
     */
    public class ScrollListenerMove extends RecyclerView.OnScrollListener {

        private final float FINAL_Y = mTopMonthLayout.getY();
        private final int TRANSPARENCY_CHANGE_UNIT = 5;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

            int firstVisiblePosition = mLayoutManager.findFirstVisibleItemPosition();

            String topMonthTitle;

            // 顶部标题展示内容赋值
            if (mViewItemData.get(firstVisiblePosition).data != null) {
                topMonthTitle = mDateFormat.format(mViewItemData.get(firstVisiblePosition).data.date).substring(0, 9);
            } else {
                topMonthTitle = mDateFormat.format(mViewItemData.get(firstVisiblePosition + 6).data.date).substring(0, 9);
            }
            mTopLastMonthTxv.setText(topMonthTitle);

            // 非月份标题，顶部标题不进行移动
            if (mViewItemData.get(firstVisiblePosition + 7).type != VIEW_TYPE_MONTH_TITLE) {
                mTopMonthLayout.setY(FINAL_Y);
                mTopLastMonthTxv.setTextColor(Color.argb(255, 0, 0, 0));
                return;
            }

            // 顶部标题位置和透明度改变
            float Y = mLayoutManager.getChildAt(0).getY();
            int alpha = 255 + TRANSPARENCY_CHANGE_UNIT * (int) (Y);
            if (alpha < 0) {
                alpha = 0;
            }
            mTopLastMonthTxv.setTextColor(Color.argb(alpha, 0, 0, 0));
            mTopMonthLayout.setY(Y);
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }
    }

    /**
     * 在此实现了顶部标题动画切换效果,可在{@link #setData(List)}方法mCalendarView.addOnScrollListener处切换
     */
    public class ScrollListenerAnim extends RecyclerView.OnScrollListener {

        /**
         * 上一次可见位置
         */
        private int lastFirstVisiblePosition = -1;

        /**
         * 上一次滑动方向</br>
         * 0上滑</br>
         * 1下滑</br>
         */
        private int lastScrollDirection = 1;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

            int firstVisiblePosition = mLayoutManager.findFirstVisibleItemPosition();

            // 第一行非月份标题不进行动画
            if (mViewItemData.get(firstVisiblePosition).type != VIEW_TYPE_MONTH_TITLE) {
                return;
            }

            // 滑动方向未变并且第一可见行未变不进行动画
            if (dy < 0 && lastScrollDirection == 0 && lastFirstVisiblePosition == firstVisiblePosition) {
                return;
            } else if (dy > 0 && lastScrollDirection == 1 && lastFirstVisiblePosition == firstVisiblePosition) {
                return;
            }
            if (dy < 0) {
                lastScrollDirection = 0;
            } else if (dy > 0) {
                lastScrollDirection = 1;
            }
            lastFirstVisiblePosition = firstVisiblePosition;

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(mViewItemData.get(firstVisiblePosition).data.date);
            mTopLastMonthTxv.setText(mDateFormat.format(calendar.getTime()).substring(0, 9));
            calendar.add(Calendar.MONTH, +1);
            mTopNextMonthTxv.setText(mDateFormat.format(calendar.getTime()).substring(0, 9));

            if (dy < 0) {
                // 上滑
                startTopMonthAnim(true);
            } else if (dy > 0) {
                // 下滑
                startTopMonthAnim(false);
            }
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }
    }

    public class CalendarItem {
        /**
         * 该项的类型：</br>
         * -1 : 空数据，占位作用</br>
         * 0 : 月标题，展示当前月的年月数据</br>
         * 1 : 一天，展示一天的号数信息</br>
         */
        public int type;

        /**
         * 日期展示数据
         */
        public String dateText;

        public CalendarItemProperty data;
    }

    public interface OnDayClickListener {
        void onClick(Date date);
    }
}
