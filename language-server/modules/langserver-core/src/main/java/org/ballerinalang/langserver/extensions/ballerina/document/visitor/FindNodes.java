/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.extensions.ballerina.document.visitor;

import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Find function, service, class and import nodes for given ST.
 */
public class FindNodes extends NodeVisitor {

    private final List<FunctionDefinitionNode> functionDefinitionNodeList;
    private final List<ClassDefinitionNode> classDefinitionNodeList;
    private final List<ServiceDeclarationNode> serviceDeclarationNodeList;
    private final List<ImportDeclarationNode> importDeclarationNodesList;

    public FindNodes() {
        this.functionDefinitionNodeList = new ArrayList<>();
        this.classDefinitionNodeList = new ArrayList<>();
        this.serviceDeclarationNodeList = new ArrayList<>();
        this.importDeclarationNodesList = new ArrayList<>();

    }

    public List<ClassDefinitionNode> getClassDefinitionNodes() {
        return classDefinitionNodeList;
    }

    public List<FunctionDefinitionNode> getFunctionDefinitionNodes() {
        return functionDefinitionNodeList;
    }

    public List<ServiceDeclarationNode> getServiceDeclarationNodes() {
        return serviceDeclarationNodeList;
    }

    public List<ImportDeclarationNode> getImportDeclarationNodesList() {
        return importDeclarationNodesList;
    }

    @Override
    public void visit(ImportDeclarationNode importDeclarationNode) {
        this.importDeclarationNodesList.add(importDeclarationNode);
    }

    @Override
    public void visit(FunctionDefinitionNode functionDefinitionNode) {
        this.functionDefinitionNodeList.add(functionDefinitionNode);
    }

    @Override
    public void visit(ClassDefinitionNode classDefinitionNode) {
        this.classDefinitionNodeList.add(classDefinitionNode);
    }

    @Override
    public void visit(ServiceDeclarationNode serviceDeclarationNode) {
        this.serviceDeclarationNodeList.add(serviceDeclarationNode);
    }
}
