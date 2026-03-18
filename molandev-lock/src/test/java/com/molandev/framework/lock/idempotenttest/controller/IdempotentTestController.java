package com.molandev.framework.lock.idempotenttest.controller;

import com.molandev.framework.lock.annotation.Idempotent;
import com.molandev.framework.lock.config.JsonResult;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/test/idempotent")
public class IdempotentTestController {

    @ExceptionHandler(Exception.class)
    public JsonResult<String> handleException(Exception e) {
        return JsonResult.failed(e.getMessage());
    }

    private final AtomicInteger counter = new AtomicInteger(0);

    @Idempotent(key = "idempotent_basic_test")
    @PostMapping("/basic-same-request")
    public JsonResult<String> performBasicSameRequestTest() {
        return JsonResult.success("Basic same request test executed, counter: " + counter.incrementAndGet());
    }

    @Idempotent(key = "#requestKey")
    @PostMapping("/parameter-based")
    public JsonResult<String> performParameterBasedTest(@RequestParam String requestKey) {
        return JsonResult.success("Parameter based test executed with key: " + requestKey + ", counter: " + counter.incrementAndGet());
    }

    @Idempotent
    @PostMapping("/default-key")
    public JsonResult<String> performDefaultKeyTest() {
        return JsonResult.success("Default key test executed, counter: " + counter.incrementAndGet());
    }

    @Idempotent(expireTime = 3)
    @PostMapping("/short-expire")
    public JsonResult<String> performShortExpireOperation() {
        return JsonResult.success("Short expire operation executed, counter: " + counter.incrementAndGet());
    }

    @Idempotent(key = "idempotent_different_request_1")
    @PostMapping("/different-request-1")
    public JsonResult<String> performDifferentRequest1() {
        return JsonResult.success("Different request 1 executed, counter: " + counter.incrementAndGet());
    }

    @Idempotent(key = "idempotent_different_request_2")
    @PostMapping("/different-request-2")
    public JsonResult<String> performDifferentRequest2() {
        return JsonResult.success("Different request 2 executed, counter: " + counter.incrementAndGet());
    }

    @GetMapping("/counter")
    public JsonResult<Integer> getCounter() {
        return JsonResult.success(counter.get());
    }

    @DeleteMapping("/counter")
    public JsonResult<Void> resetCounter() {
        counter.set(0);
        return JsonResult.success();
    }

    @PostMapping("/generate-unique-key")
    public JsonResult<String> generateUniqueKey() {
        return JsonResult.success(UUID.randomUUID().toString());
    }
}