package cn.edu.nju.changescribe.domain.vo;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class FilterVo {
    @JsonProperty("newProjectPath")
    private String newProjectPath;
    @JsonProperty("projectPath")
    private String projectPath;

    @JsonProperty("isSimpleDescribe")
    private Boolean isSimpleDescribe;

    @JsonProperty("isDetailDescribe")
    private Boolean isDetailDescribe;

    @JsonProperty("isTypeLabel")
    private Boolean isTypeLabel;
    @JsonProperty("isInterfaceList")
    private Boolean isInterfaceList;
    @JsonProperty("isSuperclassStr")
    private Boolean isSuperclassStr;
    @JsonProperty("isTypeStereotypeLabel")
    private Boolean isTypeStereotypeLabel;
    @JsonProperty("isMethodStereotypeLabel")
    private Boolean isMethodStereotypeLabel;
    @JsonProperty("isLocal")
    private Boolean isLocal;
    @JsonProperty("referencedList")
    private Boolean referencedList;
    @JsonProperty("referencedCount")
    private Integer referencedCount;

    @JsonProperty("isNewModuleDescribe")
    private Boolean isNewModuleDescribe;

    @JsonProperty("filterTypeStereotypes")
    private List<String> filterTypeStereotypes;

    @JsonProperty("filterCategory")
    private List<String> filterCategory;
    @JsonProperty("filterMethodStereotype")
    private List<String> filterMethodStereotype;

    public FilterVo() {
        this.projectPath = "/Users/chengleming/work/projectDir";
        this.newProjectPath = "/Users/chengleming/Downloads/RuoYi-Vue-v3.7.0/ruoyi-system";
        this.isSimpleDescribe = true;
        this.isTypeLabel = true;
        this.isInterfaceList = true;
        this.isSuperclassStr = true;
        this.isTypeStereotypeLabel = true;
        this.isLocal = true;
        this.referencedList = true;
        this.referencedCount = 0;
        String[] strArray = new String[]{"ENTITY", "BOUNDARY"};
        this.filterTypeStereotypes = Arrays.asList(strArray);
        this.filterCategory = new ArrayList<>();
        this.filterMethodStereotype = new ArrayList<>();
    }


    public static void main(String[] args) {
        FilterVo filterVo = new FilterVo();

        System.out.println(JSON.toJSONString(filterVo));
    }
}
