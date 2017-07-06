/**
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

import AbstractSourceGenVisitor from './abstract-source-gen-visitor';
import ServiceDefinitionVisitor from './service-definition-visitor';
import AnnotationDefinitionVisitor from './annotation-definition-visitor';
import FunctionDefinitionVisitor from './function-definition-visitor';
import PackageDefinitionVisitor from './package-definition-visitor';
import ImportDeclarationVisitor from './import-declaration-visitor';
import ConnectorDefinitionVisitor from './connector-definition-visitor';
import StructDefinitionVisitor from './struct-definition-visitor';
import ConstantDefinitionVisitor from './constant-definition-visitor';
import GlobalVariableDefinitionVisitor from './global-variable-definition-visitor';

/**
* Source generation for the Ballerina AST Root
*/
class BallerinaASTRootVisitor extends AbstractSourceGenVisitor {

    /**
    * Can visit check for the Ballerina AST Root
    * @return {boolean} true|false
    */
    canVisitBallerinaASTRoot() {
        return true;
    }

    /**
     * Begin visit ballerina AST Root source generation
     * @param {ASTNode} astRoot - Ballerina AST Root
     */
    beginVisitBallerinaASTRoot(astRoot) {
        const lineNumber = this.getTotalNumberOfLinesInSource() + 1;
        astRoot.setLineNumber(lineNumber, { doSilently: true });

        const constructedSourceSegment = astRoot.getWSRegion(0);
        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        // Increase the total number of lines
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);

        this.appendSource(constructedSourceSegment);
    }

    /**
     * Visit Service Definition
     * @param {ServiceDefinition} serviceDefinition - Service Definition ASTNode
     */
    visitServiceDefinition(serviceDefinition) {
        const serviceDefinitionVisitor = new ServiceDefinitionVisitor(this);
        serviceDefinition.accept(serviceDefinitionVisitor);
    }

    /**
     * Visit annotation definition
     * @param {AnnotationDefinition} annotationDefinition - annotation definition ASTNode
     */
    visitAnnotationDefinition(annotationDefinition) {
        const annotationDefinitionVisitor = new AnnotationDefinitionVisitor(this);
        annotationDefinition.accept(annotationDefinitionVisitor);
    }

    /**
     * Visit connector definition
     * @param {ConnectorDefinition} connectorDefinition - connector definition ASTNode
     */
    visitConnectorDefinition(connectorDefinition) {
        const connectorDefinitionVisitor = new ConnectorDefinitionVisitor(this);
        connectorDefinition.accept(connectorDefinitionVisitor);
    }

    /**
     * Visit Function Definition
     * @param {FunctionDefinition} functionDefinition - Function Definition ASTNode
     */
    visitFunctionDefinition(functionDefinition) {
        const functionDefinitionVisitor = new FunctionDefinitionVisitor(this);
        functionDefinition.accept(functionDefinitionVisitor);
    }

    /**
     * Visit Struct Definition
     * @param {StructDefinition} structDefinition - Struct Definition ASTNode
     */
    visitStructDefinition(structDefinition) {
        const structDefinitionVisitor = new StructDefinitionVisitor(this);
        structDefinition.accept(structDefinitionVisitor);
    }

    /**
     * Visit Package Definition
     * @param {PackageDefinition} packageDefinition - Package Definition ASTNode
     */
    visitPackageDefinition(packageDefinition) {
        const packageDefinitionVisitor = new PackageDefinitionVisitor(this);
        packageDefinition.accept(packageDefinitionVisitor);
    }

    /**
     * Visit Import Declaration
     * @param {ImportDeclaration} importDeclaration - Import Declaration ASTNode
     */
    visitImportDeclaration(importDeclaration) {
        const importDeclarationVisitor = new ImportDeclarationVisitor(this);
        importDeclaration.accept(importDeclarationVisitor);
    }

    /**
     * Visit Constant Definition
     * @param {ConstantDefinition} constantDefinition - Constant Definition ASTNode
     */
    visitConstantDefinition(constantDefinition) {
        const constantDefinitionVisitor = new ConstantDefinitionVisitor(this);
        constantDefinition.accept(constantDefinitionVisitor);
    }

    /**
     * Visit Global Variable Definition
     * @param {GlobalVariableDefinition} globalVariableDefinition - Global Variable Definition ASTNode
     */
    visitGlobalVariableDefinition(globalVariableDefinition) {
        const globalVariableDefinitionVisitor = new GlobalVariableDefinitionVisitor(this);
        globalVariableDefinition.accept(globalVariableDefinitionVisitor);
    }
}

export default BallerinaASTRootVisitor;
