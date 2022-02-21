package cn.edu.nju.core.model;

public class MethodModel {
    String qualifiedName;
    String name;
    String methodDocComment;
    String category;

    public MethodModel(String qualifiedName, String name, String methodDocComment, String category) {
        this.qualifiedName = qualifiedName;
        this.name = name;
        this.methodDocComment = methodDocComment;
        this.category = category;
    }
}
