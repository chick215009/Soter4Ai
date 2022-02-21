package cn.edu.nju.core.stereotype.analyzer;

import cn.edu.nju.core.stereotype.stereotyped.StereotypedMethod;
import lombok.Data;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

@Data
public class DemoVisitor extends ASTVisitor {
    private List<StereotypedMethod> methods;
    public DemoVisitor() {
        methods = new ArrayList<>();
    }

    @Override
    public boolean visit(FieldDeclaration node) {
        for (Object fragment : node.fragments()) {
            VariableDeclarationFragment v = (VariableDeclarationFragment)fragment;
            System.out.println("Field:\t" + v.getName());
        }
        return true;
    }
    @Override
    public boolean visit(MethodDeclaration node) {
        StereotypedMethod stereotypedMethod = new StereotypedMethod(node);
        stereotypedMethod.findStereotypes();
//        List<CodeStereotype> stereotypes = stereotypedMethod.getStereotypes();
        methods.add(stereotypedMethod);
        System.out.println("Method:\t" + node.getName());
        return true;
    }

    @Override
    public boolean visit(TypeDeclaration node) {
        System.out.println("Class:\t" + node.getName());
        return true;
    }

    @Override
    public boolean visit(MarkerAnnotation node) {
        System.out.println("Annotation :\t" + node.getTypeName());
        return true;
    }
}
