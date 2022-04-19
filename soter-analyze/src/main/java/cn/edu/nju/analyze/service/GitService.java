package cn.edu.nju.service;

public interface GitService {
    Boolean gitInit(String projectPath);

    Boolean gitAddAll(String projectPath);

    Boolean gitCommit(String projectPath, String message);
}
