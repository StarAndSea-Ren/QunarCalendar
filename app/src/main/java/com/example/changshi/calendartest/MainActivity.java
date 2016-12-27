package com.example.changshi.calendartest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    /**
     * 最大的日历期限
     */
    private final int MAX_SPAN = 180;

    private List<CalendarItemProperty> mCalendarData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("去哪儿日历控件");

        CustomCalendarView calendarView = (CustomCalendarView) findViewById(R.id.calendar_view);
        calendarView.setOnDayClickListener(new CustomCalendarView.OnDayClickListener() {
            @Override
            public void onClick(Date date) {
                // todo 此处可对回调过来的日期进行处理
                Toast.makeText(getApplicationContext(), date.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        // 此处设置2天后为选中日期
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, +2);
        int choosedYear = calendar.get(Calendar.YEAR);
        int choosedMonth = calendar.get(Calendar.MONTH) + 1;
        int choosedDay = calendar.get(Calendar.DATE);

        // 组装数据
        initData(choosedYear, choosedMonth, choosedDay);
        calendarView.setData(mCalendarData);
    }

    /**
     * 组装日历需要的数据集
     *
     * @param choosedYear  当前选中年
     * @param choosedMonth 当前选中月
     * @param choosedDay   当前选中日
     */
    private void initData(int choosedYear, int choosedMonth, int choosedDay) {
        mCalendarData = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        // 当前日期月份前半段用不可选日期数据填充
        int dayCountMonthBefore = calendar.get(Calendar.DAY_OF_MONTH);
        if (dayCountMonthBefore > 0) {
            calendar.set(Calendar.DATE, 1);
            for (int i = 0; i < dayCountMonthBefore - 1; i++) {
                CalendarItemProperty item = new CalendarItemProperty();
                item.date = calendar.getTime();
                item.isCanBeChoose = false;
                item.isChecked = false;
                mCalendarData.add(item);
                calendar.add(Calendar.DATE, +1);
            }
        }

        // 可选数据
        for (int i = 0; i < MAX_SPAN; i++) {
            CalendarItemProperty item = new CalendarItemProperty();
            item.date = calendar.getTime();
            item.isCanBeChoose = true;

            // 当前选中数据
            if (calendar.get(Calendar.YEAR) == choosedYear && (calendar.get(Calendar.MONTH) + 1) == choosedMonth && calendar.get(Calendar.DATE) == choosedDay) {
                item.isChecked = true;
            } else {
                item.isChecked = false;
            }

            mCalendarData.add(item);
            calendar.add(Calendar.DATE, +1);
        }

        // 最后日期月份后半段用不可选日期数据填充
        int dayCountMonthLeft = calendar.getActualMaximum(Calendar.DAY_OF_MONTH) - calendar.get(Calendar.DAY_OF_MONTH);
        for (int i = 0; i <= dayCountMonthLeft; i++) {
            CalendarItemProperty item = new CalendarItemProperty();
            item.date = calendar.getTime();
            item.isCanBeChoose = false;
            item.isChecked = false;
            mCalendarData.add(item);
            calendar.add(Calendar.DATE, +1);
        }
    }
}
