package com.minsang.study.api;

import com.minsang.study.common.HelloMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello(@RequestParam(defaultValue = "world") String name) {
        return HelloMessage.greet(name);
    }

    @GetMapping("/bye")
    public String bye(@RequestParam(defaultValue = "world") String name) {
        return HelloMessage.bye(name);
    }
}
