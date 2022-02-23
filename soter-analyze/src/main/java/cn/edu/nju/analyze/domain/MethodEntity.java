package cn.edu.nju.analyze.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class MethodEntity {
    String methodCategoryLabel;
    String methodStereotypeLabel;
    String phrase;
}
