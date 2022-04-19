package cn.edu.nju.service;

import cn.edu.nju.controller.vo.ChangedClassVO;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.List;

public interface CodeDiffService {
    List<ChangedClassVO> getChangedClasses(String projectPath) throws GitAPIException;
    List<String> getDiffProjectPaths(String classPath, String projectPath) throws IOException;

    List<String> getAdjacentProjects(String classPath, String projectPath) throws IOException;
}
