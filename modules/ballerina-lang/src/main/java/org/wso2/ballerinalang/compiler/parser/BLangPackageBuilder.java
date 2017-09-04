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
import org.ballerinalang.model.tree.CompilationUnitNode;
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

    private Stack<BlockNode> blockNodeStack = new Stack<>();

    private Stack<List<VariableNode>> paramListStack = new Stack<>();

    private Stack<List<VariableNode>> retParamListStack = new Stack<>();

    private Stack<InvocableNode> invokableNodeStack = new Stack<>();

    private Stack<ExpressionNode> exprNodeStack = new Stack<>();
    
    private Stack<PackageID> pkgIdStack = new Stack<>();
    
    private Stack<StructNode> structStack = new Stack<>();
    
    private Stack<FieldDefinitionContainer> fieldDefContainerStack = new Stack();

    public BLangPackageBuilder(CompilationUnitNode compUnit) {
        this.compUnit = compUnit;
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
        InvocableNode invNode = this.invokableNodeStack.peek();
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
        this.compUnit.addTopLevelNode((FunctionNode) this.invokableNodeStack.pop());
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
    
    private VariableNode generateBasicVarNode(String identifier) {
        IdentifierNode name = this.createIdentifier(identifier);
        VariableNode var = TreeBuilder.createVariableNode();
        var.setName(name);
        var.setType(this.typeNodeStack.pop());
        if (!this.exprNodeStack.empty()) {
            var.setInitialExpression(this.exprNodeStack.pop());
        }
        return var;
    }
    
    public void addGlobalVariable(String identifier) {
        VariableNode var = this.generateBasicVarNode(identifier);
        this.compUnit.addTopLevelNode(var);
    }
    
    public void addConstVariable(String identifier) {
        VariableNode var = this.generateBasicVarNode(identifier);
        var.addFlag(Flag.CONST);
        this.compUnit.addTopLevelNode(var);
    }
    
    public void startFieldDefConatiner() {
        this.fieldDefContainerStack.add(new FieldDefinitionContainer());
    }
    
    public void addFieldDefinition(String identifier) {
        this.fieldDefContainerStack.peek().vars.add(this.generateBasicVarNode(identifier));
    }
    
    public void startStructDef() {
        this.structStack.add(TreeBuilder.createStructNode());
    }
    
    public void endStructDef() {
        StructNode structNode = this.structStack.pop();
        this.fieldDefContainerStack.pop().vars.stream().forEach(e -> structNode.addField(e));
        this.compUnit.addTopLevelNode(structNode);
    }
    
    /**
     * Represents a list of field definitions, used in structs and annotations.
     */
    private class FieldDefinitionContainer {
        public List<VariableNode> vars = new ArrayList<>();
    }

}
