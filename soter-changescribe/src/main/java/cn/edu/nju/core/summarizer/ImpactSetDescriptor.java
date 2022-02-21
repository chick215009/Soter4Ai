package cn.edu.nju.core.summarizer;

import cn.edu.nju.core.Constants;
import cn.edu.nju.core.dependencies.TypeDependencySummary;
import cn.edu.nju.core.git.ChangedFile;
import cn.edu.nju.core.stereotype.stereotyped.StereotypeIdentifier;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;

import java.util.Set;

public class ImpactSetDescriptor {

    public static String describe(ICompilationUnit cu, ChangedFile[] differences, String operation) {

        TypeDependencySummary dependency = new TypeDependencySummary((IJavaElement) cu, operation);
        if(null != cu) {
            dependency.setDifferences(differences);
            dependency.find();
            dependency.generateSummary();
        }

        return dependency.toString();
    }

    public static String describe(StereotypeIdentifier identifier) {
        Set<String> typeReferenceStatistic = identifier.getTypeReferenceStatistic();
        StringBuilder stringBuilder = new StringBuilder();
        String lead = Constants.EMPTY_STRING;
        if (typeReferenceStatistic != null && typeReferenceStatistic.size() > 0) {
            if (identifier.getScmOperation().equals(ChangedFile.TypeChange.REMOVED.toString())) {
                lead = "was referenced by:";
            } else {
                lead = "referenced by:";
            }
        }
        stringBuilder.append(Constants.NEW_LINE + lead);

        for (String s : typeReferenceStatistic) {
            stringBuilder.append(s);
            stringBuilder.append(", ");
        }
        if (typeReferenceStatistic != null && typeReferenceStatistic.size() > 0) {
            stringBuilder.setLength(stringBuilder.length() - 2);
            stringBuilder.append(".");
            stringBuilder.append(Constants.NEW_LINE);
        }

        return stringBuilder.toString();
    }

}
