package cn.edu.nju.analyze.domain;


import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class MethodStatistic {
    private String methodStereotype;
    private Integer num;

    public MethodStatistic() {
        this.methodStereotype = "";
        this.num = 0;
    }

}