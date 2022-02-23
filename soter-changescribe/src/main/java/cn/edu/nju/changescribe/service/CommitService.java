package cn.edu.nju.changescribe.service;

import cn.edu.nju.changescribe.domain.vo.AnalyzeResultVO;
import cn.edu.nju.changescribe.domain.vo.CommitMessageVO;
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
