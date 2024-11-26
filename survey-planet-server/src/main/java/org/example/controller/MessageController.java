package org.example.controller;

import jakarta.annotation.Resource;
import org.example.entity.message.Message;
import org.example.entity.message.MessageType;
import org.example.result.Result;
import org.example.service.SiteMessageService;
import org.example.vo.MessageVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public Result<Void> setReadState(@PathVariable Long mid, @RequestParam(defaultValue = "false") Boolean isRead) {
        if (isRead) {
            siteMessageService.setRead(mid);
        } else {
            siteMessageService.setUnread(mid);
        }
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<Message>> getMessages(@RequestParam(defaultValue = "") Boolean isRead, @RequestParam(defaultValue = "")MessageType type) {
        return Result.success(siteMessageService.getMessages(isRead, type));
    }

}
