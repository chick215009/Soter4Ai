package cn.edu.nju.changescribe.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FileEntity {
    private String operation;
    private Boolean isModified;
    private String fileName;
    private String changeDescribe;
    private List<TypeEntity> typeEntityList;

    public FileEntity(String operation, Boolean isModified, String fileName) {
        this.operation = operation;
        this.isModified = isModified;
        this.fileName = fileName;
        this.changeDescribe = "";
        this.typeEntityList = new ArrayList<>();
    }
}
