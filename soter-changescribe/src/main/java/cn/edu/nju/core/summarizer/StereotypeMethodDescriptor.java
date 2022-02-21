package cn.edu.nju.core.summarizer;

import cn.edu.nju.core.Constants;
import cn.edu.nju.core.entity.MethodDescribe;
import cn.edu.nju.core.entity.TypeDescribe;
import cn.edu.nju.core.label.LabelMethod;
import cn.edu.nju.core.stereotype.stereotyped.StereotypedElement;
import cn.edu.nju.core.stereotype.stereotyped.StereotypedMethod;
import cn.edu.nju.core.stereotype.taxonomy.MethodStereotype;
import cn.edu.nju.core.textgenerator.phrase.MethodPhraseGenerator;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.io.IOException;
import java.util.*;

public class StereotypeMethodDescriptor {

    public static String describe(List<StereotypedElement> elements, TypeDescribe typeDescribe) throws IOException, ClassNotFoundException {

        String description = Constants.EMPTY_STRING;
        int i = 0;
        for (StereotypedElement method : elements) {
            if(method.getElement() instanceof MethodDeclaration && method instanceof StereotypedMethod) {

                StereotypedMethod stereotypedMethod = (StereotypedMethod) method;
                String stereotype;
                if (stereotypedMethod.getSecondaryStereotype() == null) {
                    if (stereotypedMethod.getPrimaryStereotype() != null) {
                        stereotype = stereotypedMethod.getPrimaryStereotype().name();
                    } else {
                        stereotype = "";
                    }
                } else {
                    stereotype = stereotypedMethod.getSecondaryStereotype().name();
                }
                MethodDeclaration methodDeclaration = (MethodDeclaration) method.getElement();
                Map<String, MethodDescribe> addOrRemoveDescribe = typeDescribe.getAddOrRemoveDescribe();
                String methodName = methodDeclaration.getName().getIdentifier();
                MethodPhraseGenerator phraseGenerator = new MethodPhraseGenerator(method, "BASIC");
                phraseGenerator.generate();
                if(!description.contains(phraseGenerator.getPhrase())) {
                    String phrase = (i == (elements.size() - 1)) ? phraseGenerator.getPhrase().replace(";", Constants.EMPTY_STRING) : phraseGenerator.getPhrase();
                    addOrRemoveDescribe.put(methodName, new MethodDescribe(methodName, stereotype, phrase));
                    description += phrase;
                }

            }
            i++;
        }
        return description;
    }

    public static List<LabelMethod> describe2(List<StereotypedElement> elements) throws IOException, ClassNotFoundException {
        Set<LabelMethod> set = new LinkedHashSet<>();
        for (StereotypedElement method : elements) {
            if(method instanceof StereotypedMethod && method.getElement() instanceof MethodDeclaration) {
                MethodPhraseGenerator phraseGenerator = new MethodPhraseGenerator(method, "BASIC");
                phraseGenerator.generate();
                if (!set.contains(phraseGenerator.getPhrase())) {
                    if (method.getStereotypes() != null && method.getStereotypes().size() > 0) {
                        MethodStereotype methodStereotype = (MethodStereotype) method.getStereotypes().get(0);
                        String categoryName = methodStereotype.getCategory().getName();
                        String stereotypeName = methodStereotype.getSubcategory().getName();
                        set.add(new LabelMethod(categoryName, stereotypeName, phraseGenerator.getPhrase()));
                    }
                }
            }
        }
        ArrayList<LabelMethod> list = new ArrayList<>();
        list.addAll(set);
        return list;
    }
}

