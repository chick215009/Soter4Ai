package cn.edu.nju.github.controller;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import cn.edu.nju.common.constant.HttpStatus;
import cn.edu.nju.common.core.page.PageDomain;
import cn.edu.nju.common.core.page.TableSupport;
import com.github.pagehelper.PageInfo;
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
}
