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
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.ActionNode;
import org.ballerinalang.model.tree.CompilationUnitNode;
import org.ballerinalang.model.tree.ConnectorNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.ImportPackageNode;
import org.ballerinalang.model.tree.InvocableNode;
import org.ballerinalang.model.tree.PackageDeclarationNode;
import org.ballerinalang.model.tree.StructNode;
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
 *
 * @since 0.94
 */
public class BLangPackageBuilder {

    private CompilationUnitNode compUnit;

    private Stack<TypeNode> typeNodeStack = new Stack<>();

    private Stack<List<TypeNode>> typeNodeListStack = new Stack<>();

    private Stack<BlockNode> blockNodeStack = new Stack<>();
    
    private Stack<VariableNode> varStack = new Stack<>();

    private Stack<List<VariableNode>> varListStack = new Stack<>();

    private Stack<InvocableNode> invokableNodeStack = new Stack<>();

    private Stack<ExpressionNode> exprNodeStack = new Stack<>();
    
    private Stack<PackageID> pkgIdStack = new Stack<>();
    
    private Stack<StructNode> structStack = new Stack<>();
        
    private Stack<ConnectorNode> connectorNodeStack = new Stack<>();
    
    private Stack<List<ActionNode>> actionNodeStack = new Stack<>();

    public BLangPackageBuilder(CompilationUnitNode compUnit) {
        this.compUnit = compUnit;
    }

    public void addValueType(String valueType) {
        ValueTypeNode valueTypeNode = TreeBuilder.createValueTypeNode();
        valueTypeNode.setTypeKind(TreeUtils.stringToTypeKind(valueType));
        if (!this.typeNodeListStack.empty()) {
            this.typeNodeListStack.peek().add(valueTypeNode);
        } else {
            this.typeNodeStack.push(valueTypeNode);
        }
    }

    public void startVarList() {
        this.varListStack.push(new ArrayList<>());
    }

    public void startFunctionDef() {
        this.invokableNodeStack.push(TreeBuilder.createFunctionNode());
    }

    public void startBlock() {
        this.blockNodeStack.push(TreeBuilder.createBlockNode());
    }

    private IdentifierNode createIdentifier(String identifier) {
        IdentifierNode node = TreeBuilder.createIdentifierNode();
        node.setValue(identifier);
        return node;
    }

    public void addVar(String identifier, boolean exprAvailable) {
        VariableNode var = this.generateBasicVarNode(identifier, exprAvailable);
        if (this.varListStack.empty()) {
            this.varStack.push(var);
        } else {
            this.varListStack.peek().add(var);
        }
    }

    public void endCallableUnitSignature(String identifier, boolean paramsAvail,
                                         boolean retParamsAvail, boolean retParamTypeOnly) {
        InvocableNode invNode = this.invokableNodeStack.peek();
        invNode.setName(this.createIdentifier(identifier));
        if (retParamsAvail) {
            if (retParamTypeOnly) {
                this.typeNodeListStack.pop().stream().forEach(e -> {
                    VariableNode var = TreeBuilder.createVariableNode();
                    var.setTypeNode(e);
                    invNode.addReturnParameter(var);
                });
            } else {
                this.varListStack.pop().stream().forEach(e -> invNode.addReturnParameter(e));
            }
        }
        if (paramsAvail) {
            this.varListStack.pop().stream().forEach(e -> invNode.addParameter(e));
        }
    }

    public void addVariableDefStatement(String identifier) {
        VariableDefinitionNode varDefNode = TreeBuilder.createVariableDefinitionNode();
        VariableNode var = TreeBuilder.createVariableNode();
        var.setName(this.createIdentifier(identifier));
        var.setTypeNode(this.typeNodeStack.pop());
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
        this.compUnit.addTopLevelNode((FunctionNode) this.invokableNodeStack.pop());
    }

    public void endCallableUnitBody() {
        this.invokableNodeStack.peek().setBody(this.blockNodeStack.pop());
    }

    public void addPackageId(List<String> nameComps, String version) {
        List<IdentifierNode> nameCompNodes = new ArrayList<>();
        IdentifierNode versionNode;
        if (version != null) {
            versionNode = TreeBuilder.createIdentifierNode();
            versionNode.setValue(version);
        } else {
            versionNode = null;
        }
        nameComps.stream().forEach(e -> nameCompNodes.add(this.createIdentifier(e)));
        this.pkgIdStack.add(new PackageID(nameCompNodes, versionNode));
    }
    
    public void populatePackageDeclaration() {
        PackageDeclarationNode pkgDecl = TreeBuilder.createPackageDeclarationNode();
        pkgDecl.setPackageID(this.pkgIdStack.pop());
        this.compUnit.addTopLevelNode(pkgDecl);
    }
    
    public void addImportPackageDeclaration(String alias) {
        ImportPackageNode impDecl = TreeBuilder.createImportPackageNode();
        IdentifierNode aliasNode;
        if (alias != null) {
            aliasNode = this.createIdentifier(alias);
        } else {
            aliasNode = null;
        }
        impDecl.setPackageID(this.pkgIdStack.pop());
        impDecl.setAlias(aliasNode);
        this.compUnit.addTopLevelNode(impDecl);
    }

    private VariableNode generateBasicVarNode(String identifier, boolean exprAvailable) {
        IdentifierNode name = this.createIdentifier(identifier);
        VariableNode var = TreeBuilder.createVariableNode();
        var.setName(name);
        var.setTypeNode(this.typeNodeStack.pop());
        if (exprAvailable) {
            var.setInitialExpression(this.exprNodeStack.pop());
        }
        return var;
    }

    public void addGlobalVariable(String identifier, boolean exprAvailable) {
        VariableNode var = this.generateBasicVarNode(identifier, exprAvailable);
        this.compUnit.addTopLevelNode(var);
    }
    
    public void addConstVariable(String identifier) {
        VariableNode var = this.generateBasicVarNode(identifier, true);
        var.addFlag(Flag.CONST);
        this.compUnit.addTopLevelNode(var);
    }

    public void startStructDef() {
        this.structStack.add(TreeBuilder.createStructNode());
    }
    
    public void endStructDef(String identifier) {
        StructNode structNode = this.structStack.pop();
        structNode.setName(this.createIdentifier(identifier));
        this.varListStack.pop().stream().forEach(e -> structNode.addField(e));
        this.compUnit.addTopLevelNode(structNode);
    }
    
    public void startConnectorDef() {
        ConnectorNode connectorNode = TreeBuilder.createConnectorNode();
        this.connectorNodeStack.push(connectorNode);
    }
    
    public void startConnectorBody() {
        /* end of connector definition header, so let's populate 
         * the connector information before processing the body */
        ConnectorNode connectorNode = this.connectorNodeStack.peek();
        if (!this.varStack.empty()) {
            connectorNode.setFilteredParamter(this.varStack.pop());
        }
        if (!this.varListStack.empty()) {
            this.varListStack.pop().forEach(e -> connectorNode.addParameter(e));
        }
        /* add a temporary block node to contain connector variable definitions */
        this.blockNodeStack.add(TreeBuilder.createBlockNode());
        /* action node list to contain the actions of the connector */
        this.actionNodeStack.add(new ArrayList<>());
    }
    
    public void endConnectorDef(String identifier) {
        ConnectorNode connectorNode = this.connectorNodeStack.pop();
        connectorNode.setName(this.createIdentifier(identifier));
        this.compUnit.addTopLevelNode(connectorNode);
    }
    
    public void endConnectorBody() {
        ConnectorNode connectorNode = this.connectorNodeStack.peek();
        this.blockNodeStack.pop().getStatements().stream().forEach(
                e -> connectorNode.addVariableDef((VariableDefinitionNode) e));
        this.actionNodeStack.pop().stream().forEach(e -> connectorNode.addAction(e));
    }

    public void startActionDef() {
        this.invokableNodeStack.push(TreeBuilder.createActionNode());
    }

    public void endActionDef() {
        this.connectorNodeStack.peek().addAction((ActionNode) this.invokableNodeStack.pop());
    }

    public void startProcessingTypeNodeList() {
        this.typeNodeListStack.push(new ArrayList<>());
    }

}
