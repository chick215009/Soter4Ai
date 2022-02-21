package cn.edu.nju.traning;

import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;
import cn.edu.nju.controller.vo.AnalyzeResultVO;
import cn.edu.nju.core.Constants;
import cn.edu.nju.core.entity.CommitMessage;
import cn.edu.nju.core.entity.MyModule;
import cn.edu.nju.core.entity.TypeDescribe;
import cn.edu.nju.core.filter.*;
import cn.edu.nju.core.git.ChangedFile;
import cn.edu.nju.core.git.ChangedFile.TypeChange;
import cn.edu.nju.core.stereotype.stereotyped.*;
import cn.edu.nju.core.stereotype.taxonomy.CommitStereotype;
import cn.edu.nju.core.summarizer.CommitGeneralDescriptor;
import cn.edu.nju.core.summarizer.CommitStereotypeDescriptor;
import cn.edu.nju.core.summarizer.ModificationDescriptor;
import cn.edu.nju.core.summarizer.SummarizeType;
import cn.edu.nju.core.utils.JDTASTUtil;
import cn.edu.nju.core.utils.Utils;
import cn.edu.nju.core.visitor.MethodDeclarationStatisticsVisitor;
import cn.edu.nju.core.visitor.MethodInvocationStatisticsVisitor;
import cn.edu.nju.core.visitor.TypeReferencedVisitor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jgit.api.Git;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Data
public class SummarizeChangesForTrain {
    private ChangedFile[] differences;
    private StereotypeIdentifier stereotypeIdentifier;
    private List<ChangedFile> modifiedFiles;
    private List<ChangedFile> otherFiles;
    private String projectPath;
    private String olderVersionId;
    private String newerVersionId;
    private String projectName;
    private Git git;
    private List<StereotypeIdentifier> identifiers;
    private List<StereotypeIdentifier> typesProblem;
    private Map<String, StereotypeIdentifier> summarized = new LinkedHashMap<>();//全限定类名与StereotypeIdentifier
    private List<MyModule> modules;
    private boolean filtering;
    private double filterFactor;
    private String summary;
    private Set<File> dirContainsJava;
    private static Map<String, Integer> methodInvocationStatistics;
    private StringBuilder simpleDescribe;
    private StringBuilder detailDescribe;
    private CommitMessage commitMessage;
    private AnalyzeResultVO analyzeResultVO;


    static {
        methodInvocationStatistics = new HashMap<>();
    }

    public static Map<String, Integer> getMethodInvocationStatistics() {
        return methodInvocationStatistics;
    }

    public SummarizeChangesForTrain(Git git, String projectPath) {
        this.git = git;
        this.projectPath = projectPath;
    }

    public void initSummary(final ChangedFile[] differences) {
        this.differences = differences;
        this.identifiers = new ArrayList<StereotypeIdentifier>();
        this.summarized = new TreeMap<String, StereotypeIdentifier>();
        this.modifiedFiles = new LinkedList<>();
        this.otherFiles = new LinkedList<>();
        this.typesProblem = new LinkedList<>();
        this.modules = new ArrayList<>();
        this.summary = Constants.EMPTY_STRING;
        this.commitMessage = new CommitMessage();

        removeCreatedPackages();
    }


    private void analyze(SimpleDescribeFilter simpleDescribeFilter,
                         DetailDescribeFilter detailDescribeFilter) throws IOException, ClassNotFoundException {
//        fillMethodInvocationStatistics(projectPath);
        analyzeResultVO = new AnalyzeResultVO();
        analyzeResultVO.setProjectName(projectPath.substring(projectPath.lastIndexOf("/") + 1));
        for (final ChangedFile file : differences) {
            StereotypeIdentifier identifier = null;
            try {
                if (file.getAbsolutePath().endsWith(Constants.JAVA_EXTENSION)) {
                    String changeType = file.getChangeType();
                    Set analyzeTypes = new HashSet() {{
                        add(TypeChange.UNTRACKED.name());
                        add(TypeChange.ADDED.name());
                        add(TypeChange.REMOVED.name());
                        add(TypeChange.MODIFIED.name());
                    }};

                    if (analyzeTypes.contains(changeType)) {
                        if (changeType.equals(TypeChange.ADDED.name())) {
                            analyzeResultVO.setAddedCount(analyzeResultVO.getAddedCount() + 1);
                        }else if (changeType.equals(TypeChange.REMOVED.name())) {
                            analyzeResultVO.setRemovedCount(analyzeResultVO.getRemovedCount() + 1);
                        } else if (changeType.equals(TypeChange.MODIFIED.name())) {
                            analyzeResultVO.setChangedCount(analyzeResultVO.getChangedCount() + 1);
                        }
                        identifier = getIdentifyStereotypes(file, file.getChangeType());
                    }
                } else {
                    otherFiles.add(file);
                    continue;
                }

                if (identifier != null) {
                    fillSummarized(identifier, detailDescribeFilter);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        File projectDir = new File(projectPath);

//        TypeReferencedVisitor typeReferencedVisitor = new TypeReferencedVisitor(summarized);
//        traversalJavaFile(projectDir, typeReferencedVisitor);


//        for (Map.Entry<String, StereotypeIdentifier> entry : summarized.entrySet()) {
//            StereotypeIdentifier stereotypeIdentifier = entry.getValue();
//            if (stereotypeIdentifier.getBuilder().toString().contains("It allows to:")) {
//                int index = stereotypeIdentifier.getBuilder().toString().indexOf("It allows to:");
//                stereotypeIdentifier.getBuilder().insert(index, ImpactSetDescriptor.describe(stereotypeIdentifier));
//            }
//        }

        composeCommitMessage(simpleDescribeFilter, detailDescribeFilter);
    }

    public void fillMethodInvocationStatistics(String projectPath) {
        File projectDir = new File(projectPath);
        MethodDeclarationStatisticsVisitor methodDeclarationStatisticsVisitor = new MethodDeclarationStatisticsVisitor(methodInvocationStatistics);
        traversalJavaFile(projectDir, methodDeclarationStatisticsVisitor);
        MethodInvocationStatisticsVisitor methodInvocationStatisticsVisitor = new MethodInvocationStatisticsVisitor(methodInvocationStatistics);
        traversalJavaFile(projectDir, methodInvocationStatisticsVisitor);

        /*
        List<String> removedKeys = new ArrayList<>();
        Map<String, Integer> newMethodInvocationStatistics = new HashMap<>();
        for (Map.Entry<String, Integer> entry : methodInvocationStatistics.entrySet()) {
            String key = entry.getKey();
            removedKeys.add(key);
            String[] strings = key.split(";");

//            StringBuilder TypeQualifiedName = new StringBuilder();
            String typeQualifiedName = strings[0].replaceAll("/", ".");
            typeQualifiedName  = typeQualifiedName.substring(1);

//            String methodName = strings[1].substring(0, strings[1].indexOf("(") + 1) + strings[1].substring(strings[1].lastIndexOf("/") + 1);
            List<String> methodParameterFragment = new ArrayList<>();
            String methodName = "";
            for (int i = 1; i < strings.length; i++) {
                if (strings[i].contains("(")) {
                    methodName = strings[i].substring(strings[i].indexOf(".") + 1, strings[i].indexOf("("));
                }
                if (strings[i].contains(")")) {
                    break;
                }
                methodParameterFragment.add(strings[i].substring(strings[i].lastIndexOf("/") + 1));
            }

//            String methodName = strings[1].substring(strings[1].indexOf(".") + 1, strings[1].indexOf("("));
//            for (String s : strings[1].split(";")) {
//                methodNameFragment.add(s.substring(s.lastIndexOf("/") + 1));
//            }
//            StringBuilder sb = new StringBuilder();
//            sb.append(methodName);
//            sb.append("(");
//            for (String s : methodNameFragment) {
//                sb.append(s);
//                sb.append(",");
//            }
//            sb.setLength(sb.length() - 1);
//            sb.append(")");


            StringBuilder stringBuilder = new StringBuilder();
            if (!methodName.equals("")) {
                stringBuilder.append(".");
                stringBuilder.append(methodName);
            }
            if (methodParameterFragment.size() != 0) {
                stringBuilder.append("(");
                for (String s : methodParameterFragment) {
                    stringBuilder.append(s);
                    stringBuilder.append(",");
                }
                stringBuilder.setLength(stringBuilder.length() - 1);
                stringBuilder.append(")");
            } else {
                if (methodParameterFragment.size() == 0) {
                    stringBuilder.append("()");
                }
            }

            newMethodInvocationStatistics.put(typeQualifiedName + stringBuilder.toString(), methodInvocationStatistics.get(key));

         */
    }


    private void traversalJavaFile(File rootFile, ASTVisitor visitor) {
        if (rootFile.isDirectory()) {
            if (rootFile.getName().equals(".git") || rootFile.getName().equals("_MACOSX")) {
                return;
            }
            for (File listFile : rootFile.listFiles()) {
                traversalJavaFile(listFile, visitor);
            }
        } else {
            if (rootFile.getName().endsWith(Constants.JAVA_EXTENSION)) {
                CompilationUnit compilationUnit = JDTASTUtil.getCompilationUnit(rootFile.getAbsolutePath());
                compilationUnit.accept(visitor);
//                dirContainsJava.add(rootFile.getParentFile());
            }
        }
    }


    public StereotypeIdentifier getIdentifyStereotypes(final ChangedFile file, String scmOperation) throws CoreException {
        stereotypeIdentifier = new StereotypeIdentifier(file);
        stereotypeIdentifier.identifyStereotypes();
        stereotypeIdentifier.setScmOperation(scmOperation);
        stereotypeIdentifier.setChangedFile(file);
        identifiers.add(stereotypeIdentifier);
        return stereotypeIdentifier;
    }

    /**
     * 填充summarized
     * @param identifier
     */
    public void fillSummarized(StereotypeIdentifier identifier,
                               DetailDescribeFilter detailDescribeFilter) throws IOException, ClassNotFoundException {
        if (identifier.getStereotypedElements().size() == 0) { //如果该文件没有任何AST子节点
            typesProblem.add(identifier);
            return;
        }
        int i = 0;
        for (StereotypedElement element : identifier.getStereotypedElements()) {
            SummarizeType summarizeType = new SummarizeType(element, identifier, differences);
            if (i > 0) {
                summarizeType.setLocal(true);
            } else {
                summarizeType.setLocal(false);
            }
            //如果 identifier 不是MODIFIED类型
//            if (!identifier.getScmOperation().equals(TypeChange.MODIFIED.name())) {
////                summarizeType.generate(commitMessage);
////                if (null != summarizeType.getBuilder()) {
////                    identifier.getBuilder().append(summarizeType.getBuilder().toString());
////                }
//                identifier.getBuilder().append(summarizeType.generateLabelDescribe(
//                        new LabelTypeFilter(),
//                        new StereotypeTypeFilter(),
//                        new CategoryOrStereotypeMethodFilter()));
//            }
            String key = element.getQualifiedName();
            if (!key.contains(".")) {
                key = identifier.getParser().getCompilationUnit().getPackage().getName() + "." + element.getQualifiedName();
            }
            if (!summarized.containsKey(key) && !summarized.containsValue(identifier)) {
                summarized.put(key, identifier);
            }
            i++;
        }
    }

    protected void composeCommitMessage(SimpleDescribeFilter simpleDescribeFilter,
                                        DetailDescribeFilter detailDescribeFilter) throws IOException, ClassNotFoundException {
        String currentPackage = Constants.EMPTY_STRING;
        simpleDescribe = new StringBuilder();
        detailDescribe = new StringBuilder();
        StringBuilder desc = new StringBuilder();
        int i = 1;
        int j = 1;
        boolean isInitialCommit = Utils.isInitialCommit(git);

        if (detailDescribeFilter.isNewModuleDescribe()) {
            String newModuleDescribe = Constants.EMPTY_STRING;
            if (isInitialCommit) { //如果有新包名，为desc增加新描述
                fillNewModules();
                newModuleDescribe = describeNewModules();
            }

            detailDescribe.append(newModuleDescribe);
        }



        for (Map.Entry<String, StereotypeIdentifier> identifier : summarized.entrySet()) {
            StringBuilder descTmp = new StringBuilder(Constants.EMPTY_STRING);
//            StereotypeIdentifier calculated = identifiers.get(identifiers.indexOf(identifier.getValue()));
//            if (filtering && calculated != null && calculated.getImpactPercentage() <= (filterFactor)) {
//                continue;
//            }

//            if (i == 1) {
//                detailDescribe.append("This change set is mainly composed of: " + Constants.NEW_LINE);
////                desc.append(" This change set is mainly composed of:  \n\n");
//            }
            List<TypeDescribe> typeDescribes = null;
//            if (currentPackage.trim().equals(Constants.EMPTY_STRING)) {
//                currentPackage = identifier.getValue().getParser().getCompilationUnit().getPackage().getName().getFullyQualifiedName();
//                detailDescribe.append(Constants.NEW_LINE + i + ". Changes to package " + currentPackage + ": " + Constants.NEW_LINE);
//                Map<String, List<TypeDescribe>> packageAndTypes = commitMessage.getPackageAndTypes();
//                typeDescribes = new ArrayList<>();
//                packageAndTypes.put(currentPackage, typeDescribes);
////                desc.append(i + ". Changes to package " + currentPackage + ":  \n\n");
//                i++;
//                //如果是不同一个包的类
//            } else if (!currentPackage.equals(identifier.getValue().getParser().getCompilationUnit().getPackage().getName().getFullyQualifiedName())) {
//                String[] lines = detailDescribe.toString().trim().split("\\n");
//                if (lines != null && lines.length > 0) {
//                    String lastLine = lines[lines.length - 1];
//                    if (lastLine.contains("Changes to package " + currentPackage)) {
//                        lines[lines.length - 1] = Constants.NEW_LINE;
//                        StringBuilder builder = new StringBuilder();
//                        for (String line : lines) {
//                            builder.append(line + Constants.NEW_LINE);
//                        }
//                        detailDescribe = builder;
//                        //desc = new StringBuilder(StringUtils.join(lines, "\\n)"));
//                        i--;
//                    }
//                }
//                currentPackage = identifier.getValue().getParser().getCompilationUnit().getPackage().getName().getFullyQualifiedName();
//                detailDescribe.append(Constants.NEW_LINE + i + ". Changes to package " + currentPackage + ": " + Constants.NEW_LINE);
//                Map<String, List<TypeDescribe>> packageAndTypes = commitMessage.getPackageAndTypes();
//                typeDescribes = new ArrayList<>();
//                packageAndTypes.put(currentPackage, typeDescribes);
////                desc.append(i + ". Changes to package " + currentPackage + ":  \n\n");
//                j = 1;
//                i++;
//            }
            //如果属于modified
            if (identifier.getValue().getScmOperation().equals(TypeChange.MODIFIED.toString())) {
                ModificationDescriptorForTrain modificationDescriptor = new ModificationDescriptorForTrain();
                modificationDescriptor.setDifferences(differences);
                modificationDescriptor.setFile(identifier.getValue().getChangedFile());
                modificationDescriptor.setGit(git);
                if (olderVersionId == null) {
                    modificationDescriptor.extractDifferences(identifier.getValue().getChangedFile(), git);
                } else {
                    modificationDescriptor.extractDifferencesBetweenVersions(identifier.getValue().getChangedFile(), git,
                            olderVersionId, newerVersionId);
                }

                modificationDescriptor.extractModifiedMethods();

                if (typeDescribes == null) {
                    typeDescribes = new ArrayList<>();
                }
                modificationDescriptor.describe(i, j, descTmp, typeDescribes);
            } else {
                StringBuilder nonModifiedDescribe = new StringBuilder();
                Set<String> typeReferenceStatistic = identifier.getValue().getTypeReferenceStatistic();
                List<String> referenceList = new ArrayList<>(typeReferenceStatistic);
                String typeName = identifier.getKey().substring(identifier.getKey().lastIndexOf(".") + 1);
                int x = 0;
                for (StereotypedElement element : identifier.getValue().getStereotypedElements()) {
                    if (!(element instanceof StereotypedType)) {
                        continue;
                    }
                    StereotypedType stereotypedType = (StereotypedType) element;
                    String stereotypedTypeName = stereotypedType.getName();
                    List<String> list = new ArrayList<>();
                    if (stereotypedTypeName.equals(typeName)) {
                        list = referenceList;
                    }
                    SummarizeType summarizeType = new SummarizeType(element, identifier.getValue(), differences, list);
                    if (x > 0) {
                        summarizeType.setLocal(true);
                    } else {
                        summarizeType.setLocal(false);
                    }

                    //如果 identifier 不是MODIFIED类型
                    if (!identifier.getValue().getScmOperation().equals(TypeChange.MODIFIED.name())) {
                        String describe = summarizeType.generateLabelDescribe(
                                detailDescribeFilter.getLabelTypeFilter(),
                                detailDescribeFilter.getStereotypeTypeFilter(),
                                detailDescribeFilter.getCategoryOrStereotypeMethodFilter());
                        nonModifiedDescribe.append(describe);
                    }

                    if (!identifier.getValue().getChangedFile().isRenamed()) {
                        if (!StringUtils.isBlank(nonModifiedDescribe)) {
//                            descTmp.append((i - 1) + "." + j + ". " + nonModifiedDescribe);
                            descTmp.append(nonModifiedDescribe);
                        }
                    } else {
//                        descTmp.append((i - 1) + "." + j + ". " + "Rename type " + identifier.getValue().getChangedFile().getRenamedPath().substring(identifier.getValue().getChangedFile().getRenamedPath().lastIndexOf("/") + 1).replace(Constants.JAVA_EXTENSION, Constants.EMPTY_STRING) + " with " + identifier.getValue().getChangedFile().getName().replace(Constants.JAVA_EXTENSION, "\n\n"));
                        descTmp.append("Rename type " + identifier.getValue().getChangedFile().getRenamedPath().substring(identifier.getValue().getChangedFile().getRenamedPath().lastIndexOf("/") + 1).replace(Constants.JAVA_EXTENSION, Constants.EMPTY_STRING) + " with " + identifier.getValue().getChangedFile().getName().replace(Constants.JAVA_EXTENSION, "\n\n"));
                    }
                    x++;
                }

            }
            if (!descTmp.toString().equals(Constants.EMPTY_STRING)) {
                detailDescribe.append(descTmp.toString());
                j++;
            }
        }

//        if (simpleDescribeFilter.isSimpleDescribe()) {
//            createGeneralDescriptor(simpleDescribe, isInitialCommit);
//            //Commit stereotype description
//            String summarizeCommitStereotype = summarizeCommitStereotype();
//            simpleDescribe.insert(0, summarizeCommitStereotype);
//
//            if (isInitialCommit) {
//                simpleDescribe.insert(0, "Initial commit. ");
////            desc.insert(0, "Initial commit. ");
//            } else {
//                simpleDescribe.insert(0, "BUG - FEATURE: <type-ID> \n\n");
////            desc.insert(0, "BUG - FEATURE: <type-ID> \n\n");
//            }
//        }

//        String[] lines = detailDescribe.toString().trim().split("\\n");
//        if (lines != null && lines.length > 0) {
//            String lastLine = lines[lines.length - 1];
//            if (lastLine.contains("Changes to package " + currentPackage)) {
//                lines[lines.length - 1] = "\n\n";
//                //desc = new StringBuilder(StringUtils.join(lines, "\\n"));
//            }
//        }
        String summary = "";
        if (simpleDescribeFilter.isSimpleDescribe() && detailDescribeFilter.isDetailDescribe()) {
            summary = simpleDescribe.toString() + detailDescribe.toString();
        } else if (simpleDescribeFilter.isSimpleDescribe() && !detailDescribeFilter.isDetailDescribe()) {
            detailDescribe = new StringBuilder();
            summary = simpleDescribe.toString();
        } else if (!simpleDescribeFilter.isSimpleDescribe() && detailDescribeFilter.isDetailDescribe()){
            simpleDescribe = new StringBuilder();
            summary = detailDescribe.toString();
        }
        this.setSummary(summary);
        analyzeResultVO.setSimpleDescribe(simpleDescribe.toString());
        analyzeResultVO.setDetailDescribe(detailDescribe.toString());
        removeCreatedPackages();
    }

    protected void fillNewModules() {
        for (StereotypeIdentifier identifier : identifiers) {
            String fullyQualifiedPackageName = identifier.getParser().getCompilationUnit().getPackage().getName().getFullyQualifiedName();
            MyModule myModule = new MyModule(fullyQualifiedPackageName);
            if (!modules.contains(myModule)) {
                modules.add(myModule);
            }
        }
//        for (StereotypeIdentifier identifier : identifiers) {
//            try {
//                IType[] allTypes = null;
//                if (identifier.getCompilationUnit() != null ) {
//                    allTypes = identifier.getCompilationUnit().getAllTypes();
//                    for (IType iType : allTypes) {
//                        MyModule module = createModuleFromPackageElement(iType);
//                        if (!modules.contains(module)) {
//                            modules.add(module);
//                        }
//                    }
//                }
//
//
//
//            } catch (JavaModelException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
    }

    protected String describeNewModules() {
        if (modules != null && modules.size() == 0) {
            return "";
        }
        StringBuilder descTmp = new StringBuilder(Constants.EMPTY_STRING);
        String connector = (modules.size() == 1) ? " this new module" : " these new modules";
        descTmp.append("The commit includes" + connector + ": \n\n");
        for (MyModule module : modules) {
            if (!descTmp.toString().contains("\t- " + module.getName() + Constants.NEW_LINE)) {
                descTmp.append("\t- " + module.getName() + Constants.NEW_LINE);
            }
        }
        descTmp.append(Constants.NEW_LINE);

        return descTmp.toString();
    }

    private CommitGeneralDescriptor createGeneralDescriptor(StringBuilder desc,
                                                            boolean isInitialCommit) {
        CommitGeneralDescriptor generalDescriptor = new CommitGeneralDescriptor();
        generalDescriptor.setDifferences(differences);
        generalDescriptor.setInitialCommit(isInitialCommit);
        generalDescriptor.setGit(git);
        desc.insert(0, generalDescriptor.describe());
        return generalDescriptor;
    }

    public String summarizeCommitStereotype() {
        List<StereotypedMethod> methods = new ArrayList<StereotypedMethod>();
        String result = Constants.EMPTY_STRING;

        for (StereotypeIdentifier identifier : identifiers) {
            for (StereotypedElement element : identifier.getStereotypedElements()) {
                if (!identifier.getScmOperation().equals(TypeChange.MODIFIED.name()) && !identifier.getChangedFile().isRenamed()) {
                    for (StereotypedElement stereoSubElement : element.getStereoSubElements()) {
                        if (stereoSubElement instanceof StereotypedMethod) {
                            methods.add((StereotypedMethod) stereoSubElement);
                        }
                    }
//                    methods.addAll(List<? extends StereotypedMethod>) element.getStereoSubElements());
                } else {
                    List<StructureEntityVersion> modifiedMethods = identifier.getChangedFile().getModifiedMethods();
                    if (modifiedMethods != null) {
                        for (StructureEntityVersion structureEntityVersion : modifiedMethods) {
                            StereotypedElement stereotypedMethod = getStereotypedElementFromName(
                                    element,
                                    structureEntityVersion);
                            if (stereotypedMethod != null) {
                                methods.add((StereotypedMethod) stereotypedMethod);
                            }
                        }
                    }
                }
            }
        }

        if (methods.size() > 0) {
            StereotypedCommit stereotypedCommit = new StereotypedCommit(methods);
            analyzeResultVO.setMethodStatisticJson(stereotypedCommit.buildSignature());
            CommitStereotype stereotype = stereotypedCommit.findStereotypes();
            analyzeResultVO.setCommitStereotype(stereotype.name());

            if (stereotype != null) {
                result = CommitStereotypeDescriptor.describe(stereotypeIdentifier.getCompilationUnit(), stereotypedCommit);
            }
        }

        return result;
    }

    public StereotypedElement getStereotypedElementFromName(StereotypedElement element, StructureEntityVersion searchedElement) {
        StereotypedElement result = null;
        if (element.getStereoSubElements() != null) {
            for (StereotypedElement stereotyped : element.getStereoSubElements()) {
                if (stereotyped.getFullyQualifiedName().equals(searchedElement.getJavaStructureNode().getFullyQualifiedName()) ||
                        searchedElement.getJavaStructureNode().getFullyQualifiedName().endsWith(stereotyped.getFullyQualifiedName())) {
                    result = stereotyped;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 删除临时用于存储上一版本项目的缓存
     */
    protected void removeCreatedPackages() {
        IFolder folder = null;
        String tmpFolderPath = "src/commsummtmp";
        if (null != projectPath && !projectPath.isEmpty()) {
            File tmpFolder = new File(projectPath + System.getProperty("file.separator") + tmpFolderPath);
            if (tmpFolder.exists()) {
                tmpFolder.delete();
            }
        }
    }

    private MyModule createModuleFromPackageElement(IType iType) {
        String packageName = iType.getPackageFragment().getElementName();
        String extractedName = packageName.substring(packageName.lastIndexOf("."));

        return new MyModule(packageName + extractedName);
    }

    public void summarize(final ChangedFile[] differences, SimpleDescribeFilter simpleDescribeFilter, DetailDescribeFilter detailDescribeFilter) throws IOException, ClassNotFoundException {
        initSummary(differences);
        analyze(simpleDescribeFilter, detailDescribeFilter);
    }


}
