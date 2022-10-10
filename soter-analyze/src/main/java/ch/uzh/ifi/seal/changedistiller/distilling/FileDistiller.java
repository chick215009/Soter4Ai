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
import ch.uzh.ifi.seal.changedistiller.model.entities.ClassHistory;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.structuredifferencing.StructureDiffNode;
import ch.uzh.ifi.seal.changedistiller.structuredifferencing.StructureDifferencer;
import ch.uzh.ifi.seal.changedistiller.structuredifferencing.StructureNode;
import com.google.inject.Inject;

import java.io.File;
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
        StructureDiffNode structureDiff = structureDifferencer.getDifferences();//找到不相同的节点
        if (structureDiff != null) {
        	fChanges = new LinkedList<SourceCodeChange>();
            // first node is (usually) the compilation unit
            processRootChildren(structureDiff);
        } else {
        	fChanges = Collections.emptyList();
        }
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

    public ClassHistory getClassHistory() {
        return fClassHistory;
    }

}
