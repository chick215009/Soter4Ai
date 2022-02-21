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

import static ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType.*;

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
        sConversionMap.put(Assignment.class, ASSIGNMENT);
        sConversionMap.put(CompoundAssignment.class, ASSIGNMENT);
        sConversionMap.put(PostfixExpression.class, POSTFIX_EXPRESSION);
        sConversionMap.put(PrefixExpression.class, PREFIX_EXPRESSION);
        sConversionMap.put(AllocationExpression.class, CLASS_INSTANCE_CREATION);
        sConversionMap.put(QualifiedAllocationExpression.class, CLASS_INSTANCE_CREATION);
        sConversionMap.put(AssertStatement.class, ASSERT_STATEMENT);
        sConversionMap.put(BreakStatement.class, BREAK_STATEMENT);
        sConversionMap.put(ExplicitConstructorCall.class, CONSTRUCTOR_INVOCATION);
        sConversionMap.put(ContinueStatement.class, CONTINUE_STATEMENT);
        sConversionMap.put(DoStatement.class, DO_STATEMENT);
        sConversionMap.put(EmptyStatement.class, EMPTY_STATEMENT);
        sConversionMap.put(ForeachStatement.class, FOREACH_STATEMENT);
        sConversionMap.put(ForStatement.class, FOR_STATEMENT);
        sConversionMap.put(IfStatement.class, IF_STATEMENT);
        sConversionMap.put(LabeledStatement.class, LABELED_STATEMENT);
        sConversionMap.put(LocalDeclaration.class, VARIABLE_DECLARATION_STATEMENT);
        sConversionMap.put(MessageSend.class, METHOD_INVOCATION);
        sConversionMap.put(ReturnStatement.class, RETURN_STATEMENT);
        sConversionMap.put(SwitchStatement.class, SWITCH_STATEMENT);
        sConversionMap.put(CaseStatement.class, SWITCH_CASE);
        sConversionMap.put(SingleTypeReference.class, SINGLE_TYPE); // Bug #14: Cannot distinguish between primitive and simple types without resolving bindings
        sConversionMap.put(SynchronizedStatement.class, SYNCHRONIZED_STATEMENT);
        sConversionMap.put(ThrowStatement.class, THROW_STATEMENT);
        sConversionMap.put(TryStatement.class, TRY_STATEMENT);
        sConversionMap.put(WhileStatement.class, WHILE_STATEMENT);
        sConversionMap.put(ParameterizedSingleTypeReference.class, PARAMETERIZED_TYPE);
        sConversionMap.put(ParameterizedQualifiedTypeReference.class, PARAMETERIZED_TYPE);
        sConversionMap.put(Javadoc.class, JAVADOC);
        sConversionMap.put(QualifiedTypeReference.class, QUALIFIED_TYPE);
        sConversionMap.put(Argument.class, PARAMETER);
        sConversionMap.put(TypeParameter.class, TYPE_PARAMETER);
        sConversionMap.put(Wildcard.class, WILDCARD_TYPE);
        sConversionMap.put(StringLiteral.class, STRING_LITERAL);
        sConversionMap.put(ExtendedStringLiteral.class, STRING_LITERAL);
        sConversionMap.put(StringLiteralConcatenation.class, STRING_LITERAL);
        sConversionMap.put(FalseLiteral.class, BOOLEAN_LITERAL);
        sConversionMap.put(TrueLiteral.class, BOOLEAN_LITERAL);
        sConversionMap.put(NullLiteral.class, NULL_LITERAL);
        sConversionMap.put(DoubleLiteral.class, NUMBER_LITERAL);
        sConversionMap.put(FloatLiteral.class, NUMBER_LITERAL);
        sConversionMap.put(LongLiteral.class, NUMBER_LITERAL);
        sConversionMap.put(LongLiteralMinValue.class, NUMBER_LITERAL);
        sConversionMap.put(IntLiteral.class, NUMBER_LITERAL);
        sConversionMap.put(IntLiteralMinValue.class, NUMBER_LITERAL);
        sConversionMap.put(CharLiteral.class, CHARACTER_LITERAL);
        sConversionMap.put(AND_AND_Expression.class, INFIX_EXPRESSION);
        sConversionMap.put(OR_OR_Expression.class, INFIX_EXPRESSION);
        sConversionMap.put(ArrayAllocationExpression.class, ARRAY_CREATION);
        sConversionMap.put(ArrayInitializer.class, ARRAY_INITIALIZER);
        sConversionMap.put(ArrayQualifiedTypeReference.class, QUALIFIED_TYPE);
        sConversionMap.put(ArrayReference.class, ARRAY_ACCESS);
        sConversionMap.put(ArrayTypeReference.class, ARRAY_TYPE);
        sConversionMap.put(BinaryExpression.class, INFIX_EXPRESSION);
        sConversionMap.put(CombinedBinaryExpression.class, INFIX_EXPRESSION);
        sConversionMap.put(Block.class, BLOCK);
        sConversionMap.put(CastExpression.class, CAST_EXPRESSION);
        sConversionMap.put(ClassLiteralAccess.class, TYPE_LITERAL);
        sConversionMap.put(ConditionalExpression.class, CONDITIONAL_EXPRESSION);
        sConversionMap.put(EqualExpression.class, INFIX_EXPRESSION);
        sConversionMap.put(FieldReference.class, FIELD_ACCESS);
        sConversionMap.put(QualifiedNameReference.class, QUALIFIED_NAME);
        sConversionMap.put(SingleNameReference.class, SIMPLE_NAME);
        sConversionMap.put(QualifiedSuperReference.class, QUALIFIED_NAME);
        sConversionMap.put(SuperReference.class, SIMPLE_NAME);
        sConversionMap.put(QualifiedThisReference.class, QUALIFIED_NAME);
        sConversionMap.put(ThisReference.class, SIMPLE_NAME);
        sConversionMap.put(UnaryExpression.class, PREFIX_EXPRESSION);
        sConversionMap.put(InstanceOfExpression.class, INSTANCEOF_EXPRESSION);
        sConversionMap.put(FieldDeclaration.class, FIELD);
        sConversionMap.put(MethodDeclaration.class, METHOD);
        sConversionMap.put(Clinit.class, METHOD);
        sConversionMap.put(ConstructorDeclaration.class, METHOD);
        sConversionMap.put(TypeDeclaration.class, CLASS);
        sConversionMap.put(Initializer.class, FIELD);
    }

    @Override
    public EntityType convertNode(Object node) {
        if (!(node instanceof ASTNode)) {
            throw new RuntimeException("Node must be of type ASTNode.");
        }
        
        return sConversionMap.get(node.getClass());
    }

}
