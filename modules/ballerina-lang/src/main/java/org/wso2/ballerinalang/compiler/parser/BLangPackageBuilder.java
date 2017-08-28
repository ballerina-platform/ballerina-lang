/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.ballerinalang.compiler.parser;

import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.TreeUtils;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.InvokableNode;
import org.ballerinalang.model.tree.PackageNode;
import org.ballerinalang.model.tree.VariableNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.LiteralNode;
import org.ballerinalang.model.tree.statements.BlockNode;
import org.ballerinalang.model.tree.statements.VariableDefinitionNode;
import org.ballerinalang.model.tree.types.TypeNode;
import org.ballerinalang.model.tree.types.ValueTypeNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * This class builds the package AST of a Ballerina source file.
 */
public class BLangPackageBuilder {

    private PackageNode pkgNode;
    
    private Stack<TypeNode> typeNodeStack = new Stack<>();
    
    private Stack<BlockNode> blockNodeStack = new Stack<>();
    
    private Stack<List<VariableNode>> paramListStack = new Stack<>();
    
    private Stack<List<VariableNode>> retParamListStack = new Stack<>();
    
    private Stack<InvokableNode> invokableNodeStack = new Stack<>();
    
    private Stack<ExpressionNode> exprNodeStack = new Stack<>();
            
    public BLangPackageBuilder(PackageNode pkgNode) {
        this.pkgNode = pkgNode;
    }
    
    public void addValueType(String valueType) {
        ValueTypeNode valueTypeNode = TreeBuilder.createValueTypeNode();
        valueTypeNode.setTypeKind(TreeUtils.stringToTypeKind(valueType));
        this.typeNodeStack.push(valueTypeNode);
    }
    
    public void startParamList() {
        this.paramListStack.add(new ArrayList<>());
    }
    
    public void startFunctionDef() {
        this.invokableNodeStack.push(TreeBuilder.createFunctionNode());
    }
    
    public void startBlock() {
        this.blockNodeStack.push(TreeBuilder.createBlockNode());
    }
    
    public void endReturnParams() {
        this.retParamListStack.push(this.paramListStack.pop());
    }
    
    private IdentifierNode createIdentifier(String identifier) {
        IdentifierNode node = TreeBuilder.createIdentifierNode();
        node.setValue(identifier);
        return node;
    }
    
    public void addParam(String identifier) {
        VariableNode var = TreeBuilder.createVariableNode();
        var.setName(this.createIdentifier(identifier));
        var.setType(this.typeNodeStack.pop());
        this.paramListStack.peek().add(var);
    }
    
    private List<VariableNode> getLastParamsList() {
        if (this.paramListStack.empty()) {
            return new ArrayList<>(0);
        } else {
            return this.paramListStack.pop();
        }
    }
    
    private List<VariableNode> getLastRetParamsList() {
        if (this.retParamListStack.empty()) {
            return new ArrayList<>(0);
        } else {
            return this.retParamListStack.pop();
        }
    }
    
    public void endCallableUnitSignature() {
        InvokableNode invNode = this.invokableNodeStack.peek();
        this.getLastParamsList().stream().forEach(e -> invNode.addParameter(e));
        this.getLastRetParamsList().stream().forEach(e -> invNode.addReturnParameter(e));
    }
    
    public void addVariableDefStatement(String identifier) {
        VariableDefinitionNode varDefNode = TreeBuilder.createVariableDefinitionNode();
        VariableNode var = TreeBuilder.createVariableNode();
        var.setName(this.createIdentifier(identifier));
        var.setType(this.typeNodeStack.pop());
        var.setInitialExpression(this.exprNodeStack.pop());
        varDefNode.setVariable(var);
        this.blockNodeStack.peek().addStatement(varDefNode);
    }
    
    public void addLiteralValue(Object value) {
        LiteralNode litExpr = TreeBuilder.createLiteralExpression();
        litExpr.setValue(value);
        this.exprNodeStack.push(litExpr);
    }
    
    public void endFunctionDef() {
        this.invokableNodeStack.peek().setBody(this.blockNodeStack.pop());
        this.pkgNode.addFunction((FunctionNode) this.invokableNodeStack.pop());
    }
    
}
