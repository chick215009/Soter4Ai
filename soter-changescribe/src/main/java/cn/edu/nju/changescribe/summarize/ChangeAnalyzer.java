package cn.edu.nju.changescribe.summarize;

import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;
import cn.edu.nju.changescribe.domain.*;
import cn.edu.nju.core.Constants;
import cn.edu.nju.core.entity.MyModule;
import cn.edu.nju.core.filter.DetailDescribeFilter;
import cn.edu.nju.core.git.ChangedFile;
import cn.edu.nju.core.git.SCMRepository;
import cn.edu.nju.core.label.LabelMethod;
import cn.edu.nju.core.label.TypeLabel;
import cn.edu.nju.core.stereotype.stereotyped.*;
import cn.edu.nju.core.stereotype.taxonomy.CodeStereotype;
import cn.edu.nju.core.stereotype.taxonomy.CommitStereotype;
import cn.edu.nju.core.stereotype.taxonomy.MethodStereotype;
import cn.edu.nju.core.stereotype.taxonomy.TypeStereotype;
import cn.edu.nju.core.summarizer.CommitStereotypeDescriptor;
import cn.edu.nju.core.summarizer.ModificationDescriptor;
import cn.edu.nju.core.summarizer.SummarizeType;
import cn.edu.nju.core.textgenerator.phrase.MethodPhraseGenerator;
import cn.edu.nju.core.utils.Utils;
import cn.edu.nju.core.visitor.TypeReferencedVisitor;
import cn.edu.nju.github.entity.CommitEntity;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ChangeAnalyzer {
    private String projectPath;
    //该类型的Java文件都会被用于分析
    private static Set analyzeTypes = new HashSet() {{
        add(ChangedFile.TypeChange.UNTRACKED.name());
        add(ChangedFile.TypeChange.ADDED.name());
        add(ChangedFile.TypeChange.REMOVED.name());
        add(ChangedFile.TypeChange.MODIFIED.name());
    }};

    private List<StereotypeIdentifier> identifiers;
    private List<ChangedFile> otherFiles;
    //存储没有AST子节点
    private List<StereotypeIdentifier> typesProblem;
    private Map<String, StereotypeIdentifier> summarized;
    private List<MyModule> newModules;
    private Git git;

    public void changeAnalyzerInit() {
        identifiers = new ArrayList<>();
        otherFiles = new ArrayList<>();
        typesProblem = new ArrayList<>();
        summarized = new LinkedHashMap<>();
        newModules = new ArrayList<>();
    }

    public ChangeAnalyzer(String projectPath) {
        this.projectPath = projectPath;
        changeAnalyzerInit();
    }

    public void analyze() {
        try {
            SCMRepository scmRepository = new SCMRepository(projectPath);
            git = scmRepository.getGit();
            Status status = scmRepository.getStatus();
            Set<ChangedFile> differences = SCMRepository.getDifferences(status, projectPath);
            //初始化summarized
            fillSummarized(differences);
            fillNewModules();
            SummaryEntity summaryEntity = analyzeCommitEntity();

            StereotypedCommit stereotypedCommit = getStereotypedCommit();
            String signature = stereotypedCommit.buildSignature();
            CommitStereotype stereotype = stereotypedCommit.findStereotypes();
            String name = stereotype.name();
            String result = CommitStereotypeDescriptor.describe(stereotypedCommit);

            boolean isInitialCommit = Utils.isInitialCommit(git);

            StringBuilder simpleDescribe = new StringBuilder();
            if (isInitialCommit) {
                simpleDescribe.append("Initial commit. \n");
            } else {
                simpleDescribe.append("BUG - FEATURE: <type-ID> \n");
            }
            simpleDescribe.append(result);

            summaryEntity.setSimpleDescribe(simpleDescribe.toString());
            summaryEntity.setNewModuleDescribe(describeNewModules());
            summaryEntity.setIsInitialCommit(isInitialCommit);
            summaryEntity.setCommitStereotype(name);
            summaryEntity.setMethodStatisticJson(signature);

        } catch (GitAPIException e) {
            e.printStackTrace();
        }

    }

    public String getSimpleDescribe() {
        StereotypedCommit stereotypedCommit = getStereotypedCommit();
        if (stereotypedCommit != null) {
            String signature = stereotypedCommit.buildSignature();
            CommitStereotype stereotype = stereotypedCommit.findStereotypes();
            String name = stereotype.name();
            String result = CommitStereotypeDescriptor.describe(stereotypedCommit);
            return result;
        } else {
            return "";
        }
    }

    public StereotypedCommit getStereotypedCommit() {
        List<StereotypedMethod> methods = new ArrayList<StereotypedMethod>();
        String result = Constants.EMPTY_STRING;

        for (StereotypeIdentifier identifier : identifiers) {
            for (StereotypedElement element : identifier.getStereotypedElements()) {
                if (!identifier.getScmOperation().equals(ChangedFile.TypeChange.MODIFIED.name()) && !identifier.getChangedFile().isRenamed()) {
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
            return new StereotypedCommit(methods);
        } else {
            return null;
        }
    }

    public String summarizeCommitStereotype() {
        List<StereotypedMethod> methods = new ArrayList<StereotypedMethod>();
        String result = Constants.EMPTY_STRING;

        for (StereotypeIdentifier identifier : identifiers) {
            for (StereotypedElement element : identifier.getStereotypedElements()) {
                if (!identifier.getScmOperation().equals(ChangedFile.TypeChange.MODIFIED.name()) && !identifier.getChangedFile().isRenamed()) {
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
            String signature = stereotypedCommit.buildSignature();
            CommitStereotype stereotype = stereotypedCommit.findStereotypes();
            stereotype.name();
            if (stereotype != null) {
                result = CommitStereotypeDescriptor.describe(stereotypedCommit);
            }
        }

        return result;
    }

    public StereotypedElement getStereotypedElementFromName(StereotypedElement element, StructureEntityVersion searchedElement) {
        StereotypedElement result = null;
        if (element.getStereoSubElements() != null) {
            for (StereotypedElement stereotyped : element.getStereoSubElements()) {
//                if (stereotyped.getFullyQualifiedName().equals(searchedElement.getJavaStructureNode().getFullyQualifiedName()) ||
//                        searchedElement.getJavaStructureNode().getFullyQualifiedName().endsWith(stereotyped.getFullyQualifiedName())) {
//                    result = stereotyped;
//                    break;
//                }
                if (stereotyped.getFullyQualifiedName().equals(searchedElement.getUniqueName()) ||
                        searchedElement.getUniqueName().endsWith(stereotyped.getFullyQualifiedName())) {
                    result = stereotyped;
                    break;
                }
            }
        }
        return result;
    }

    public SummaryEntity analyzeCommitEntity() {
        SummaryEntity summaryEntity = new SummaryEntity();
        String currentPackage = "";
        for (Map.Entry<String, StereotypeIdentifier> entry : summarized.entrySet()) {
            String key = entry.getKey();
            StereotypeIdentifier identifier = entry.getValue();
            if (currentPackage.trim().equals("") //第一次遍历
            || !currentPackage.trim().equals(identifier.getParser().getCompilationUnit().getPackage().getName().getFullyQualifiedName())) { //遍历到新的包
                currentPackage = identifier.getParser().getCompilationUnit().getPackage().getName().getFullyQualifiedName();
                PackageEntity packageEntity = new PackageEntity();
                packageEntity.setPackageName(currentPackage);
                summaryEntity.getPackageEntityList().add(packageEntity);
            }


            String typeName = key.substring(key.lastIndexOf(".") + 1);
            if (identifier.getScmOperation().equals(ChangedFile.TypeChange.MODIFIED.toString())) {
                FileEntity fileEntity = new FileEntity(identifier.getScmOperation(), true, identifier.getChangedFile().getName());
                ModificationDescriptor modificationDescriptor = new ModificationDescriptor();
                modificationDescriptor.setFile(identifier.getChangedFile());
                modificationDescriptor.setGit(git);
                modificationDescriptor.extractDifferences(identifier.getChangedFile(), git);//获得该文件changes
                modificationDescriptor.extractModifiedMethods();//获取发生变化的函数
                String describe = modificationDescriptor.describe();
                fileEntity.setChangeDescribe(describe);
                summaryEntity.getPackageEntityList().get(summaryEntity.getPackageEntityList().size() - 1).getFileEntityList().add(fileEntity);
            } else {
                FileEntity fileEntity = new FileEntity(identifier.getScmOperation(), false, identifier.getChangedFile().getName());
                for (StereotypedElement element : identifier.getStereotypedElements()) {
                    if (!(element instanceof StereotypedType)) {
                        continue;
                    }
                    StereotypedType stereotypedType = (StereotypedType) element;
                    String typeLabel = getTypeLabel(stereotypedType);
                    List<String> interfaceList = getInterfaceList(stereotypedType);
                    String superclassStr = getSuperclassStr(stereotypedType);
                    String typeStereotypeLabel = getTypeStereotypeLabel(stereotypedType);

                    TypeEntity typeEntity = new TypeEntity();
                    typeEntity.setTypeName(typeName);
                    typeEntity.setTypeLabel(typeLabel);
                    typeEntity.setTypeStereotype(typeStereotypeLabel);
                    typeEntity.setInterfaceList(interfaceList);
                    typeEntity.setSuperClassStr(superclassStr);

                    if (!identifier.getScmOperation().equals(ChangedFile.TypeChange.MODIFIED.toString())) {
                        List<MethodEntity> methodEntityList = getMethodEntityList(stereotypedType);
                        typeEntity.getMethodEntityList().addAll(methodEntityList);
                    }
                    fileEntity.getTypeEntityList().add(typeEntity);
                    summaryEntity.getPackageEntityList().get(summaryEntity.getPackageEntityList().size() - 1).getFileEntityList().add(fileEntity);
                }
            }
        }

        return summaryEntity;
    }

    public StereotypeIdentifier getIdentifyStereotypes(final ChangedFile file, String scmOperation) throws CoreException {
        StereotypeIdentifier stereotypeIdentifier = new StereotypeIdentifier(file);
        stereotypeIdentifier.identifyStereotypes();
        stereotypeIdentifier.setScmOperation(scmOperation);
        stereotypeIdentifier.setChangedFile(file);
        identifiers.add(stereotypeIdentifier);
        return stereotypeIdentifier;
    }

    public void fillNewModules() {
        for (StereotypeIdentifier identifier : identifiers) {
            if (identifier.getChangedFile().getChangeType().equals(ChangedFile.TypeChange.ADDED)) {
                String fullyQualifiedPackageName = identifier.getParser().getCompilationUnit().getPackage().getName().getFullyQualifiedName();
                MyModule myModule = new MyModule(fullyQualifiedPackageName);
                if (!newModules.contains(myModule)) {
                    newModules.add(myModule);
                }
            }
        }


    }

    protected String describeNewModules() {
        if (newModules != null && newModules.size() == 0) {
            return "";
        }
        StringBuilder descTmp = new StringBuilder(Constants.EMPTY_STRING);
        String connector = (newModules.size() == 1) ? " this new module" : " these new modules";
        descTmp.append("The commit includes" + connector + ": \n\n");
        for (MyModule module : newModules) {
            if (!descTmp.toString().contains("\t- " + module.getName() + Constants.NEW_LINE)) {
                descTmp.append("\t- " + module.getName() + Constants.NEW_LINE);
            }
        }
        descTmp.append(Constants.NEW_LINE);

        return descTmp.toString();
    }


    public void fillSummarized(Set<ChangedFile> differences) {
        try {
            for (final ChangedFile file : differences) {
                StereotypeIdentifier identifier = null;
                if (file.getAbsolutePath().endsWith(Constants.JAVA_EXTENSION)) {
                    String changeType = file.getChangeType();
                    if (analyzeTypes.contains(changeType)) {
                        identifier = getIdentifyStereotypes(file, file.getChangeType());
                    }
                } else {
                    otherFiles.add(file);
                    continue;
                }

                if (identifier != null) {
                    if (identifier.getStereotypedElements().size() == 0) { //如果该文件没有任何AST子节点
                        typesProblem.add(identifier);
                        return;
                    }


                    for (StereotypedElement element : identifier.getStereotypedElements()) {
                        String key = element.getQualifiedName();
                        if (!key.contains(".")) {
                            key = identifier.getParser().getCompilationUnit().getPackage().getName() + "." + element.getQualifiedName();
                        }
                        if (!summarized.containsKey(key) && !summarized.containsValue(identifier)) {
                            summarized.put(key, identifier);
                        }
                    }
                }

            }
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }

    public String getTypeLabel(StereotypedType stereotypedType) {
        if (stereotypedType.isInterface()) {
            return TypeLabel.INTERFACE.getLabel();
        } else if (stereotypedType .getElement().resolveBinding() != null
                && Modifier.isAbstract(stereotypedType .getElement().resolveBinding().getModifiers())) {
            return TypeLabel.ABSTRACT.getLabel();
        } else {
            return TypeLabel.USUAL_CLASS.getLabel();
        }
    }

    public List<String> getInterfaceList(StereotypedType stereotypedType) {
        List<String> interfaceList = new ArrayList<>();

        ITypeBinding[] interfaces = null;
        if (stereotypedType.getElement().resolveBinding() != null) {
            interfaces = stereotypedType.getElement().resolveBinding().getInterfaces();
        }

        if (interfaces != null && interfaces.length != 0) {
            for (ITypeBinding anInterface : interfaces) {
                interfaceList.add(anInterface.getName());
            }
        }

        return interfaceList;
    }

    public String getSuperclassStr(StereotypedType stereotypedType) {
        String superclassStr = "";
        ITypeBinding superclass = null;
        if (stereotypedType.getElement().resolveBinding() != null) {
            superclass = stereotypedType.getElement().resolveBinding().getSuperclass();
        }

        if (superclass != null && !superclass.getKey().equals("Ljava/lang/Object;")) {
            superclassStr = superclass.getName();
        }

        return superclassStr;
    }

    public String getTypeStereotypeLabel(StereotypedType stereotypedType) {
        String typeStereotypeLabel = "";

        List<CodeStereotype> stereotypes = stereotypedType.getStereotypes();
        if (stereotypes != null && stereotypes.size() > 0) {
            TypeStereotype typeStereotype = (TypeStereotype) stereotypes.get(0);
            String typeStereotypeName = typeStereotype.getName();
            if (typeStereotypeName.equals(TypeStereotype.ENTITY.getName())) {
                typeStereotypeLabel = TypeStereotype.ENTITY.getName();
            } else if (typeStereotypeName.equals(TypeStereotype.MINIMAL_ENTITY.getName())) {
                typeStereotypeLabel = TypeStereotype.MINIMAL_ENTITY.getName();
            } else if (typeStereotypeName.equals(TypeStereotype.DATA_PROVIDER.getName())) {
                typeStereotypeLabel = TypeStereotype.DATA_PROVIDER.getName();
            } else if (typeStereotypeName.equals(TypeStereotype.COMMANDER.getName())) {
                typeStereotypeLabel = TypeStereotype.COMMANDER.getName();
            } else if (typeStereotypeName.equals(TypeStereotype.BOUNDARY.getName())) {
                typeStereotypeLabel = TypeStereotype.BOUNDARY.getName();
            } else if (typeStereotypeName.equals(TypeStereotype.CONTROLLER.getName())) {
                typeStereotypeLabel = TypeStereotype.CONTROLLER.getName();
            } else if (typeStereotypeName.equals(TypeStereotype.PURE_CONTROLLER.getName())) {
                typeStereotypeLabel = TypeStereotype.PURE_CONTROLLER.getName();
            } else if (typeStereotypeName.equals(TypeStereotype.LARGE_CLASS.getName())) {
                typeStereotypeLabel = TypeStereotype.LAZY_CLASS.getName();
            } else if (typeStereotypeName.equals(TypeStereotype.LAZY_CLASS.getName())) {
                typeStereotypeLabel = TypeStereotype.LAZY_CLASS.getName();
            } else if (typeStereotypeName.equals(TypeStereotype.DEGENERATE.getName())) {
                typeStereotypeLabel = TypeStereotype.DEGENERATE.getName();
            }

        }

        return typeStereotypeLabel;
    }

    public List<MethodEntity> getMethodEntityList(StereotypedType stereotypedType) {
        Set<MethodEntity> set = new LinkedHashSet<>();
        List<StereotypedElement> elements = stereotypedType.getStereoSubElements();
        for (StereotypedElement method : elements) {
            if (method instanceof StereotypedMethod && method.getElement() instanceof MethodDeclaration) {
                MethodPhraseGenerator phraseGenerator = new MethodPhraseGenerator(method, "BASIC");
                try {
                    phraseGenerator.generate();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                if (!set.contains(phraseGenerator.getPhrase())) {
                    if (method.getStereotypes() != null && method.getStereotypes().size() > 0) {
                        MethodStereotype methodStereotype = (MethodStereotype) method.getStereotypes().get(0);
                        String categoryName = methodStereotype.getCategory().getName();
                        String stereotypeName = methodStereotype.getSubcategory().getName();
                        MethodEntity methodEntity = new MethodEntity(categoryName, stereotypeName, phraseGenerator.getPhrase());
                        set.add(methodEntity);
                    }
                }
            }
        }
        ArrayList<MethodEntity> list = new ArrayList<>();
        list.addAll(set);
        return list;
    }

    public static void main(String[] args) {
        ChangeAnalyzer changeAnalyzer = new ChangeAnalyzer("/Users/chengleming/work/projectDir");
        changeAnalyzer.analyze();
    }
}
