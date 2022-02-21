package cn.edu.nju.core.summarizer;

import cn.edu.nju.core.Constants;
import cn.edu.nju.core.Constants;
import cn.edu.nju.core.git.ChangedFile;
import cn.edu.nju.core.label.LabelType;
import cn.edu.nju.core.label.TypeLabel;
import cn.edu.nju.core.stereotype.stereotyped.StereotypedElement;
import cn.edu.nju.core.stereotype.stereotyped.StereotypedType;
import cn.edu.nju.core.stereotype.taxonomy.CodeStereotype;
import cn.edu.nju.core.stereotype.taxonomy.TypeStereotype;
import cn.edu.nju.core.textgenerator.phrase.util.PhraseUtils;
import cn.edu.nju.core.git.ChangedFile;
import cn.edu.nju.core.stereotype.stereotyped.StereotypedElement;
import cn.edu.nju.core.stereotype.stereotyped.StereotypedType;
import cn.edu.nju.core.textgenerator.phrase.util.PhraseUtils;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Type;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GeneralDescriptor {

    public static String describe(StereotypedElement element, CompilationUnit cu, String operation, boolean isLocal) throws IOException, ClassNotFoundException {
        StereotypedType type = (StereotypedType) element;
        StringBuilder description = new StringBuilder();
        ITypeBinding superclass = null;
        ITypeBinding[] interfaces = null;
        if (type.getElement().resolveBinding() != null) {
            superclass = type.getElement().resolveBinding().getSuperclass();
            interfaces = type.getElement().resolveBinding().getInterfaces();
        }


        if(type.isInterface()) {
            description.append(describeInterface(type) + " ");
        } else {
            if (interfaces != null && superclass != null && interfaces.length != 0 && !superclass.getKey().equals("Ljava/lang/Object;")) {
                description.append(PhraseUtils.getImplementationDescription(interfaces));
                description.append(", and ");
                description.append(PhraseUtils.getExtensionDescription(superclass));
            } else if (interfaces != null && interfaces.length != 0) {
                description.append(PhraseUtils.getImplementationDescription(interfaces));
            } else if (superclass != null && !superclass.getKey().equals("Ljava/lang/Object;")) {
                description.append(PhraseUtils.getExtensionDescription(superclass));
            } else if (type.isBoundary()) {
                description.append("boundary class");
            } else if (type.isEntity() || type.isDataProvider() || type.isCommander()) {
                description.append("entity class");
            } else if (type.isMinimalEntity()) {
                description.append("trivial entity class");
            } else if (type.isFactory()) {
                description.append("object creator class");
            } else if (type.isController() || type.isPureController()) {
                description.append("controller class");
            } else if (type.isDataClass()) {
                description.append("data class");
            } else {
                description.append("class");
            }
            if (type.getElement().resolveBinding() != null && Modifier.isAbstract(type.getElement().resolveBinding().getModifiers())) {
                description.insert(0, "an abstract ");
            } else {
                description.insert(0, PhraseUtils.getIndefiniteArticle(description.toString()).concat(" "));
            }
        }

        if(!isLocal) {
            description.insert(0, describeOperation(operation) + " ");
        } else {
            description.insert(0, describeOperation(operation) + " a local ");
        }

        description.append(" for ");
        description.append(type.getElement().getName().getFullyQualifiedName());
//        NounPhrase classNamePhrase = new NounPhrase(Tokenizer.split(type.getElement().getName().getFullyQualifiedName()));
//        classNamePhrase.generate();
//        description.append(classNamePhrase.toString());


        return description.toString();
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

    private static String describeInterface(StereotypedType type) {
        StringBuilder template = null;
        try {
            ITypeBinding[] interfaces = null;
            if (null != type.getElement().resolveBinding()) {
                interfaces = type.getElement().resolveBinding().getInterfaces();
            }

            template = new StringBuilder();
            if (null != interfaces && interfaces.length > 0) {
                final String enumeratedTypes = PhraseUtils.enumeratedTypes(type.getElement().resolveBinding().getInterfaces());
                template.append(PhraseUtils.getIndefiniteArticle(enumeratedTypes));
                template.append(" ");
                template.append(enumeratedTypes);
                template.append(" ");
                template.append("interface extension");
            }
            else {
                template.append("an interface declaration");
            }
        } catch (NullPointerException e) {
            System.out.println("error");
            template = new StringBuilder();
        }
        return template.toString();
    }

    public static LabelType describe2(StereotypedElement element, CompilationUnit cu, String operation, boolean isLocal) {


        StereotypedType type = (StereotypedType) element;
        ITypeBinding superclass = null;
        ITypeBinding[] interfaces = null;
        if (type.getElement().resolveBinding() != null) {
            superclass = type.getElement().resolveBinding().getSuperclass();
            interfaces = type.getElement().resolveBinding().getInterfaces();
        }

        String typeLabel;
        if (type.isInterface()) {
            typeLabel = TypeLabel.INTERFACE.getLabel();
        } else if (type.getElement().resolveBinding() != null
                && Modifier.isAbstract(type.getElement().resolveBinding().getModifiers())) {
            typeLabel = TypeLabel.ABSTRACT.getLabel();
        } else {
            typeLabel = TypeLabel.USUAL_CLASS.getLabel();
        }

        List<String> interfaceList = new ArrayList<>();
        String superclassStr = "";

        if (interfaces != null && interfaces.length != 0) {
            for (ITypeBinding anInterface : interfaces) {
                interfaceList.add(anInterface.getName());
            }
        }

        if (superclass != null && !superclass.getKey().equals("Ljava/lang/Object;")) {
            superclassStr = superclass.getName();
        }

        String typeStereotypeLabel = "";

        List<CodeStereotype> stereotypes = type.getStereotypes();
        if (stereotypes != null && stereotypes.size() > 0) {
            TypeStereotype typeStereotype = (TypeStereotype) stereotypes.get(0);
            String typeStereotypeName = typeStereotype.getName();
            if (typeStereotypeName.equals(TypeStereotype.ENTITY.getName())) {
                typeStereotypeLabel = TypeStereotype.ENTITY.getName();
            } else if (typeStereotypeName.equals(TypeStereotype.MINIMAL_ENTITY.getName())) {
                typeStereotypeLabel = TypeStereotype.MINIMAL_ENTITY.getName();
            } else if (typeStereotypeName.equals(TypeStereotype.DATA_PROVIDER.getName())) {
                typeStereotypeLabel = TypeStereotype.DATA_PROVIDER.getName();
            } else if (typeStereotypeName.equals(TypeStereotype.COMMANDER.getName())) {
                typeStereotypeLabel = TypeStereotype.COMMANDER.getName();
            } else if (typeStereotypeName.equals(TypeStereotype.BOUNDARY.getName())) {
                typeStereotypeLabel = TypeStereotype.BOUNDARY.getName();
            } else if (typeStereotypeName.equals(TypeStereotype.CONTROLLER.getName())) {
                typeStereotypeLabel = TypeStereotype.CONTROLLER.getName();
            } else if (typeStereotypeName.equals(TypeStereotype.PURE_CONTROLLER.getName())) {
                typeStereotypeLabel = TypeStereotype.PURE_CONTROLLER.getName();
            } else if (typeStereotypeName.equals(TypeStereotype.LARGE_CLASS.getName())) {
                typeStereotypeLabel = TypeStereotype.LAZY_CLASS.getName();
            } else if (typeStereotypeName.equals(TypeStereotype.LAZY_CLASS.getName())) {
                typeStereotypeLabel = TypeStereotype.LAZY_CLASS.getName();
            } else if (typeStereotypeName.equals(TypeStereotype.DEGENERATE.getName())) {
                typeStereotypeLabel = TypeStereotype.DEGENERATE.getName();
            }

        }

        return new LabelType(operation, typeLabel, interfaceList, superclassStr, typeStereotypeLabel, isLocal, type.getName(), new ArrayList<>(), new ArrayList<>());
    }

}
