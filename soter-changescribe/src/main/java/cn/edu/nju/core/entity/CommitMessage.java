package cn.edu.nju.core.entity;

import lombok.Data;

import java.util.*;

@Data
public class CommitMessage {
    List<String> properties; //发生变化的配置文件
    String commitStereotype; //变更类型
    String commitStereotypeDescribe; //变更类型相应的描述

    Map<String, List<TypeDescribe>> packageAndTypes; //key为包名，value为类全限定类名List

    public CommitMessage() {
        this.packageAndTypes = new LinkedHashMap<>();
    }
}
