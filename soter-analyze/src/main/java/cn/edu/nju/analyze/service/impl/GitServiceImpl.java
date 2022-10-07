package cn.edu.nju.analyze.service.impl;

import cn.edu.nju.analyze.service.GitService;
import cn.edu.nju.core.git.SCMRepository;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Service;

import java.io.File;

//@Service
public class GitServiceImpl implements GitService {
    @Override
    public Boolean gitInit(String projectPath) {
        File projectFile = new File(projectPath);
        try {
            Git.init().setDirectory(projectFile).call();
        } catch (GitAPIException e) {
            return false;
        }
        return true;
    }

    @Override
    public Boolean gitAddAll(String projectPath) {
        SCMRepository scmRepository = new SCMRepository(projectPath);
        Git git = scmRepository.getGit();
        try {
            git.add().addFilepattern(".").call();
        } catch (GitAPIException e) {
            return false;
        }
        return true;
    }

    @Override
    public Boolean gitCommit(String projectPath, String message) {
        SCMRepository scmRepository = new SCMRepository(projectPath);
        Git git = scmRepository.getGit();
        try {
            git.commit().setMessage(message).call();
        } catch (GitAPIException e) {
            return false;
        }

        return true;
    }
}
