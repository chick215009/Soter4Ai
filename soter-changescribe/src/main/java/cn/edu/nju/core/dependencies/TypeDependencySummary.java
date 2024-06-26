package cn.edu.nju.core.dependencies;

import cn.edu.nju.core.Constants;
import cn.edu.nju.core.git.ChangedFile.TypeChange;
import cn.edu.nju.core.textgenerator.phrase.util.PhraseUtils;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.internal.core.NamedMember;
import org.eclipse.jdt.internal.core.ResolvedSourceField;
import org.eclipse.jdt.internal.core.ResolvedSourceMethod;
import org.eclipse.jdt.internal.core.ResolvedSourceType;

import java.util.ArrayList;

public class TypeDependencySummary extends DependencySummary {

    public TypeDependencySummary(IJavaElement element, String operation) {
        setElement(element);
        setOperation(operation);
        this.setDependencies(new ArrayList<SearchMatch>());

//        Display.getDefault().asyncExec(new Runnable() {
//            public void run() {
//                setProject(ProjectInformation.getProject(ProjectInformation.getSelectedProject()));
//            }
//        });
    }

    @Override
    public void find() {

//        SearchEngine engine = new SearchEngine();
//        IJavaSearchScope workspaceScope = null;
//
//        if(getProject() != null) {
//            workspaceScope = SearchEngine.createJavaSearchScope(createSearchScope());
//        } else {
//            workspaceScope = SearchEngine.createWorkspaceScope();
//        }
//
//        SearchPattern pattern = SearchPattern.createPattern(
//                getElement().getPrimaryElement().getElementName().replace(Constants.JAVA_EXTENSION, Constants.EMPTY_STRING),
//                IJavaSearchConstants.TYPE,
//                IJavaSearchConstants.REFERENCES,
//                SearchPattern.R_EXACT_MATCH);
//        SearchParticipant[] participant = new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() };
//        try {
//            engine.search(pattern, participant, workspaceScope, createSearchRequestor(), new NullProgressMonitor());
//        } catch (CoreException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
    }

    @Override
    public void generateSummary() {
        if(getDependencies() != null && getDependencies().size() > 0) {
            String lead = Constants.NEW_LINE;
            if(getOperation().equals(TypeChange.REMOVED.toString())) {
                lead = "Was referenced by:";
            } else {
                lead = "Referenced by:";
            }
            setBuilder(new StringBuilder(lead +Constants.NEW_LINE));


            for (SearchMatch match : getDependencies()) {
                NamedMember type = null;
                if(match.getElement() instanceof ResolvedSourceMethod) {
                    type = ((ResolvedSourceMethod )match.getElement());
                } else if(match.getElement() instanceof ResolvedSourceType) {
                    type = ((ResolvedSourceType )match.getElement());
                } else if(match.getElement() instanceof ResolvedSourceField) {
                    type = ((ResolvedSourceField)match.getElement());
                }

                getBuilder().append(Constants.TAB);
                getBuilder().append(Constants.SPACE);

                if(match.isInsideDocComment()) {
                    getBuilder().append("Referenced in comments of");
                } else if(match.isImplicit()) {
                    getBuilder().append("Implicit reference in");
                }

                getBuilder().append(Constants.SPACE);
                getBuilder().append(type.getParent().getElementName());
                getBuilder().append(Constants.SPACE);
                getBuilder().append(PhraseUtils.getStringType(type.getDeclaringType()));
                getBuilder().append(Constants.NEW_LINE);
            }
        }
    }

    public String toString() {
        if(getBuilder() == null) {
            setBuilder(new StringBuilder());
        }
        return getBuilder().toString();
    }
}

