package cn.edu.nju.analyze.service.impl;

import cn.edu.nju.analyze.domain.vo.ChangedClassVO;
import cn.edu.nju.analyze.service.CodeDiffService;
import cn.edu.nju.core.git.ChangedFile;
import cn.edu.nju.core.git.SCMRepository;
import cn.edu.nju.core.utils.Utils;
import lombok.extern.log4j.Log4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import cn.edu.nju.core.git.ChangedFile.TypeChange;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


//@Log4j
@Service
public class CodeDiffServiceImpl implements CodeDiffService {
    @Override
    public List<ChangedClassVO> getChangedClasses(String projectPath) throws GitAPIException {
        List<ChangedClassVO> changedClasses = new ArrayList<>();

        SCMRepository scmRepository = new SCMRepository(projectPath);
        Git git = scmRepository.getGit();
        Status status = scmRepository.getStatus();
        Set<ChangedFile> differences = SCMRepository.getDifferences(status, projectPath);
        for (ChangedFile changedFile : differences) {
            if (changedFile.getChangeType().equals(TypeChange.MODIFIED.name())) {
                changedClasses.add(new ChangedClassVO(changedFile.getName(), changedFile.getAbsolutePath()));
            }
        }
        return changedClasses;
    }

    @Override
    public List<String> getDiffProjectPaths(String classPath, String projectPath) throws IOException {
        List<String> res = new ArrayList<>();
        SCMRepository scmRepository = new SCMRepository(projectPath);
        Git git = scmRepository.getGit();
        String path = "";
        if (classPath.startsWith(projectPath)) {
            path = classPath.substring(projectPath.length() + 1);
        } else {
            return res;
        }
        File oldFile = Utils.getFileContentOfLastCommit(path, git.getRepository());
        res.add(oldFile.getAbsolutePath());
        res.add(classPath);
        return res;
    }

    @Override
    public List<String> getAdjacentProjects(String classPath, String projectPath) throws IOException {
        List<String> projects = new ArrayList<>();
        List<String> diffProjectPaths = getDiffProjectPaths(classPath, projectPath);
        String oldStr = new String(Files.readAllBytes(Paths.get(diffProjectPaths.get(0))));
        String newStr = new String(Files.readAllBytes(Paths.get(diffProjectPaths.get(1))));

        projects.add(oldStr);
        projects.add(newStr);

        return projects;
    }


}
