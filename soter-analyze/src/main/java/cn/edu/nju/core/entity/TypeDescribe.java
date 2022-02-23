package cn.edu.nju.core.entity;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class TypeDescribe {
    String changeType;
    String stereoType;
    Map<String, MethodDescribe> modifyDescribe;//当类发生变化时有值，key为函数名，value为对应的描述
    Map<String, MethodDescribe> addOrRemoveDescribe;//当类发生增删有值；

    public TypeDescribe() {
        modifyDescribe = new LinkedHashMap<>();
        addOrRemoveDescribe = new LinkedHashMap<>();
    }
}
