package cn.edu.nju.core.entity;

import lombok.Data;

@Data
public class CommitField {
    String simpleDescribe;
    String detailDescribe;

    public CommitField(String simpleDescribe, String detailDescribe) {
        this.simpleDescribe = simpleDescribe;
        this.detailDescribe = detailDescribe;
    }
}
