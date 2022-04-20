package cn.edu.nju.web.controller.commit;

import cn.edu.nju.analyze.domain.SummaryEntity;
import cn.edu.nju.analyze.domain.request.HistoryDetailRequest;
import cn.edu.nju.analyze.domain.vo.CommitGenerateVO;
import cn.edu.nju.analyze.domain.vo.GeneratedCommitVO;
import cn.edu.nju.analyze.domain.vo.SelectedProjectVO;
import cn.edu.nju.analyze.service.ICodeAnalyzeService;
import cn.edu.nju.analyze.summarize.ChangeAnalyzer;
import cn.edu.nju.common.constant.HttpStatus;
import cn.edu.nju.common.core.domain.AjaxResult;
import cn.edu.nju.common.core.page.PageDomain;
import cn.edu.nju.common.core.page.TableDataInfo;
import cn.edu.nju.common.core.page.TableSupport;
import cn.edu.nju.common.core.redis.RedisCache;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.ParserConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/commit/generate")
public class CodeAnalyzeController {
    @Autowired
    ICodeAnalyzeService codeAnalyzeService;

    @Autowired
    private RedisCache redisCache;

    @GetMapping(value = "/analyze")
    @PreAuthorize("@ss.hasPermi('commit:generate:analyze')")
    public AjaxResult generateCommit(String localProjectPath) {
        SummaryEntity summaryEntity = codeAnalyzeService.getSummaryEntity(localProjectPath);
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

//        try {
//            if (redisCache.hasKey("commitGeneratedHistory")) {
//                List<GeneratedCommitVO> commitGeneratedHistory = redisCache.getCacheObject("commitGeneratedHistory");
//                commitGeneratedHistory.add(0, new GeneratedCommitVO(new Date(System.currentTimeMillis()), localProjectPath, summaryEntityJSON));
//                redisCache.deleteObject("commitGeneratedHistory");
//                redisCache.setCacheObject("commitGeneratedHistory", commitGeneratedHistory);
//            } else {
//                List<GeneratedCommitVO> commitGeneratedHistory = new ArrayList<>();
//                commitGeneratedHistory.add(0, new GeneratedCommitVO(new Date(System.currentTimeMillis()), localProjectPath, summaryEntityJSON));
//                redisCache.setCacheObject("commitGeneratedHistory", commitGeneratedHistory);
//            }
//        }catch (JSONException e) {
////            e.printStackTrace();
//        }


        return success;
    }

    @GetMapping(value = "/statistics")
    @PreAuthorize("@ss.hasPermi('commit:generate:statistics')")
    public AjaxResult getChangedFileStatistics(String localProjectPath) {
        AjaxResult success = AjaxResult.success();
        success.put("fileNum", 333);
        success.put("addNum", 2);
        success.put("removeNum", 3);
        success.put("changedNum", 67);

        return success;
    }

    @GetMapping(value = "/history")
    @PreAuthorize("@ss.hasPermi('commit:generate:history')")
    public TableDataInfo getCommitGeneratedHistory() {
        try {
//            List<GeneratedCommitVO> commitGeneratedHistory = new ArrayList<>();
//            if (redisCache.hasKey("commitGeneratedHistory")) {
//                commitGeneratedHistory = redisCache.getCacheObject("commitGeneratedHistory");
//            }
            List<GeneratedCommitVO> commitGeneratedHistory = codeAnalyzeService.getCommitGeneratedHistoryByLocalProjectPath("");
            TableDataInfo rspData = new TableDataInfo();
            rspData.setRows(commitGeneratedHistory);
            rspData.setCode(HttpStatus.SUCCESS);
            rspData.setMsg("成功");
            return rspData;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new TableDataInfo();

    }

    @RequestMapping(value = "/detail")
    @PreAuthorize("@ss.hasPermi('commit:generate:detail')")
    public AjaxResult getHistoryDetail(@RequestBody HistoryDetailRequest request) {
        SummaryEntity summaryEntity = JSON.parseObject(request.getSummaryEntityJSON(), SummaryEntity.class);
        AjaxResult success = AjaxResult.success();
        String describe = ChangeAnalyzer.getDescribeHTML(summaryEntity);

        success.put("methodStatistics", summaryEntity.getMethodStatisticJson());
        success.put("describe", describe);
        success.put("commitStereotype", summaryEntity.getCommitStereotype());

        success.put("fileNum", summaryEntity.getFileNum());
        success.put("addNum", summaryEntity.getAddNum());
        success.put("removeNum", summaryEntity.getRemoveNum());
        success.put("changedNum", summaryEntity.getChangedNum());
        return success;
    }

    @RequestMapping(value = "/project_list")
    @PreAuthorize("@ss.hasPermi('commit:generate:history')")
    public AjaxResult getProjectList() {
        List<String> paths = codeAnalyzeService.getProjectPathInHistory();
        List<SelectedProjectVO> res = new ArrayList<>();
        for (int i = 0; i < paths.size(); i++) {
            res.add(new SelectedProjectVO(paths.get(i), paths.get(i)));
        }

        AjaxResult success = AjaxResult.success();
        success.put("paths", res);
        return success;
    }

    @GetMapping(value = "/project_history")
    @PreAuthorize("@ss.hasPermi('commit:generate:history')")
    public TableDataInfo getCommitGeneratedProjectHistory(String path) {
        try {
//            List<GeneratedCommitVO> commitGeneratedHistory = new ArrayList<>();
//            if (redisCache.hasKey("commitGeneratedHistory")) {
//                commitGeneratedHistory = redisCache.getCacheObject("commitGeneratedHistory");
//            }
            List<GeneratedCommitVO> commitGeneratedHistory = codeAnalyzeService.getCommitGeneratedHistoryByLocalProjectPath(path);
            TableDataInfo rspData = new TableDataInfo();
            rspData.setRows(commitGeneratedHistory);
            rspData.setCode(HttpStatus.SUCCESS);
            rspData.setMsg("成功");
            return rspData;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new TableDataInfo();

    }

    @GetMapping(value = "/content")
    @PreAuthorize("@ss.hasPermi('commit:generate:analyze')")
    public AjaxResult getFileContent(String localProjectPath) {
//        localProjectPath = "/Users/chengleming/MasterThesis/Soter/soter-admin/src/main/java/cn/edu/nju/web/controller/commit/CodeAnalyzeController.java";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(localProjectPath));
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            String ls = System.getProperty("line.separator");
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }
// 删除最后一个新行分隔符
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            reader.close();

            String content = stringBuilder.toString();
            return AjaxResult.success(content);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return AjaxResult.error();


    }

    @GetMapping(value = "/files")
    @PreAuthorize("@ss.hasPermi('commit:generate:analyze')")
    public AjaxResult getFileList(String username, String repoName, String stereoType, String[] tags) {
        List<String> paths = codeAnalyzeService.getPathsByGithubStereotype(username, repoName, stereoType);
        return AjaxResult.success(paths);
    }
}

