package cn.edu.nju.changescribe.service.impl;

import cn.edu.nju.changescribe.domain.SummaryEntity;
import cn.edu.nju.changescribe.domain.vo.AnalyzeResultVO;
import cn.edu.nju.core.filter.DetailDescribeFilter;
import cn.edu.nju.core.filter.SimpleDescribeFilter;
import cn.edu.nju.core.git.ChangedFile;
import cn.edu.nju.core.git.SCMRepository;
import cn.edu.nju.core.summarizer.SummarizeChanges;
import cn.edu.nju.changescribe.service.CommitService;
import cn.edu.nju.changescribe.domain.vo.CommitMessageVO;
import lombok.extern.log4j.Log4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Set;

@Log4j
@Service
public class CommitServiceImpl implements CommitService {
    @Override
    public CommitMessageVO generateCommitMessage(String projectPath) {
        try {
            SCMRepository scmRepository = new SCMRepository(projectPath);
            Git git = scmRepository.getGit();
            Status status = scmRepository.getStatus();
            Set<ChangedFile> differences = SCMRepository.getDifferences(status, projectPath);

            SummarizeChanges summarizeChanges = new SummarizeChanges(git, projectPath);
            ChangedFile[] changedFiles = new ChangedFile[differences.size()];
            summarizeChanges.summarize(differences.toArray(changedFiles));


            CommitMessageVO commitMessageVO = new CommitMessageVO(summarizeChanges.getSimpleDescribe().toString(),
                    summarizeChanges.getDetailDescribe().toString());
            System.out.println(commitMessageVO.getSimpleDescribe());
            System.out.println(commitMessageVO.getDetailDescribe());
            return commitMessageVO;
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    @Override
    public String generateCommitMessageWithFilter(String projectPath, SimpleDescribeFilter simpleDescribeFilter, DetailDescribeFilter detailDescribeFilter) throws GitAPIException, IOException, ClassNotFoundException {
        SCMRepository scmRepository = new SCMRepository(projectPath);
        Git git = scmRepository.getGit();
        Status status = scmRepository.getStatus();
        Set<ChangedFile> differences = SCMRepository.getDifferences(status, projectPath);

        SummarizeChanges summarizeChanges = new SummarizeChanges(git, projectPath);
        ChangedFile[] changedFiles = new ChangedFile[differences.size()];
        summarizeChanges.summarize2(differences.toArray(changedFiles), simpleDescribeFilter, detailDescribeFilter);
        return summarizeChanges.getSummary();
    }

    @Override
    public AnalyzeResultVO generateAnalyzeResultVo(String projectPath, SimpleDescribeFilter simpleDescribeFilter, DetailDescribeFilter detailDescribeFilter) throws GitAPIException, IOException, ClassNotFoundException {
        SCMRepository scmRepository = new SCMRepository(projectPath);
        Git git = scmRepository.getGit();
        Status status = scmRepository.getStatus();
        Set<ChangedFile> differences = SCMRepository.getDifferences(status, projectPath);

        SummarizeChanges summarizeChanges = new SummarizeChanges(git, projectPath);
        ChangedFile[] changedFiles = new ChangedFile[differences.size()];
        summarizeChanges.summarize2(differences.toArray(changedFiles), simpleDescribeFilter, detailDescribeFilter);
        return summarizeChanges.getAnalyzeResultVO();
    }

    public static void main(String[] args) {
        String projectPath = "/Users/chengleming/work/projectDir";
        SCMRepository scmRepository = new SCMRepository("/Users/chengleming/work/projectDir");
        Git git = scmRepository.getGit();
        try {
            Status status = scmRepository.getStatus();
            Set<ChangedFile> differences = SCMRepository.getDifferences(status, projectPath);
            SummarizeChanges summarizeChanges = new SummarizeChanges(git, projectPath);
            ChangedFile[] changedFiles = new ChangedFile[differences.size()];
            summarizeChanges.summarize3(differences.toArray(changedFiles));
            SummaryEntity summaryEntity = summarizeChanges.getSummaryEntity();
            System.out.println(summaryEntity);
        } catch (GitAPIException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
