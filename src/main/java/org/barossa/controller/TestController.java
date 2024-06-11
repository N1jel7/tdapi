package org.barossa.controller;

import lombok.RequiredArgsConstructor;
import org.barossa.service.TelegramService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {
    private final TelegramService telegramService;

    @GetMapping
    void test() {
        telegramService.login("375292874071");
    }
}
