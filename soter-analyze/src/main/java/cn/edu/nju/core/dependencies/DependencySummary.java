package cn.edu.nju.core.dependencies;


import cn.edu.nju.core.Constants;
import cn.edu.nju.core.git.ChangedFile;
import lombok.Data;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.ResolvedSourceField;
import org.eclipse.jdt.internal.core.ResolvedSourceMethod;
import org.eclipse.jdt.internal.core.ResolvedSourceType;

import java.util.List;

@Data
public class DependencySummary {

    private IJavaElement element;
    private List<SearchMatch> dependencies;
    private StringBuilder builder;
    private ChangedFile[] differences;
    private String operation;
    private IProject project;

    public void find() {
    }

    public void generateSummary() {
    }

    public SearchRequestor createSearchRequestor() {
        SearchRequestor requestor = new SearchRequestor() {
            @Override
            public void acceptSearchMatch(SearchMatch match) throws CoreException {
                IJavaElement type = null;
                if(match.getElement() instanceof ResolvedSourceMethod) {
                    type = ((ResolvedSourceMethod )match.getElement()).getParent();
                } else if(match.getElement() instanceof ResolvedSourceType) {
                    type = ((ResolvedSourceType )match.getElement()).getParent();
                } else if(match.getElement() instanceof ResolvedSourceField) {
                    type = ((ResolvedSourceField)match.getElement()).getParent();
                }
                if(null != type && inChangedFiles(type.getElementName())) {
                    addMatched(match);
                }
            }
        };
        return requestor;
    }

    public IJavaElement[] createSearchScope() {
        final IJavaElement[] scope = new IJavaElement[getDifferences().length];
        String projectName = getProject().getName();
        int i = 0;
        for(final ChangedFile cf : getDifferences()) {
            if(cf.getPath().startsWith(projectName)) {
                scope[i] = JavaCore.create(getProject().findMember(cf.getPath().replaceFirst(projectName, Constants.EMPTY_STRING)));
            } else {
                scope[i] = JavaCore.create(getProject().findMember(cf.getPath()));
            }
            i++;
        }
        return scope;
    }

    public void addMatched(SearchMatch match) {
        if(!included(match)) {
            this.getDependencies().add(match);
        }
    }

    public boolean included(SearchMatch matchVerify) {
        boolean included = false;
        for (SearchMatch match : this.getDependencies()) {
            if(match.getResource() == matchVerify.getResource()) {
                included = true;
                break;
            }
        }

        return included;
    }

    private boolean inChangedFiles(String file) {
        boolean exist = false;
        for(ChangedFile cf : getDifferences()) {
            if(cf.getPath().endsWith(Constants.SLASH + file + Constants.JAVA_EXTENSION)) {
                exist = true;
                System.out.println("exists original: " + cf.getPath() + " searched: " + file);
                break;
            }
        }
        return exist;
    }

}

