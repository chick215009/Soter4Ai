package cn.edu.nju.analyze.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AnalyzeResult {
    private String selectKey;
    private String analyzeJSON;
}
