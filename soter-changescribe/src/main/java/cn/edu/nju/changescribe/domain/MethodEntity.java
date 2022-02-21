package cn.edu.nju.changescribe.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class MethodEntity {
    String methodCategoryLabel;
    String methodStereotypeLabel;
    String phrase;
}
