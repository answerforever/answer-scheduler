package com.answer.scheduler.model;

import lombok.Data;

/**
 * 日历事件实体
 *
 * @author zhaodong
 * @date 2019/10/12 15:19
 */
@Data
public class CalendarEventModel {
    /**
     * 标题
     */
    private String title;
    /**
     * 开始时间
     */
    private String start;
}
