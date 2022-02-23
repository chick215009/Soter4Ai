package cn.edu.nju.core.visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.Map;

public class MethodInvocationStatisticsVisitor extends ASTVisitor {

    private Map<String, Integer> methodInvocationStatistics;

    public MethodInvocationStatisticsVisitor(Map<String, Integer> methodInvocationStatistics) {
        this.methodInvocationStatistics = methodInvocationStatistics;
    }

    public boolean visit(MethodInvocation node) {
        if (node.resolveMethodBinding() != null) {
            String key = node.resolveMethodBinding().getKey();
            if (methodInvocationStatistics.containsKey(key)) {
                methodInvocationStatistics.put(key, methodInvocationStatistics.get(key) + 1);
            }
        }
        return true;
    }
}
