package cn.edu.nju.core.integration;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class MyVisitor extends ASTVisitor {
    @Override
    public boolean visit(TypeDeclaration node) {
        System.out.println(node.resolveBinding().getQualifiedName());
        return super.visit(node);
    }
}
