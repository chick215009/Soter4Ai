package cn.edu.nju.core.filter;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DetailDescribeFilter {
    private boolean detailDescribe;
    private boolean newModuleDescribe;
    private LabelTypeFilter labelTypeFilter;
    private StereotypeTypeFilter stereotypeTypeFilter;
    private CategoryOrStereotypeMethodFilter categoryOrStereotypeMethodFilter;

    public DetailDescribeFilter() {
        this.newModuleDescribe = true;
        this.labelTypeFilter = new LabelTypeFilter();
        this.stereotypeTypeFilter = new StereotypeTypeFilter();
        this.categoryOrStereotypeMethodFilter = new CategoryOrStereotypeMethodFilter();
    }
}
