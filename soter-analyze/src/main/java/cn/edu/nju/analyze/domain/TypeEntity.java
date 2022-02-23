package cn.edu.nju.analyze.domain;

import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
@AllArgsConstructor
public class TypeEntity {
    private List<String> referencedList;
    private String typeStereotype;
    private Boolean isLocal;
    private List<SourceCodeChange> changes;
    private String operation;
    private String typeLabel;//属于哪一种类 抽象、接口、普通类
    private List<String> interfaceList;
    private String superClassStr;//继承的类
    private String typeName;
    private List<MethodEntity> methodEntityList;

    public TypeEntity() {
        this.referencedList = new ArrayList<>();
        this.typeStereotype = "";
        this.isLocal = false;
        this.changes = new ArrayList<>();
        this.operation = "";
        this.typeLabel = "";
        this.interfaceList = new ArrayList<>();
        this.superClassStr = "";
        this.typeName = "";
        this.methodEntityList = new ArrayList<>();
    }

}
