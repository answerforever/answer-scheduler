package com.answer.scheduler.manager;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.answer.scheduler.enums.CleanAreaEnum;
import com.answer.scheduler.model.CalendarEventModel;
import com.answer.scheduler.utils.RedisUtils;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 清扫计划
 *
 * @author zhaodong
 * @date 2019/10/12 15:15
 */
@Service
public class CleanScheduleManager {

    private static final List<String> nameList = Arrays.asList("Li", "Liang", "Zhao");

    private static final String SCHEDULER_OFFSET_KEY = "scheduler:offset";

    private static final Map<String, String> MAP_NAME_FIELD = new HashMap<>();

    static {
        MAP_NAME_FIELD.put("Li", "李苗苗");
        MAP_NAME_FIELD.put("Liang", "梁宝心");
        MAP_NAME_FIELD.put("Zhao", "赵栋");
    }

    private static final List<String> areaList = Arrays.asList("卫生间", "客厅", "厨房");

    private int offsetOfLi = 0;

    private int offsetOfCao = 0;

    private int offsetOfZhao = 0;

    /**
     * 获取当前月份计划事件
     *
     * @return
     */
    public List<CalendarEventModel> getCurrentMonthSchedule() {
        List<CalendarEventModel> calendarEventModels = new ArrayList<>();
        //时间范围，当前时间往前一个月
        Date currentDate = DateUtil.date();
        //获取周末
        List<Date> sunDayList = getSundayOfMonth(currentDate);

        Map<String, String> offsetMap = new HashMap<>();
        //redis获取
        Map<String, String> mapData = RedisUtils.hgetall(SCHEDULER_OFFSET_KEY);
        if (mapData != null) {
            offsetMap = mapData;
        } else {
            offsetMap.put("Li", "0");
            offsetMap.put("Liang", "1");
            offsetMap.put("Zhao", "2");
        }

        Map<String, String> finalOffsetMap = offsetMap;
        sunDayList.forEach(s -> {
            String currentDateStr = DateUtil.format(s, "yyyy-MM-dd");
            nameList.forEach(n -> {
                CalendarEventModel calendarEventModel = new CalendarEventModel();
                Integer currentOffset = Integer.valueOf(finalOffsetMap.get(n));
                CleanAreaEnum cleanAreaEnum = CleanAreaEnum.values()[currentOffset];
                String currentTitle = cleanAreaEnum.getMsg() + "：" + MAP_NAME_FIELD.get(n);
                calendarEventModel.setTitle(currentTitle);
                calendarEventModel.setStart(currentDateStr);
                //偏移量设置
                currentOffset++;
                if (currentOffset >= CleanAreaEnum.values().length) {
                    currentOffset = 0;
                }
                finalOffsetMap.put(n, String.valueOf(currentOffset));
                calendarEventModels.add(calendarEventModel);
            });
        });

        //更新缓存
        RedisUtils.hsset(SCHEDULER_OFFSET_KEY, finalOffsetMap);
        return calendarEventModels;
    }

    /**
     * 获取月份的周末
     *
     * @param currentDate
     * @return
     */
    public List<Date> getSundayOfMonth(Date currentDate) {
        //获取当前月的第一天
        Date startDateOfMonth = DateUtil.beginOfMonth(currentDate);
        //获取当前月的最后一天
        Date endDateOfMonth = DateUtil.endOfMonth(currentDate);
        List<Date> sunDayList = new ArrayList<>(6);
        for (Date i = startDateOfMonth; i.before(endDateOfMonth);
             i = DateUtil.offset(i, DateField.DAY_OF_MONTH, 1)) {
            if (DateUtil.dayOfWeek(i) == 1) {
                sunDayList.add(i);
            }
        }
        return sunDayList;
    }

}
