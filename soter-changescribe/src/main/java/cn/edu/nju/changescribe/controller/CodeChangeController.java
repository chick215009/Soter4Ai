package cn.edu.nju.changescribe.controller;

import cn.edu.nju.changescribe.service.ICodeChangeService;
import cn.edu.nju.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("code/change")
public class CodeChangeController {
    @Autowired
    private ICodeChangeService codeChangeService;

    @GetMapping(value = "/file")
    public AjaxResult getFileStatistic(@PathVariable String localProjectPath) {
        return AjaxResult.success(codeChangeService.getChangedFileStatistic(localProjectPath));
    }
}
