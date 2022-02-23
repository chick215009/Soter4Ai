package cn.edu.nju.core.model;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class BasicAnalyzedFile {
//    private String sourcePath;//相对路径
    private String absolutePath;//绝对路径
//    private String name;//文件名
    private String projectPath;//项目路径

    private String packageName;//包名

    private List<ImportModel> imports;

    private List<CommentModel> comments;

    private List<ClassModel> classes;

    public BasicAnalyzedFile(String projectPath, String absolutePath) {
        this.projectPath = projectPath;
        this.absolutePath = absolutePath;

        this.imports = new LinkedList<>();
        this.comments = new LinkedList<>();
        this.classes = new LinkedList<>();
    }

}
