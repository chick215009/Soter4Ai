package cn.edu.nju.core.model;

import lombok.Data;
import lombok.extern.log4j.Log4j;

import java.util.LinkedList;
import java.util.List;

//@Log4j
@Data
public class ClassModel {
    String qualifiedName;
    String typeDocComment;
    String primaryStereotypeName;
    int referencesCount;

    List<FieldModel> fields;
    List<MethodModel> methods;
    List<ClassModel> classes;

    public ClassModel(String qualifiedName, String typeDocComment, String primaryStereotypeName) {
        this.qualifiedName = qualifiedName;
        this.typeDocComment = typeDocComment;
        this.primaryStereotypeName = primaryStereotypeName;
        fields = new LinkedList<>();
        methods = new LinkedList<>();
        classes = new LinkedList<>();
    }

}
