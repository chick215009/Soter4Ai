package cn.edu.nju.analyze.service.impl;

import cn.edu.nju.analyze.domain.SummaryEntity;
import cn.edu.nju.analyze.service.ICodeAnalyzeService;
import cn.edu.nju.analyze.summarize.ChangeAnalyzer;
import org.springframework.stereotype.Service;

@Service
public class CodeAnalyzeServiceImpl implements ICodeAnalyzeService {
    @Override
    public SummaryEntity getSummaryEntity(String localProjectPath) {
        ChangeAnalyzer changeAnalyzer = new ChangeAnalyzer(localProjectPath);
        changeAnalyzer.analyze();
        return changeAnalyzer.getSummaryEntity();
    }
}
