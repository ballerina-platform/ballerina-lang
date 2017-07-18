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
 * Visitor for connector action
 */
class ConnectorActionVisitor extends AbstractSourceGenVisitor {

    /**
     * Can visit check for connector action
     * @return {boolean} true|false
     */
    canVisitConnectorAction() {
        return true;
    }

    /**
     * Begin visit of the connector action
     * @param {ConnectorAction} connectorAction - Connector action ASTNode
     */
    beginVisitConnectorAction(connectorAction) {
        /**
         * set the configuration start for the function definition language construct
         * If we need to add additional parameters which are dynamically added to the configuration start
         * that particular source generation has to be constructed here
         */
        const actionReturnTypes = connectorAction.getReturnTypes();
        const useDefaultWS = connectorAction.whiteSpace.useDefault;
        if (useDefaultWS) {
            this.currentPrecedingIndentation = this.getCurrentPrecedingIndentation();
            this.replaceCurrentPrecedingIndentation('\n' + this.getIndentation());
        }

        let connectorActionReturnTypesSource = '';
        if (!_.isEmpty(actionReturnTypes)) {
            // if return types were not there before && no space ATM before (, add a space before
            const precedingWS = ((_.isEmpty(connectorAction.getWSRegion(3)) &&
                    actionReturnTypes[0].whiteSpace.useDefault) ? ' ' : connectorAction.getWSRegion(3));
            connectorActionReturnTypesSource = precedingWS + '(' + connectorAction.getReturnTypesAsString() + ')';
        }

        let constructedSourceSegment = '';
        const annotationAttachments = connectorAction.getChildrenOfType(
                                            connectorAction.getFactory().isAnnotationAttachment);
        annotationAttachments.forEach(
            (annotationAttachment, index) => {
                let annotationAttachmentVisitor;
                if (index === 0) {
                    annotationAttachmentVisitor = new AnnotationAttachmentVisitor(this, true);
                } else {
                    annotationAttachmentVisitor = new AnnotationAttachmentVisitor(this);
                }
                annotationAttachment.accept(annotationAttachmentVisitor);
            });

        // Set the line number
        const lineNumber = this.getTotalNumberOfLinesInSource()
            + this.getEndLinesInSegment(constructedSourceSegment) + 1;
        connectorAction.setLineNumber(lineNumber, { doSilently: true });

        constructedSourceSegment += !_.isEmpty(annotationAttachments) &&
                                        (useDefaultWS || _.last(annotationAttachments).whiteSpace.useDefault)
                                            ? this.getIndentation() : '';
        constructedSourceSegment += 'action' + connectorAction.getWSRegion(1)
            + connectorAction.getActionName()
            + connectorAction.getWSRegion(2) + '(' + connectorAction.getArgumentsAsString()
            + ')' + connectorActionReturnTypesSource
            + connectorAction.getWSRegion(4) + '{' + connectorAction.getWSRegion(5);
        constructedSourceSegment += (useDefaultWS) ? this.getIndentation() : '';

        // Set the new line number count
        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);

        this.appendSource(constructedSourceSegment);
        this.indent();
    }

    /**
     * Visit Connector action
     */
    visitConnectorAction() {
    }

    /**
     * End visit of the Connector Action
     * @param {ConnectorAction} connectorAction - Connector action ASTNode
     */
    endVisitConnectorAction(connectorAction) {
        this.outdent();
        this.appendSource('}' + connectorAction.getWSRegion(6));
        this.appendSource((connectorAction.whiteSpace.useDefault) ?
                      this.currentPrecedingIndentation : '');
        this.getParent().appendSource(this.getGeneratedSource());
    }

    /**
     * Visit Statements
     * @param {Statement} statement - statement ASTNode
     */
    visitStatement(statement) {
        const statementVisitorFactory = new StatementVisitorFactory();
        const statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
        statement.accept(statementVisitor);
    }

    /**
     * Visit Connector Declarations
     * @param {ConnectorDeclaration} connectorDeclaration - connector declaration ASTNode
     */
    visitConnectorDeclaration(connectorDeclaration) {
        const connectorDeclarationVisitor = new ConnectorDeclarationVisitor(this);
        connectorDeclaration.accept(connectorDeclarationVisitor);
    }

    /**
     * Visit variable Declaration
     * @param {VariableDeclaration} variableDeclaration - variable Definition ASTNode
     */
    visitVariableDeclaration(variableDeclaration) {
        const variableDeclarationVisitor = new VariableDeclarationVisitor(this);
        variableDeclaration.accept(variableDeclarationVisitor);
    }

    /**
     * Visit Worker Declaration
     * @param {WorkerDeclaration} workerDeclaration - worker declaration ASTNode
     */
    visitWorkerDeclaration(workerDeclaration) {
        const workerDeclarationVisitor = new WorkerDeclarationVisitor(this);
        workerDeclaration.accept(workerDeclarationVisitor);
    }
}

export default ConnectorActionVisitor;
