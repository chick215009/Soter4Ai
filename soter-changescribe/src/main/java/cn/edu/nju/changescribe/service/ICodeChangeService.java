package cn.edu.nju.changescribe.service;

import cn.edu.nju.changescribe.domain.ChangedFileStatistic;
import cn.edu.nju.changescribe.domain.MethodStatistic;

import java.util.List;

public interface ICodeChangeService {
    List<MethodStatistic> getMethodStatisticList(String projectPath);
    ChangedFileStatistic getChangedFileStatistic(String projectPath);

}
