package cn.edu.nju.service;

import cn.edu.nju.controller.vo.AnalyzeResultVO;
import cn.edu.nju.controller.vo.CommitMessageVO;
import cn.edu.nju.core.filter.DetailDescribeFilter;
import cn.edu.nju.core.filter.SimpleDescribeFilter;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;

public interface CommitService {
    CommitMessageVO generateCommitMessage(String projectPath);
    String generateCommitMessageWithFilter(String projectPath, SimpleDescribeFilter simpleDescribeFilter, DetailDescribeFilter detailDescribeFilter) throws GitAPIException, IOException, ClassNotFoundException;
    AnalyzeResultVO generateAnalyzeResultVo(String projectPath,
                                            SimpleDescribeFilter simpleDescribeFilter,
                                            DetailDescribeFilter detailDescribeFilter) throws GitAPIException, IOException, ClassNotFoundException;
}
