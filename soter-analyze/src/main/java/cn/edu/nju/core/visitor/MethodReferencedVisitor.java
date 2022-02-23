package cn.edu.nju.core.visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class MethodReferencedVisitor extends ASTVisitor {
//    public boolean visit(MethodInvocation node) {
//        String key = node.resolveMethodBinding().getKey();
//
//    }

    public boolean visit(MethodDeclaration node) {
        String key = node.resolveBinding().getKey();
        return true;
    }
}
