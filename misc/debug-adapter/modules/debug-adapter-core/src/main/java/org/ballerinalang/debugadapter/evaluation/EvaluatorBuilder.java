/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.debugadapter.evaluation;

import io.ballerina.compiler.syntax.tree.AnnotAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerina.compiler.syntax.tree.BracedExpressionNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.ConditionalExpressionNode;
import io.ballerina.compiler.syntax.tree.ErrorConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.ExplicitAnonymousFunctionExpressionNode;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.ImplicitAnonymousFunctionExpressionNode;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.IndexedExpressionNode;
import io.ballerina.compiler.syntax.tree.InterpolationNode;
import io.ballerina.compiler.syntax.tree.LetExpressionNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.NilLiteralNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.OptionalFieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.QueryExpressionNode;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerina.compiler.syntax.tree.RestArgumentNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TemplateExpressionNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TrapExpressionNode;
import io.ballerina.compiler.syntax.tree.TypeCastExpressionNode;
import io.ballerina.compiler.syntax.tree.TypeTestExpressionNode;
import io.ballerina.compiler.syntax.tree.TypeofExpressionNode;
import io.ballerina.compiler.syntax.tree.UnaryExpressionNode;
import io.ballerina.compiler.syntax.tree.XMLFilterExpressionNode;
import io.ballerina.compiler.syntax.tree.XMLStepExpressionNode;
import org.ballerinalang.debugadapter.EvaluationContext;
import org.ballerinalang.debugadapter.evaluation.engine.Evaluator;
import org.ballerinalang.debugadapter.evaluation.engine.action.RemoteMethodCallActionEvaluator;
import org.ballerinalang.debugadapter.evaluation.engine.expression.AnnotationAccessExpressionEvaluator;
import org.ballerinalang.debugadapter.evaluation.engine.expression.AnonFunctionExpressionEvaluator;
import org.ballerinalang.debugadapter.evaluation.engine.expression.BasicLiteralEvaluator;
import org.ballerinalang.debugadapter.evaluation.engine.expression.BinaryExpressionEvaluator;
import org.ballerinalang.debugadapter.evaluation.engine.expression.BuiltinSimpleNameReferenceEvaluator;
import org.ballerinalang.debugadapter.evaluation.engine.expression.ConditionalExpressionEvaluator;
import org.ballerinalang.debugadapter.evaluation.engine.expression.ErrorConstructorExpressionEvaluator;
import org.ballerinalang.debugadapter.evaluation.engine.expression.FieldAccessExpressionEvaluator;
import org.ballerinalang.debugadapter.evaluation.engine.expression.FunctionInvocationExpressionEvaluator;
import org.ballerinalang.debugadapter.evaluation.engine.expression.LetExpressionEvaluator;
import org.ballerinalang.debugadapter.evaluation.engine.expression.MemberAccessExpressionEvaluator;
import org.ballerinalang.debugadapter.evaluation.engine.expression.MethodCallExpressionEvaluator;
import org.ballerinalang.debugadapter.evaluation.engine.expression.NewExpressionEvaluator;
import org.ballerinalang.debugadapter.evaluation.engine.expression.OptionalFieldAccessExpressionEvaluator;
import org.ballerinalang.debugadapter.evaluation.engine.expression.QualifiedNameReferenceEvaluator;
import org.ballerinalang.debugadapter.evaluation.engine.expression.QueryExpressionEvaluator;
import org.ballerinalang.debugadapter.evaluation.engine.expression.RangeExpressionEvaluator;
import org.ballerinalang.debugadapter.evaluation.engine.expression.SimpleNameReferenceEvaluator;
import org.ballerinalang.debugadapter.evaluation.engine.expression.StringTemplateEvaluator;
import org.ballerinalang.debugadapter.evaluation.engine.expression.TrapExpressionEvaluator;
import org.ballerinalang.debugadapter.evaluation.engine.expression.TypeCastExpressionEvaluator;
import org.ballerinalang.debugadapter.evaluation.engine.expression.TypeOfExpressionEvaluator;
import org.ballerinalang.debugadapter.evaluation.engine.expression.TypeTestExpressionEvaluator;
import org.ballerinalang.debugadapter.evaluation.engine.expression.UnaryExpressionEvaluator;
import org.ballerinalang.debugadapter.evaluation.engine.expression.XMLFilterExpressionEvaluator;
import org.ballerinalang.debugadapter.evaluation.engine.expression.XMLStepExpressionEvaluator;
import org.ballerinalang.debugadapter.evaluation.engine.expression.XMLTemplateExpressionEvaluator;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

import static org.ballerinalang.debugadapter.evaluation.EvaluationException.createEvaluationException;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.INVALID_ARGUMENT;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.UNSUPPORTED_EXPRESSION;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.REST_ARG_IDENTIFIER;

/**
 * A {@code NodeVisitor} based implementation used to traverse and capture evaluatable segments of a parsed ballerina
 * expression.
 * <br><br>
 * Supported expression types. (Language specification v2020R1)
 * <ul>
 * <li> Literal expression
 * <li> Multiplicative expression
 * <li> Additive expression
 * <li> Numerical comparison expression
 * <li> Variable reference expression
 * <li> Field access expression
 * <li> Function call expression
 * <li> Method call expression
 * <li> Braced expression
 * <li> Member access expression
 * <li> Optional field access expression
 * <li> Binary bitwise expression
 * <li> Logical expression
 * <li> Conditional expression
 * <li> Typeof expression
 * <li> Equality expression
 * <li> String template expression
 * <li> XML template expression
 * <li> Shift expression
 * <li> Unary expression
 * <li> Type test expression
 * <li> Type cast expression
 * <li> Trap expression
 * <li> Range expression
 * <li> XML attribute access expression
 * <li> New expression
 * <li> Error constructor expression
 * <li> Annotation access expression
 * <li> XML navigation expression
 * <li> Checking expression
 * <li> Query expression
 * <li> Let expression
 * <li> Anonymous function expression
 * </ul>
 * <p>
 * Supported action types.
 * <ul>
 * <li> Remote method call action
 * </ul>
 *
 * @since 2.0.0
 */
public class EvaluatorBuilder extends NodeVisitor {

    private final Set<SyntaxKind> supportedSyntax = new HashSet<>();
    private final Set<SyntaxKind> capturedSyntax = new HashSet<>();
    private final List<Node> unsupportedNodes = new ArrayList<>();
    private final EvaluationContext context;
    private Evaluator result = null;
    private EvaluationException builderException = null;

    public EvaluatorBuilder(EvaluationContext context) {
        this.context = context;
        prepareForEvaluation();
    }

    /**
     * Parses a given ballerina expression and transforms into a tree of executable {@link Evaluator} instances.
     *
     * @param parsedExpr Parsed Ballerina expression node.
     * @throws EvaluationException If validation/parsing is failed.
     */
    public Evaluator build(ExpressionNode parsedExpr) throws EvaluationException {
        clearState();
        parsedExpr.accept(this);
        if (unsupportedSyntaxDetected()) {
            final StringJoiner errors = new StringJoiner(System.lineSeparator());
            unsupportedNodes.forEach(node -> errors.add(String.format("'%s' - %s", node.toString(), node.kind())));
            throw createEvaluationException(UNSUPPORTED_EXPRESSION, errors);
        }
        if (result == null) {
            throw builderException;
        }
        return result;
    }

    @Override
    public void visit(BracedExpressionNode bracedExpressionNode) {
        visitSyntaxNode(bracedExpressionNode);
        bracedExpressionNode.expression().accept(this);
    }

    @Override
    public void visit(BinaryExpressionNode binaryExpressionNode) {
        visitSyntaxNode(binaryExpressionNode);
        binaryExpressionNode.lhsExpr().accept(this);
        Evaluator lhsEvaluator = result;
        binaryExpressionNode.rhsExpr().accept(this);
        Evaluator rhsEvaluator = result;

        switch (binaryExpressionNode.operator().kind()) {
            case ELLIPSIS_TOKEN:
            case DOUBLE_DOT_LT_TOKEN:
                result = new RangeExpressionEvaluator(context, binaryExpressionNode, lhsEvaluator, rhsEvaluator);
                return;
            default:
                result = new BinaryExpressionEvaluator(context, binaryExpressionNode, lhsEvaluator, rhsEvaluator);
        }
    }

    @Override
    public void visit(FunctionCallExpressionNode functionCallExpressionNode) {
        visitSyntaxNode(functionCallExpressionNode);
        try {
            List<Map.Entry<String, Evaluator>> argEvaluators = processArgs(functionCallExpressionNode.arguments());
            result = new FunctionInvocationExpressionEvaluator(context, functionCallExpressionNode, argEvaluators);
        } catch (EvaluationException e) {
            builderException = e;
        }
    }

    @Override
    public void visit(MethodCallExpressionNode methodCallExpressionNode) {
        visitSyntaxNode(methodCallExpressionNode);
        try {
            // visits object expression.
            methodCallExpressionNode.expression().accept(this);
            Evaluator expression = result;
            List<Map.Entry<String, Evaluator>> argEvaluators = processArgs(methodCallExpressionNode.arguments());
            result = new MethodCallExpressionEvaluator(context, methodCallExpressionNode, expression, argEvaluators);
        } catch (EvaluationException e) {
            builderException = e;
        }
    }

    @Override
    public void visit(ErrorConstructorExpressionNode errorConstructorExpressionNode) {
        visitSyntaxNode(errorConstructorExpressionNode);
        try {
            List<Map.Entry<String, Evaluator>> argEvaluators = processArgs(errorConstructorExpressionNode.arguments());
            result = new ErrorConstructorExpressionEvaluator(context, errorConstructorExpressionNode, argEvaluators);
        } catch (EvaluationException e) {
            builderException = e;
        }
    }

    @Override
    public void visit(FieldAccessExpressionNode fieldAccessExpressionNode) {
        visitSyntaxNode(fieldAccessExpressionNode);
        // visits object expression.
        fieldAccessExpressionNode.expression().accept(this);
        Evaluator expression = result;
        result = new FieldAccessExpressionEvaluator(context, expression, fieldAccessExpressionNode);
    }

    @Override
    public void visit(OptionalFieldAccessExpressionNode optionalFieldAccessExpressionNode) {
        visitSyntaxNode(optionalFieldAccessExpressionNode);
        // visits object expression.
        optionalFieldAccessExpressionNode.expression().accept(this);
        Evaluator expression = result;
        result = new OptionalFieldAccessExpressionEvaluator(context, expression, optionalFieldAccessExpressionNode);
    }

    @Override
    public void visit(ConditionalExpressionNode conditionalExpressionNode) {
        visitSyntaxNode(conditionalExpressionNode);
        // Visits all the sub expressions.
        conditionalExpressionNode.lhsExpression().accept(this);
        Evaluator lhsExprEvaluator = result;
        conditionalExpressionNode.middleExpression().accept(this);
        Evaluator middleExprEvaluator = result;
        conditionalExpressionNode.endExpression().accept(this);
        Evaluator endExprEvaluator = result;
        result = new ConditionalExpressionEvaluator(context, conditionalExpressionNode, lhsExprEvaluator,
                middleExprEvaluator, endExprEvaluator);
    }

    @Override
    public void visit(TypeofExpressionNode typeofExpressionNode) {
        visitSyntaxNode(typeofExpressionNode);
        typeofExpressionNode.expression().accept(this);
        Evaluator subExprEvaluator = result;
        result = new TypeOfExpressionEvaluator(context, typeofExpressionNode, subExprEvaluator);
    }

    @Override
    public void visit(IndexedExpressionNode indexedExpressionNode) {
        visitSyntaxNode(indexedExpressionNode);
        indexedExpressionNode.containerExpression().accept(this);
        Evaluator containerEvaluator = result;
        SeparatedNodeList<ExpressionNode> keyNodes = indexedExpressionNode.keyExpression();
        // Removes separator nodes from the key list.
        for (int index = keyNodes.size() - 2; index > 0; index -= 2) {
            keyNodes.remove(index);
        }
        List<Evaluator> keyEvaluators = new ArrayList<>();
        for (int idx = 0; idx < keyNodes.size(); idx++) {
            final ExpressionNode keyExprNode = keyNodes.get(idx);
            keyExprNode.accept(this);
            if (result == null) {
                builderException = createEvaluationException(INVALID_ARGUMENT, keyExprNode.toSourceCode().trim());
                return;
            }
            // Todo - should we disable GC like intellij expression evaluator does?
            keyEvaluators.add(result);
        }
        result = new MemberAccessExpressionEvaluator(context, indexedExpressionNode, containerEvaluator, keyEvaluators);
    }

    @Override
    public void visit(PositionalArgumentNode positionalArgumentNode) {
        visitSyntaxNode(positionalArgumentNode);
        positionalArgumentNode.expression().accept(this);
    }

    @Override
    public void visit(NamedArgumentNode namedArgumentNode) {
        visitSyntaxNode(namedArgumentNode);
        namedArgumentNode.expression().accept(this);
    }

    @Override
    public void visit(RestArgumentNode restArgumentNode) {
        visitSyntaxNode(restArgumentNode);
        restArgumentNode.expression().accept(this);
    }

    @Override
    public void visit(TypeTestExpressionNode typeTestExpressionNode) {
        visitSyntaxNode(typeTestExpressionNode);
        result = new TypeTestExpressionEvaluator(context, typeTestExpressionNode);
    }

    @Override
    public void visit(TypeCastExpressionNode typeCastExpressionNode) {
        visitSyntaxNode(typeCastExpressionNode);
        typeCastExpressionNode.expression().accept(this);

        // Since the query expression evaluator is capable of handling its type casts itself, no need to create a
        // separate type cast evaluator in this context.
        if (typeCastExpressionNode.expression().kind() == SyntaxKind.QUERY_EXPRESSION) {
            return;
        }

        Evaluator subExprEvaluator = result;
        result = new TypeCastExpressionEvaluator(context, typeCastExpressionNode, subExprEvaluator);
    }

    @Override
    public void visit(AnnotAccessExpressionNode annotAccessExpressionNode) {
        visitSyntaxNode(annotAccessExpressionNode);
        annotAccessExpressionNode.expression().accept(this);
        Evaluator exprEvaluator = result;
        result = new AnnotationAccessExpressionEvaluator(context, annotAccessExpressionNode, exprEvaluator);
    }

    @Override
    public void visit(TemplateExpressionNode templateExpressionNode) {
        visitSyntaxNode(templateExpressionNode);
        Optional<Token> typeOp = templateExpressionNode.type();
        if (typeOp.isEmpty()) {
            // Todo - throw an error instead?
            return;
        }
        SyntaxKind type = typeOp.get().kind();
        if (type == SyntaxKind.STRING_KEYWORD) {
            List<Evaluator> templateMemberEvaluators = new ArrayList<>();
            // Cannot use foreach or lambda, until the ClassCastException is fixed from the compiler side.
            for (int i = 0; i < templateExpressionNode.content().size(); i++) {
                Node templateMemberNode = templateExpressionNode.content().get(i);
                templateMemberNode.accept(this);
                templateMemberEvaluators.add(result);
            }
            result = new StringTemplateEvaluator(context, templateExpressionNode, templateMemberEvaluators);
        } else if (type == SyntaxKind.XML_KEYWORD) {
            result = new XMLTemplateExpressionEvaluator(context, templateExpressionNode);
        }
    }

    @Override
    public void visit(InterpolationNode interpolationNode) {
        visitSyntaxNode(interpolationNode);
        interpolationNode.expression().accept(this);
    }

    @Override
    public void visit(XMLStepExpressionNode xmlStepExpressionNode) {
        visitSyntaxNode(xmlStepExpressionNode);
        xmlStepExpressionNode.expression().accept(this);
        Evaluator subExprEvaluator = result;
        result = new XMLStepExpressionEvaluator(context, xmlStepExpressionNode, subExprEvaluator);
    }

    @Override
    public void visit(XMLFilterExpressionNode xmlFilterExpressionNode) {
        visitSyntaxNode(xmlFilterExpressionNode);
        xmlFilterExpressionNode.expression().accept(this);
        Evaluator subExprEvaluator = result;
        result = new XMLFilterExpressionEvaluator(context, xmlFilterExpressionNode, subExprEvaluator);
    }

    @Override
    public void visit(TrapExpressionNode trapExpressionNode) {
        visitSyntaxNode(trapExpressionNode);
        trapExpressionNode.expression().accept(this);
        Evaluator subExprEvaluator = result;
        result = new TrapExpressionEvaluator(context, trapExpressionNode, subExprEvaluator);
    }

    @Override
    public void visit(UnaryExpressionNode unaryExpressionNode) {
        visitSyntaxNode(unaryExpressionNode);
        unaryExpressionNode.expression().accept(this);
        Evaluator subExprEvaluator = result;
        result = new UnaryExpressionEvaluator(context, unaryExpressionNode, subExprEvaluator);
    }

    @Override
    public void visit(ExplicitNewExpressionNode explicitNewExpressionNode) {
        visitSyntaxNode(explicitNewExpressionNode);
        try {
            List<Map.Entry<String, Evaluator>> argEvaluators = processArgs(explicitNewExpressionNode
                    .parenthesizedArgList().arguments());
            result = new NewExpressionEvaluator(context, explicitNewExpressionNode, argEvaluators);
        } catch (EvaluationException e) {
            builderException = e;
        }
    }

    @Override
    public void visit(ImplicitNewExpressionNode implicitNewExpressionNode) {
        visitSyntaxNode(implicitNewExpressionNode);
        result = new NewExpressionEvaluator(context, implicitNewExpressionNode, null);
    }

    @Override
    public void visit(ExplicitAnonymousFunctionExpressionNode explicitAnonFunctionNode) {
        visitSyntaxNode(explicitAnonFunctionNode);
        result = new AnonFunctionExpressionEvaluator(context, explicitAnonFunctionNode);
    }

    @Override
    public void visit(ImplicitAnonymousFunctionExpressionNode implicitAnonFunctionNode) {
        visitSyntaxNode(implicitAnonFunctionNode);
        result = new AnonFunctionExpressionEvaluator(context, implicitAnonFunctionNode);
    }

    @Override
    public void visit(QueryExpressionNode queryExpressionNode) {
        visitSyntaxNode(queryExpressionNode);
        result = new QueryExpressionEvaluator(context, queryExpressionNode);
    }

    @Override
    public void visit(LetExpressionNode letExpressionNode) {
        visitSyntaxNode(letExpressionNode);
        result = new LetExpressionEvaluator(context, letExpressionNode);
    }

    public void visit(RemoteMethodCallActionNode methodCallActionNode) {
        visitSyntaxNode(methodCallActionNode);
        try {
            // visits object expression.
            methodCallActionNode.expression().accept(this);
            Evaluator expression = result;
            List<Map.Entry<String, Evaluator>> argEvaluators = processArgs(methodCallActionNode.arguments());
            result = new RemoteMethodCallActionEvaluator(context, methodCallActionNode, expression, argEvaluators);
        } catch (EvaluationException e) {
            builderException = e;
        }
    }

    @Override
    public void visit(QualifiedNameReferenceNode qualifiedNameReferenceNode) {
        visitSyntaxNode(qualifiedNameReferenceNode);
        result = new QualifiedNameReferenceEvaluator(context, qualifiedNameReferenceNode);
    }

    @Override
    public void visit(SimpleNameReferenceNode simpleNameReferenceNode) {
        visitSyntaxNode(simpleNameReferenceNode);
        result = new SimpleNameReferenceEvaluator(context, simpleNameReferenceNode);
    }

    @Override
    public void visit(BuiltinSimpleNameReferenceNode builtinSimpleNameReferenceNode) {
        visitSyntaxNode(builtinSimpleNameReferenceNode);
        result = new BuiltinSimpleNameReferenceEvaluator(context, builtinSimpleNameReferenceNode);
    }

    @Override
    public void visit(BasicLiteralNode basicLiteralNode) {
        visitSyntaxNode(basicLiteralNode);
        result = new BasicLiteralEvaluator(context, basicLiteralNode);
    }

    @Override
    public void visit(NilLiteralNode nilLiteralNode) {
        visitSyntaxNode(nilLiteralNode);
        result = new BasicLiteralEvaluator(context, nilLiteralNode);
    }

    @Override
    protected void visitSyntaxNode(Node node) {
        capturedSyntax.add(node.kind());
        if (!supportedSyntax.contains(node.kind())) {
            unsupportedNodes.add(node);
        }
    }

    @Override
    public void visit(Token token) {
        if (token.kind() == SyntaxKind.TEMPLATE_STRING) {
            result = new BasicLiteralEvaluator(context, token);
        }
    }

    private boolean unsupportedSyntaxDetected() {
        return !unsupportedNodes.isEmpty();
    }

    private void prepareForEvaluation() {
        // Adds expression syntax.
        addLiteralExpressionSyntax();
        addStringTemplateExpressionSyntax();
        addXmlTemplateExpressionSyntax();
        addNewExpressionSyntax();
        addVariableReferenceExpressionSyntax();
        addFieldAccessExpressionSyntax();
        addOptionalFieldAccessExpressionSyntax();
        addXmlAttributeAccessExpressionSyntax();
        addAnnotationAccessExpressionSyntax();
        addMemberAccessExpressionSyntax();
        addFunctionCallExpressionSyntax();
        addMethodCallExpressionSyntax();
        addErrorConstructorExpressionSyntax();
        addAnonymousFunctionExpressionSyntax();
        addLetExpressionSyntax();
        addTypeCastExpressionSyntax();
        addTypeOfExpressionSyntax();
        addUnaryExpressionSyntax();
        addMultiplicativeExpressionSyntax();
        addAdditiveExpressionSyntax();
        addShiftExpressionSyntax();
        addRangeExpressionSyntax();
        addNumericalComparisonExpressionSyntax();
        addTypeTestExpressionSyntax();
        addEqualityExpressionSyntax();
        addBinaryBitwiseExpressionSyntax();
        addLogicalExpressionSyntax();
        addConditionalExpressionSyntax();
        addCheckingExpressionSyntax();
        addTrapExpressionSyntax();
        addQueryExpressionSyntax();
        addXmlNavigationExpressionSyntax();
        addMiscellaneousSyntax();

        // Adds action syntax.
        addRemoteMethodCallActionSyntax();
    }

    private void addLiteralExpressionSyntax() {
        // nil
        supportedSyntax.add(SyntaxKind.NIL_LITERAL);
        // boolean
        supportedSyntax.add(SyntaxKind.BOOLEAN_LITERAL);
        supportedSyntax.add(SyntaxKind.TRUE_KEYWORD);
        supportedSyntax.add(SyntaxKind.FALSE_KEYWORD);
        // numeric literal
        supportedSyntax.add(SyntaxKind.NUMERIC_LITERAL);
        supportedSyntax.add(SyntaxKind.DECIMAL_INTEGER_LITERAL_TOKEN);
        supportedSyntax.add(SyntaxKind.DECIMAL_FLOATING_POINT_LITERAL_TOKEN);
        // string
        supportedSyntax.add(SyntaxKind.STRING_LITERAL);
        // Todo - Add support for hex literals
        // Todo - Add support for byte array literal
    }

    private void addStringTemplateExpressionSyntax() {
        supportedSyntax.add(SyntaxKind.STRING_TEMPLATE_EXPRESSION);
        supportedSyntax.add(SyntaxKind.INTERPOLATION);
    }

    private void addXmlTemplateExpressionSyntax() {
        supportedSyntax.add(SyntaxKind.XML_TEMPLATE_EXPRESSION);
    }

    private void addNewExpressionSyntax() {
        supportedSyntax.add(SyntaxKind.IMPLICIT_NEW_EXPRESSION);
        supportedSyntax.add(SyntaxKind.EXPLICIT_NEW_EXPRESSION);
    }

    private void addVariableReferenceExpressionSyntax() {
        supportedSyntax.add(SyntaxKind.SIMPLE_NAME_REFERENCE);
        supportedSyntax.add(SyntaxKind.QUALIFIED_NAME_REFERENCE);
        // Todo - Xml qualified name
    }

    private void addFieldAccessExpressionSyntax() {
        supportedSyntax.add(SyntaxKind.FIELD_ACCESS);
    }

    private void addOptionalFieldAccessExpressionSyntax() {
        supportedSyntax.add(SyntaxKind.OPTIONAL_FIELD_ACCESS);
    }

    private void addXmlAttributeAccessExpressionSyntax() {
    }

    private void addAnnotationAccessExpressionSyntax() {
        supportedSyntax.add(SyntaxKind.ANNOT_ACCESS);
    }

    private void addMemberAccessExpressionSyntax() {
        supportedSyntax.add(SyntaxKind.INDEXED_EXPRESSION);
    }

    private void addFunctionCallExpressionSyntax() {
        supportedSyntax.add(SyntaxKind.FUNCTION_CALL);
        supportedSyntax.add(SyntaxKind.POSITIONAL_ARG);
        supportedSyntax.add(SyntaxKind.NAMED_ARG);
        supportedSyntax.add(SyntaxKind.REST_ARG);
        supportedSyntax.add(SyntaxKind.OPEN_PAREN_TOKEN);
        supportedSyntax.add(SyntaxKind.CLOSE_PAREN_TOKEN);
        // Todo: Add named args and rest args
    }

    private void addMethodCallExpressionSyntax() {
        supportedSyntax.add(SyntaxKind.METHOD_CALL);
        supportedSyntax.add(SyntaxKind.POSITIONAL_ARG);
        supportedSyntax.add(SyntaxKind.NAMED_ARG);
        supportedSyntax.add(SyntaxKind.REST_ARG);
        supportedSyntax.add(SyntaxKind.OPEN_PAREN_TOKEN);
        supportedSyntax.add(SyntaxKind.CLOSE_PAREN_TOKEN);
    }

    private void addErrorConstructorExpressionSyntax() {
        supportedSyntax.add(SyntaxKind.ERROR_CONSTRUCTOR);
    }

    private void addAnonymousFunctionExpressionSyntax() {
        supportedSyntax.add(SyntaxKind.IMPLICIT_ANONYMOUS_FUNCTION_EXPRESSION);
        supportedSyntax.add(SyntaxKind.EXPLICIT_ANONYMOUS_FUNCTION_EXPRESSION);
    }

    private void addLetExpressionSyntax() {
        supportedSyntax.add(SyntaxKind.LET_EXPRESSION);
    }

    private void addTypeCastExpressionSyntax() {
        supportedSyntax.add(SyntaxKind.TYPE_CAST_EXPRESSION);
    }

    private void addTypeOfExpressionSyntax() {
        supportedSyntax.add(SyntaxKind.TYPEOF_EXPRESSION);
    }

    private void addUnaryExpressionSyntax() {
        supportedSyntax.add(SyntaxKind.UNARY_EXPRESSION);
    }

    private void addMultiplicativeExpressionSyntax() {
        supportedSyntax.add(SyntaxKind.BINARY_EXPRESSION);
        supportedSyntax.add(SyntaxKind.ASTERISK_TOKEN);
        supportedSyntax.add(SyntaxKind.SLASH_TOKEN);
        supportedSyntax.add(SyntaxKind.PERCENT_TOKEN);
    }

    private void addAdditiveExpressionSyntax() {
        supportedSyntax.add(SyntaxKind.BINARY_EXPRESSION);
        supportedSyntax.add(SyntaxKind.PLUS_TOKEN);
        supportedSyntax.add(SyntaxKind.MINUS_TOKEN);
    }

    private void addShiftExpressionSyntax() {
        // Todo
    }

    private void addRangeExpressionSyntax() {
    }

    private void addNumericalComparisonExpressionSyntax() {
        supportedSyntax.add(SyntaxKind.BINARY_EXPRESSION);
        supportedSyntax.add(SyntaxKind.LT_TOKEN);
        supportedSyntax.add(SyntaxKind.LT_EQUAL_TOKEN);
        supportedSyntax.add(SyntaxKind.GT_TOKEN);
        supportedSyntax.add(SyntaxKind.GT_EQUAL_TOKEN);
    }

    private void addTypeTestExpressionSyntax() {
        supportedSyntax.add(SyntaxKind.TYPE_TEST_EXPRESSION);
    }

    private void addEqualityExpressionSyntax() {
        // value equality
        supportedSyntax.add(SyntaxKind.DOUBLE_EQUAL_TOKEN);
        supportedSyntax.add(SyntaxKind.NOT_EQUAL_TOKEN);
        // reference equality
        supportedSyntax.add(SyntaxKind.TRIPPLE_EQUAL_TOKEN);
        supportedSyntax.add(SyntaxKind.NOT_DOUBLE_EQUAL_TOKEN);
    }

    private void addBinaryBitwiseExpressionSyntax() {
        // Todo
    }

    private void addLogicalExpressionSyntax() {
        // Todo
    }

    private void addConditionalExpressionSyntax() {
        supportedSyntax.add(SyntaxKind.CONDITIONAL_EXPRESSION);
    }

    private void addCheckingExpressionSyntax() {
        // Todo
    }

    private void addTrapExpressionSyntax() {
        supportedSyntax.add(SyntaxKind.TRAP_EXPRESSION);
    }

    private void addQueryExpressionSyntax() {
        supportedSyntax.add(SyntaxKind.QUERY_EXPRESSION);
    }

    private void addXmlNavigationExpressionSyntax() {
        supportedSyntax.add(SyntaxKind.XML_FILTER_EXPRESSION);
        supportedSyntax.add(SyntaxKind.XML_STEP_EXPRESSION);
    }

    private void addRemoteMethodCallActionSyntax() {
        supportedSyntax.add(SyntaxKind.REMOTE_METHOD_CALL_ACTION);
    }

    private void addMiscellaneousSyntax() {
        // typedesc expressions
        supportedSyntax.add(SyntaxKind.RECORD_TYPE_DESC);
        supportedSyntax.add(SyntaxKind.OBJECT_TYPE_DESC);
        supportedSyntax.add(SyntaxKind.NIL_TYPE_DESC);
        supportedSyntax.add(SyntaxKind.OPTIONAL_TYPE_DESC);
        supportedSyntax.add(SyntaxKind.ARRAY_TYPE_DESC);
        supportedSyntax.add(SyntaxKind.INT_TYPE_DESC);
        supportedSyntax.add(SyntaxKind.BYTE_TYPE_DESC);
        supportedSyntax.add(SyntaxKind.FLOAT_TYPE_DESC);
        supportedSyntax.add(SyntaxKind.DECIMAL_TYPE_DESC);
        supportedSyntax.add(SyntaxKind.STRING_TYPE_DESC);
        supportedSyntax.add(SyntaxKind.BOOLEAN_TYPE_DESC);
        supportedSyntax.add(SyntaxKind.XML_TYPE_DESC);
        supportedSyntax.add(SyntaxKind.JSON_TYPE_DESC);
        supportedSyntax.add(SyntaxKind.HANDLE_TYPE_DESC);
        supportedSyntax.add(SyntaxKind.ANY_TYPE_DESC);
        supportedSyntax.add(SyntaxKind.ANYDATA_TYPE_DESC);
        supportedSyntax.add(SyntaxKind.NEVER_TYPE_DESC);
        supportedSyntax.add(SyntaxKind.VAR_TYPE_DESC);
        supportedSyntax.add(SyntaxKind.SERVICE_TYPE_DESC);
        supportedSyntax.add(SyntaxKind.MAP_TYPE_DESC);
        supportedSyntax.add(SyntaxKind.UNION_TYPE_DESC);
        supportedSyntax.add(SyntaxKind.ERROR_TYPE_DESC);
        supportedSyntax.add(SyntaxKind.STREAM_TYPE_DESC);
        supportedSyntax.add(SyntaxKind.TABLE_TYPE_DESC);
        supportedSyntax.add(SyntaxKind.FUNCTION_TYPE_DESC);
        supportedSyntax.add(SyntaxKind.TUPLE_TYPE_DESC);
        supportedSyntax.add(SyntaxKind.PARENTHESISED_TYPE_DESC);
        supportedSyntax.add(SyntaxKind.READONLY_TYPE_DESC);
        supportedSyntax.add(SyntaxKind.DISTINCT_TYPE_DESC);
        supportedSyntax.add(SyntaxKind.INTERSECTION_TYPE_DESC);
        supportedSyntax.add(SyntaxKind.SINGLETON_TYPE_DESC);
        supportedSyntax.add(SyntaxKind.TYPE_REFERENCE_TYPE_DESC);
        supportedSyntax.add(SyntaxKind.TYPEDESC_TYPE_DESC);
        supportedSyntax.add(SyntaxKind.FUTURE_TYPE_DESC);

        // braced expression
        supportedSyntax.add(SyntaxKind.BRACED_EXPRESSION);
        supportedSyntax.add(SyntaxKind.OPEN_PAREN_TOKEN);
        supportedSyntax.add(SyntaxKind.CLOSE_PAREN_TOKEN);
        // misc
        supportedSyntax.add(SyntaxKind.IDENTIFIER_TOKEN);
        supportedSyntax.add(SyntaxKind.NONE);
        supportedSyntax.add(SyntaxKind.EOF_TOKEN);
    }

    private void clearState() {
        capturedSyntax.clear();
        unsupportedNodes.clear();
        result = null;
        builderException = null;
    }

    private List<Map.Entry<String, Evaluator>> processArgs(SeparatedNodeList<FunctionArgumentNode> args)
            throws EvaluationException {

        List<Map.Entry<String, Evaluator>> argEvaluators = new ArrayList<>();
        for (FunctionArgumentNode argExprNode : args) {
            argExprNode.accept(this);
            if (result == null) {
                throw createEvaluationException(INVALID_ARGUMENT, argExprNode);
            }

            switch (argExprNode.kind()) {
                case POSITIONAL_ARG:
                    argEvaluators.add(new AbstractMap.SimpleEntry<>("", result));
                    break;
                case NAMED_ARG:
                    String namedArg = ((NamedArgumentNode) argExprNode).argumentName().name().toSourceCode();
                    argEvaluators.add(new AbstractMap.SimpleEntry<>(namedArg.trim(), result));
                    break;
                case REST_ARG:
                    argEvaluators.add(new AbstractMap.SimpleEntry<>(REST_ARG_IDENTIFIER, result));
                    break;
                default:
                    builderException = createEvaluationException(INVALID_ARGUMENT, argExprNode);
                    break;
            }
        }
        return argEvaluators;
    }
}
