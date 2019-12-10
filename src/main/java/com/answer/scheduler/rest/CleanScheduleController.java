package com.answer.scheduler.rest;

/**
 * @author zhaodong
 * @date 2019/10/12 16:51
 */

import com.answer.scheduler.manager.CleanScheduleManager;
import com.answer.scheduler.model.CalendarEventModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("clean_schedule")
public class CleanScheduleController {

    @Autowired
    private CleanScheduleManager cleanScheduleManager;

    @GetMapping(value = "/get_schedule")
    public List<CalendarEventModel> getScheduleOfMonth() {
        return cleanScheduleManager.getCurrentMonthSchedule();
    }
}
