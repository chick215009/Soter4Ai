package cn.edu.nju.web.controller.commit;

import cn.edu.nju.analyze.domain.vo.CommitGeneratedStatisticsVO;
import cn.edu.nju.analyze.service.ICodeAnalyzeService;
import cn.edu.nju.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/commit")
public class HostController {
    @Autowired
    ICodeAnalyzeService codeAnalyzeService;

    @GetMapping(value="/host")
    @PreAuthorize("@ss.hasPermi('commit:generate:analyze')")
    public AjaxResult getGeneratedCommitStatistics(String localProjectPath) {
        CommitGeneratedStatisticsVO statisticsVO = codeAnalyzeService.getCommitGeneratedStatisticsVO();
        AjaxResult success = AjaxResult.success(statisticsVO);
        return success;
    }
}
