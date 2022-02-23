package cn.edu.nju.core.summarizer;

import cn.edu.nju.core.Constants;
import cn.edu.nju.core.entity.CommitMessage;
import cn.edu.nju.core.entity.TypeDescribe;
import cn.edu.nju.core.filter.CategoryOrStereotypeMethodFilter;
import cn.edu.nju.core.filter.LabelTypeFilter;
import cn.edu.nju.core.filter.StereotypeTypeFilter;
import cn.edu.nju.core.git.ChangedFile;
import cn.edu.nju.core.label.LabelMethod;
import cn.edu.nju.core.label.LabelType;
import cn.edu.nju.core.stereotype.stereotyped.StereotypeIdentifier;
import cn.edu.nju.core.stereotype.stereotyped.StereotypedElement;
import cn.edu.nju.core.stereotype.stereotyped.StereotypedType;
import lombok.Data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Data
public class SummarizeType {

    private StringBuilder builder;
    private StereotypedElement element;
    private ChangedFile[] differences;
    private StereotypeIdentifier identifier;
    private boolean isLocal;
    private List<String> referencedList;

    public SummarizeType(StereotypedElement element, StereotypeIdentifier identifier, ChangedFile[] differences, List<String> referencedList) {
        super();
        this.element = element;
        this.differences = differences;
        this.identifier = identifier;
        this.builder = new StringBuilder();
        this.referencedList = referencedList;
    }

    public SummarizeType(StereotypedElement element, StereotypeIdentifier identifier, ChangedFile[] differences) {
        super();
        this.element = element;
        this.differences = differences;
        this.identifier = identifier;
        this.builder = new StringBuilder();
        this.referencedList = new ArrayList<>();
    }

    /**
     * 产生对对类的描述，非modified
     */
    public void generate(CommitMessage commitMessage) throws IOException, ClassNotFoundException {

        StringBuilder localBuilder = new StringBuilder(Constants.EMPTY_STRING);
        builder = new StringBuilder();
        builder.append(GeneralDescriptor.describe(element, identifier.getParser().getCompilationUnit(), identifier.getScmOperation(), isLocal()));


        StereotypedType type = (StereotypedType) element;
        String typeStereoType = "";
        if (type.getPrimaryStereotype() != null) {
            typeStereoType = type.getPrimaryStereotype().getName();
        }
        TypeDescribe typeDescribe = new TypeDescribe();
        typeDescribe.setChangeType(identifier.getScmOperation());
        typeDescribe.setStereoType(typeStereoType);


        localBuilder.append(StereotypeMethodDescriptor.describe(getElement().getStereoSubElements(), typeDescribe));
//        String currentPackage = identifier.getParser().getCompilationUnit().getPackage().getName().getFullyQualifiedName();
//        commitMessage.getPackageAndTypes().get(currentPackage).add(typeDescribe);
//        localBuilder.append(ImpactSetDescriptor.describe(identifier.getCompilationUnit(), getDifferences(), identifier.getScmOperation()));
//        localBuilder.append(ImpactSetDescriptor.describe(identifier));
        localBuilder.append(Constants.NEW_LINE);

        if(!localBuilder.toString().trim().equals(Constants.EMPTY_STRING)) {
            if(getElement().getStereoSubElements() != null && getElement().getStereoSubElements().size() > 0) {
                builder.append("It allows to:");
            }
            builder.append(Constants.NEW_LINE);
            builder.append(localBuilder.toString());
        } else {
            builder.append(Constants.NEW_LINE);
        }
    }


    public String  generateLabelDescribe(LabelTypeFilter labelTypeFilter,
                                      StereotypeTypeFilter stereotypeTypeFilter,
                                      CategoryOrStereotypeMethodFilter categoryOrStereotypeMethodFilter) throws IOException, ClassNotFoundException {
        /*
        对类的描述：
            如果类是接口，继承了哪些接口
            如果不是接口，实现了哪些接口、继承了哪些类
            如果是 abstract
            （枚举类）
        类的标签：
            boundary class、entity class、trivial entity class、object creator class、controller class、data class
        isLocal

         */

        LabelType labelType = GeneralDescriptor.describe2(element, identifier.getParser().getCompilationUnit(), identifier.getScmOperation(), isLocal());
        labelType.setReferencedList(referencedList);
        List<LabelMethod> labelMethodList = StereotypeMethodDescriptor.describe2(getElement().getStereoSubElements());
        labelType.setLabelMethodList(labelMethodList);

        String describe = labelType.describe(labelTypeFilter, stereotypeTypeFilter, categoryOrStereotypeMethodFilter);
        return describe;
    }

}
