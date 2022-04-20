package cn.edu.nju.web.controller.github;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import cn.edu.nju.analyze.domain.SummaryEntity;
import cn.edu.nju.analyze.domain.request.AnalyzeRequest;
import cn.edu.nju.analyze.domain.request.CompareCommitRequest;
import cn.edu.nju.analyze.domain.vo.CommitInfoVO;
import cn.edu.nju.analyze.service.ICodeAnalyzeService;
import cn.edu.nju.analyze.summarize.ChangeAnalyzer;
import cn.edu.nju.common.constant.HttpStatus;
import cn.edu.nju.common.core.page.PageDomain;
import cn.edu.nju.common.core.page.TableSupport;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cn.edu.nju.common.annotation.Log;
import cn.edu.nju.common.core.controller.BaseController;
import cn.edu.nju.common.core.domain.AjaxResult;
import cn.edu.nju.common.enums.BusinessType;
import cn.edu.nju.github.domain.GitCommit;
import cn.edu.nju.github.service.IGitCommitService;
import cn.edu.nju.common.utils.poi.ExcelUtil;
import cn.edu.nju.common.core.page.TableDataInfo;

/**
 * git commit信息Controller
 *
 * @author clm
 * @date 2022-02-14
 */
@RestController
@RequestMapping("/github/commit")
public class GitCommitController extends BaseController
{
    @Autowired
    private IGitCommitService gitCommitService;

    @Autowired
    private ICodeAnalyzeService codeAnalyzeService;

    /**
     * 查询git commit信息列表
     */
    @PreAuthorize("@ss.hasPermi('github:commit:list')")
    @GetMapping("/list")
    public TableDataInfo list(GitCommit gitCommit)
    {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        ArrayList<GitCommit> list = gitCommitService.selectGitCommitList(gitCommit);
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(HttpStatus.SUCCESS);
        rspData.setMsg("查询成功");

        int total = list.size();
        if (total > pageSize) {
            int toIndex = pageSize * pageNum;
            if (toIndex > total) {
                toIndex = total;
            }
            rspData.setRows(list.subList(pageSize * (pageNum - 1), toIndex));
        } else {
            rspData.setRows(list);
        }

        rspData.setTotal(list.size());
        return rspData;
    }

    /**
     * 查询git commit信息列表
     */
    @PreAuthorize("@ss.hasPermi('github:commit:list')")
    @GetMapping("/recent")
    public TableDataInfo getRecentlist(String username, String repoName)
    {
        List<CommitInfoVO> recentCommit = codeAnalyzeService.getRecentCommit(username, repoName);
        TableDataInfo tableDataInfo = new TableDataInfo();
        tableDataInfo.setCode(HttpStatus.SUCCESS);
        tableDataInfo.setMsg("查询成功");

        tableDataInfo.setRows(recentCommit);
        return tableDataInfo;
    }

    /**
     * 查询git commit信息列表
     */
    @PreAuthorize("@ss.hasPermi('github:commit:list')")
    @GetMapping("/intag")
    public TableDataInfo getCommitInTag(String username, String repoName, String tag)
    {
        System.out.println(username + repoName + tag);
        List<CommitInfoVO> recentCommit = codeAnalyzeService.getRecentCommitBeforeTag(username, repoName, tag);
        TableDataInfo tableDataInfo = new TableDataInfo();
        tableDataInfo.setCode(HttpStatus.SUCCESS);
        tableDataInfo.setMsg("查询成功");

        tableDataInfo.setRows(recentCommit);
        return tableDataInfo;
    }

    /**
     * 查询git commit信息列表
     */
    @PreAuthorize("@ss.hasPermi('github:commit:list')")
    @PostMapping("/compare")
    public AjaxResult compareCommit(@RequestBody CompareCommitRequest request) throws GitAPIException, IOException {
        SummaryEntity summaryEntity = codeAnalyzeService.compareCommit(request.getUsername(), request.getRepoName(), request.getPreCommit(), request.getNextCommit());
        String describe = ChangeAnalyzer.getDescribeHTML(summaryEntity);
        String summaryEntityJSON = JSON.toJSONString(summaryEntity);


        AjaxResult success = AjaxResult.success();
        success.put("methodStatistics", summaryEntity.getMethodStatisticJson());
        success.put("describe", describe);
        success.put("commitStereotype", summaryEntity.getCommitStereotype());

        success.put("fileNum", summaryEntity.getFileNum());
        success.put("addNum", summaryEntity.getAddNum());
        success.put("removeNum", summaryEntity.getRemoveNum());
        success.put("changedNum", summaryEntity.getChangedNum());
         return success;
    }





    /**
     * 导出git commit信息列表
     */
    @PreAuthorize("@ss.hasPermi('github:commit:export')")
    @Log(title = "git commit信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, GitCommit gitCommit)
    {
        List<GitCommit> list = gitCommitService.selectGitCommitList(gitCommit);
        ExcelUtil<GitCommit> util = new ExcelUtil<GitCommit>(GitCommit.class);
        util.exportExcel(response, list, "git commit信息数据");
    }

    /**
     * 获取git commit信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('github:commit:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(gitCommitService.selectGitCommitById(id));
    }

    /**
     * 新增git commit信息
     */
    @PreAuthorize("@ss.hasPermi('github:commit:add')")
    @Log(title = "git commit信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody GitCommit gitCommit)
    {
        return toAjax(gitCommitService.insertGitCommit(gitCommit));
    }

    /**
     * 修改git commit信息
     */
    @PreAuthorize("@ss.hasPermi('github:commit:edit')")
    @Log(title = "git commit信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody GitCommit gitCommit)
    {
        return toAjax(gitCommitService.updateGitCommit(gitCommit));
    }

    /**
     * 删除git commit信息
     */
    @PreAuthorize("@ss.hasPermi('github:commit:remove')")
    @Log(title = "git commit信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(gitCommitService.deleteGitCommitByIds(ids));
    }

    @PostMapping(value = "/analyze")
    @PreAuthorize("@ss.hasPermi('commit:generate:analyze')")
    public AjaxResult analyzeGithubProject(@RequestBody AnalyzeRequest request) {
        if (request.getTags() == null) {
            String res = codeAnalyzeService.analyzeProjectInGithub(request.getUsername(), request.getRepoName());
            return AjaxResult.success(res);
        } else {
            List<String> tagList = new ArrayList<>();
            for (String tag : request.getTags()) {
                tagList.add(tag);
            }
            return AjaxResult.success(codeAnalyzeService.analyzeProjectInGithub(request.getUsername(), request.getRepoName(), tagList));
        }


    }

    @GetMapping(value = "/tags")
    @PreAuthorize("@ss.hasPermi('commit:generate:analyze')")
    public AjaxResult analyzeDisplayProjectTgas(String username, String repoName) {
        return AjaxResult.success(codeAnalyzeService.displayTags(username, repoName));
    }
}
