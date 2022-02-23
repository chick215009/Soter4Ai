package cn.edu.nju.core.filter;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
public class CategoryOrStereotypeMethodFilter {
    Set<String> filterCategory;
    Set<String> filterStereotype;
    Boolean methodStereotypeLabel;

    public CategoryOrStereotypeMethodFilter() {
        this.filterCategory = new HashSet<>();
        this.filterStereotype = new HashSet<>();
        this.methodStereotypeLabel = true;
    }
}
