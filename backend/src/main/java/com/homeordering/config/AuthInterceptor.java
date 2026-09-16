package com.homeordering.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homeordering.common.AuthContext;
import com.homeordering.common.BusinessException;
import com.homeordering.common.ErrorCode;
import com.homeordering.common.Result;
import com.homeordering.entity.User;
import com.homeordering.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

/**
 * 校验 Authorization = uuid_timestamp，写入 AuthContext
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            if (token == null || token.isBlank()) {
                throw new BusinessException(ErrorCode.NOT_LOGIN);
            }
            String uuid = parseUuid(token);
            if (uuid == null || uuid.isBlank()) {
                throw new BusinessException(ErrorCode.TOKEN_INVALID);
            }
            User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUuid, uuid));
            if (user == null) {
                throw new BusinessException(ErrorCode.USER_NOT_FOUND);
            }
            AuthContext.set(user);
            return true;
        } catch (BusinessException e) {
            writeError(response, e);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }

    private String parseUuid(String token) {
        int idx = token.lastIndexOf('_');
        if (idx <= 0) {
            return null;
        }
        return token.substring(0, idx);
    }

    private void writeError(HttpServletResponse response, BusinessException e) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), Result.error(e.getCode(), e.getMessage()));
    }
}
