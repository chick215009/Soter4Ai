package cn.edu.nju.core.ast;

import lombok.Data;
import org.eclipse.jdt.core.*;

import java.util.Vector;

@Data
public class ProjectInformation {
    private int totalIUnits;
    private int totalTypes;
    private int totalMethods;
    private Vector<Integer> methodsCounter;
    private double methodsMean;
    private double methodsStdDev;
    private IJavaProject project;

    public ProjectInformation(final IJavaProject project) {
        super();
        this.totalIUnits = 0;
        this.totalTypes = 0;
        this.totalMethods = 0;
        this.methodsCounter = new Vector<Integer>();
        this.project = project;
    }

    public void compute() {
        try {
            IPackageFragment[] packageFragments;
            for (int length = (packageFragments = this.project
                    .getPackageFragments()).length, j = 0; j < length; ++j) {
                final IPackageFragment element = packageFragments[j];
                ICompilationUnit[] compilationUnits;
                for (int length2 = (compilationUnits = element
                        .getCompilationUnits()).length, k = 0; k < length2; ++k) {
                    final ICompilationUnit junit = compilationUnits[k];
                    boolean typeCounted = false;
                    IType[] allTypes;
                    for (int length3 = (allTypes = junit.getAllTypes()).length, l = 0; l < length3; ++l) {
                        final IType type = allTypes[l];
                        if (type.isInterface() || type.isClass()) {
                            if (!typeCounted) {
                                ++this.totalIUnits;
                                typeCounted = true;
                            }
                            ++this.totalTypes;
                            this.methodsCounter.add(type.getMethods().length);
                            this.totalMethods += type.getMethods().length;
                        }
                    }
                }
            }
            if (this.methodsCounter.size() != 0) {
                this.methodsMean = this.totalMethods
                        / this.methodsCounter.size();
                double sumOfSquareDifference = 0.0;
                for (final Integer i : this.methodsCounter) {
                    sumOfSquareDifference += Math
                            .pow(i - this.methodsMean, 2.0);
                    this.methodsStdDev = Math.sqrt(sumOfSquareDifference
                            / this.methodsCounter.size());
                }
            } else {
                this.methodsMean = 0.0;
                this.methodsStdDev = 0.0;
            }
        } catch (JavaModelException ex) {
            System.err
                    .println("Oops! An error occured when computing project information");
        }
    }

//    public static IResource getSelectedProject() {
//        IWorkbench iworkbench = PlatformUI.getWorkbench();
//        IWorkbenchWindow iworkbenchwindow = null;
//        IWorkbenchPage iworkbenchpage = null;
//        if (iworkbench != null && iworkbench.getWorkbenchWindows().length > 0) {
//            iworkbenchwindow = iworkbench.getWorkbenchWindows()[0];
//        }
//        if (iworkbenchwindow != null) {
//            iworkbenchpage = iworkbenchwindow.getActivePage();
//        }
//        ISelection selection = iworkbenchpage.getSelection();
//        //the current selection in the navigator view
//
//        return extractSelection(selection);
//    }

//    public static IResource extractSelection(ISelection sel) {
//        if (!(sel instanceof IStructuredSelection))
//            return null;
//        IStructuredSelection ss = (IStructuredSelection) sel;
//        Object element = ss.getFirstElement();
//        if (element instanceof IResource)
//            return (IResource) element;
//        if (!(element instanceof IAdaptable))
//            return null;
//        IAdaptable adaptable = (IAdaptable) element;
//        Object adapter = adaptable.getAdapter(IResource.class);
//        return (IResource) adapter;
//    }

//    public static IProject getProject(ISelection selection) {
//        IProject project = null;
//        if (selection != null && selection.isEmpty() == false
//                && selection instanceof IStructuredSelection) {
//            IStructuredSelection ssel = (IStructuredSelection) selection;
//            if (ssel.size() == 1) {
//                Object obj = ssel.getFirstElement();
//                if (ssel instanceof TreeSelection) {
//                    TreeSelection ts = (TreeSelection) ssel;
//                    obj = ts.getPaths()[0].getFirstSegment();
//                } else {
//                    obj = ssel.getFirstElement();
//                }
//                if (obj instanceof IJavaProject) {
//                    return ((IJavaProject) obj).getProject();
//                }
//                if (obj instanceof IResource) {
//                    project = getProject((IResource) obj);
//                } else if (obj instanceof IJavaProject) {
//                    project = ((IJavaProject) obj).getProject();
//                }
//            }
//        }
//        return project;
//    }
}