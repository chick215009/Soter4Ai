package cn.edu.nju.analyze.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Data
public class SummaryEntity {
    private String simpleDescribe;
    private String newModuleDescribe;
    private List<PackageEntity> packageEntityList;
    private Boolean isInitialCommit;
    private List<String> properties;
    private String commitStereotype;
    private String methodStatisticJson;

    private Integer fileNum;
    private Integer addNum;
    private Integer removeNum;
    private Integer changedNum;



    public SummaryEntity() {
        this.simpleDescribe = "";
        this.newModuleDescribe = "";
        this.packageEntityList = new ArrayList<>();
        this.isInitialCommit = false;
        this.properties = new ArrayList<>();
        this.commitStereotype = "";
        this.methodStatisticJson = "";
        fileNum = 0;
        addNum = 0;
        removeNum = 0;
        changedNum = 0;
    }
}
