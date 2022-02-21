package cn.edu.nju.core.model;

import lombok.Data;

@Data
public class ImportModel {
    private String importName;
    private String className;

    public ImportModel(String importName) {
        this.importName = importName;
        this.className = importName.substring(importName.lastIndexOf('.') + 1);
    }
}
