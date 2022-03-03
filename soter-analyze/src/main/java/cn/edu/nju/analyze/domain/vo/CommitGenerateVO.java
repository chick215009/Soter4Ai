package cn.edu.nju.analyze.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class CommitGenerateVO {
    String methodStatistics;
    String describe;
    String commitStereotype;
    String summaryEntityJson;
}
