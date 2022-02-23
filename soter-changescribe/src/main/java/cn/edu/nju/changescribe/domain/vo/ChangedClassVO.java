package cn.edu.nju.changescribe.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChangedClassVO {
    @JsonProperty("className")
    private String className;
    @JsonProperty("classPath")
    private String classPath;
}
