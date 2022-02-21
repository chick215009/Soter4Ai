package cn.edu.nju.core.visitor;

import cn.edu.nju.core.stereotype.stereotyped.StereotypeIdentifier;
import cn.edu.nju.core.utils.JDTASTUtil;
import org.eclipse.jdt.core.dom.*;

import java.util.Map;

/*
由于对于相同包的文件是不需要导包的，所以需要根据 函数参数声明 和 成员变量声明 判断依赖
 */
public class TypeReferencedInCommonFileVisitor extends ASTVisitor {
    private Map<String, StereotypeIdentifier> summarized;

    public TypeReferencedInCommonFileVisitor(Map<String, StereotypeIdentifier> summarized) {
        this.summarized = summarized;
    }

    public boolean visit(FieldDeclaration node) {
        for (Object fragment : node.fragments()) {
            if (fragment instanceof VariableDeclaration) {
                IVariableBinding iVariableBinding = ((VariableDeclaration) fragment).resolveBinding();
                fillDependence(summarized, iVariableBinding, node);
            }

        }
        return true;
    }

    public boolean visit(MethodDeclaration node) {
        for (Object parameter : node.parameters()) {
            if (parameter instanceof SingleVariableDeclaration) {
                IVariableBinding iVariableBinding = ((SingleVariableDeclaration) parameter).resolveBinding();
                fillDependence(summarized, iVariableBinding, node);
            }
        }
        return true;
    }

    private void fillDependence(Map<String, StereotypeIdentifier> summarized, IVariableBinding iVariableBinding, ASTNode node) {
        if (iVariableBinding == null) {
            return;
        }
        ITypeBinding iTypeBinding = iVariableBinding.getType();
        if (iTypeBinding.getPackage() == null) {
            return;
        }
        String referencedFullyQualifiedName = iTypeBinding.getQualifiedName();
        if (!referencedFullyQualifiedName.contains(".")) {
            referencedFullyQualifiedName = iTypeBinding.getPackage().getName() + "." + referencedFullyQualifiedName;
        }
        if (!summarized.containsKey(referencedFullyQualifiedName)) {
            return;
        }

        String referenceFullyQualifiedName = JDTASTUtil.getFullyQualifiedNameFromCompilationUnit((CompilationUnit) node.getRoot());
        summarized.get(referencedFullyQualifiedName).getTypeReferenceStatistic().add(referenceFullyQualifiedName);
    }

}
