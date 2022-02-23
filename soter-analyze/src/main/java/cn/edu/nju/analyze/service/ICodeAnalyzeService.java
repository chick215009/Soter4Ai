package cn.edu.nju.analyze.service;

import cn.edu.nju.analyze.domain.SummaryEntity;

public interface ICodeAnalyzeService {
    SummaryEntity getSummaryEntity(String localProjectPath);
}
