package cn.edu.nju.core.integration;//package cn.edu.nju.integration;
//
//import ch.uzh.ifi.seal.changedistiller.ChangeDistiller;
//import ch.uzh.ifi.seal.changedistiller.distilling.FileDistiller;
//import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
//import cn.edu.nju.model.*;
//import cn.edu.nju.stereotype.stereotyped.StereotypeIdentifier;
//import cn.edu.nju.stereotype.stereotyped.StereotypedElement;
//import cn.edu.nju.stereotype.stereotyped.StereotypedMethod;
//import cn.edu.nju.stereotype.stereotyped.StereotypedType;
//import cn.edu.nju.stereotype.visitor.ImpactVisitor;
//import cn.edu.nju.utils.JDTASTUtil;
//import co.edu.unal.colswe.changescribe.core.util.Utils;
//import org.apache.commons.lang3.StringUtils;
//import org.eclipse.core.runtime.CoreException;
//import org.eclipse.jdt.core.dom.*;
//import org.eclipse.jgit.api.Git;
//import org.eclipse.jgit.api.Status;
//import org.eclipse.jgit.api.errors.GitAPIException;
//import org.eclipse.jgit.lib.Repository;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.*;
//
//public class Descriptors {
//    //文件分割符
//    final static String fileSeparator = System.getProperty("file.separator");
//
//    //提取代码变更需要使用的类
//    private static FileDistiller distiller = ChangeDistiller.createFileDistiller(ChangeDistiller.Language.JAVA);
//
//    /**
//     *
//     * @param projectPath 项目路径
//     */
//    public void scribeChanges(String projectPath) throws IOException, GitAPIException, CoreException {
//        File projectFile = new File(projectPath);
//        String dotGitDirPath = projectPath + fileSeparator + ".git";
//        File gitFile = new File(dotGitDirPath);
//
//        //获取 git
//        Git git;
////        Utils.getFileContentOfLastCommit()
//        if (!gitFile.exists()) {
//            git = Git.init()
//                    .setDirectory(projectFile)
//                    .call();
//        } else {
//            git = Git.open(gitFile);
//        }
//        git.add().addFilepattern(".").call();
//        Repository repository = git.getRepository();
//        Status status = git.status().call();
//
//        ScribeContent scribeContent = new ScribeContent();//所有生成的内容存储在这个类当中
//        List<BasicAnalyzedFile> analyzedFiles = analyzedProject(projectPath);
//        calculateImpact(analyzedFiles);
//    }
//
//    public List<BasicAnalyzedFile> analyzedProject(String projectPath) {
//        List<BasicAnalyzedFile> analyzedFiles = new LinkedList<>();
//        List<String> paths = new LinkedList<>();
//        getAllFilePath(new File(projectPath), paths);
//        for (String path : paths) {
//            BasicAnalyzedFile basicAnalyzedFile = new BasicAnalyzedFile(projectPath, path);
//            StereotypeIdentifier stereotypeIdentifier = new StereotypeIdentifier(path);
//            stereotypeIdentifier.identifyStereotypes();
//
//            basicAnalyzedFile.setPackageName(stereotypeIdentifier.getCompilationUnit().getPackage().getName().toString());
//            for (Object o : stereotypeIdentifier.getCompilationUnit().imports()) {
//                String importName = ((ImportDeclaration) o).getName().getFullyQualifiedName();
//                basicAnalyzedFile.getImports().add(new ImportModel(importName));
//            }
//
//            for (Object o : stereotypeIdentifier.getCompilationUnit().getCommentList()) {
//                String comment = "";
//                if (o instanceof Javadoc) {
//                    comment = javaDocToString((Javadoc) o);
//                }
//                else if (o instanceof LineComment) {
//                    comment = ((LineComment)o).toString();
//                }
//                if (!StringUtils.isBlank(comment)) {
//                    basicAnalyzedFile.getComments().add(new CommentModel(comment));
//                }
//            }
//
//
//            for (StereotypedElement stereotypedElement : stereotypeIdentifier.getStereotypedElements()) {
//                if (stereotypedElement instanceof StereotypedType) {
//                    StereotypedType stereotypedType = (StereotypedType) stereotypedElement;
//                    ClassModel classModel = createClassModel(stereotypedType);
//                    basicAnalyzedFile.getClasses().add(classModel);
//                }
//            }
//
//            analyzedFiles.add(basicAnalyzedFile);
//        }
//
//
//        return analyzedFiles;
//
//    }
//
//    public void getAllFilePath(File projectFile, List<String> resultList) {
//
//        File[] files = projectFile.listFiles();
//        for (File file : files) {
//            if (file.isFile()) {
//                if (file.getName().endsWith(".xml")) {
//                    continue;
//                }
//                if (file.getName().endsWith(".java")) {
//                    resultList.add(file.getAbsolutePath());
//                }
//
//            }
//            if (file.isDirectory()) {
//                if (file.getName().equals(".DS_Store") || file.getName().equals(".git") ) {
//                    continue;
//                }
//                getAllFilePath(file, resultList);
//            }
//
//        }
//    }
//
//
//
//    public void getDifferences(Status repositoryStatus, String projectPath, ScribeContent scribeContent, Repository repository) throws IOException, CoreException {
//        for (String path : repositoryStatus.getAdded()) { //path 为 source path
//            StereotypeIdentifier stereotypeIdentifier = new StereotypeIdentifier(projectPath + fileSeparator + path);
//            stereotypeIdentifier.identifyStereotypes();
//            ChangedFile changedFile = new ChangedFile(path, ChangedFile.ChangeType.ADDED, projectPath);
//            changedFile.setStereotypeIdentifier(stereotypeIdentifier);
//            scribeContent.getAddedFiles().add(changedFile);
//        }
//
//        for (String path : repositoryStatus.getMissing()) {
//            StereotypeIdentifier stereotypeIdentifier = new StereotypeIdentifier(projectPath + fileSeparator + path);
//            stereotypeIdentifier.identifyStereotypes();
//            ChangedFile changedFile = new ChangedFile(path, ChangedFile.ChangeType.REMOVED, projectPath);
//            changedFile.setStereotypeIdentifier(stereotypeIdentifier);
//            scribeContent.getMissingFiles().add(changedFile);
//        }
//
//        for (String path : repositoryStatus.getChanged()) {
//            File oldFile = Utils.getFileContentOfLastCommit(path, repository);
//            File newFile = new File(projectPath + File.separator + path);
//            distiller.extractClassifiedSourceCodeChanges(oldFile, newFile);
//            List<SourceCodeChange> changes = distiller.getSourceCodeChanges();
//
//            StereotypeIdentifier stereotypeIdentifier = new StereotypeIdentifier(newFile);
//            stereotypeIdentifier.identifyStereotypes();
//            ChangedFile changedFile = new ModifiedFile(newFile.getAbsolutePath(), oldFile.getPath(), ChangedFile.ChangeType.MODIFIED, projectPath, changes);
//            changedFile.setStereotypeIdentifier(stereotypeIdentifier);
//            scribeContent.getModifiedFiles().add(changedFile);
//        }
//    }
//
//    public void calculateImpact(List<BasicAnalyzedFile> analyzedFiles) {
//        Map<String, Integer> impactMap = new HashMap<>();
//        for (BasicAnalyzedFile analyzedFile : analyzedFiles) {
//            CompilationUnit compilationUnit = JDTASTUtil.getCompilationUnit(analyzedFile.getAbsolutePath());
//            ImpactVisitor impact = new ImpactVisitor(impactMap);
//            compilationUnit.accept(impact);
//        }
//        for (BasicAnalyzedFile analyzedFile : analyzedFiles) {
//            for (ClassModel aClass : analyzedFile.getClasses()) {
//                if (impactMap.containsKey(aClass.getQualifiedName())) {
//                    aClass.setReferencesCount(impactMap.get(aClass.getQualifiedName()));
//                }
//            }
//        }
//    }
//
//    public void calculateImpact(ScribeContent scribeContent) {
//        Map<String, Integer> impactMap = new HashMap<>();
//        for (ChangedFile addedFile : scribeContent.addedFiles) {
//            CompilationUnit compilationUnit = JDTASTUtil.getCompilationUnit(addedFile.getAbsolutePath());
//
//
//            ImpactVisitor impactVisitor = new ImpactVisitor(impactMap);
//            compilationUnit.accept(impactVisitor);
//        }
//
//        for (ChangedFile addedFile : scribeContent.addedFiles) {
//            String fileName = addedFile.getName();
//            fileName = fileName.substring(0, fileName.indexOf("."));
//            if (impactMap.containsKey(fileName)) {
//                addedFile.setPreferenceCount(impactMap.get(fileName));
//            }
//        }
//    }
//
//    public String javaDocToString (Javadoc doc) {
//        if (doc == null) {
//            return null;
//        }
//        StringBuilder stringBuilder = new StringBuilder();
//        for (Object o : doc.tags()) {
//            stringBuilder.append(o.toString());
//        }
//
//        return stringBuilder.toString();
//    }
//
//    public ClassModel createClassModel(StereotypedType stereotypedType) {
//        TypeDeclaration typeDeclaration = stereotypedType.getType();
//
//        Javadoc typeDeclarationJavadoc = typeDeclaration.getJavadoc();
//        String typeDocComment = javaDocToString(typeDeclarationJavadoc);
//
//        String typeQualifiedName = typeDeclaration.resolveBinding().getQualifiedName();
//
//        String primaryStereotypeName = null;
//        if (stereotypedType.getPrimaryStereotype() != null) {
//            primaryStereotypeName = stereotypedType.getPrimaryStereotype().name();
//        }
//        ClassModel classModel = new ClassModel(typeQualifiedName, typeDocComment, primaryStereotypeName);
//
//        for (IVariableBinding field : stereotypedType.getFields()) {
//            if (field == null) {
//                continue;
//            }
//            String qualifiedName = null;
//            if (field.getType() != null) {
//                qualifiedName = field.getType().getQualifiedName();
//            }
//            String name = field.getName();
//            FieldModel fieldModel = new FieldModel(qualifiedName, name);
//            classModel.getFields().add(fieldModel);
//        }
//
//        for (StereotypedMethod method : stereotypedType.getTotalMethods()) {
//            String qualifiedName = null;
//            try {
//                qualifiedName = method.getFullyQualifiedName();
//            } catch (Exception e) {
//
//            }
//            String name = method.getQualifiedName();
//            String methodDocComment = null;
//            if (method.getJavadoc() != null) {
//                methodDocComment = javaDocToString(method.getJavadoc());
//            }
//            String category = method.getPrimaryStereotype().getSubcategory().getName();
//            MethodModel methodModel = new MethodModel(qualifiedName, name, methodDocComment, category);
//            classModel.getMethods().add(methodModel);
//        }
//
//        for (StereotypedType type: stereotypedType.getStereotypedSubTypes()) {
//            ClassModel childClassModel = createClassModel(type);
//            classModel.getClasses().add(childClassModel);
//        }
//
//        return classModel;
//    }
//}
