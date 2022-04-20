package cn.edu.nju.analyze.summarize;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.util.List;

public class MethodVisitor extends ASTVisitor {
    public List<MethodDeclaration> methods;

    public MethodVisitor(List<MethodDeclaration> methods) {
        this.methods = methods;
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        methods.add(node);
        return true;
    }
}
