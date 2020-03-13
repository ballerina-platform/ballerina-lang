/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerinalang.compiler.internal.parser;

import io.ballerinalang.compiler.internal.parser.tree.STAssignmentStatement;
import io.ballerinalang.compiler.internal.parser.tree.STBinaryExpression;
import io.ballerinalang.compiler.internal.parser.tree.STBlockStatement;
import io.ballerinalang.compiler.internal.parser.tree.STBracedExpression;
import io.ballerinalang.compiler.internal.parser.tree.STDefaultableParameter;
import io.ballerinalang.compiler.internal.parser.tree.STEmptyNode;
import io.ballerinalang.compiler.internal.parser.tree.STExternalFuncBody;
import io.ballerinalang.compiler.internal.parser.tree.STFunctionDefinition;
import io.ballerinalang.compiler.internal.parser.tree.STMissingToken;
import io.ballerinalang.compiler.internal.parser.tree.STModulePart;
import io.ballerinalang.compiler.internal.parser.tree.STModuleTypeDefinition;
import io.ballerinalang.compiler.internal.parser.tree.STNode;
import io.ballerinalang.compiler.internal.parser.tree.STNodeList;
import io.ballerinalang.compiler.internal.parser.tree.STRecordField;
import io.ballerinalang.compiler.internal.parser.tree.STRecordFieldWithDefaultValue;
import io.ballerinalang.compiler.internal.parser.tree.STRecordRestDescriptor;
import io.ballerinalang.compiler.internal.parser.tree.STRecordTypeDescriptor;
import io.ballerinalang.compiler.internal.parser.tree.STRecordTypeReference;
import io.ballerinalang.compiler.internal.parser.tree.STRequiredParameter;
import io.ballerinalang.compiler.internal.parser.tree.STRestParameter;
import io.ballerinalang.compiler.internal.parser.tree.STReturnTypeDescriptor;
import io.ballerinalang.compiler.internal.parser.tree.STToken;
import io.ballerinalang.compiler.internal.parser.tree.STVariableDeclaration;
import io.ballerinalang.compiler.internal.parser.tree.SyntaxKind;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

/**
 * Parser tree builder.
 * 
 * @since 1.2.0
 */
public class BallerinaParserListener {

    private final ArrayDeque<STNode> nodesStack = new ArrayDeque<>();
    private List<STNode> statements = new ArrayList<>();
    private Stack<List<STNode>> parameters = new Stack<>();
    private Stack<List<STNode>> recordFields = new Stack<>();

    public void exitCompUnit(STToken eofToken) {
        List<STNode> importDecls = new ArrayList<>();
        List<STNode> otherDecls = new ArrayList<>();
        for (Iterator<STNode> nodeItr = nodesStack.descendingIterator(); nodeItr.hasNext();) {
            STNode member = nodeItr.next();
            if (member.kind == SyntaxKind.IMPORT_DECLARATION) {
                importDecls.add(member);
            } else {
                otherDecls.add(member);
            }
        }

        STModulePart modulePart = new STModulePart(new STNodeList(importDecls), new STNodeList(otherDecls), eofToken);
        nodesStack.push(modulePart);
    }

    public void exitModifier(STToken modifier) {
        this.nodesStack.push(modifier);
    }

    public void addEmptyModifier() {
        this.nodesStack.push(new STEmptyNode());
    }

    public void exitFunctionDefinition() {
        STNode body = this.nodesStack.pop();
        STNode returnTypeDesc = this.nodesStack.pop();
        STNode closeParenthesis = this.nodesStack.pop();
        STNode parameters = this.nodesStack.pop();
        STNode openParenthesis = this.nodesStack.pop();
        STNode name = this.nodesStack.pop();
        STNode functionKeyword = this.nodesStack.pop();
        STNode modifier = this.nodesStack.pop();
        STFunctionDefinition func = new STFunctionDefinition(modifier, functionKeyword, name, openParenthesis,
                parameters, closeParenthesis, returnTypeDesc, body);
        this.nodesStack.push(func);
    }

    public void exitFunctionSignature() {
        // do nothing
    }

    public void startParamList() {
        this.parameters.add(new ArrayList<>());
    }

    public void exitParamList() {
        STNode params = new STNodeList(this.parameters.pop());
        this.nodesStack.push(params);
    }

    public void exitReturnTypeDescriptor() {
        STNode type = this.nodesStack.pop();
        STNode annot = this.nodesStack.pop();
        STNode returnsKeyword = this.nodesStack.pop();
        STReturnTypeDescriptor returnTypeDesc = new STReturnTypeDescriptor(returnsKeyword, annot, type);
        this.nodesStack.push(returnTypeDesc);
    }

    public void exitTypeDescriptor(STNode type) {
        this.nodesStack.push(type);
    }

    public void exitAnnotations() {
        // TODO:
        this.addEmptyNode();
    }

    public void exitFunctionBody() {
        // Do nothing.
        // 'exitFunctionBodyBlock()' or 'exitExternalFunctionBody()' method will
        // add the relevant node to the stack
    }

    public void exitFunctionBodyBlock() {
        STNode closeBrace = this.nodesStack.pop();
        STNode stmts = new STNodeList(this.statements);
        STNode openBrace = this.nodesStack.pop();
        STBlockStatement block = new STBlockStatement(SyntaxKind.BLOCK_STATEMENT, openBrace, stmts, closeBrace);
        this.nodesStack.push(block);

        // reset the statements
        this.statements = new ArrayList<>();
    }

    public void exitExternalFunctionBody() {
        STNode semicolon = this.nodesStack.pop();
        STNode externalKeyword = this.nodesStack.pop();
        STNode annotation = this.nodesStack.pop();
        STNode assign = this.nodesStack.pop();
        STExternalFuncBody externFunc = new STExternalFuncBody(SyntaxKind.EXTERNAL_FUNCTION_BODY, assign, annotation,
                externalKeyword, semicolon);
        this.nodesStack.push(externFunc);
    }

    public void exitIdentifier(STToken identifier) {
        this.nodesStack.push(identifier);
    }

    public void exitRequiredParameter() {
        STNode paramName = this.nodesStack.pop();
        STNode type = this.nodesStack.pop();
        STNode leadingComma = this.nodesStack.pop();

        // TODO: add access modifier
        STNode accessModifier = new STEmptyNode();

        STRequiredParameter param =
                new STRequiredParameter(SyntaxKind.PARAMETER, leadingComma, accessModifier, type, paramName);
        this.parameters.peek().add(param);
    }

    public void exitDefaultableParameter() {
        STNode expr = this.nodesStack.pop();
        STNode equal = this.nodesStack.pop();
        STNode paramName = this.nodesStack.pop();
        STNode type = this.nodesStack.pop();
        STNode leadingComma = this.nodesStack.pop();

        // TODO: add access modifier
        STNode accessModifier = new STEmptyNode();

        STDefaultableParameter param = new STDefaultableParameter(SyntaxKind.PARAMETER, leadingComma, accessModifier,
                type, paramName, equal, expr);
        this.parameters.peek().add(param);
    }

    public void exitRestParameter() {
        STNode paramName = this.nodesStack.pop();
        STNode ellipsis = this.nodesStack.pop();
        STNode type = this.nodesStack.pop();
        STNode leadingComma = this.nodesStack.pop();
        STRestParameter param = new STRestParameter(SyntaxKind.PARAMETER, leadingComma, type, ellipsis, paramName);
        this.parameters.peek().add(param);
    }

    public void addEmptyNode() {
        this.nodesStack.push(new STEmptyNode());
    }

    public void addMissingNode(SyntaxKind kind) {
        this.nodesStack.push(new STMissingToken(kind));
    }

    public void exitSyntaxNode(STNode token) {
        this.nodesStack.push(token);
    }

    public void exitVarDefStmt(boolean hasExpr) {
        STNode semicolon = this.nodesStack.pop();

        STNode expr;
        STNode assign;
        if (hasExpr) {
            expr = this.nodesStack.pop();
            assign = this.nodesStack.pop();
        } else {
            expr = new STEmptyNode();
            assign = new STEmptyNode();
        }

        STNode varName = this.nodesStack.pop();
        STNode type = this.nodesStack.pop();
        STVariableDeclaration varDef =
                new STVariableDeclaration(SyntaxKind.LOCAL_VARIABLE_DECL, type, varName, assign, expr, semicolon);
        this.statements.add(varDef);
    }

    public void exitLiteral(STToken literal) {
        this.nodesStack.push(literal);
    }

    public void exitAssignmentStmt() {
        STNode semicolon = this.nodesStack.pop();
        STNode expr = this.nodesStack.pop();
        STNode assign = this.nodesStack.pop();
        STNode varRef = this.nodesStack.pop();
        STAssignmentStatement assignmentStmt =
                new STAssignmentStatement(SyntaxKind.ASSIGNMENT_STATEMENT, varRef, assign, expr, semicolon);
        this.statements.add(assignmentStmt);
    }

    public void exitOperator(STToken op) {
        this.nodesStack.push(op);
    }

    public void endBinaryExpression() {
        STNode rhsExpr = this.nodesStack.pop();
        STNode operator = this.nodesStack.pop();
        STNode lhsExpr = this.nodesStack.pop();

        STBinaryExpression binaryExpr =
                new STBinaryExpression(SyntaxKind.BINARY_EXPRESSION, lhsExpr, operator, rhsExpr);
        this.nodesStack.push(binaryExpr);
    }

    public void endBracedExpression() {
        STNode closeParen = this.nodesStack.pop();
        STNode expression = this.nodesStack.pop();
        STNode openParen = this.nodesStack.pop();
        STBracedExpression bracedExpr =
                new STBracedExpression(SyntaxKind.BRACED_EXPRESSION, openParen, expression, closeParen);
        this.nodesStack.push(bracedExpr);
    }

    public STNode getLastNode() {
        if (!statements.isEmpty()) {
            return statements.get(statements.size() - 1);
        }

        return nodesStack.peek();
    }

    public void exitModuleTypeDefinition() {
        STNode comma = this.nodesStack.pop();
        STNode typeDescriptor = this.nodesStack.pop();
        STNode typeName = this.nodesStack.pop();
        STNode typeKeyword = this.nodesStack.pop();
        STNode modifier = this.nodesStack.pop();
        STModuleTypeDefinition typeDef =
                new STModuleTypeDefinition(modifier, typeKeyword, typeName, typeDescriptor, comma);
        this.nodesStack.push(typeDef);

        System.out.println("--------------------------------------------------");
        System.out.println(typeDef);
    }

    public void exitRecordTypeDescriptor() {
        STNode bodyEndDelimiter = this.nodesStack.pop();
        STNode fields = this.nodesStack.pop();
//        STNode restDescriptor = this.nodesStack.pop();
        STNode bodyStartDelimiter = this.nodesStack.pop();
        STNode recordKeyword = this.nodesStack.pop();
        STRecordTypeDescriptor recordTypeDesc =
                new STRecordTypeDescriptor(recordKeyword, bodyStartDelimiter, fields, bodyEndDelimiter);
        this.nodesStack.push(recordTypeDesc);
    }

    public void startFieldDescriptors() {
        this.recordFields.add(new ArrayList<>());
    }

    public void exitFieldDescriptors() {
        STNode params = new STNodeList(this.recordFields.pop());
        this.nodesStack.push(params);
    }

    public void exitRecordField() {
        STNode semicolonToken = this.nodesStack.pop();
        STNode questionMarkToken = this.nodesStack.pop();
        STNode fieldName = this.nodesStack.pop();
        STNode type = this.nodesStack.pop();
        STRecordField recordField = new STRecordField(type, fieldName, questionMarkToken, semicolonToken);
        this.recordFields.peek().add(recordField);
    }

    public void exitRecordFieldWithDefaultValue() {
        STNode semicolonToken = this.nodesStack.pop();
        STNode equalsToken = this.nodesStack.pop();
        STNode expression = this.nodesStack.pop();
        STNode fieldName = this.nodesStack.pop();
        STNode type = this.nodesStack.pop();
        STRecordFieldWithDefaultValue recordField =
                new STRecordFieldWithDefaultValue(type, fieldName, equalsToken, expression, semicolonToken);
        this.recordFields.peek().add(recordField);
    }

    public void exitRecordRestDescriptor() {
        STNode semicolonToken = this.nodesStack.pop();
        STNode ellipsis = this.nodesStack.pop();
        STNode type = this.nodesStack.pop();
        STRecordRestDescriptor restDesc = new STRecordRestDescriptor(type, ellipsis, semicolonToken);
        this.nodesStack.push(restDesc);
    }

    public void exitRecordTypeReference() {
        STNode semicolonToken = this.nodesStack.pop();
        STNode type = this.nodesStack.pop();
        STNode asterisk = this.nodesStack.pop();
        STRecordTypeReference typeRef = new STRecordTypeReference(asterisk, type, semicolonToken);
        this.recordFields.peek().add(typeRef);
    }
}
