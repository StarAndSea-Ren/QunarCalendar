package com.example.changshi.calendartest;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

/**
 * Created by changshi on 2016/8/19.
 */
public class CalendarAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<CustomCalendarView.CalendarItem> mDatas;
    private Context mContext;
    private OnRecyclerViewItemClickListener mItemOnClickListener;

    public CalendarAdapter(Context context, List<CustomCalendarView.CalendarItem> data) {
        this.mContext = context;
        this.mDatas = data;
    }

    public void setItemOnClickListener(OnRecyclerViewItemClickListener listener){
        mItemOnClickListener = listener;
    }

    public void setDatas(List<CustomCalendarView.CalendarItem> data) {
        this.mDatas = data;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return mDatas.get(position).type;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {

            // 某天的号数数据
            case CustomCalendarView.VIEW_TYPE_DAY:
                return new DayViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_calendar_day_view, parent, false));

            // 空白占位
            case CustomCalendarView.VIEW_TYPE_PLACE_HOLDER:
                return new PlaceViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_calendar_day_view, parent, false));

            // 月标题
            case CustomCalendarView.VIEW_TYPE_MONTH_TITLE:
                return new MonthTitleViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_calendar_month_title, parent, false));

            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        // 某天的号数数据
        if (holder instanceof DayViewHolder) {

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(mDatas.get(position).data.date);

            ((DayViewHolder) holder).dayTxv.setText(mDatas.get(position).dateText);

            // 正常黑色字体展示
            ((DayViewHolder) holder).dayTxv.setTextColor(0xff1e1e1e);

            // 周末红色字体
            if (calendar.get(Calendar.DAY_OF_WEEK) == 1||calendar.get(Calendar.DAY_OF_WEEK) == 7){
                ((DayViewHolder) holder).dayTxv.setTextColor(0xfff31e1f);
            }

            // 当前日期蓝色字体，并用“今天”替换号数
            if (calendar.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR) && calendar.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR)) {
                ((DayViewHolder) holder).dayTxv.setTextColor(0xff1194de);
                ((DayViewHolder) holder).dayTxv.setText("今天");
            }

            // 不可选灰色
            if (!mDatas.get(position).data.isCanBeChoose){
                ((DayViewHolder) holder).dayTxv.setTextColor(0xff949494);
            }

            // 当前选中
            if (mDatas.get(position).data.isChecked){
                ((DayViewHolder) holder).dayTxv.setTextColor(0xffffffff);
                ((DayViewHolder) holder).dayTxv.setBackgroundResource(R.drawable.circle_blue);
            }else {
                ((DayViewHolder) holder).dayTxv.setBackgroundColor(0xffffffff);
            }
        }

        // 月标题
        else if (holder instanceof MonthTitleViewHolder) {
            ((MonthTitleViewHolder) holder).monthTitleTxv.setText(mDatas.get(position).dateText);
        }
    }

    public class DayViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView dayTxv;

        public DayViewHolder(View itemView) {
            super(itemView);
            dayTxv = (TextView) itemView.findViewById(R.id.tv_day);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mItemOnClickListener != null){
                mItemOnClickListener.onItemClick(v,getAdapterPosition());
            }
        }
    }

    public class PlaceViewHolder extends RecyclerView.ViewHolder {

        public TextView dayTxv;

        public PlaceViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class MonthTitleViewHolder extends RecyclerView.ViewHolder {

        public TextView monthTitleTxv;

        public MonthTitleViewHolder(View itemView) {
            super(itemView);
            monthTitleTxv = (TextView) itemView.findViewById(R.id.tv_month_title);
        }
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public interface OnRecyclerViewItemClickListener{
        void onItemClick(View v,int position);
    }
}
