package cn.edu.nju.analyze.summarize;

import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;
import cn.edu.nju.analyze.domain.*;
import cn.edu.nju.common.utils.FileCounter;
import cn.edu.nju.core.Constants;
import cn.edu.nju.core.entity.MyModule;
import cn.edu.nju.core.git.ChangedFile;
import cn.edu.nju.core.git.SCMRepository;
import cn.edu.nju.core.label.TypeLabel;
import cn.edu.nju.core.stereotype.stereotyped.*;
import cn.edu.nju.core.stereotype.taxonomy.CodeStereotype;
import cn.edu.nju.core.stereotype.taxonomy.CommitStereotype;
import cn.edu.nju.core.stereotype.taxonomy.MethodStereotype;
import cn.edu.nju.core.stereotype.taxonomy.TypeStereotype;
import cn.edu.nju.core.summarizer.CommitStereotypeDescriptor;
import cn.edu.nju.core.summarizer.ModificationDescriptor;
import cn.edu.nju.core.textgenerator.phrase.MethodPhraseGenerator;
import cn.edu.nju.core.utils.JDTASTUtil;
import cn.edu.nju.core.utils.Utils;
import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.apache.commons.math3.stat.descriptive.summary.Sum;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Data
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
    private SummaryEntity summaryEntity;

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

    public boolean analyze() {
        try {
            SCMRepository scmRepository = new SCMRepository(projectPath);//仓库类
            git = scmRepository.getGit();//git类
            Status status = scmRepository.getStatus();//git状态
            Set<ChangedFile> differences = scmRepository.getDifferences();//获取所有增删改的文件
            //Set<ChangedFile> differences = SCMRepository.getDifferences(status, projectPath);//获取所有增删改的文件

            //初始化summarized
            fillSummarized(differences);//填充map，key为URI地址，value为变化文件的抽象类
            fillNewModules();//记录新增了哪些模块/包
            summaryEntity = analyzeCommitEntity();//核心
//            summaryEntity.setMethodStatisticJson(buildSignature(projectPath));

            FileCounter fileCounter = new FileCounter(projectPath);
            fileCounter.searchFiles();
            summaryEntity.setFileNum(fileCounter.getFileList().size());
//            fillChangedFileStatistics(summaryEntity, status);

            StereotypedCommit stereotypedCommit = getStereotypedCommit();
            if (summaryEntity.getPackageEntityList().size() > 0 && stereotypedCommit == null){
                return true;
            }

            if (stereotypedCommit == null) {
                return false;
            }
            String signature = stereotypedCommit.buildSignature();
            CommitStereotype stereotype = stereotypedCommit.findStereotypes();
            if (stereotype == null) {
                return false;
            }
            String name = stereotype.name();
            String result = CommitStereotypeDescriptor.describe(stereotypedCommit);

            boolean isInitialCommit = Utils.isInitialCommit(git);

            StringBuilder simpleDescribe = new StringBuilder();
            if (isInitialCommit) {
                simpleDescribe.append("Initial commit. \n");
            } else {
                simpleDescribe.append("BF \n");
            }
            simpleDescribe.append(result);

            summaryEntity.setSimpleDescribe(simpleDescribe.toString());
            summaryEntity.setNewModuleDescribe(describeNewModules());
            summaryEntity.setIsInitialCommit(isInitialCommit);
            summaryEntity.setCommitStereotype(name);
            summaryEntity.setMethodStatisticJson(signature);


        } catch (GitAPIException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    public boolean analyze(String username, String repoName,Map<String, Set<String>> stereotypeMap) {
        try {
            SCMRepository scmRepository = new SCMRepository(projectPath);
            git = scmRepository.getGit();
            Status status = scmRepository.getStatus();
            Set<ChangedFile> differences = SCMRepository.getDifferences(status, projectPath);

            //初始化summarized
            fillSummarized(differences);
            fillNewModules();
            summaryEntity = analyzeCommitEntity();
//            summaryEntity.setMethodStatisticJson(buildSignature(projectPath));

            FileCounter fileCounter = new FileCounter(projectPath);
            fileCounter.searchFiles();
            summaryEntity.setFileNum(fileCounter.getFileList().size());
//            fillChangedFileStatistics(summaryEntity, status);

            StereotypedCommit stereotypedCommit = getStereotypedCommit();
            if (stereotypedCommit == null) {
                return false;
            }
            String signature = stereotypedCommit.buildSignature(username, repoName, stereotypeMap);
            CommitStereotype stereotype = stereotypedCommit.findStereotypes();
            if (stereotype == null) {
                return false;
            }
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

        return true;
    }
    public void fillChangedFileStatistics(SummaryEntity summaryEntity, Status status) {
        summaryEntity.setChangedNum(status.getChanged().size() + status.getModified().size());//changed为文件重命名，modified为修改文件内容
        summaryEntity.setAddNum(status.getAdded().size());
        summaryEntity.setRemoveNum(status.getRemoved().size());
    }

    public String getDescribe(SummaryEntity summaryEntity) {
        StringBuilder des = new StringBuilder();
        des.append(summaryEntity.getSimpleDescribe());

        if (summaryEntity.getPackageEntityList().size() > 0) {
            des.append("ChangeScribeStart \n");
        }

//        if (!otherFiles.isEmpty()){//直接输出非java文件的diff
//            List<DiffEntry> diffs = null;
//            try {
//                diffs = git.diff().setCached(true).call();
//
//                ByteArrayOutputStream out = new ByteArrayOutputStream();
//                DiffFormatter df = new DiffFormatter(out);
//                df.setRepository(git.getRepository());
//
//                for (DiffEntry diffEntry : diffs) {
//                    for (ChangedFile cf : otherFiles){
//                        if (diffEntry.getNewPath().equals(cf.getPath()) || diffEntry.getOldPath().equals(cf.getPath())){
//                            df.format(diffEntry);
//                            des.append("Here are some none java files diff: \n");
//                            des.append(out);
//                        }
//                    }
//                }
//
//            } catch (Exception e) {
//                System.err.println(e);
//            }
//        }

        if (!typesProblem.isEmpty()){
            for (StereotypeIdentifier Si:typesProblem){
                des.append(Si.getChangedFile().getChangeType() + " a almost empty file " + Si.getChangedFile().getName() + " with zero ast node ");
            }
        }

        int i = 1;
        for (PackageEntity packageEntity : summaryEntity.getPackageEntityList()) {
            StringBuilder packageDes = new StringBuilder();
            //packageDes.append(i + ". ");
            packageDes.append("Changes to " + packageEntity.getPackageName() + ": \n");
            int j = 1;
            for (FileEntity fileEntity : packageEntity.getFileEntityList()) {
                StringBuilder fileDes = new StringBuilder();
//                fileDes.append(" |%| ");
                //fileDes.append(i + "." + j + ". ");
                if (fileEntity.getOperation().equals(ChangedFile.TypeChange.MODIFIED.name())) {
                    fileDes.append("Modifications to " + fileEntity.getFileName() + "\n");
                    fileDes.append(fileEntity.getChangeDescribe());
                } else if (fileEntity.getOperation().equals(ChangedFile.TypeChange.ADDED.name()) ||
                        fileEntity.getOperation().equals(ChangedFile.TypeChange.REMOVED.name())) {
                    StringBuilder typeDes = new StringBuilder();
                    for (TypeEntity typeEntity : fileEntity.getTypeEntityList()) {
                        if (fileEntity.getOperation().equals(ChangedFile.TypeChange.ADDED.name())) {
                            typeDes.append("Add ");
                        } else {
                            typeDes.append("Remove ");
                        }
                        typeDes.append(typeEntity.getTypeStereotype() + " " + typeEntity.getTypeName());
                        if (typeEntity.getTypeLabel().equals(TypeLabel.ABSTRACT)) {
                            typeDes.append(" abstract class ");
                        } else if (typeEntity.getTypeLabel().equals(TypeLabel.INTERFACE)) {
                            typeDes.append(" interface ");
                        } else if (typeEntity.getTypeLabel().equals(TypeLabel.USUAL_CLASS)) {
                            typeDes.append(" class ");
                        }
                        if (typeEntity.getInterfaceList().size() > 0) {
                            typeDes.append(" implements ");
                            for (String interfaceName : typeEntity.getInterfaceList()) {
                                typeDes.append(interfaceName + ", ");
                            }
                        }
                        if (!typeEntity.getSuperClassStr().equals("")) {
                            if (typeEntity.getInterfaceList().size() > 0) {
                                typeDes.append(", and extends " + typeEntity.getSuperClassStr());
                            } else {
                                typeDes.append(" extends " + typeEntity.getSuperClassStr());
                            }
                        }
                        if (typeEntity.getMethodEntityList().size() > 0) {
                            typeDes.append("\n" + "It allows to: \n" );
                            for (MethodEntity methodEntity : typeEntity.getMethodEntityList()) {
                                typeDes.append( methodEntity.getPhrase());
                            }
                        }

                    }
                    fileDes.append(typeDes);
                }
//                fileDes.append(" |%%| ");
                packageDes.append(fileDes);
                j++;
            }
            des.append(packageDes);
            i++;
        }
        return des.toString();
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
                            StereotypedMethod method = (StereotypedMethod) stereoSubElement;
                            method.setTypeAbsolutePath(identifier.getChangedFile().getAbsolutePath());
                            methods.add(method);
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
                                StereotypedMethod method = (StereotypedMethod) stereotypedMethod;
                                method.setTypeAbsolutePath(identifier.getChangedFile().getAbsolutePath());
                                methods.add(method);
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
                if (stereotyped instanceof StereotypedType){
                    StereotypedElement tmp = getStereotypedElementFromName(stereotyped,searchedElement);
                    if (tmp != null){
                        result = tmp;
                        break;
                    }
                } else {
                    if (stereotyped.getFullyQualifiedName().equals(searchedElement.getUniqueName()) ||
                            searchedElement.getUniqueName().endsWith(stereotyped.getFullyQualifiedName())) {
                        result = stereotyped;
                        break;
                    }
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
            String packageName = "NonePackage";
            if (identifier.getParser().getCompilationUnit().getPackage() != null){
                packageName = identifier.getParser().getCompilationUnit().getPackage().getName().getFullyQualifiedName();
            }
            if (currentPackage.trim().equals("") //第一次遍历
            || !currentPackage.trim().equals(packageName)) { //遍历到新的包
                currentPackage = packageName;
                PackageEntity packageEntity = new PackageEntity();
                packageEntity.setPackageName(currentPackage);
                summaryEntity.getPackageEntityList().add(packageEntity);
            }


            String typeName = key.substring(key.lastIndexOf(".") + 1);
            if (identifier.getScmOperation().equals(ChangedFile.TypeChange.MODIFIED.toString())) {
                summaryEntity.setChangedNum(summaryEntity.getChangedNum() + 1);
                FileEntity fileEntity = new FileEntity(identifier.getScmOperation(), true, identifier.getChangedFile().getName());
                ModificationDescriptor modificationDescriptor = new ModificationDescriptor();
                modificationDescriptor.setFile(identifier.getChangedFile());
                modificationDescriptor.setGit(git);
                modificationDescriptor.extractDifferences(identifier.getChangedFile(), git);//获得该文件changes
                modificationDescriptor.extractModifiedMethods();//获取发生变化的函数
                String describe = modificationDescriptor.describe();//字符串结果 核心
                summaryEntity.className.addAll(modificationDescriptor.className);
                summaryEntity.methodName.addAll(modificationDescriptor.methodName);
                fileEntity.setChangeDescribe(describe);
                String absolutePath = identifier.getChangedFile().getAbsolutePath();
                fileEntity.setAbsolutePath(absolutePath);
                summaryEntity.getPackageEntityList().get(summaryEntity.getPackageEntityList().size() - 1).getFileEntityList().add(fileEntity);
            } else {
                if (identifier.getScmOperation().equals(ChangedFile.TypeChange.REMOVED.toString())) {
                    summaryEntity.setRemoveNum(summaryEntity.getRemoveNum() + 1);
                } else if (identifier.getScmOperation().equals(ChangedFile.TypeChange.ADDED.toString())) {
                    summaryEntity.setAddNum(summaryEntity.getAddNum() + 1);
                }
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
                    String absolutePath = identifier.getChangedFile().getAbsolutePath();
                    typeEntity.setAbsolutePath(absolutePath);

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
                //if (file.getChangeType() == "REMOVED"){
                //    String s = "1";
                //}

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
                        continue;
                    }


                    for (StereotypedElement element : identifier.getStereotypedElements()) {
                        String key = element.getQualifiedName();
                        if (!key.contains(".")) {
                            if (identifier.getParser().getCompilationUnit().getPackage() == null){
                                key = "NonePackage" + "." + element.getQualifiedName();
                            } else {
                                key = identifier.getParser().getCompilationUnit().getPackage().getName() + "." + element.getQualifiedName();
                            }
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

    public static String getDescribeHTML(SummaryEntity summaryEntity) {
        StringBuilder des = new StringBuilder();
        des.append("<b>" + summaryEntity.getSimpleDescribe() + "</b>");

        if (summaryEntity.getPackageEntityList().size() > 0) {
            des.append("This change set is mainly composed of: " + "</br>");
        }

        int i = 1;
        for (PackageEntity packageEntity : summaryEntity.getPackageEntityList()) {
            StringBuilder packageDes = new StringBuilder();
            packageDes.append("<h3>" + i + ". ");
            packageDes.append("Changes to " + packageEntity.getPackageName() + ": " + "</br>" + "</h3>");
            int j = 1;
            for (FileEntity fileEntity : packageEntity.getFileEntityList()) {
                StringBuilder fileDes = new StringBuilder();
                fileDes.append("<h4>" + i + "." + j + ". ");
                if (fileEntity.getOperation().equals(ChangedFile.TypeChange.MODIFIED.name())) {
                    fileDes.append("Modifications to " +
//                            "<font color=#33ccff>" + fileEntity.getFileName() + "</font>" +
                            "<a style=\"color:#33ccff;\" href=\"http://localhost:1024/code/change?localpath=" +
                            fileEntity.getAbsolutePath() + "\" " +"target=\"_blank\">" +
                            fileEntity.getFileName() +
                            "</a>" +
                            "</h4>" + "</br>");
                    fileEntity.setChangeDescribe(fileEntity.getChangeDescribe().replaceAll("\n", "</br>"));
                    fileEntity.setChangeDescribe(fileEntity.getChangeDescribe().replaceAll("\t", "&nbsp&nbsp"));
                    fileDes.append(fileEntity.getChangeDescribe());
                } else if (fileEntity.getOperation().equals(ChangedFile.TypeChange.ADDED.name()) ||
                        fileEntity.getOperation().equals(ChangedFile.TypeChange.REMOVED.name())) {
                    StringBuilder typeDes = new StringBuilder();
                    for (TypeEntity typeEntity : fileEntity.getTypeEntityList()) {
                        if (fileEntity.getOperation().equals(ChangedFile.TypeChange.ADDED.name())) {
                            typeDes.append("Add ");
                        } else {
                            typeDes.append("Remove ");
                        }
                        typeDes.append(typeEntity.getTypeStereotype() +
//                                "<font color=#33ccff>"  +typeEntity.getTypeName() + "</font>" + "</br>");
                                "<a style=\"color:#33ccff;\" href=\"http://localhost:1024/code/change?localpath=" +
                                typeEntity.getAbsolutePath() + "\" " +"target=\"_blank\">" +
                                typeEntity.getTypeName() +
                                "</a>" );
                        if (typeEntity.getTypeLabel().equals(TypeLabel.ABSTRACT)) {
                            typeDes.append(" abstract class ");
                        } else if (typeEntity.getTypeLabel().equals(TypeLabel.INTERFACE)) {
                            typeDes.append(" interface ");
                        } else if (typeEntity.getTypeLabel().equals(TypeLabel.USUAL_CLASS)) {
                            typeDes.append(" class ");
                        }
                        if (typeEntity.getInterfaceList().size() > 0) {
                            typeDes.append("implements ");
                            for (String interfaceName : typeEntity.getInterfaceList()) {
                                typeDes.append(interfaceName + ", ");
                            }
                        }
                        if (!typeEntity.getSuperClassStr().equals("")) {
                            if (typeEntity.getInterfaceList().size() > 0) {
                                typeDes.append(", and extends " + typeEntity.getSuperClassStr());
                            } else {
                                typeDes.append("extends " + typeEntity.getSuperClassStr());
                            }
                        }
                        if (typeEntity.getMethodEntityList().size() > 0) {
                            typeDes.append("</br>" + "It allows to: " + "</br>" );
                            for (MethodEntity methodEntity : typeEntity.getMethodEntityList()) {
                                typeDes.append( methodEntity.getPhrase());
                            }
                        }

                    }

                    fileDes.append(typeDes);
                }
                packageDes.append(fileDes);
                j++;
            }
            des.append(packageDes);
            i++;
        }
        return des.toString();
    }

    public String buildSignature(String projectPath) {
        List<MethodDeclaration> methods = new ArrayList<>();
        File file = new File(projectPath);		//获取其file对象
        traverseFile(file, methods);

        Map<MethodStereotype, Integer> signatureMap = new TreeMap<MethodStereotype, Integer>();
        for (MethodDeclaration method : methods) {
            StereotypedMethod stereotypedMethod = new StereotypedMethod(method);
            stereotypedMethod.findStereotypes();
            if(!signatureMap.containsKey(stereotypedMethod.getStereotypes().get(0))) {
                signatureMap.put((MethodStereotype) stereotypedMethod.getStereotypes().get(0), 1);
            } else {
                Integer value = signatureMap.get(stereotypedMethod.getStereotypes().get(0));
                signatureMap.put((MethodStereotype) stereotypedMethod.getStereotypes().get(0), value + 1);
            }
        }

//       System.out.println("signatures: " + getSignatureMap().toString());
        return JSON.toJSONString(signatureMap);

    }

    private void  traverseFile(File file, List<MethodDeclaration> methods) {
        File[] fs = file.listFiles();
        for(File f:fs){
            if(f.isDirectory())	{//若是目录，则递归打印该目录下的文件
                traverseFile(f, methods);
            } else {
                if(f.isFile() && f.getAbsolutePath().endsWith(".java")) {
                    CompilationUnit compilationUnit = JDTASTUtil.getCompilationUnit(f.getAbsolutePath());
                    compilationUnit.accept(new MethodVisitor(methods));
                }
            }
        }
    }

    public static void main(String[] args) {
//        ChangeAnalyzer changeAnalyzer = new ChangeAnalyzer("/Users/chengleming/work/projectDir");
//        changeAnalyzer.analyze();
//        changeAnalyzer.getDescribe(changeAnalyzer.summaryEntity);
        ChangeAnalyzer changeAnalyzer = new ChangeAnalyzer("/Users/chengleming/work/projectDir");
        changeAnalyzer.buildSignature("/Users/chengleming/MasterThesis/Soter/tmp/mavenbase");
    }
}
