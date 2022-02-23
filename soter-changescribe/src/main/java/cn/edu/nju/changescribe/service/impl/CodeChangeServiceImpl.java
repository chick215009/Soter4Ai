package cn.edu.nju.changescribe.service.impl;

import cn.edu.nju.changescribe.domain.ChangedFileStatistic;
import cn.edu.nju.changescribe.domain.MethodStatistic;
import cn.edu.nju.changescribe.service.ICodeChangeService;
import cn.edu.nju.common.utils.FileCounter;
import cn.edu.nju.core.Constants;
import cn.edu.nju.core.git.ChangedFile;
import cn.edu.nju.core.git.SCMRepository;
import cn.edu.nju.core.stereotype.stereotyped.StereotypeIdentifier;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CodeChangeServiceImpl implements ICodeChangeService {
    @Override
    public List<MethodStatistic> getMethodStatisticList(String projectPath) {
        return null;
    }

    @Override
    public ChangedFileStatistic getChangedFileStatistic(String projectPath) {
        ChangedFileStatistic changedFileStatistic = new ChangedFileStatistic();
        try {
            FileCounter fileCounter = new FileCounter(projectPath);
            fileCounter.searchFiles();
            changedFileStatistic.setTotalFileNum(fileCounter.getFileList().size());

            SCMRepository scmRepository = new SCMRepository(projectPath);
            Git git = scmRepository.getGit();
            Status status = scmRepository.getStatus();
            Set<ChangedFile> differences = SCMRepository.getDifferences(status, projectPath);

            for (ChangedFile changedFile : differences) {
                String changeType = changedFile.getChangeType();
                if (changeType.equals(ChangedFile.TypeChange.ADDED.name())) {
                    changedFileStatistic.setAddedFileNum(changedFileStatistic.getAddedFileNum() + 1);
                }else if (changeType.equals(ChangedFile.TypeChange.REMOVED.name())) {
                    changedFileStatistic.setRemovedFileNum(changedFileStatistic.getRemovedFileNum() + 1);
                } else if (changeType.equals(ChangedFile.TypeChange.MODIFIED.name())) {
                    changedFileStatistic.setChangedFileNum(changedFileStatistic.getChangedFileNum() + 1);
                }
            }
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return changedFileStatistic;
    }


}
