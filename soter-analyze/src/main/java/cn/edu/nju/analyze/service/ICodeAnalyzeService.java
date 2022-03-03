package cn.edu.nju.analyze.service;

import cn.edu.nju.analyze.domain.SummaryEntity;
import cn.edu.nju.analyze.domain.vo.CommitGenerateVO;
import cn.edu.nju.analyze.domain.vo.CommitGeneratedStatisticsVO;
import cn.edu.nju.analyze.domain.vo.GeneratedCommitVO;

import java.util.List;

public interface ICodeAnalyzeService {
    SummaryEntity getSummaryEntity(String localProjectPath);
    CommitGenerateVO getCommitGenerateVO(String localProjectPath);
    List<GeneratedCommitVO> getCommitGeneratedHistory();
    CommitGeneratedStatisticsVO getCommitGeneratedStatisticsVO();
}
