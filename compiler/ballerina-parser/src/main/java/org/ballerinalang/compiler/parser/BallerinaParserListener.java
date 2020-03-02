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
package org.ballerinalang.compiler.parser;

import org.ballerinalang.compiler.parser.tree.ASTNode;
import org.ballerinalang.compiler.parser.tree.AssignmentStmtNode;
import org.ballerinalang.compiler.parser.tree.BinaryExpressionNode;
import org.ballerinalang.compiler.parser.tree.BlockNode;
import org.ballerinalang.compiler.parser.tree.BracedExpressionNode;
import org.ballerinalang.compiler.parser.tree.DefaultableParameterNode;
import org.ballerinalang.compiler.parser.tree.EmptyNode;
import org.ballerinalang.compiler.parser.tree.ExternFuncBodyNode;
import org.ballerinalang.compiler.parser.tree.FunctionNode;
import org.ballerinalang.compiler.parser.tree.IdentifierNode;
import org.ballerinalang.compiler.parser.tree.InvalidNode;
import org.ballerinalang.compiler.parser.tree.LiteralNode;
import org.ballerinalang.compiler.parser.tree.MissingNode;
import org.ballerinalang.compiler.parser.tree.ModifierNode;
import org.ballerinalang.compiler.parser.tree.OperatorNode;
import org.ballerinalang.compiler.parser.tree.ParameterNode;
import org.ballerinalang.compiler.parser.tree.ParametersNode;
import org.ballerinalang.compiler.parser.tree.RestParameterNode;
import org.ballerinalang.compiler.parser.tree.ReturnTypeDescNode;
import org.ballerinalang.compiler.parser.tree.SyntaxNode;
import org.ballerinalang.compiler.parser.tree.TypeNode;
import org.ballerinalang.compiler.parser.tree.VarDefStmtNode;

import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Parser tree builder.
 * 
 * @since 1.2.0
 */
public class BallerinaParserListener {

    private final ArrayDeque<ASTNode> nodesStack = new ArrayDeque<>();
    private List<ASTNode> statements = new ArrayList<>();
    private Stack<List<ASTNode>> parameters = new Stack<>();

    public void exitCompUnit() {
        PrintStream out = System.out;
        out.println("--------------------------------------");
        while (!this.nodesStack.isEmpty()) {
            out.println(this.nodesStack.removeLast());
            out.println();
        }
        out.println("--------------------------------------");
    }

    public void exitModifier(Token modifier) {
        this.nodesStack.push(new ModifierNode(modifier));
    }

    public void addEmptyModifier() {
        this.nodesStack.push(new EmptyNode());
    }

    public void exitFunctionDefinition() {
        FunctionNode func = new FunctionNode();
        func.body = this.nodesStack.pop();
        func.returnType = this.nodesStack.pop();
        func.rightParenthesis = this.nodesStack.pop();
        func.parameters = this.nodesStack.pop();
        func.leftParenthesis = this.nodesStack.pop();
        func.name = this.nodesStack.pop();
        func.functionKeyword = this.nodesStack.pop();
        func.modifier = this.nodesStack.pop();
        this.nodesStack.push(func);
    }

    public void exitFunctionSignature() {
        // do nothing
    }

    public void startParamList() {
        this.parameters.add(new ArrayList<>());
    }

    public void exitParamList() {
        ParametersNode params = new ParametersNode();
        params.parameters = this.parameters.pop();
        this.nodesStack.push(params);
    }

    public void exitReturnTypeDescriptor() {
        ReturnTypeDescNode returnTypeDesc = new ReturnTypeDescNode();
        returnTypeDesc.type = this.nodesStack.pop();
        returnTypeDesc.annot = this.nodesStack.pop();
        returnTypeDesc.returnsKeyword = this.nodesStack.pop();
        this.nodesStack.push(returnTypeDesc);
    }

    public void exitTypeDescriptor(Token type) {
        this.nodesStack.push(new TypeNode(type));
    }

    public void exitAnnotations() {
        // TODO:
        this.addEmptyNode();
    }

    public void exitFunctionBody() {
        // do nothing
        // exitFunctionBodyBlock() or exitExternalFunctionBody() method will add the relevant node
        // to the stack
    }

    public void exitFunctionBodyBlock() {
        BlockNode block = new BlockNode();
        block.rightBrace = this.nodesStack.pop();
        block.stmts = this.statements;
        block.leftBrace = this.nodesStack.pop();

        this.nodesStack.push(block);

        // reset the statements
        this.statements = new ArrayList<>();
    }

    public void exitExternalFunctionBody() {
        ExternFuncBodyNode externFunc = new ExternFuncBodyNode();
        externFunc.semicolon = this.nodesStack.pop();
        externFunc.externalKeyword = this.nodesStack.pop();
        externFunc.annotation = this.nodesStack.pop();
        externFunc.assign = this.nodesStack.pop();
        this.nodesStack.push(externFunc);
    }

    public void exitIdentifier(Token name) {
        this.nodesStack.push(new IdentifierNode(name));
    }

    public void exitErrorNode() {
        this.nodesStack.push(new InvalidNode());
    }

    public void exitErrorNode(String content) {
        this.nodesStack.push(new InvalidNode(content));
    }

    public void exitRequiredParameter() {
        ParameterNode param = new ParameterNode();
        param.varName = this.nodesStack.pop();
        param.type = this.nodesStack.pop();
        param.comma = this.nodesStack.pop();
        this.parameters.peek().add(param);
    }

    public void exitDefaultableParameter() {
        DefaultableParameterNode param = new DefaultableParameterNode();
        param.expr = this.nodesStack.pop();
        param.assign = this.nodesStack.pop();
        param.varName = this.nodesStack.pop();
        param.type = this.nodesStack.pop();
        param.comma = this.nodesStack.pop();
        this.parameters.peek().add(param);
    }

    public void exitRestParameter() {
        RestParameterNode param = new RestParameterNode();
        param.varName = this.nodesStack.pop();
        param.ellipsis = this.nodesStack.pop();
        param.type = this.nodesStack.pop();
        param.comma = this.nodesStack.pop();
        this.parameters.peek().add(param);
    }

    public void addEmptyNode() {
        this.nodesStack.push(new EmptyNode());
    }

    public void addMissingNode() {
        this.nodesStack.push(new MissingNode());
    }

    public void addMissingNode(String text) {
        this.nodesStack.push(new MissingNode(text));
    }

    public ASTNode getLastNode() {
        return this.nodesStack.peek();
    }

    public void exitSyntaxNode(Token content) {
        this.nodesStack.push(new SyntaxNode(content));
    }

    public void exitVarDefStmt(boolean hasExpr) {
        VarDefStmtNode varDef = new VarDefStmtNode();
        varDef.semicolon = this.nodesStack.pop();

        if (hasExpr) {
            varDef.expr = this.nodesStack.pop();
            varDef.assign = this.nodesStack.pop();
        } else {
            varDef.expr = new EmptyNode();
            varDef.assign = new EmptyNode();
        }

        varDef.varName = this.nodesStack.pop();
        varDef.type = this.nodesStack.pop();
        this.statements.add(varDef);
    }

    public void exitLiteral(Token token) {
        this.nodesStack.push(new LiteralNode(token));
    }

    public void exitAssignmentStmt() {
        AssignmentStmtNode assignmentStmt = new AssignmentStmtNode();
        assignmentStmt.semicolon = this.nodesStack.pop();
        assignmentStmt.expr = this.nodesStack.pop();
        assignmentStmt.assign = this.nodesStack.pop();
        assignmentStmt.varRef = this.nodesStack.pop();
        this.statements.add(assignmentStmt);
    }

    public void exitOperator(Token content) {
        this.nodesStack.push(new OperatorNode(content));
    }

    public void endBinaryExpression() {
        BinaryExpressionNode binaryExpr = new BinaryExpressionNode();
        binaryExpr.rhsExpr = this.nodesStack.pop();
        binaryExpr.operator = this.nodesStack.pop();
        binaryExpr.lhsExpr = this.nodesStack.pop();
        this.nodesStack.push(binaryExpr);
    }

    public void endBracedExpression() {
        BracedExpressionNode bracedExpr = new BracedExpressionNode();
        bracedExpr.closeParenthesis = this.nodesStack.pop();
        bracedExpr.expression = this.nodesStack.pop();
        bracedExpr.openParenthesis = this.nodesStack.pop();
        this.nodesStack.push(bracedExpr);
    }
}
