package cn.edu.nju.core.filter;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SimpleDescribeFilter {
    private boolean simpleDescribe;

    public SimpleDescribeFilter() {
        this.simpleDescribe = true;
    }
}
