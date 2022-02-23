package cn.edu.nju.core.filter;

import lombok.AllArgsConstructor;
import lombok.Data;

/*
用来筛选对应的标签是否显示
 */
@Data
@AllArgsConstructor
public class LabelTypeFilter {
    private boolean isTypeLabel;
    private boolean isInterfaceList;
    private boolean isSuperclassStr;
    private boolean isTypeStereotypeLabel;
    private boolean isLocal;
    private boolean referencedList;
    private int referencedCount;



    public LabelTypeFilter() {
        this.isTypeLabel = true;
        this.isInterfaceList = true;
        this.isSuperclassStr = true;
        this.isTypeStereotypeLabel = true;
        this.isLocal = true;
        this.referencedList = true;
        this.referencedCount = 0;
    }
}
