package cn.edu.nju.analyze.service;

public interface GitService {
    Boolean gitInit(String projectPath);

    Boolean gitAddAll(String projectPath);

    Boolean gitCommit(String projectPath, String message);
}
