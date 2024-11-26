package org.example.controller;

import jakarta.annotation.Resource;
import org.example.result.Result;
import org.example.service.SiteMessageService;
import org.example.vo.MessageVO;
import org.springframework.web.bind.annotation.*;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Resource
    private SiteMessageService siteMessageService;

    @GetMapping("/{mid}")
    public Result<MessageVO> getMessage(@PathVariable Long mid) {
        return Result.success(siteMessageService.getMessage(mid));
    }

    @PutMapping("/{mid}")
    public Result<Void> setUnread(@PathVariable Long mid) {
        siteMessageService.setUnread(mid);
        return Result.success();
    }

}
