package cn.edu.nju.changescribe.service.impl;

import cn.edu.nju.changescribe.service.ICommitGenerateService;
import cn.edu.nju.changescribe.summarize.ChangeAnalyzer;
import org.springframework.stereotype.Service;

@Service
public class CommitGenerateServiceImpl implements ICommitGenerateService {
    @Override
    public String getGeneratedCommitMessage(String localProjectPath) {
        ChangeAnalyzer changeAnalyzer = new ChangeAnalyzer(localProjectPath);
        changeAnalyzer.analyze();
        return changeAnalyzer.getDescribe(changeAnalyzer.getSummaryEntity());
    }
}
