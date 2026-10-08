package com.homeordering.controller;

import com.homeordering.common.Result;
import com.homeordering.dto.request.FeedbackRequest;
import com.homeordering.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public Result<String> submit(@RequestBody FeedbackRequest request) {
        feedbackService.submit(request == null ? null : request.getContent());
        return Result.success("已收到反馈");
    }
}
