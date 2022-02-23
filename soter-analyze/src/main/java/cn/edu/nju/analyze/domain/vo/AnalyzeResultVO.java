package cn.edu.nju.analyze.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AnalyzeResultVO {
    @JsonProperty("addedCount")
    private Integer addedCount;
    @JsonProperty("removedCount")
    private Integer removedCount;
    @JsonProperty("changedCount")
    private Integer changedCount;
    @JsonProperty("projectName")
    private String projectName;
    @JsonProperty("commitStereotype")
    private String commitStereotype;
    @JsonProperty("simpleDescribe")
    private String simpleDescribe;
    @JsonProperty("detailDescribe")
    private String detailDescribe;
    @JsonProperty("methodStatisticJson")
    private String methodStatisticJson;

    public AnalyzeResultVO() {
        this.addedCount = 0;
        this.removedCount = 0;
        this.changedCount = 0;

        this.projectName = "";
        this.commitStereotype = "未定义";

        this.simpleDescribe = "";
        this.detailDescribe = "";
    }
}
