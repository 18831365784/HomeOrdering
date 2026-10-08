package com.homeordering.service.impl;

import com.homeordering.common.AuthContext;
import com.homeordering.common.BusinessException;
import com.homeordering.common.ErrorCode;
import com.homeordering.entity.Feedback;
import com.homeordering.entity.User;
import com.homeordering.mapper.FeedbackMapper;
import com.homeordering.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackMapper feedbackMapper;

    @Override
    public void submit(String content) {
        User user = AuthContext.requireUser();
        if (content == null || content.isBlank()) {
            throw new BusinessException(ErrorCode.PARAM_INVALID.getCode(), "请填写问题描述");
        }
        String text = content.trim();
        if (text.length() > 1000) {
            throw new BusinessException(ErrorCode.PARAM_INVALID.getCode(), "问题描述不能超过1000字");
        }
        Feedback feedback = new Feedback();
        feedback.setUserUuid(user.getUuid());
        feedback.setNickname(user.getNickname());
        feedback.setFamilyId(user.getFamilyId());
        feedback.setContent(text);
        feedback.setCreateTime(LocalDateTime.now());
        feedbackMapper.insert(feedback);
    }
}
