package cn.edu.nju.traning;
import ch.uzh.ifi.seal.changedistiller.ChangeDistiller;
import ch.uzh.ifi.seal.changedistiller.distilling.FileDistiller;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeType;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.*;
import ch.uzh.ifi.seal.changedistiller.structuredifferencing.java.JavaStructureNode.Type;
import cn.edu.nju.core.Constants;
import cn.edu.nju.core.entity.TypeDescribe;
import cn.edu.nju.core.git.ChangedFile;
import cn.edu.nju.core.dependencies.MethodDependencySummary;
import cn.edu.nju.core.textgenerator.phrase.NounPhrase;
import cn.edu.nju.core.textgenerator.phrase.Parameter;
import cn.edu.nju.core.textgenerator.phrase.ParameterPhrase;
import cn.edu.nju.core.textgenerator.phrase.VerbPhrase;
import cn.edu.nju.core.textgenerator.phrase.util.PhraseUtils;
import cn.edu.nju.core.textgenerator.pos.POSTagger;
import cn.edu.nju.core.textgenerator.pos.TaggedTerm;
import cn.edu.nju.core.textgenerator.tokenizer.Tokenizer;
import cn.edu.nju.core.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.core.NamedMember;
import org.eclipse.jdt.internal.core.ResolvedSourceField;
import org.eclipse.jdt.internal.core.ResolvedSourceMethod;
import org.eclipse.jdt.internal.core.ResolvedSourceType;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("restriction")
public class ModificationDescriptorForTrain {

    private List<SourceCodeChange> changes;
    private ChangedFile file;
    private Git git;
    private ChangedFile[] differences;
    private List<SourceCodeChange> addedRemovedFunctionalities;
    private boolean onlyStructuralChanges = false;

    /**
     * 提取java文件中的源码改动
     * @param file
     * @param git
     */
    public void extractDifferences(ChangedFile file, Git git) {
        FileDistiller distiller = ChangeDistiller.createFileDistiller(ChangeDistiller.Language.JAVA);
        try {
            compareModified(file, distiller, git);
        } catch(IllegalStateException ex) {
            ex.printStackTrace();
        }
        changes = distiller.getSourceCodeChanges();
    }

    public void extractDifferencesBetweenVersions(ChangedFile file, Git git, String olderID, String currentID) {
        FileDistiller distiller = ChangeDistiller.createFileDistiller(ChangeDistiller.Language.JAVA);
        try {
            compareModifiedVersions(file, distiller, git, olderID, currentID);
        } catch(IllegalStateException ex) {
            ex.printStackTrace();
        }
        changes = distiller.getSourceCodeChanges();
    }

    /**
     * 修改ChangeFile中modifiedMethods参数，列举被修改的方法
     */
    public void extractModifiedMethods() {
        List<StructureEntityVersion> modifiedMethods = new ArrayList<StructureEntityVersion>();
        // 本来在这边set的，但是我觉得没有意义，就移到下面去了
        // file.setModifiedMethods(modifiedMethods);
        if(changes != null) {
            for(SourceCodeChange change : changes) {
                if(change.getRootEntity() != null && (change.getRootEntity().getType().equals(JavaEntityType.METHOD) ||
                        change.getRootEntity().getType().equals(JavaEntityType.METHOD_DECLARATION))) {
                    if(!modifiedMethods.contains(change.getRootEntity())) {
                        modifiedMethods.add(change.getRootEntity());
                    }
                } else if(change.getChangedEntity() != null && change.getChangedEntity().getType().equals(JavaEntityType.METHOD)) {
                    StructureEntityVersion entityVersion = new StructureEntityVersion(change.getChangedEntity().getType(), change.getChangedEntity().getUniqueName(), change.getChangedEntity().getModifiers(), change.getChangedEntity().getJavaStructureNode());
                    if(!modifiedMethods.contains(entityVersion)) {
                        modifiedMethods.add(entityVersion);
                    }
                }
            }
        }
        file.setModifiedMethods(modifiedMethods);
    }

    public void describe(int i, int j, StringBuilder desc, List<TypeDescribe> typeDescribes) throws IOException, ClassNotFoundException {
        StringBuilder localDescription = new StringBuilder(Constants.EMPTY_STRING);
        addedRemovedFunctionalities = new ArrayList<SourceCodeChange>();
        if(changes != null) {
            // 对源码改动进行解析
            for(SourceCodeChange change : changes) {
                StringBuilder descTmp = new StringBuilder(Constants.EMPTY_STRING);
                if(change instanceof Update) {
                    Update update = (Update) change;
                    if(isStructuralChange(update.getChangeType())) {
                        describeUpdate(descTmp, change, update);
                    }

                } else if(change instanceof Insert) {
                    Insert insert = (Insert) change;
                    if(isStructuralChange(insert.getChangeType())) {
                        // 描述新增语句
                        describeInsert(descTmp, insert);
                    }
                } else if(change instanceof Delete) {
                    Delete delete = (Delete) change;
                    if(isStructuralChange(delete.getChangeType())) {
                        describeDelete(descTmp, delete);
                    }
                } else if(change instanceof Move) {
                }

                if(!descTmp.toString().equals(Constants.EMPTY_STRING) && (change instanceof Update || change instanceof Insert || change instanceof Delete)) {

                    if(!localDescription.toString().toLowerCase().contains(descTmp.toString().toLowerCase())) {
                        desc.append(Constants.TAB);

                        desc.append(descTmp.toString());
                        localDescription.append(descTmp.toString());

                        if(!descTmp.toString().equals(Constants.EMPTY_STRING) && (change instanceof Update || change instanceof Insert || change instanceof Delete)) {
                            desc.append(Constants.NEW_LINE);
                        }
                    }
                }
            }

            // 描述被修改文件依赖的修改。暂时禁用，涉及到eclipse组件
//		    if(addedRemovedFunctionalities != null && addedRemovedFunctionalities.size() > 0) {
//		    	describeCollateralChanges(desc);
//		    }

            // 调整了一下输出格式
            if(!localDescription.toString().equals(Constants.EMPTY_STRING)) {
                if(changes != null && changes.size() > 0) {
                    desc.insert(0, (i-1) + "." + j + "." + "Modifications to " + file.getName() + "\n");
                    desc.append(Constants.NEW_LINE);
                }
                desc.append(Constants.NEW_LINE);
            }
        }

        // 注释临时输出
//		System.out.println("Modification Descriptor: " + desc.toString());

    }

    public void describeDelete(StringBuilder desc, Delete delete) throws IOException, ClassNotFoundException {
        if(delete.getChangeType() == ChangeType.STATEMENT_DELETE) {
            String statementType = delete.getChangedEntity().getType().name().toLowerCase().replace("statement", Constants.EMPTY_STRING).replace("_", " ");
            desc.append("Remove ");
            desc.append(statementType);
            if(delete.getChangedEntity().getAstNode() != null && delete.getChangedEntity().getAstNode() instanceof LocalDeclaration) {
                LocalDeclaration localDec = (LocalDeclaration) delete.getChangedEntity().getAstNode();
                NounPhrase phrase = new NounPhrase(Tokenizer.split(new String(localDec.name)));
                phrase.generate();
                desc.append(" to " + phrase.toString() + " at " + getRootEntityJavaStructureNodeName(delete) + " method");
            } else if(delete.getChangedEntity().getAstNode() != null && delete.getChangedEntity().getAstNode() instanceof ForeachStatement) {
                ForeachStatement forDec = (ForeachStatement) delete.getChangedEntity().getAstNode();
                NounPhrase phrase = null;
                if(forDec.collection instanceof MessageSend) {
                    phrase = new NounPhrase(Tokenizer.split(((MessageSend)forDec.collection).receiver.toString()));
                } else {
                    phrase = new NounPhrase(Tokenizer.split((forDec.collection).toString().toString()));
                }
                phrase.generate();
                desc.append(" loop for " + phrase.toString() + " collection at " + getRootEntityJavaStructureNodeName(delete) + " method");
            } else if(delete.getChangedEntity().getAstNode() != null && delete.getChangedEntity().getAstNode() instanceof MessageSend) {
                MessageSend messageSend = (MessageSend) delete.getChangedEntity().getAstNode();
                NounPhrase phrase = new NounPhrase(Tokenizer.split(new String(messageSend.selector)));
                phrase.generate();
                desc.append(" to " + phrase.toString());
                if(messageSend.arguments != null && messageSend.arguments.length > 0) {
                    if(messageSend.arguments[0] instanceof SingleNameReference) {
                        phrase = new NounPhrase(Tokenizer.split(new String(((SingleNameReference)messageSend.arguments[0]).token)));
                    } else if(messageSend.arguments[0] instanceof TrueLiteral) {
                        phrase = new NounPhrase(Tokenizer.split(new String(((TrueLiteral)messageSend.arguments[0]).toString())));
                    }
                    phrase.generate();
                }
                if(!desc.toString().endsWith(phrase.toString())) {
                    desc.append(" " + phrase.toString());
                }
                desc.append(" at " + getRootEntityJavaStructureNodeName(delete) + " method");
            } else if(delete.getChangedEntity().getAstNode() != null && delete.getChangedEntity().getAstNode() instanceof CompoundAssignment) {
                CompoundAssignment statement = (CompoundAssignment) delete.getChangedEntity().getAstNode();
                if(delete.getChangedEntity().getAstNode() instanceof PrefixExpression) {
                    PrefixExpression prefixExpression = (PrefixExpression) delete.getChangedEntity().getAstNode();

                    if(OperatorIds.PLUS == prefixExpression.operator) {
                        desc.append(" to increment ");
                    } else if(OperatorIds.MINUS == prefixExpression.operator) {
                        desc.append(" to decrement ");
                    }

                    desc.append(" to " + prefixExpression.lhs.toString());
                } else {
                    desc.append(" to " + statement.lhs.toString());
                }
                desc.append(" at " + delete.getRootEntity().getJavaStructureNode().getName() + " " + delete.getRootEntity().getJavaStructureNode().getType().name().toLowerCase());
            } else if(delete.getChangedEntity().getAstNode() != null && delete.getChangedEntity().getAstNode() instanceof ReturnStatement) {
                desc.append(" statement ");
                desc.append(" at " + delete.getRootEntity().getJavaStructureNode().getName() + " " + delete.getRootEntity().getJavaStructureNode().getType().name().toLowerCase());
            } else if(delete.getChangedEntity().getAstNode() != null && delete.getChangedEntity().getAstNode() instanceof IfStatement) {
                desc.append(" statement ");
                if(!delete.getRootEntity().getJavaStructureNode().getName().equals(Constants.EMPTY_STRING)) {
                    desc.append(" at " + delete.getRootEntity().getJavaStructureNode().getName() + " " + delete.getRootEntity().getJavaStructureNode().getType().name().toLowerCase());
                }
            } else if(delete.getChangedEntity().getAstNode() != null && delete.getChangedEntity().getAstNode() instanceof Assignment) {
                desc.append(" statement of " + ((Assignment)delete.getChangedEntity().getAstNode()).lhs);
                desc.append(" at " + delete.getRootEntity().getJavaStructureNode().getName() + " " + delete.getRootEntity().getJavaStructureNode().getType().name().toLowerCase());
            } else if(delete.getChangedEntity().getAstNode() != null && delete.getChangedEntity().getAstNode() instanceof ForStatement) {
                desc.append(" loop with " + ((ForStatement)delete.getChangedEntity().getAstNode()).condition + " condition");
                desc.append(" at " + delete.getRootEntity().getJavaStructureNode().getName() + " " + delete.getRootEntity().getJavaStructureNode().getType().name().toLowerCase());
            } else if(delete.getChangedEntity().getAstNode() != null
                    && (delete.getChangedEntity().getAstNode() instanceof ThrowStatement)
                    && (((ThrowStatement)delete.getChangedEntity().getAstNode()).exception instanceof AllocationExpression)) {
                // 上方新增一个对AllocationExpression实例的判断
                desc.append(" statement of " + ((AllocationExpression)((ThrowStatement)delete.getChangedEntity().getAstNode()).exception).type + " exception");
            }
        } else if(delete.getChangeType() == ChangeType.PARENT_CLASS_DELETE) {
            desc.append(StringUtils.capitalize("Remove parent class ") + delete.getChangedEntity().getUniqueName());
        } else if(delete.getChangeType() == ChangeType.PARENT_INTERFACE_DELETE) {
            desc.append(StringUtils.capitalize("Remove parent interface ") + delete.getChangedEntity().getUniqueName());
        } else if(delete.getChangeType() == ChangeType.ADDING_METHOD_OVERRIDABILITY || delete.getChangeType() == ChangeType.ADDING_ATTRIBUTE_MODIFIABILITY) {
            desc.append(StringUtils.capitalize("Remove final modifier of ") + delete.getRootEntity().getJavaStructureNode().getName().toString() + " " + delete.getRootEntity().getJavaStructureNode().getType().name().toString().toLowerCase());
        } else if(delete.getChangeType() == ChangeType.ALTERNATIVE_PART_DELETE) {
            desc.append(StringUtils.capitalize("Remove else part of ") + delete.getChangedEntity().getUniqueName() + " condition ");
        } else if(delete.getChangeType() == ChangeType.COMMENT_DELETE || delete.getChangeType() == ChangeType.DOC_DELETE) {
            String type = delete.getChangedEntity().getType().name().toLowerCase().replace("_", " ");
            String entityType = delete.getRootEntity().getJavaStructureNode().getType().name().toLowerCase();
            desc.append("Remove " + type +" at " + delete.getRootEntity().getJavaStructureNode().getName() + " " + entityType);
        } else if(delete.getChangeType() == ChangeType.REMOVED_FUNCTIONALITY) {
            describeAdditionalRemovedFunctionality(desc, delete, "Remove");
        } else if(delete.getChangeType() == ChangeType.REMOVED_OBJECT_STATE) {
            desc.append("Remove (Object state) " + delete.getChangedEntity().getName().substring(0, delete.getChangedEntity().getName().indexOf(":")) + " attribute");
        } else if(delete.getChangeType() == ChangeType.PARAMETER_DELETE) {
            if(delete.getChangedEntity().getAstNode() instanceof Argument) {
                Argument arg = (Argument) delete.getChangedEntity().getAstNode();
                Parameter parameter = new Parameter(arg.type.toString(), new String(arg.name));
                ParameterPhrase phrase = new ParameterPhrase(parameter);
                phrase.generate();
                desc.append("Remove parameter " + phrase.toString() + " at " + delete.getRootEntity().getJavaStructureNode().getName() + " " + delete.getRootEntity().getJavaStructureNode().getType().name().toLowerCase());
            }
        }
    }

    @SuppressWarnings("static-access")
    public void describeInsert(StringBuilder desc, Insert insert) throws IOException, ClassNotFoundException {

        String fType = insert.getChangedEntity().getType().name().toLowerCase().replace("_", " ");
        String uniqueName = insert.getChangedEntity().getUniqueName();

        fType = "Add " + fType + " " + uniqueName;

        if(insert.getChangeType() == ChangeType.ADDITIONAL_FUNCTIONALITY) {
            describeAdditionalRemovedFunctionality(desc, insert, "Add");
        } else if(insert.getChangeType() == ChangeType.ADDITIONAL_OBJECT_STATE ) {
            desc.append("Add (Object state) " + insert.getChangedEntity().getName().substring(0, insert.getChangedEntity().getName().indexOf(":")) + " attribute");
        } else if(insert.getChangeType() == ChangeType.INCREASING_ACCESSIBILITY_CHANGE) {
            desc.append("Increasing accessibility change " + insert.getChangedEntity().toString().substring(insert.getChangedEntity().toString().indexOf(":") + 1) + Constants.EMPTY_STRING);
        } else if(insert.getChangeType() == ChangeType.COMMENT_INSERT || insert.getChangeType() == ChangeType.DOC_INSERT) {
            String entityType = insert.getRootEntity().getJavaStructureNode().getType().name().toLowerCase();
            desc.append(StringUtils.capitalize(fType) +" at " + insert.getRootEntity().getJavaStructureNode().getName() + " " + entityType);
        } else if(insert.getChangeType() == ChangeType.PARENT_CLASS_INSERT) {
            desc.append(StringUtils.capitalize("Add parent class ") + insert.getChangedEntity().getUniqueName());
        } else if(insert.getChangeType() == ChangeType.PARENT_INTERFACE_INSERT) {
            desc.append(StringUtils.capitalize("Add parent interface ") + insert.getChangedEntity().getUniqueName());
        } else if(insert.getChangeType() == ChangeType.REMOVING_METHOD_OVERRIDABILITY || insert.getChangeType() == ChangeType.REMOVING_ATTRIBUTE_MODIFIABILITY) {
            desc.append(StringUtils.capitalize("Add final modifier to ") + insert.getRootEntity().getJavaStructureNode().getName().toString() + " " + insert.getRootEntity().getJavaStructureNode().getType().name().toString().toLowerCase());
        } else if(insert.getChangeType() == ChangeType.ALTERNATIVE_PART_INSERT) {
            desc.append(StringUtils.capitalize("Add else part of ") + insert.getChangedEntity().getUniqueName() + " condition ");
        } else if(insert.getChangedEntity().getType() == JavaEntityType.METHOD_INVOCATION) {
            MessageSend methodC = (MessageSend) insert.getChangedEntity().getAstNode();
            String referencedObject = Constants.EMPTY_STRING;
            String object = Constants.EMPTY_STRING;
            if(methodC.receiver.toString().equals(Constants.EMPTY_STRING)) {
                referencedObject = " to local method ";
            } else {
                referencedObject = " to method ";
                object = " of " + methodC.receiver.toString() + " object ";
            }

            desc.append(StringUtils.capitalize(fType) + referencedObject + new String(methodC.selector) + object + " at " + getRootEntityJavaStructureNodeName(insert) + " method");
        } else if (insert.getChangeType() == ChangeType.PARAMETER_INSERT) {
            if(insert.getChangedEntity().getAstNode() instanceof Argument) {
                Argument arg = (Argument) insert.getChangedEntity().getAstNode();
                Parameter parameter = new Parameter(arg.type.toString(), new String(arg.name));
                ParameterPhrase phrase = new ParameterPhrase(parameter);
                phrase.generate();
                desc.append("Add parameter " + phrase.toString() + " at " + insert.getRootEntity().getJavaStructureNode().getName() + " " + insert.getRootEntity().getJavaStructureNode().getType().name().toLowerCase());
            }
        } else if(insert.getChangeType() == ChangeType.STATEMENT_INSERT)  {
            desc.append(StringUtils.capitalize(fType) + " ");

            if(insert.getChangedEntity().getAstNode() instanceof PrefixExpression) {
                PrefixExpression prefixExpression = (PrefixExpression) insert.getChangedEntity().getAstNode();

                if(prefixExpression.PLUS == prefixExpression.operator) {
                    desc.append(" to increment ");
                } else if(prefixExpression.MINUS == prefixExpression.operator) {
                    desc.append(" to decrement ");
                }

                desc.append(" " + prefixExpression.lhs.toString());

            } else if(insert.getChangedEntity().getAstNode() != null && insert.getChangedEntity().getAstNode() instanceof CompoundAssignment) {
                CompoundAssignment statement = (CompoundAssignment) insert.getChangedEntity().getAstNode();
                desc.append(" " + statement.lhs.toString() + " variable to " + statement.expression.toString()+ " at " + insert.getRootEntity().getJavaStructureNode().getName() + " " + insert.getRootEntity().getJavaStructureNode().getType().name().toLowerCase());
            } else if(insert.getChangedEntity().getAstNode() != null && insert.getChangedEntity().getAstNode() instanceof Assignment) {
                Assignment statement = (Assignment) insert.getChangedEntity().getAstNode();
                desc.append(" to " + statement.lhs.toString() + " at " + insert.getRootEntity().getJavaStructureNode().getName() + " " + insert.getRootEntity().getJavaStructureNode().getType().name().toLowerCase());
            } else {
                fType = insert.getChangeType().name().toLowerCase().replace("_", " ");
                desc.append(" at " + getRootEntityJavaStructureNodeName(insert) + " method");
            }
        }
    }

    private boolean isStructuralChange(ChangeType changeType) {
        boolean isStructural = true;
        if(isOnlyStructuralChanges()) {
            System.out.println("CHANGE TYPE: " + changeType.toString() + " ISSTRUCTURAL: " + changeType.isBodyChange());
            if(!changeType.isBodyChange()) {
                isStructural = false;
            }
        }
        return isStructural;
    }

    public void describeAdditionalRemovedFunctionality(StringBuilder desc, SourceCodeChange change, String operation) throws IOException, ClassNotFoundException {
        String className = change.getParentEntity().getName();
        MethodDeclaration method = null;
        if (change.getChangedEntity().getAstNode() instanceof MethodDeclaration) {
            method = (MethodDeclaration) change.getChangedEntity().getAstNode();
        }
        String verb = Constants.EMPTY_STRING;
        boolean hasLeadingVerb = true;
        StringBuilder localDescriptor = new StringBuilder();

        String functionality = change.getChangedEntity().getName().substring(0, change.getChangedEntity().getName().indexOf("("));
        VerbPhrase phrase = null;
        LinkedList<TaggedTerm> tags = POSTagger.tag(Tokenizer.split(functionality));
        if(tags != null && tags.size() > 0) {
            hasLeadingVerb = PhraseUtils.hasLeadingVerb(tags.get(0));
        }

        if(method != null && method.returnType != null && !method.returnType.toString().equals(Constants.EMPTY_STRING) && !method.returnType.toString().equals("void") && !hasLeadingVerb) {
            verb = "get";
            NounPhrase nounPhrase = new NounPhrase(Tokenizer.split(functionality));
            phrase = new VerbPhrase(verb, nounPhrase);

        } else {
            phrase = new VerbPhrase(POSTagger.tag(Tokenizer.split(functionality)), className, null, false);
        }
        phrase.generate();
        if(change.getChangedEntity().getAstNode() != null && !(change.getChangedEntity().getAstNode() instanceof ConstructorDeclaration)) {

            if(change.getChangedEntity() != null && change.getChangedEntity().isPrivate()) {
                localDescriptor.append(" private ");
            }
            if(change.getChangedEntity() != null && change.getChangedEntity().isPrivate() &&
                    operation.equals("Remove") && isUnUsedMethod(change)) {
                localDescriptor.append("and ");
            }
            if(operation.equals("Remove") && isUnUsedMethod(change)) {
                localDescriptor.append(" unused ");
            }
            localDescriptor.insert(0, " " + PhraseUtils.getIndefiniteArticle(localDescriptor.toString().trim()));
            localDescriptor.insert(0, operation);
            // phrase.toString()会报空指针错误
            //localDescriptor.append(" functionality to " + phrase.toString());
            localDescriptor.append(" functionality to " + "class");
            if(method.returnType != null && !method.returnType.toString().equals(Constants.EMPTY_STRING) && !method.returnType.toString().equals("void") && !hasLeadingVerb) {
                localDescriptor.append(" (");
                localDescriptor.append(Constants.EMPTY_STRING + method.returnType.toString());
                localDescriptor.append(")");

            }
        } else {
            localDescriptor.append(operation + " a ");
            describeDeprecatedMethod(localDescriptor, change);
            localDescriptor.append("constructor method");
        }
        desc.append(localDescriptor.toString());
        addedRemovedFunctionalities.add(change);
    }

    public void describeUpdate(StringBuilder desc,
                               SourceCodeChange change, Update update) {
        String fType = "Modify " + update.getChangedEntity().getType().name().toLowerCase().replace("_", " ");
        if(fType.equals("Modify variable declaration statement")) {
            fType = "Modify variable declaration ";
        }
        if(update.getChangeType() == ChangeType.STATEMENT_UPDATE) {

            if(update.getChangedEntity().getType() == JavaEntityType.METHOD_INVOCATION) {
                desc.append(StringUtils.capitalize(fType));
                MessageSend methodC = (MessageSend) update.getChangedEntity().getAstNode();
                MessageSend methodN = (MessageSend) update.getNewEntity().getAstNode();

                if(!methodC.receiver.toString().equals(methodN.receiver.toString())) {
                    String receiverA = (!methodC.receiver.toString().equals(Constants.EMPTY_STRING)) ? new String(methodC.receiver.toString()) : new String(methodC.selector);
//                    desc.append(" " + receiverA + " at " + update.getRootEntity().getJavaStructureNode().getName() + " method");
                    desc.append(" " + receiverA + " at " + getRootEntityJavaStructureNodeName(update) + " method");
                } else if(!(new String(methodC.selector)).equals((new String(methodN.selector)))) {
                    desc.append(new String(methodC.selector) + " at " + getParentEntityName(update) + " method");
                } else if((methodC.arguments != null) && (methodN.arguments != null) && (!methodC.arguments.equals(methodN.arguments))) {
                    // BUG解决：上方加入对methodC.arguments是否为空的判断，否则会出现null.equals()的情况！
                    String name = !(new String(methodC.selector)).equals(Constants.EMPTY_STRING) ? (new String(methodC.selector)) : methodC.receiver.toString();
                    String methodName = update.getRootEntity().getUniqueName().substring(update.getRootEntity().getUniqueName().lastIndexOf(".") + 1, update.getRootEntity().getUniqueName().length());
                    ASTNode astNode = update.getRootEntity().getJavaStructureNode().getASTNode();
                    if (astNode instanceof MethodDeclaration) {
                        MethodDeclaration methodDeclaration = (MethodDeclaration) astNode;
                        if (methodDeclaration.arguments != null && methodDeclaration.arguments.length >0) {
                            StringBuilder methodNameStringBuilder = new StringBuilder(methodName.substring(0, methodName.indexOf("(") + 1));

                            for (Argument argument : methodDeclaration.arguments) {
                                methodNameStringBuilder.append(new String(argument.type.getLastToken()) +
                                        " " +
                                        new String(argument.name));
                                methodNameStringBuilder.append(", ");
                            }
                            if (methodNameStringBuilder.toString().endsWith(", ")) {
                                methodNameStringBuilder.delete(methodNameStringBuilder.length() - 2, methodNameStringBuilder.length());
                                methodNameStringBuilder.append(")");
                            }

                            methodName = methodNameStringBuilder.toString();
                        }
                    }
                    desc.replace(desc.lastIndexOf(fType), desc.lastIndexOf(fType) + fType.length(), Constants.EMPTY_STRING);
                    desc.insert(0, "Modify arguments list when calling " + name + " method at " + methodName + " method");
                }
            } else if(update.getChangedEntity().getType() == JavaEntityType.ASSIGNMENT) {
                desc.append(StringUtils.capitalize(fType) + " ");
                Assignment asC = (Assignment) update.getChangedEntity().getAstNode();
                Assignment asN = (Assignment) update.getNewEntity().getAstNode();

                if(asC.lhs != asN.lhs) {
                    desc.append(" of " + new String(asC.lhs.toString()) + " type");
                    if(!update.getParentEntity().getName().equals(Constants.EMPTY_STRING)) {
                        desc.append(" at " + getParentEntityName(update) + " method");
                    }
                } else if(asC.expression != asN.expression) {
                    desc.append(" of " + new String(asC.expression.toString()) + " to " + new String(asN.expression.toString()));
                    if(!update.getParentEntity().getName().equals(Constants.EMPTY_STRING)) {
                        desc.append(" at " + getParentEntityName(update) + " method");
                    }

                }
            } else if(update.getChangedEntity().getAstNode() instanceof PrefixExpression) {
                desc.append(StringUtils.capitalize(fType));
                PrefixExpression prefixExpression = (PrefixExpression) update.getChangedEntity().getAstNode();
                if(OperatorIds.PLUS == prefixExpression.operator) {
                    desc.append(" increment ");
                } else if(OperatorIds.MINUS == prefixExpression.operator) {
                    desc.append(" decrement ");
                }
                desc.append(" " + prefixExpression.lhs.toString());
            } else if(update.getChangedEntity().getAstNode() != null && update.getChangedEntity().getAstNode() instanceof ReturnStatement) {
                String beforeName = update.getChangedEntity().getName().replace(";", Constants.EMPTY_STRING);
                String afterName = update.getNewEntity().getUniqueName().replace(";", Constants.EMPTY_STRING);
                desc.append(StringUtils.capitalize(fType));
                if(!afterName.contains(Constants.NEW_LINE)) {
                    desc.append(" " + beforeName + " with " + afterName);
                }
                desc.append(" at " + getRootEntityJavaStructureNodeName(update) + " " + update.getRootEntity().getJavaStructureNode().getType().name().toLowerCase());
            } else {
                String name = Constants.EMPTY_STRING;
                if(update.getChangedEntity().getAstNode() instanceof LocalDeclaration) {
                    name = new String(((LocalDeclaration) update.getChangedEntity().getAstNode()).name);
                } else {
                    name = update.getChangedEntity().getName();
                }
                desc.append(StringUtils.capitalize(fType) + " " + name);
                if(!update.getParentEntity().getName().equals(Constants.EMPTY_STRING)) {
                    desc.append(" at " + getParentEntityName(update) + " method");
                }
            }
        } else if(update.getChangeType() == ChangeType.METHOD_RENAMING) {
            desc.append("Rename " + update.getChangedEntity().getName().substring(0, update.getChangedEntity().getName().indexOf("(")) + " method " + " with " + update.getNewEntity().getName().substring(0, update.getNewEntity().getName().indexOf("(")));
        } else if(update.getChangeType() == ChangeType.ATTRIBUTE_RENAMING) {
            desc.append("Rename " + update.getChangedEntity().getName().substring(0, update.getChangedEntity().getName().indexOf(":")).trim() + " object attribute " + " with " + update.getNewEntity().getName().substring(0, update.getNewEntity().getName().indexOf(":")).trim());
        } else if(update.getChangeType() == ChangeType.ATTRIBUTE_TYPE_CHANGE) {
            if(update.getChangedEntity().getAstNode() != null && update.getChangedEntity().getJavaStructureNode() != null) {
                if(update.getChangedEntity().getJavaStructureNode().getASTNode() instanceof FieldDeclaration) {

                    FieldDeclaration field = (FieldDeclaration) update.getChangedEntity().getJavaStructureNode().getASTNode();
                    desc.append("Change attribute type of " + new String(field.name) + " with " + update.getNewEntity().getAstNode().toString());
                } else if(update.getChangedEntity().getJavaStructureNode().getASTNode() instanceof TrueLiteral) {
                    System.out.println("hola");
                }
            } else {
                if(update.getChangedEntity().getName() == null) {
                    update.getChangedEntity().setName(update.getChangedEntity().getUniqueName());
                }
                String name = (!update.getChangedEntity().getName().equals(Constants.EMPTY_STRING)) ? update.getChangedEntity().getName() : update.getChangedEntity().getAstNode().toString();
                desc.append("Change attribute type " + name + " with " + update.getNewEntity().getUniqueName().toString());
            }
        } else if(update.getChangeType() == ChangeType.CONDITION_EXPRESSION_CHANGE) {
            // 此处substring的开头应改为0，否则当字符串长度仅为1时，程序会报错！
            desc.append("Modify conditional expression " + update.getChangedEntity().getName().substring(0, update.getChangedEntity().getName().length() - 1) + " with " + update.getNewEntity().getUniqueName() + " at " + getParentEntityName(update) + " method");
        } else if(update.getChangeType() == ChangeType.INCREASING_ACCESSIBILITY_CHANGE) {
            String forValue = (update.getRootEntity().getJavaStructureNode().getName().indexOf(":") > - 1) ? update.getRootEntity().getJavaStructureNode().getName().substring(0, update.getRootEntity().getJavaStructureNode().getName().indexOf(":") - 1) : update.getRootEntity().getJavaStructureNode().getName();
            desc.append("Increase accessibility of " + update.getChangedEntity().getUniqueName() + " to " + update.getNewEntity().getUniqueName() + " for " + forValue + " " + update.getRootEntity().getType().name().toLowerCase());
        } else if(update.getChangeType() == ChangeType.PARENT_CLASS_CHANGE) {
            desc.append("Modify the " + "parent class " + update.getChangedEntity().getUniqueName() + " with " + update.getNewEntity().getUniqueName());
        } else if(update.getChangeType() == ChangeType.PARENT_INTERFACE_CHANGE) {
            desc.append("Modify the " + "parent interface " + update.getChangedEntity().getUniqueName() + " with " + update.getNewEntity().getUniqueName());
        } else if(update.getChangeType() == ChangeType.DECREASING_ACCESSIBILITY_CHANGE) {
            desc.append("Decrease accessibility of " + update.getChangedEntity().getUniqueName() + " to " + update.getNewEntity().getUniqueName() + " for " + update.getRootEntity().getJavaStructureNode().getName() + " " + update.getRootEntity().getType().name().toLowerCase());
        } else if(update.getChangeType() == ChangeType.COMMENT_UPDATE || update.getChangeType() == ChangeType.DOC_UPDATE) {
            String entityType = update.getRootEntity().getJavaStructureNode().getType().name().toLowerCase();
            desc.append(fType +" at " + getRootEntityJavaStructureNodeName(update)+ " " + entityType);
        } else if(update.getChangeType() == ChangeType.PARAMETER_TYPE_CHANGE) {
            desc.append("Type's " + update.getChangedEntity().getUniqueName().substring(0, update.getChangedEntity().getUniqueName().indexOf(":") - 1).trim() + " paramater change of " + update.getChangedEntity().getUniqueName().substring(update.getChangedEntity().getUniqueName().indexOf(":") + 1, update.getChangedEntity().getUniqueName().length()).trim()  + " to " + update.getNewEntity().getUniqueName().substring(update.getNewEntity().getUniqueName().indexOf(":") + 1, update.getNewEntity().getUniqueName().length()).trim() + " for " + update.getRootEntity().getJavaStructureNode().getName() + " " + update.getRootEntity().getType().name().toLowerCase());
        } else if(update.getChangeType() == ChangeType.RETURN_TYPE_CHANGE) {
            desc.append(fType + " " + update.getChangedEntity().getUniqueName().substring(update.getChangedEntity().getUniqueName().indexOf(":") + 1).trim() + " with " + update.getNewEntity().getUniqueName().substring(update.getNewEntity().getUniqueName().indexOf(":") + 1, update.getNewEntity().getUniqueName().length()).trim()  + " for " + update.getRootEntity().getJavaStructureNode().getName() + " " + update.getRootEntity().getType().name().toLowerCase());
        }

    }

    private static void compareModified(ChangedFile file, FileDistiller distiller,Git git) {
        try {
            File previousType = Utils.getFileContentOfLastCommit(file.getPath(), git.getRepository());
            File currentType = new File(file.getAbsolutePath());
            distiller.extractClassifiedSourceCodeChanges(previousType, currentType);
        } catch (RevisionSyntaxException e) {
            e.printStackTrace();
        } catch (AmbiguousObjectException e) {
            e.printStackTrace();
        } catch (IncorrectObjectTypeException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private void compareModifiedVersions(ChangedFile file, FileDistiller distiller, Git git, String olderID, String currentID) {
        File previousType = null;
        File currentType = null;

        try {
            previousType = Utils.getFileContentOfCommitID(file.getPath(), git.getRepository(), olderID);
            currentType = Utils.getFileContentOfCommitID(file.getPath(), git.getRepository(), currentID);
            distiller.extractClassifiedSourceCodeChanges(previousType, currentType);

        } catch (RevisionSyntaxException e) {
            e.printStackTrace();
        } catch (AmbiguousObjectException e) {
            e.printStackTrace();
        } catch (IncorrectObjectTypeException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void describeDeprecatedMethod(StringBuilder desc, SourceCodeChange insert) {
        if(insert.getChangedEntity() != null && insert.getChangedEntity().getAstNode() != null) {
            Annotation[] annotations = ((ConstructorDeclaration)insert.getChangedEntity().getAstNode()).annotations;
            if(annotations != null && annotations.length > 0) {
                for (Annotation annotation : annotations) {
                    if(annotation.type.toString().equals("Deprecated")) {
                        desc.append("deprecated ");
                        break;
                    }
                }
            }

        }
    }

    /**
     * 默认为true，其余的代码均暂时注释
     * @param change
     * @return
     */
    private boolean isUnUsedMethod(SourceCodeChange change) {
        boolean isUnUsed = true;
		/*MethodDependencySummary methodDependencySummary = new MethodDependencySummary(change.getChangedEntity().getName());
		methodDependencySummary.setConstructor(change.getChangedEntity().getJavaStructureNode().getType() == Type.CONSTRUCTOR);
		methodDependencySummary.setDifferences(getDifferences());
		methodDependencySummary.find();

		if(methodDependencySummary.getDependencies() != null && methodDependencySummary.getDependencies().size() > 0) {
			isUnUsed = false;
		}*/

        return isUnUsed;
    }

    /**
     * 描述依赖文件的改变，先禁用
     * @param descriptor
     */
    private void describeCollateralChanges(StringBuilder descriptor) {
        List<NamedMember> impactedElements = new ArrayList<NamedMember>();
        StringBuilder localDescriptor = new StringBuilder(Constants.EMPTY_STRING);
        //search collateral changes
        for (SourceCodeChange change : addedRemovedFunctionalities) {
            MethodDependencySummary methodDependencySummary = new MethodDependencySummary(change.getChangedEntity().getUniqueName());
            methodDependencySummary.setConstructor(change.getChangedEntity().getJavaStructureNode().getType() == Type.CONSTRUCTOR);
            methodDependencySummary.setDifferences(getDifferences());
            methodDependencySummary.find();
            if(methodDependencySummary.getDependencies() != null && methodDependencySummary.getDependencies().size() > 0) {
                List<SearchMatch> dependencies = methodDependencySummary.getDependencies();
                for (SearchMatch searchMatch : dependencies) {
                    NamedMember type = getNamedMemberFromElement(searchMatch);
                    impactedElements.add(type);
                }
            }
        }
        if(impactedElements.size() > 0) {
            localDescriptor.append("\n\t\tThe added/removed methods triggered changes to ");
            for (NamedMember type : impactedElements) {
                IJavaElement iJavaElement = type.getParent();
                String name = iJavaElement.getElementName();
                if(!localDescriptor.toString().contains(name)) {
                    localDescriptor.append(name + " " + PhraseUtils.getStringType(type.getDeclaringType()) + ", ");
                }
            }

            if(localDescriptor.toString().trim().length() > 0) {
                localDescriptor = new StringBuilder(localDescriptor.substring(0, localDescriptor.length() - 2));
                localDescriptor.append(Constants.NEW_LINE);
            }
            descriptor.append(localDescriptor.toString());
        }
    }

    private NamedMember getNamedMemberFromElement(SearchMatch searchMatch) {
        NamedMember type = null;
        if (searchMatch.getElement() instanceof ResolvedSourceMethod) {
            type = ((ResolvedSourceMethod) searchMatch.getElement());
        } else if (searchMatch.getElement() instanceof ResolvedSourceType) {
            type = ((ResolvedSourceType) searchMatch.getElement());
        } else if (searchMatch.getElement() instanceof ResolvedSourceField) {
            type = ((ResolvedSourceField) searchMatch.getElement());
        }
        return type;
    }

    public List<SourceCodeChange> getChanges() {
        return changes;
    }

    public void setChanges(List<SourceCodeChange> changes) {
        this.changes = changes;
    }

    public ChangedFile getFile() {
        return file;
    }

    public void setFile(ChangedFile file) {
        this.file = file;
    }

    public Git getGit() {
        return git;
    }

    public void setGit(Git git) {
        this.git = git;
    }

    public ChangedFile[] getDifferences() {
        return differences;
    }

    public void setDifferences(ChangedFile[] differences) {
        this.differences = differences;
    }

    public List<SourceCodeChange> getAddedRemovedFunctionalities() {
        return addedRemovedFunctionalities;
    }

    public void setAddedRemovedFunctionalities(
            List<SourceCodeChange> addedRemovedFunctionalities) {
        this.addedRemovedFunctionalities = addedRemovedFunctionalities;
    }

    public boolean isOnlyStructuralChanges() {
        return onlyStructuralChanges;
    }

    public void setOnlyStructuralChanges(boolean onlyStructuralChanges) {
        this.onlyStructuralChanges = onlyStructuralChanges;
    }

    private String getRootEntityJavaStructureNodeName(SourceCodeChange sourceCodeChange) {
        String methodName = sourceCodeChange.getRootEntity().getUniqueName().substring(sourceCodeChange.getRootEntity().getUniqueName().lastIndexOf(".") + 1, sourceCodeChange.getRootEntity().getUniqueName().length());
        ASTNode astNode = sourceCodeChange.getRootEntity().getJavaStructureNode().getASTNode();
        if (astNode instanceof MethodDeclaration) {
            MethodDeclaration methodDeclaration = (MethodDeclaration) astNode;
            if (methodDeclaration.arguments != null && methodDeclaration.arguments.length >0) {
                StringBuilder methodNameStringBuilder = new StringBuilder(methodName.substring(0, methodName.indexOf("(") + 1));

                for (Argument argument : methodDeclaration.arguments) {
                    methodNameStringBuilder.append(new String(argument.type.getLastToken()) +
                            " " +
                            new String(argument.name));
                    methodNameStringBuilder.append(", ");
                }
                if (methodNameStringBuilder.toString().endsWith(", ")) {
                    methodNameStringBuilder.delete(methodNameStringBuilder.length() - 2, methodNameStringBuilder.length());
                    methodNameStringBuilder.append(")");
                }

                methodName = methodNameStringBuilder.toString();
            }
        }

        return methodName;
    }

    private String getParentEntityName(SourceCodeChange sourceCodeChange) {
        String methodName = sourceCodeChange.getParentEntity().getName();
        ASTNode astNode = sourceCodeChange.getRootEntity().getJavaStructureNode().getASTNode();
        if (astNode instanceof MethodDeclaration) {
            MethodDeclaration methodDeclaration = (MethodDeclaration) astNode;
            if (methodDeclaration.arguments != null && methodDeclaration.arguments.length >0) {
                StringBuilder methodNameStringBuilder = new StringBuilder(methodName.substring(0, methodName.indexOf("(") + 1));

                for (Argument argument : methodDeclaration.arguments) {
                    methodNameStringBuilder.append(new String(argument.type.getLastToken()) +
                            " " +
                            new String(argument.name));
                    methodNameStringBuilder.append(", ");
                }
                if (methodNameStringBuilder.toString().endsWith(", ")) {
                    methodNameStringBuilder.delete(methodNameStringBuilder.length() - 2, methodNameStringBuilder.length());
                    methodNameStringBuilder.append(")");
                }

                methodName = methodNameStringBuilder.toString();
            }
        }

        return methodName;
    }
}
