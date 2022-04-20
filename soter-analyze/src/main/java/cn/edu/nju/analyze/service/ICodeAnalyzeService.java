package cn.edu.nju.analyze.service;

import cn.edu.nju.analyze.domain.SummaryEntity;
import cn.edu.nju.analyze.domain.vo.*;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.List;

public interface ICodeAnalyzeService {
    SummaryEntity getSummaryEntity(String localProjectPath);
    SummaryEntity getSummaryEntity(String localProjectPath, String username, String repoName);
//    CommitGenerateVO getCommitGenerateVO(String localProjectPath);
    List<GeneratedCommitVO> getCommitGeneratedHistoryByLocalProjectPath(String localProjectPath);
//    List<GeneratedCommitVO> getCommitGeneratedHistory();
    CommitGeneratedStatisticsVO getCommitGeneratedStatisticsVOByProjectPath(String localProjectPath);
    List<String> getProjectPathInHistory();
    String analyzeProjectInGithub(String username, String repoName);
    CommitGeneratedStatisticsVO analyzeProjectInGithub(String username, String repoName, List<String> tags);
    List<TagsVO> displayTags(String username, String repoName);
    List<String> getPathsByGithubStereotype(String username, String repoName, String stereotype);
    List<CommitInfoVO> getRecentCommit(String username, String repoName);

    List<CommitInfoVO> getRecentCommitBeforeTag(String username, String repoName, String tag);

    SummaryEntity compareCommit(String username, String repoName, String preCommit, String nextCommit) throws GitAPIException, IOException;
}
