package cn.edu.nju.core.stereotype.visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import java.util.Map;

public class ImpactVisitor extends ASTVisitor {
    Map<String, Integer> impactMap;
//    public boolean visit(TypeDeclaration node) {
//        for (FieldDeclaration field : node.getFields()) {
//            Type type = field.getType();
//            List fragments = field.fragments();
//            int nodeType = field.getNodeType();
//        }
//        return true;
//    }
    public ImpactVisitor(Map<String, Integer> impactMap) {
        this.impactMap = impactMap;
    }
    public boolean visit(MethodDeclaration node) {

        for (Object o : node.parameters()) {
            if (o instanceof SingleVariableDeclaration &&
                    ((SingleVariableDeclaration) o).resolveBinding() != null &&
                    ((SingleVariableDeclaration) o).resolveBinding().getType() != null) {
                String qualifiedName = ((SingleVariableDeclaration) o).resolveBinding().getType().getQualifiedName();
                impactMap.put(qualifiedName, impactMap.getOrDefault(qualifiedName, 0) + 1);
            }
//            final SingleVariableDeclaration parameter = (SingleVariableDeclaration)o;
//            if (parameter.getType() instanceof SimpleType) {
//                SimpleType nodeType = (SimpleType) parameter.getType();
//                Name typeName = nodeType.getName();
//                String fullyQualifiedName = typeName.getFullyQualifiedName();
//                impactMap.put(fullyQualifiedName, impactMap.getOrDefault(fullyQualifiedName, 0) + 1);
//            }
        }

        return true;
    }

    public boolean visit(FieldDeclaration node) {
        if (node.getType() != null && node.getType().resolveBinding() != null) {
            String qualifiedName = node.getType().resolveBinding().getQualifiedName();
            impactMap.put(qualifiedName, impactMap.getOrDefault(qualifiedName, 0) + node.fragments().size());
        }

        return true;
    }
}
