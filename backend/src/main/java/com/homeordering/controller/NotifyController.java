package com.homeordering.controller;

import com.homeordering.common.Result;
import com.homeordering.config.WxSubscribeProperties;
import com.homeordering.dto.response.MakerSubscribeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notify")
@RequiredArgsConstructor
public class NotifyController {

    private final WxSubscribeProperties subscribeProperties;

    @GetMapping("/maker-subscribe")
    public Result<MakerSubscribeDTO> makerSubscribe() {
        MakerSubscribeDTO dto = new MakerSubscribeDTO();
        boolean enabled = subscribeProperties.enabled();
        dto.setEnabled(enabled);
        dto.setTemplateId(enabled ? subscribeProperties.getMakerTemplateId().trim() : "");
        return Result.success(dto);
    }
}
