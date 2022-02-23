package cn.edu.nju.core.visitor;

import lombok.Data;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.util.Map;

@Data
public class MethodDeclarationStatisticsVisitor extends ASTVisitor {
    private Map<String, Integer> methodInvocationStatistics;

    public MethodDeclarationStatisticsVisitor(Map<String, Integer> methodInvocationStatistics) {
        this.methodInvocationStatistics = methodInvocationStatistics;
    }

    public boolean visit(MethodDeclaration node) {

//        List<String> parameters = new ArrayList<>();
//        for (Object parameter : node.parameters()) {
//            if (parameter instanceof SingleVariableDeclaration) {
//                String identifier = ((SingleVariableDeclaration) parameter).getName().getIdentifier();
//                parameters.add(identifier);
//            }
//        }
//
//        StringBuilder stringBuilder = new StringBuilder();


        if (node.resolveBinding() != null) {
            IMethodBinding iMethodBinding = node.resolveBinding();

            methodInvocationStatistics.put(node.resolveBinding().getKey(), 0);
        }

        return true;
    }
}
