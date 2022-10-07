package cn.edu.nju.core.stereotype.rules;

import cn.edu.nju.core.stereotype.analyzer.MethodAnalyzer;
import cn.edu.nju.core.stereotype.information.VariableInfo;
import cn.edu.nju.core.stereotype.taxonomy.MethodStereotype;
import lombok.Data;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.Type;


@Data
public class MethodStereotypeRules {
	protected MethodAnalyzer methodAnalyzer;

	protected MethodStereotype checkForAbstract() {
		if (!this.methodAnalyzer.hasBody()) {
			return MethodStereotype.ABSTRACT;
		}
		return null;
	}

	protected MethodStereotype checkForEmpty() {
		if (!this.methodAnalyzer.hasStatements()) {
			return MethodStereotype.EMPTY;
		}
		return null;
	}

	protected MethodStereotype checkForMutatorStereotype() {
		if (this.methodAnalyzer.getSetFields().isEmpty()) {
			return null;
		}
		if (!this.isVoid(this.methodAnalyzer.getReturnType())
				&& !this.isBoolean(this.methodAnalyzer.getReturnType())) {
			return MethodStereotype.NON_VOID_COMMAND;
		}
		if (this.methodAnalyzer.getSetFields().size() == 1) {
			return MethodStereotype.SET;
		}
		return MethodStereotype.COMMAND;
	}

	protected MethodStereotype checkForAccessorStereotype() {
		if (this.methodAnalyzer.getSetFields().isEmpty()) {
			if (!this.isVoid(this.methodAnalyzer.getReturnType())) {
				if (!this.methodAnalyzer.getGetFields().isEmpty()
						&& this.methodAnalyzer.getPropertyFields().isEmpty()) {
					return MethodStereotype.GET;
				}
				if (this.isBoolean(this.methodAnalyzer.getReturnType())) {
					if (!this.methodAnalyzer.getPropertyFields().isEmpty()) {
						return MethodStereotype.PREDICATE;
					}
				} else if (!this.methodAnalyzer.getPropertyFields().isEmpty()) {
					return MethodStereotype.PROPERTY;
				}
			} else if (!this.methodAnalyzer.getVoidAccessorFields().isEmpty()) {
				return MethodStereotype.VOID_ACCESSOR;
			}
		}
		return null;
	}

	protected MethodStereotype checkForCreationalStereotype() {
		if (this.methodAnalyzer.isConstructor()) {
			return MethodStereotype.CONSTRUCTOR;
		}
		if (this.methodAnalyzer.overridesClone()) {
			return MethodStereotype.COPY_CONSTRUCTOR;
		}
		if (this.methodAnalyzer.overridesFinalize()) {
			return MethodStereotype.DESTRUCTOR;
		}
		if (this.methodAnalyzer.isInstantiatedReturn()) {
			return MethodStereotype.FACTORY;
		}
		return null;
	}

	protected MethodStereotype checkForCollaborationalStereotype(
			final boolean asPrimaryStereotype) {
		boolean allPrimitiveParameters = true; //默认所有方法参数是基本类型
		boolean allPrimitiveVariables = true; //默认所有本地变量（方法内部定义的变量）是基本类型
		int returnedFieldVariables = 0; //返回的成员变量数量（成员变量与返回结果相关联的数量）
		int modifiedObjectParameters = 0; //被修改的方法参数数量
		for (final VariableInfo parameter : this.methodAnalyzer.getParameters()) { //遍历方法参数
			//确定所有方法参数是否为基本类型
			if (parameter.getVariableBinding() != null && !this.isPrimitive(parameter.getVariableBinding())) {
				allPrimitiveParameters = false;
			}
			//统计返回的本地变量数量
			if (parameter.isReturned()
					&& !parameter.getAssignedFields().isEmpty()) {
				++returnedFieldVariables;
			}
			//如果方法参数没有修改，或者参数为基本类型
			if (!parameter.isModified()
					|| this.isPrimitive(parameter.getVariableBinding())) {
				continue;
			}
			//统计被修改的方法参数数量
			++modifiedObjectParameters;
		}
		for (final VariableInfo variable : this.methodAnalyzer.getVariables()) { //遍历变量
			//确定所有本地变量是否为基本类型
			if (variable.getVariableBinding() != null && !this.isPrimitive(variable.getVariableBinding())) {
				allPrimitiveVariables = false;
			}
			//如果本地变量并没有被返回，或者本地变量与成员变量无关
			if (!variable.isReturned() || variable.getAssignedFields().isEmpty()) {
				continue;
			}
			//统计返回的成员变量数量
			++returnedFieldVariables;
		}
		if (asPrimaryStereotype) {
			//如果方法参数存在非基本类型，并且本地变量存在非基本类型
			if ((!this.methodAnalyzer.getParameters().isEmpty() && !allPrimitiveParameters)
					|| (!this.methodAnalyzer.getVariables().isEmpty() && !allPrimitiveVariables)) {
				return MethodStereotype.COLLABORATOR;
			}
		//如果方法参数的数量+本地变量的数量>返回的成员变量数量。此外，方法参数发生了修改，或者存在非基本类型本地变量
		} else if (((!this.methodAnalyzer.getParameters().isEmpty() && modifiedObjectParameters > 0) || (!this.methodAnalyzer
				.getVariables().isEmpty() && !allPrimitiveVariables))
				&& this.methodAnalyzer.getParameters().size()
						+ this.methodAnalyzer.getVariables().size() > returnedFieldVariables) {
			return MethodStereotype.COLLABORATOR;
		}
		//如果调用了本地方法，也调用了外部方法
		if (!this.methodAnalyzer.getInvokedLocalMethods().isEmpty()
				&& !this.methodAnalyzer.getInvokedExternalMethods().isEmpty()) {
			return MethodStereotype.COLLABORATOR;
		}
		//如果调用了外部方法，并且该方法使用了成员变量
		if (!this.methodAnalyzer.getInvokedExternalMethods().isEmpty()
				&& this.methodAnalyzer.usesFields()) {
			return MethodStereotype.COLLABORATOR;
		}

		//如果方法参数和本地变量都是基本类型
		if (allPrimitiveParameters && allPrimitiveVariables) {
			//如果调用了本地方法，并且没有调用外部方法
			if (!this.methodAnalyzer.getInvokedLocalMethods().isEmpty()
					&& this.methodAnalyzer.getInvokedExternalMethods()
							.isEmpty()) {
				return MethodStereotype.LOCAL_CONTROLLER;
			}
			//如果调用了外部方法，没有调用本地方法，没有和成员变量有交互（获取、修改成员变量的信息）
			if (!this.methodAnalyzer.getInvokedExternalMethods().isEmpty()
					&& this.methodAnalyzer.getInvokedLocalMethods().isEmpty()
					&& !this.methodAnalyzer.usesFields()
					&& this.methodAnalyzer.getGetFields().isEmpty()
					&& this.methodAnalyzer.getPropertyFields().isEmpty()
					&& this.methodAnalyzer.getSetFields().isEmpty()) {
				return MethodStereotype.CONTROLLER;
			}
		}
		return null;
	}

	private boolean isVoid(final Type type) {
		if (type != null && type.isPrimitiveType()) {
			final PrimitiveType primitive = (PrimitiveType) type;
			if (primitive.getPrimitiveTypeCode().equals(
					(Object) PrimitiveType.VOID)) {
				return true;
			}
		}
		return false;
	}

	private boolean isBoolean(final Type type) {
		if (type != null && type.isPrimitiveType()) {
			final PrimitiveType primitive = (PrimitiveType) type;
			if (primitive.getPrimitiveTypeCode().equals(
					(Object) PrimitiveType.BOOLEAN)) {
				return true;
			}
		}
		return false;
	}

	private boolean isPrimitive(final IVariableBinding binding) {
		return binding.getType().isPrimitive();
	}
}
