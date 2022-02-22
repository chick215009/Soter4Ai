package cn.edu.nju.core.stereotype.stereotyped;

import cn.edu.nju.core.ast.JParser;
import cn.edu.nju.core.ast.JParser;
import cn.edu.nju.core.git.ChangedFile;
import cn.edu.nju.core.git.ChangedFile;
import lombok.Data;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.search.SearchMatch;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Data
public class StereotypeIdentifier {
	private JParser parser;
	private List<StereotypedElement> stereotypedElements;
	double methodsMean;
	double methodsStdDev;
	private ICompilationUnit compilationUnit;
	private String scmOperation;
	private StringBuilder builder;
	private ChangedFile changedFile;
	private List<SearchMatch> relatedTypes;
	private double impactPercentage;
	private Set<String> typeReferenceStatistic; //被引用的全限定类名

//	public StereotypeIdentifier() {
//		super();
//		this.stereotypedElements = new LinkedList<StereotypedElement>();
//		builder = new StringBuilder();
//	}

	public StereotypeIdentifier(final ICompilationUnit unit,
								final double methodsMean, final double methodsStdDev) {
		super();
		this.compilationUnit = unit;
		this.parser = new JParser(unit);
		this.methodsMean = methodsMean;
		this.methodsStdDev = methodsStdDev;
		this.stereotypedElements = new LinkedList<StereotypedElement>();
		this.builder = new StringBuilder();
	}

	public StereotypeIdentifier(final IMember member, final double methodsMean,
								final double methodsStdDev) {
		super();
		this.parser = new JParser(member);
		this.methodsMean = methodsMean;
		this.methodsStdDev = methodsStdDev;
		this.stereotypedElements = new LinkedList<StereotypedElement>();
	}

//	public StereotypeIdentifier(File file) throws CoreException {
//		super();
//		this.parser = new JParser(file);
//		this.stereotypedElements = new LinkedList<StereotypedElement>();
//		this.builder = new StringBuilder();
//	}

	public StereotypeIdentifier(ChangedFile changedFile) {
		this.parser = new JParser(changedFile);
		this.stereotypedElements = new LinkedList<>();
		this.builder = new StringBuilder();
//		this.typeReferenceStatistic = new HashSet();
	}

	public void setParameters(final ICompilationUnit unit,
							  final double methodsMean, final double methodsStdDev) {
		this.parser = new JParser(unit);
		this.methodsMean = methodsMean;
		this.methodsStdDev = methodsStdDev;
	}

	public void setParameters(final IMember member, final double methodsMean,
							  final double methodsStdDev) {
		this.parser = new JParser(member);
		this.methodsMean = methodsMean;
		this.methodsStdDev = methodsStdDev;
	}

	/**
	 * 确认TypeDeclaration、MethodDeclaration的stereotype
	 */
	public void identifyStereotypes() {
		if (this.parser == null) {
			return;
		}
		this.parser.parse();
		for (final ASTNode element : this.parser.getElements()) {
			try {
				StereotypedElement stereoElement;
				if (element instanceof TypeDeclaration) {
					stereoElement = new StereotypedType((TypeDeclaration) element, this.methodsMean, this.methodsStdDev);
				}
//				else if (element instanceof MethodDeclaration) {
//					stereoElement = new StereotypedMethod((MethodDeclaration) element);
//				}
				else {
					continue;
				}
//				else if (element instanceof EnumDeclaration){
//					//TODO
//				} else if (element instanceof AnnotationTypeDeclaration) {
//					//TODO
//				} else {
				stereoElement.findStereotypes();
				this.stereotypedElements.add(stereoElement);
			} catch (NullPointerException ex) {
				ex.printStackTrace();
			}
		}
	}


	@Override
	public String toString() {
		if (null == builder) {
			builder = new StringBuilder();
		}
		return builder.toString();
	}
}
//
//import java.io.File;
//import java.util.LinkedList;
//import java.util.List;
//
//import cn.edu.nju.git.ChangedFile;
//import cn.edu.nju.utils.JDTASTUtil;
//import lombok.Data;
//import org.eclipse.core.runtime.CoreException;
//import org.eclipse.jdt.core.ICompilationUnit;
//import org.eclipse.jdt.core.IMember;
//import org.eclipse.jdt.core.dom.ASTNode;
//import org.eclipse.jdt.core.dom.CompilationUnit;
//import org.eclipse.jdt.core.dom.MethodDeclaration;
//import org.eclipse.jdt.core.dom.TypeDeclaration;
//import org.eclipse.jdt.core.search.SearchMatch;
//import co.edu.unal.colswe.changescribe.core.ast.JParser;

//@Data
//public class StereotypeIdentifier {
//	private JParser parser;
//	private List<StereotypedElement> stereotypedElements;
//	private double methodMean;
//	private double methodsStdDev;
//	private ICompilationUnit compilationUnit;
////	private CompilationUnit compilationUnit;
//	private ChangedFile changedFile;
//	private String scmOperation;
//	private StringBuilder builder;
//	private List<SearchMatch> relatedTypes;
//	private double impactPercentage;
//
//
////	private List<StereotypedType> typeElements;
////	double methodsMean;
////	double methodsStdDev;
////	private ICompilationUnit compilationUnit;
////	private String scmOperation;
////	private StringBuilder builder;
////	private ChangedFile changedFile;
////	private List<SearchMatch> relatedTypes;
////	private double impactPercentaje;
//
//	public StereotypeIdentifier() {
//		super();
//		this.stereotypedElements = new LinkedList<StereotypedElement>();
////		builder = new StringBuilder();
//	}
//
//	public StereotypeIdentifier(File file) throws CoreException {
//		super();
//		this.parser = new JParser(file);
//		this.stereotypedElements = new LinkedList<StereotypedElement>();
////		this.builder = new StringBuilder();
//	}
//
////	public StereotypeIdentifier(String filePath) {
////		this.compilationUnit = JDTASTUtil.getCompilationUnit(filePath);
////		this.stereotypedElements = new LinkedList<>();
////	}
//
//
//
//	public void identifyStereotypes() {
//		for (final Object o : this.compilationUnit.types()) {
//			if (o instanceof TypeDeclaration) {
//				TypeDeclaration typeDeclaration = (TypeDeclaration) o;
//				StereotypedElement stereotypedElement= new StereotypedType(typeDeclaration, 0, 0);
//				stereotypedElement.findStereotypes();
//				this.stereotypedElements.add(stereotypedElement);
//			}
//		}
//	}
//
//}
