package cn.edu.nju.analyze.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommitMessageVO {
    @JsonProperty("simpleDescribe")
    String simpleDescribe;
    @JsonProperty("detailDescribe")
    String detailDescribe;
}
