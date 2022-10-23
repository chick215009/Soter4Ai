package ch.uzh.ifi.seal.changedistiller.distilling;

/*
 * #%L
 * ChangeDistiller
 * %%
 * Copyright (C) 2011 - 2013 Software Architecture and Evolution Lab, Department of Informatics, UZH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import ch.uzh.ifi.seal.changedistiller.ast.ASTHelper;
import ch.uzh.ifi.seal.changedistiller.ast.ASTHelperFactory;
import ch.uzh.ifi.seal.changedistiller.distilling.refactoring.RefactoringCandidateContainer;
import ch.uzh.ifi.seal.changedistiller.distilling.refactoring.RefactoringCandidateProcessor;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.HeadType;
import ch.uzh.ifi.seal.changedistiller.model.entities.ClassHistory;
import ch.uzh.ifi.seal.changedistiller.model.entities.HeadChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.structuredifferencing.StructureDiffNode;
import ch.uzh.ifi.seal.changedistiller.structuredifferencing.StructureDifferencer;
import ch.uzh.ifi.seal.changedistiller.structuredifferencing.StructureNode;
import ch.uzh.ifi.seal.changedistiller.structuredifferencing.java.JavaStructureNode;
import com.google.inject.Inject;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Distills {@link SourceCodeChange}s between two {@link File}.
 *
 * @author Beat Fluri
 * @author Giacomo Ghezzi
 */
public class FileDistiller {

    private DistillerFactory fDistillerFactory;
    private ASTHelperFactory fASTHelperFactory;
    private RefactoringCandidateProcessor fRefactoringProcessor;
    private RefactoringCandidateContainer fRefactoringContainer;

    private List<SourceCodeChange> fChanges;
    private List<HeadChange> fHeadChanges;
    private ASTHelper<StructureNode> fLeftASTHelper;
    private ASTHelper<StructureNode> fRightASTHelper;
    private ClassHistory fClassHistory;
    private String fVersion;

    @Inject
    FileDistiller(
            DistillerFactory distillerFactory,
            ASTHelperFactory factory,
            RefactoringCandidateProcessor refactoringProcessor) {
        fDistillerFactory = distillerFactory;
        fASTHelperFactory = factory;
        fRefactoringProcessor = refactoringProcessor;
        fRefactoringContainer = new RefactoringCandidateContainer();
    }

    /**
     * Extracts classified {@link SourceCodeChange}s between two {@link File}s.
     *
     * @param left
     *            file to extract changes
     * @param right
     *            file to extract changes
     */
    public void extractClassifiedSourceCodeChanges(File left, File right) {
    	extractClassifiedSourceCodeChanges(left, "default", right, "default");
    }

    /**
     * Extracts classified {@link SourceCodeChange}s between two {@link File}s.
     *
     * @param left
     *            file to extract changes
     * @param leftVersion
     * 			  version of the language in the left file
     * @param right
     *            file to extract changes
     * @param leftVersion
     * 			  version of the language in the right file
     */
    //建立两颗语法树Helper，进行比较，version都为default
    @SuppressWarnings("unchecked")
    public void extractClassifiedSourceCodeChanges(File left, String leftVersion, File right, String rightVersion) {

    	fLeftASTHelper = fASTHelperFactory.create(left, leftVersion);
        fRightASTHelper = fASTHelperFactory.create(right, rightVersion);

        extractDifferences();
    }

	private void extractDifferences() {
		StructureDifferencer structureDifferencer = new StructureDifferencer();
        structureDifferencer.extractDifferences( ////为啥不直接送去遍历AST呢？？？
                fLeftASTHelper.createStructureTree(),
                fRightASTHelper.createStructureTree());

        extractHeadDiff(fLeftASTHelper.createStructureTree(), fRightASTHelper.createStructureTree());
        StructureDiffNode structureDiff = structureDifferencer.getDifferences();//找到不相同的节点
        if (structureDiff != null) {
        	fChanges = new LinkedList<SourceCodeChange>();
            // first node is (usually) the compilation unit
            processRootChildren(structureDiff);
        } else {
        	fChanges = Collections.emptyList();
        }
	}

    private void extractHeadDiff(StructureNode left,StructureNode right){
        fHeadChanges = new LinkedList<>();

        if (!(left instanceof JavaStructureNode && right instanceof JavaStructureNode)){
            System.out.println("left or right is not a JavaStructureNode.");
            return;
        }

        JavaStructureNode LL = (JavaStructureNode)left;
        JavaStructureNode RR = (JavaStructureNode)right;

        if (!(LL.getASTNode() instanceof CompilationUnitDeclaration && RR.getASTNode() instanceof CompilationUnitDeclaration)){
            System.out.println("leftNode or rightNode is not a CompilationUnitDeclaration.");
            return;
        }



        CompilationUnitDeclaration LAstnode = (CompilationUnitDeclaration)LL.getASTNode();
        CompilationUnitDeclaration RAstnode = (CompilationUnitDeclaration)RR.getASTNode();
        for (ImportReference i :LAstnode.imports){
            boolean has = false;
            StringBuffer L = new StringBuffer();
            i.print(0,L);
            for (ImportReference j :RAstnode.imports){
                StringBuffer R = new StringBuffer();
                j.print(0,R);
                if (L.toString().equals(R.toString())){
                    has = true;
                    break;
                }
            }
            if (!has){
                fHeadChanges.add(new HeadChange(HeadType.DEL_IMPORT,L.toString()));
            }
        }

        for (ImportReference i :RAstnode.imports){
            boolean has = false;
            StringBuffer L = new StringBuffer();
            i.print(0,L);
            for (ImportReference j :LAstnode.imports){
                StringBuffer R = new StringBuffer();
                j.print(0,R);
                if (L.toString().equals(R.toString())){
                    has = true;
                    break;
                }
            }
            if (!has){
                fHeadChanges.add(new HeadChange(HeadType.ADD_IMPORT,L.toString()));
            }
        }

        return;
    }

    public void extractClassifiedSourceCodeChanges(File left, File right, String version) {
    	fVersion = version;
    	this.extractClassifiedSourceCodeChanges(left, right);
    	fRefactoringProcessor.processRefactoringCandidates(fClassHistory, fLeftASTHelper, fRightASTHelper, fRefactoringContainer);
    }

    private void processRootChildren(StructureDiffNode diffNode) {
        for (StructureDiffNode child : diffNode.getChildren()) {
            if (child.isClassOrInterfaceDiffNode() && mayHaveChanges(child.getLeft(), child.getRight())) {
                if (fClassHistory == null) {
                	if (fVersion != null) {
                		fClassHistory = new ClassHistory(fRightASTHelper.createStructureEntityVersion(child.getRight(), fVersion));
                	} else {
                		fClassHistory = new ClassHistory(fRightASTHelper.createStructureEntityVersion(child.getRight()));
                	}
                }
                processClassDiffNode(child);
            }
        }
    }

    private void processClassDiffNode(StructureDiffNode child) {
    	ClassDistiller classDistiller;
    	if (fVersion != null) {
        classDistiller =
                new ClassDistiller(
                        child,
                        fClassHistory,
                        fLeftASTHelper,
                        fRightASTHelper,
                        fRefactoringProcessor,
                        fDistillerFactory,
                        fVersion);
    	} else {
    		classDistiller =
                new ClassDistiller(
                        child,
                        fClassHistory,
                        fLeftASTHelper,
                        fRightASTHelper,
                        fRefactoringProcessor,
                        fDistillerFactory);
    	}
        classDistiller.extractChanges();
        fChanges.addAll(classDistiller.getSourceCodeChanges());
    }

    private boolean mayHaveChanges(StructureNode left, StructureNode right) {
        return (left != null) && (right != null);
    }

    public List<SourceCodeChange> getSourceCodeChanges() {
        return fChanges;
    }

    public List<HeadChange> getfHeadChanges() {
        return fHeadChanges;
    }

    public void setfHeadChanges(List<HeadChange> fHeadChanges) {
        this.fHeadChanges = fHeadChanges;
    }

    public ClassHistory getClassHistory() {
        return fClassHistory;
    }

}
