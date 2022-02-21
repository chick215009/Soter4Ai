package cn.edu.nju.core.visitor;

import cn.edu.nju.core.stereotype.stereotyped.StereotypeIdentifier;
import cn.edu.nju.core.utils.JDTASTUtil;
import lombok.Data;
import org.eclipse.jdt.core.dom.*;

import java.util.Map;

@Data
public class TypeReferencedVisitor extends ASTVisitor {
    private Map<String, StereotypeIdentifier> summarized;

    public TypeReferencedVisitor(Map<String, StereotypeIdentifier> summarized) {
        this.summarized = summarized;
    }


    //对于不同文件夹
    public boolean visit(ImportDeclaration node) {
        String referencedFullyQualifiedName = node.getName().getFullyQualifiedName();
        if (!summarized.containsKey(referencedFullyQualifiedName)) {
            return true;
        }
        StereotypeIdentifier stereotypeIdentifier = summarized.get(referencedFullyQualifiedName);

        CompilationUnit compilationUnit = (CompilationUnit) (node.getParent());

        String referenceFullyQualifiedName = JDTASTUtil.getFullyQualifiedNameFromCompilationUnit(compilationUnit);
        stereotypeIdentifier.getTypeReferenceStatistic().add(referenceFullyQualifiedName);
        return true;
    }

    //对于相同文件夹
    public boolean visit(FieldDeclaration node) {
        for (Object fragment : node.fragments()) {
            if (fragment instanceof VariableDeclaration) {
                IVariableBinding iVariableBinding = ((VariableDeclaration) fragment).resolveBinding();
                fillDependence(summarized, iVariableBinding, node);
            }

        }
        return true;
    }

    //对于相同文件夹
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
