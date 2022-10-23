package ch.uzh.ifi.seal.changedistiller.ast.java;

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

import ch.uzh.ifi.seal.changedistiller.ast.ASTNodeTypeConverter;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.EntityType;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import org.eclipse.jdt.internal.compiler.ast.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link ASTNodeTypeConverter} for the Java programming language.
 *
 * @author Beat Fluri
 * @author Michael Wuersch
 */
public final class JavaASTNodeTypeConverter implements ASTNodeTypeConverter {

    private static Map<Class<? extends ASTNode>, JavaEntityType> sConversionMap =
            new HashMap<Class<? extends ASTNode>, JavaEntityType>();

    static {
        sConversionMap.put(Assignment.class, JavaEntityType.ASSIGNMENT);
        sConversionMap.put(CompoundAssignment.class, JavaEntityType.ASSIGNMENT);
        sConversionMap.put(PostfixExpression.class, JavaEntityType.POSTFIX_EXPRESSION);
        sConversionMap.put(PrefixExpression.class, JavaEntityType.PREFIX_EXPRESSION);
        sConversionMap.put(AllocationExpression.class, JavaEntityType.CLASS_INSTANCE_CREATION);
        sConversionMap.put(QualifiedAllocationExpression.class, JavaEntityType.CLASS_INSTANCE_CREATION);
        sConversionMap.put(AssertStatement.class, JavaEntityType.ASSERT_STATEMENT);
        sConversionMap.put(BreakStatement.class, JavaEntityType.BREAK_STATEMENT);
        sConversionMap.put(ExplicitConstructorCall.class, JavaEntityType.CONSTRUCTOR_INVOCATION);
        sConversionMap.put(ContinueStatement.class, JavaEntityType.CONTINUE_STATEMENT);
        sConversionMap.put(DoStatement.class, JavaEntityType.DO_STATEMENT);
        sConversionMap.put(EmptyStatement.class, JavaEntityType.EMPTY_STATEMENT);
        sConversionMap.put(ForeachStatement.class, JavaEntityType.FOREACH_STATEMENT);
        sConversionMap.put(ForStatement.class, JavaEntityType.FOR_STATEMENT);
        sConversionMap.put(IfStatement.class, JavaEntityType.IF_STATEMENT);
        sConversionMap.put(LabeledStatement.class, JavaEntityType.LABELED_STATEMENT);
        sConversionMap.put(LocalDeclaration.class, JavaEntityType.VARIABLE_DECLARATION_STATEMENT);
        sConversionMap.put(MessageSend.class, JavaEntityType.METHOD_INVOCATION);
        sConversionMap.put(ReturnStatement.class, JavaEntityType.RETURN_STATEMENT);
        sConversionMap.put(SwitchStatement.class, JavaEntityType.SWITCH_STATEMENT);
        sConversionMap.put(CaseStatement.class, JavaEntityType.SWITCH_CASE);
        sConversionMap.put(SingleTypeReference.class, JavaEntityType.SINGLE_TYPE); // Bug #14: Cannot distinguish between primitive and simple types without resolving bindings
        sConversionMap.put(SynchronizedStatement.class, JavaEntityType.SYNCHRONIZED_STATEMENT);
        sConversionMap.put(ThrowStatement.class, JavaEntityType.THROW_STATEMENT);
        sConversionMap.put(TryStatement.class, JavaEntityType.TRY_STATEMENT);
        sConversionMap.put(WhileStatement.class, JavaEntityType.WHILE_STATEMENT);
        sConversionMap.put(ParameterizedSingleTypeReference.class, JavaEntityType.PARAMETERIZED_TYPE);
        sConversionMap.put(ParameterizedQualifiedTypeReference.class, JavaEntityType.PARAMETERIZED_TYPE);
        sConversionMap.put(Javadoc.class, JavaEntityType.JAVADOC);
        sConversionMap.put(QualifiedTypeReference.class, JavaEntityType.QUALIFIED_TYPE);
        sConversionMap.put(Argument.class, JavaEntityType.PARAMETER);
        sConversionMap.put(TypeParameter.class, JavaEntityType.TYPE_PARAMETER);
        sConversionMap.put(Wildcard.class, JavaEntityType.WILDCARD_TYPE);
        sConversionMap.put(StringLiteral.class, JavaEntityType.STRING_LITERAL);
        sConversionMap.put(ExtendedStringLiteral.class, JavaEntityType.STRING_LITERAL);
        sConversionMap.put(StringLiteralConcatenation.class, JavaEntityType.STRING_LITERAL);
        sConversionMap.put(FalseLiteral.class, JavaEntityType.BOOLEAN_LITERAL);
        sConversionMap.put(TrueLiteral.class, JavaEntityType.BOOLEAN_LITERAL);
        sConversionMap.put(NullLiteral.class, JavaEntityType.NULL_LITERAL);
        sConversionMap.put(DoubleLiteral.class, JavaEntityType.NUMBER_LITERAL);
        sConversionMap.put(FloatLiteral.class, JavaEntityType.NUMBER_LITERAL);
        sConversionMap.put(LongLiteral.class, JavaEntityType.NUMBER_LITERAL);
        sConversionMap.put(LongLiteralMinValue.class, JavaEntityType.NUMBER_LITERAL);
        sConversionMap.put(IntLiteral.class, JavaEntityType.NUMBER_LITERAL);
        sConversionMap.put(IntLiteralMinValue.class, JavaEntityType.NUMBER_LITERAL);
        sConversionMap.put(CharLiteral.class, JavaEntityType.CHARACTER_LITERAL);
        sConversionMap.put(AND_AND_Expression.class, JavaEntityType.INFIX_EXPRESSION);
        sConversionMap.put(OR_OR_Expression.class, JavaEntityType.INFIX_EXPRESSION);
        sConversionMap.put(ArrayAllocationExpression.class, JavaEntityType.ARRAY_CREATION);
        sConversionMap.put(ArrayInitializer.class, JavaEntityType.ARRAY_INITIALIZER);
        sConversionMap.put(ArrayQualifiedTypeReference.class, JavaEntityType.QUALIFIED_TYPE);
        sConversionMap.put(ArrayReference.class, JavaEntityType.ARRAY_ACCESS);
        sConversionMap.put(ArrayTypeReference.class, JavaEntityType.ARRAY_TYPE);
        sConversionMap.put(BinaryExpression.class, JavaEntityType.INFIX_EXPRESSION);
        sConversionMap.put(CombinedBinaryExpression.class, JavaEntityType.INFIX_EXPRESSION);
        sConversionMap.put(Block.class, JavaEntityType.BLOCK);
        sConversionMap.put(CastExpression.class, JavaEntityType.CAST_EXPRESSION);
        sConversionMap.put(ClassLiteralAccess.class, JavaEntityType.TYPE_LITERAL);
        sConversionMap.put(ConditionalExpression.class, JavaEntityType.CONDITIONAL_EXPRESSION);
        sConversionMap.put(EqualExpression.class, JavaEntityType.INFIX_EXPRESSION);
        sConversionMap.put(FieldReference.class, JavaEntityType.FIELD_ACCESS);
        sConversionMap.put(QualifiedNameReference.class, JavaEntityType.QUALIFIED_NAME);
        sConversionMap.put(SingleNameReference.class, JavaEntityType.SIMPLE_NAME);
        sConversionMap.put(QualifiedSuperReference.class, JavaEntityType.QUALIFIED_NAME);
        sConversionMap.put(SuperReference.class, JavaEntityType.SIMPLE_NAME);
        sConversionMap.put(QualifiedThisReference.class, JavaEntityType.QUALIFIED_NAME);
        sConversionMap.put(ThisReference.class, JavaEntityType.SIMPLE_NAME);
        sConversionMap.put(UnaryExpression.class, JavaEntityType.PREFIX_EXPRESSION);
        sConversionMap.put(InstanceOfExpression.class, JavaEntityType.INSTANCEOF_EXPRESSION);
        sConversionMap.put(FieldDeclaration.class, JavaEntityType.FIELD);
        sConversionMap.put(MethodDeclaration.class, JavaEntityType.METHOD);
        sConversionMap.put(Clinit.class, JavaEntityType.METHOD);
        sConversionMap.put(ConstructorDeclaration.class, JavaEntityType.METHOD);
        sConversionMap.put(TypeDeclaration.class, JavaEntityType.CLASS);
        sConversionMap.put(Initializer.class, JavaEntityType.FIELD);
        sConversionMap.put(MarkerAnnotation.class,JavaEntityType.ANNOTATION);
    }

    @Override
    public EntityType convertNode(Object node) {
        if (!(node instanceof ASTNode)) {
            throw new RuntimeException("Node must be of type ASTNode.");
        }

        return sConversionMap.get(node.getClass());
    }

}
