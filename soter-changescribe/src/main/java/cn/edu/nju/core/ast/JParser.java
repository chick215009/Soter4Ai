package cn.edu.nju.core.ast;

import cn.edu.nju.core.Constants;
import cn.edu.nju.core.git.ChangedFile;
import cn.edu.nju.core.utils.JDTASTUtil;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.Document;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class JParser {
    private CompilationUnit unit;
    private List<ASTNode> elements;
    private Document document;
    private ASTRewrite astRewrite;
    private
    IMember member;

    public JParser(final ICompilationUnit unit) {
        super();
        final ASTParser parser = ASTParser.newParser(4);
        parser.setResolveBindings(true);
        parser.setKind(8);

        parser.setSource(unit);

        this.unit = (CompilationUnit) parser.createAST((IProgressMonitor)null);
        this.elements = new ArrayList<ASTNode>();

    }

    public JParser(ChangedFile changedFile) {
        ASTParser astParser = JDTASTUtil.getASTParser(changedFile.getAbsolutePath());
        this.unit = (CompilationUnit) astParser.createAST(null);

        IProblem[] problems = this.unit.getProblems();
//        if (problems != null && problems.length > 0) {
//            System.out.println("Got {} problems compiling the source file: " + problems.length);
//            for (IProblem problem : problems) {
//                System.out.println("Got {} problems compiling the source file: " + problem);
//            }
//        }

        this.elements = new ArrayList<>();
    }

//    public JParser(final File file) throws CoreException {
//        super();
//        final ASTParser parser = ASTParser.newParser(4);
//        parser.setKind(8);
//        parser.setResolveBindings(true);
//        parser.setSource(fileToString(file));
//
//        try {
//            String projectName = ProjectInformation.getProject(
//                    ProjectInformation.getSelectedProject()).getName();
//            if (ProjectInformation.getProject(
//                    ProjectInformation.getSelectedProject()).hasNature(
//                    JavaCore.NATURE_ID)) {
//                IJavaProject project = JavaCore.create(
//                        ProjectInformation.getSelectedProject().getWorkspace()
//                                .getRoot()).getJavaProject(projectName);
//                project.open((IProgressMonitor) null);
//                parser.setProject(project);
//            }
//        } catch (NoClassDefFoundError e) {
//            // TODO: handle exception
//        }
//        this.unit = (CompilationUnit) parser.createAST((IProgressMonitor)null);
//
//        IProblem[] problems = this.unit.getProblems();
//        if (problems != null && problems.length > 0) {
//            System.out.println("Got {} problems compiling the source file: " + problems.length);
//            for (IProblem problem : problems) {
//                System.out.println("Got {} problems compiling the source file: " + problem);
//            }
//        }
//        this.elements = new ArrayList<ASTNode>();
//    }

    @SuppressWarnings("resource")
    private static char[] fileToString(final File file) {
        BufferedReader in = null;
        final StringBuffer buffer = new StringBuffer();
        try {
            in = new BufferedReader(new FileReader(file));
            String line = null;
            while (null != (line = in.readLine())) {
                buffer.append(line).append(Constants.NEW_LINE);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return buffer.toString().toCharArray();
    }

    public JParser(final IMember member) {
        this(member.getCompilationUnit());
        this.member = member;
    }

    /**
     * 填充elements
     */
    public void parse() {
        if (this.member != null) {
            if (this.member instanceof IType) {
                this.elements.add(this.unit.findDeclaringNode(((IType)this.member).getKey()));
            }
            else if (this.member instanceof IMethod) {
                final ASTParser parser = ASTParser.newParser(4);
                parser.setProject((this.member).getJavaProject());
                parser.setResolveBindings(true);
                final IBinding binding = parser.createBindings(new IJavaElement[] { (IMethod)this.member }, (IProgressMonitor)null)[0];
                if (binding instanceof IMethodBinding) {
                    final ASTNode method = this.unit.findDeclaringNode(((IMethodBinding)binding).getKey());
                    this.elements.add(method);
                }
            }
        }
        else {
            for (final Object o : this.unit.types()) {
                if (!(o instanceof TypeDeclaration)) {
                    continue;
                }
                this.elements.add((ASTNode)o);
            }
        }
    }

    public List<ASTNode> getElements() {
        return this.elements;
    }

    public CompilationUnit getCompilationUnit() {
        return this.unit;
    }

    public Document getDocument() {
        return this.document;
    }

    public ASTRewrite getAstRewrite() {
        if (this.astRewrite == null) {
            this.astRewrite = ASTRewrite.create(this.unit.getAST());
        }
        return this.astRewrite;
    }
}
