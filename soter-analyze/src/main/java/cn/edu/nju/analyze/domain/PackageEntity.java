package cn.edu.nju.analyze.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Data
public class PackageEntity {
    private String packageName;
    private List<FileEntity> FileEntityList;
    private List<TypeEntity> typeEntityList;

    public PackageEntity() {
        this.packageName = "";
        this.typeEntityList = new ArrayList<>();
        this.FileEntityList = new ArrayList<>();
    }
}
