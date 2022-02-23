package cn.edu.nju.web.controller.commit;

import cn.edu.nju.analyze.domain.SummaryEntity;
import cn.edu.nju.analyze.service.ICodeAnalyzeService;
import cn.edu.nju.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/commit/generate")
public class CodeAnalyzeController {
    @Autowired
    ICodeAnalyzeService codeAnalyzeService;

    @GetMapping(value = "/analyze")
//    @PreAuthorize("@ss.hasPermi('commit:generate:analyze')")
    public AjaxResult generateCommit(String localProjectPath) {
        SummaryEntity summaryEntity = codeAnalyzeService.getSummaryEntity(localProjectPath);
        AjaxResult success = AjaxResult.success(summaryEntity);
        return success;
    }
}
