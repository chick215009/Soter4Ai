package cn.edu.nju.analyze.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class GeneratedCommitVO {
    private Date generatedDate;
    private String localProjectPath;
    private String summaryEntityJSON;
}
