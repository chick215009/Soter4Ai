package cn.edu.nju.core.label;

import cn.edu.nju.core.Constants;
import cn.edu.nju.core.filter.CategoryOrStereotypeMethodFilter;
import cn.edu.nju.core.filter.LabelTypeFilter;
import cn.edu.nju.core.filter.StereotypeTypeFilter;
import cn.edu.nju.core.git.ChangedFile;
import cn.edu.nju.core.textgenerator.phrase.util.PhraseUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LabelType {
    private String operation;
    private String typeLabel; //属于哪一种类 抽象、接口、普通类
    private List<String> interfaceList; //实现 or 继承的几口
    private String superclassStr; //继承的类
    private String typeStereotypeLabel; //类的stereotype
    private boolean isLocal; //是否是非 public class
    private String typeName; //类名

    private List<String> referencedList;
    private List<LabelMethod> labelMethodList;

    public String describe(LabelTypeFilter labelTypeFilter,
                           StereotypeTypeFilter typeFilter,
                           CategoryOrStereotypeMethodFilter methodFilter) {
        if (typeFilter.getFilterStereotype().contains("NO_STEREOTYPE") && typeStereotypeLabel.equals("")) {
            return "";
        }
        if (typeFilter.getFilterStereotype().contains(typeStereotypeLabel)) {
            return "";
        }

        if (referencedList.size() < labelTypeFilter.getReferencedCount()) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(describeOperation(operation) + " ");
        boolean describeStereotype = false;
        String article = "";
        if (typeStereotypeLabel != null && !typeStereotypeLabel.equals("") && labelTypeFilter.isTypeStereotypeLabel()) {
            describeStereotype = true;
            article = PhraseUtils.getIndefiniteArticle(typeStereotypeLabel);
        }

        boolean describeInterfaces = false;
        if (interfaceList != null && interfaceList.size() > 0 && labelTypeFilter.isInterfaceList()) {
            describeInterfaces = true;
        }

        boolean describeSuperclass = false;
        if (superclassStr != null && !superclassStr.equals("") && labelTypeFilter.isSuperclassStr()) {
            describeSuperclass = true;
        }

        boolean describeLocal = false;
        if (isLocal && labelTypeFilter.isLocal()) {
            describeLocal = true;
        }

        boolean describeReferenceList = false;
        if (referencedList != null && referencedList.size() > 0 && labelTypeFilter.isReferencedList()) {
            describeReferenceList = true;
        }

        switch (typeLabel) {
            case "interface":
                if (describeStereotype) {
                    stringBuilder.append(article + " " + typeStereotypeLabel + " ");
                } else {
                    stringBuilder.append("a ");
                }
                if (describeLocal) {
                    stringBuilder.append("local interface");
                } else {
                    stringBuilder.append("interface");
                }
                break;
            case "abstract":
                if (describeStereotype) {
                    stringBuilder.append(article + " " + typeStereotypeLabel + " ");
                } else {
                    stringBuilder.append("a ");
                }
                if (describeLocal) {
                    stringBuilder.append("local abstract class");
                } else {
                    stringBuilder.append("abstract class");
                }
                break;
            default:
                if (describeStereotype) {
                    stringBuilder.append(article + " " + typeStereotypeLabel + " ");
                } else {
                    stringBuilder.append("a ");
                }
                if (describeLocal) {
                    stringBuilder.append("local class");
                } else {
                    stringBuilder.append("class");
                }
        }
        if (describeInterfaces) {
            if (typeLabel.equals("interface")) {
                stringBuilder.append(" extends ");
            } else {
                stringBuilder.append(" implements ");
            }
            stringBuilder.append(describeInterfaces(interfaceList));
        }

        if (describeSuperclass) {
            if (describeInterfaces) {
                stringBuilder.append(" and, ");
            }
            stringBuilder.append(" extends " + superclassStr);
        }
        stringBuilder.append(": " + typeName);
        stringBuilder.append(Constants.NEW_LINE);

        if (describeReferenceList) {
            StringBuilder lead = new StringBuilder();
            if (operation.equals(ChangedFile.TypeChange.REMOVED.toString())) {
                lead.append("was referenced by");
            } else {
                lead.append("referenced by");
            }

            for (int i = 0; i < referencedList.size(); i++) {
                lead.append(referencedList.get(i));
                if (i != referencedList.size() - 1) {
                    lead.append(", ");
                } else {
                    lead.append(Constants.NEW_LINE);
                }
            }

            stringBuilder.append(lead);
        }

        StringBuilder methodDescribe = new StringBuilder();
        if (labelMethodList != null && labelMethodList.size() > 0) {
            methodDescribe.append("It allows to: " + Constants.NEW_LINE);
            for (LabelMethod labelMethod : labelMethodList) {
                String methodCategoryLabel = labelMethod.getMethodCategoryLabel();
                if (methodFilter.getFilterCategory().contains(methodCategoryLabel)) {
                    continue;
                }
                String methodStereotypeLabel = labelMethod.getMethodStereotypeLabel();
                if (methodFilter.getFilterStereotype().contains(methodStereotypeLabel)) {
                    continue;
                }
                methodDescribe.append(labelMethod.getPhrase());
            }
        }

        if (!methodDescribe.equals("It allows to: " + Constants.NEW_LINE)) {
            stringBuilder.append(methodDescribe);
        }
        return stringBuilder.toString();
    }

    private String describeInterfaces(List<String> interfaceList) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < interfaceList.size(); i++) {
            stringBuilder.append(interfaceList.get(i));
            if (i != interfaceList.size() - 1) {
                stringBuilder.append(", ");
            }
        }

        return stringBuilder.toString();
    }

    private static String describeOperation(String operation) {
        String description = Constants.EMPTY_STRING;
        if(operation.equals(ChangedFile.TypeChange.ADDED.toString()) || operation.equals(ChangedFile.TypeChange.UNTRACKED.toString())) {
            description = "Add";
        } else if(operation.equals(ChangedFile.TypeChange.REMOVED.toString())) {
            description = "Remove";
        }
        return description;
    }
}
