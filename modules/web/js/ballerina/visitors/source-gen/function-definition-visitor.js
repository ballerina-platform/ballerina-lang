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

import _ from 'lodash';
import AbstractSourceGenVisitor from './abstract-source-gen-visitor';
import AnnotationAttachmentVisitor from './annotation-attachment-visitor';
import StatementVisitorFactory from './statement-visitor-factory';
import ConnectorDeclarationVisitor from './connector-declaration-visitor';
import VariableDeclarationVisitor from './variable-declaration-visitor';
import WorkerDeclarationVisitor from './worker-declaration-visitor';

/**
 * Source generation function definition
 */
class FunctionDefinitionVisitor extends AbstractSourceGenVisitor {

    /**
     * Can visit check for function definition
     * @return {boolean} true| false - whether the function definition can visit or not
     */
    canVisitFunctionDefinition() {
        return true;
    }

    /**
     * Begin visit for the resource definition
     * @param {FunctionDefinition} functionDefinition - function definition node
     */
    beginVisitFunctionDefinition(functionDefinition) {
        const useDefaultWS = functionDefinition.whiteSpace.useDefault;
        // handle preceding indentation
        if (useDefaultWS) {
            this.currentPrecedingIndentation = this.getCurrentPrecedingIndentation();
            this.replaceCurrentPrecedingIndentation('\n' + this.getIndentation());
        }
        // generate source fragement for return types
        let functionReturnTypesSource;
        if (!_.isEmpty(functionDefinition.getReturnTypes())) {
            const prependSpace = _.first(functionDefinition.getReturnTypes()).whiteSpace.useDefault;
            // if there were no return types before, return type wrapper shold be prepended with a space
            functionReturnTypesSource = (prependSpace ? ' ' : functionDefinition.getWSRegion(4)) +
                (functionDefinition.hasReturnsKeyword() ? 'returns ' : '');
            functionReturnTypesSource += '(' + functionDefinition.getWSRegion(5)
                                            + functionDefinition.getReturnTypesAsString() + ')';
        }

        let constructedSourceSegment = '';
        // generate source for annotation attachmments
        functionDefinition.getChildrenOfType(functionDefinition.getFactory().isAnnotationAttachment).forEach(
            (annotationAttachment) => {
                const annotationAttachmentVisitor = new AnnotationAttachmentVisitor(this);
                annotationAttachment.accept(annotationAttachmentVisitor);
            });

        const lineNumber = this.getTotalNumberOfLinesInSource()
            + this.getEndLinesInSegment(constructedSourceSegment) + 1;
        functionDefinition.setLineNumber(lineNumber, { doSilently: true });

        // start creating complete source for function def
        constructedSourceSegment += ((functionDefinition.isNative()
                    ? 'native' + functionDefinition.getWSRegion(0) : ''));
        constructedSourceSegment += 'function' + functionDefinition.getWSRegion(1)
                    + (functionDefinition.isLambda() ? '' : functionDefinition.getFunctionName())
                    + functionDefinition.getWSRegion(2)
                    + '(' + functionDefinition.getWSRegion(3)
                    + functionDefinition.getArgumentsAsString() + ')';
        constructedSourceSegment += (!_.isNil(functionReturnTypesSource) ? functionReturnTypesSource : '');
        constructedSourceSegment += functionDefinition.getWSRegion(6)
                    + (functionDefinition.isNative() ? '' : '{') + functionDefinition.getWSRegion(7);
        constructedSourceSegment += (useDefaultWS) ? this.getIndentation() : '';

        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);
        this.appendSource(constructedSourceSegment);
        this.indent();
    }

    /**
     * Visit Function Definition
     */
    visitFunctionDefinition() {
    }

    /**
     * End visit for the resource definition
     * @param {FunctionDefinition} functionDefinition - function definition node
     */
    endVisitFunctionDefinition(functionDefinition) {
        this.outdent();
        const constructedSourceSegment = (functionDefinition.isNative() ? ';' : '}')
            + functionDefinition.getWSRegion(8) + ((functionDefinition.whiteSpace.useDefault) ?
                this.currentPrecedingIndentation : '');
        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);
        this.appendSource(constructedSourceSegment);
        const generatedSource = this.getGeneratedSource();
        functionDefinition.getViewState().source = generatedSource;
        this.getParent().appendSource(generatedSource);
    }

    /**
     * Visit Statement
     * @param {Statement} statement - Statement ASTNode
     */
    visitStatement(statement) {
        const statementVisitorFactory = new StatementVisitorFactory();
        const statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
        statement.accept(statementVisitor);
    }

    /**
     * visits commentStatement
     * @param {Object} statement - comment statement
     */
    visitCommentStatement(statement) {
        const statementVisitorFactory = new StatementVisitorFactory();
        const statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
        statement.accept(statementVisitor);
    }

    /**
     * Visit Connector Declaration
     * @param {ConnectorDeclaration} connectorDeclaration - connector Declaration ASTNode
     */
    visitConnectorDeclaration(connectorDeclaration) {
        const connectorDeclarationVisitor = new ConnectorDeclarationVisitor(this);
        connectorDeclaration.accept(connectorDeclarationVisitor);
    }

    /**
     * Visit variable Declaration
     * @param {VariableDeclaration} variableDeclaration - variable Declaration ASTNode
     */
    visitVariableDeclaration(variableDeclaration) {
        const variableDeclarationVisitor = new VariableDeclarationVisitor(this);
        variableDeclaration.accept(variableDeclarationVisitor);
    }

    /**
     * Visit worker Declaration
     * @param {WorkerDeclaration} workerDeclaration - worker Declaration ASTNode
     */
    visitWorkerDeclaration(workerDeclaration) {
        const workerDeclarationVisitor = new WorkerDeclarationVisitor(this);
        workerDeclaration.accept(workerDeclarationVisitor);
    }
}

export default FunctionDefinitionVisitor;
