package cn.edu.nju.service;

public interface FileService {
    Boolean clearDirectory(String directoryPath);

    Boolean updateProject(String projectPath, String newProjectPath);
}
