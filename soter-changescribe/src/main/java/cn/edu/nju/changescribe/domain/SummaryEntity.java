package cn.edu.nju.changescribe.domain;

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
    private String commitStereotypeDescribe;
    private List<MethodStatistic> methodStatisticList;
    private String methodStatisticJson;

    public SummaryEntity() {
        this.simpleDescribe = "";
        this.newModuleDescribe = "";
        this.packageEntityList = new ArrayList<>();
        this.isInitialCommit = false;
        this.properties = new ArrayList<>();
        this.commitStereotype = "";
        this.commitStereotypeDescribe = "";
        this.methodStatisticList = new ArrayList<>();
        this.methodStatisticJson = "";
    }
}