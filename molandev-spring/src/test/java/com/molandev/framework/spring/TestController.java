package com.molandev.framework.spring;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/test")
public class TestController {

    @PostMapping("/datetime")
    public JsonResult<DateTimeTestEntity> testDateTime(@RequestBody DateTimeTestEntity entity) {
        return JsonResult.success(entity);
    }

    @PostMapping("/date")
    public JsonResult<DateTestEntity> testDate(@RequestBody DateTestEntity entity) {
        return JsonResult.success(entity);
    }

    @PostMapping("/time")
    public JsonResult<TimeTestEntity> testTime(@RequestBody TimeTestEntity entity) {
        return JsonResult.success(entity);
    }

    @GetMapping("/param/datetime")
    public JsonResult<String> testDateTimeParam(@RequestParam("dateTime") LocalDateTime dateTime) {
        return JsonResult.success(dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    @GetMapping("/param/date")
    public JsonResult<String> testDateParam(@RequestParam("date") LocalDate date) {
        return JsonResult.success(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    }

    @GetMapping("/param/time")
    public JsonResult<String> testTimeParam(@RequestParam("time") LocalTime time) {
        return JsonResult.success(time.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

    @Getter
    @Setter
    public static class DateTimeTestEntity {
        private LocalDateTime localDateTime;
    }

    @Getter
    @Setter
    public static class DateTestEntity {
        private LocalDate localDate;
    }

    @Getter
    @Setter
    public static class TimeTestEntity {
        private LocalTime localTime;
    }
}