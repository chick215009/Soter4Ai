package cn.edu.nju.analyze.service.impl;


import cn.edu.nju.analyze.service.FileService;
import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

//@Service
//@Log4j
public class FileServiceImpl implements FileService {
    @Override
    public Boolean clearDirectory(String directoryPath) {
        try {
            File file = new File(directoryPath);
            for (File listFile : file.listFiles()) {
                delFile(listFile);
            }
        } catch (Exception e) {
//            log.error(e.getMessage());
            return false;
        }
        System.out.println("清空文件夹");

        return true;
    }

    @Override
    public Boolean updateProject(String projectPath, String newProjectPath) {
        File projectFile = new File(projectPath);

        //清空除了 .git 文件以外的所有文件
        for (File file : projectFile.listFiles()) {
            if (file.getName().equals(".git")) {
                continue;
            } else {
                delFile(file);
            }
        }

        try {
            FileUtils.copyDirectory(new File(newProjectPath), new File(projectPath), new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return !(pathname.getName().equals(".git"));
                }
            });
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private void delFile(File file) {
        if (!file.exists()) {
            return;
        }

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                delFile(f);
            }
        }
        file.delete();
    }


}
