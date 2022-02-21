package cn.edu.nju.core.filter;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;


@Data
@AllArgsConstructor
public class StereotypeTypeFilter {
    Set<String> filterStereotype;

    public StereotypeTypeFilter() {
        this.filterStereotype = new HashSet<>();
    }
}
