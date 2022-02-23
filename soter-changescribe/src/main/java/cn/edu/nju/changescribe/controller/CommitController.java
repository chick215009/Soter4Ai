package cn.edu.nju.changescribe.controller;

import cn.edu.nju.changescribe.service.ICommitGenerateService;
import cn.edu.nju.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("commit")
public class CommitController {
    @Autowired
    private ICommitGenerateService commitGenerateService;

    @GetMapping(value = "/generate")
    public AjaxResult generateCommit(@PathVariable String localProjectPath) {
        return AjaxResult.success(commitGenerateService.getGeneratedCommitMessage(localProjectPath));
    }
}
