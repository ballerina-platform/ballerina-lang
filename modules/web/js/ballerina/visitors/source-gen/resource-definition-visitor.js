/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import WorkerDeclarationVisitor from './worker-declaration-visitor';

/**
 * Source generation visitor for Resource definition
 */
class ResourceDefinitionVisitor extends AbstractSourceGenVisitor {

    /**
     * Can visit check for the resource definition
     * @return {boolean} true|false
     */
    canVisitResourceDefinition() {
        return true;
    }

    /**
     * Begin visit for the resource definition
     * @param {ResourceDefinition} resourceDefinition - resource definition node
     */
    beginVisitResourceDefinition(resourceDefinition) {
        /**
         * set the configuration start for the resource definition language construct
         * If we need to add additional parameters which are dynamically added to the configuration start
         * that particular source generation has to be constructed here
         */
        const useDefaultWS = resourceDefinition.whiteSpace.useDefault;
        if (useDefaultWS) {
            this.currentPrecedingIndentation = this.getCurrentPrecedingIndentation();
            this.replaceCurrentPrecedingIndentation('\n' + this.getIndentation());
        }
        let constructedSourceSegment = '';
        resourceDefinition.getChildrenOfType(resourceDefinition.getFactory().isAnnotationAttachment).forEach(
            (annotationAttachment, index) => {
                let annotationAttachmentVisitor;
                if (index === 0) {
                    annotationAttachmentVisitor = new AnnotationAttachmentVisitor(this, true);
                } else {
                    annotationAttachmentVisitor = new AnnotationAttachmentVisitor(this);
                }
                annotationAttachment.accept(annotationAttachmentVisitor);
            });

        const lineNumber = this.getTotalNumberOfLinesInSource()
            + this.getEndLinesInSegment(constructedSourceSegment) + 1;
        resourceDefinition.setLineNumber(lineNumber, { doSilently: true });

        constructedSourceSegment += this.getIndentation() + 'resource' + resourceDefinition.getWSRegion(0)
                  + resourceDefinition.getResourceName()
                  + resourceDefinition.getWSRegion(1)
                  + '(';

        constructedSourceSegment += resourceDefinition.getParametersAsString()
                + ')' + resourceDefinition.getWSRegion(3)
                + '{' + resourceDefinition.getWSRegion(4)
                + ((useDefaultWS) ? this.getIndentation() : '');
        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);
        this.appendSource(constructedSourceSegment);
        this.indent();
    }

    /**
     * Visit resource definition
     */
    visitResourceDefinition() {
    }

    /**
     * Visit resource level statements
     * @param {ASTNode} statement - Statement Node
     */
    visitStatement(statement) {
        const statementVisitorFactory = new StatementVisitorFactory();
        const statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
        statement.accept(statementVisitor);
    }

    /**
     * Visit resource level connector declarations
     * @param {ConnectorDeclaration} connectorDeclaration - connector declaration AST node
     */
    visitConnectorDeclaration(connectorDeclaration) {
        const connectorDeclarationVisitor = new ConnectorDeclarationVisitor(this);
        connectorDeclaration.accept(connectorDeclarationVisitor);
    }

    /**
     * Visit resource level worker declarations
     * @param {ASTNode} workerDeclaration - worker declaration AST Node
     */
    visitWorkerDeclaration(workerDeclaration) {
        const workerDeclarationVisitor = new WorkerDeclarationVisitor(this);
        workerDeclaration.accept(workerDeclarationVisitor);
    }

    /**
     * End visit for resource definition source generation
     * @param {ASTNode} resourceDefinition - resource definition ASTNode
     */
    endVisitResourceDefinition(resourceDefinition) {
        this.outdent();
        const constructedSourceSegment = '}' + resourceDefinition.getWSRegion(5)
            + ((resourceDefinition.whiteSpace.useDefault) ? this.currentPrecedingIndentation : '');
        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);
        this.appendSource(constructedSourceSegment);
        this.getParent().appendSource(this.getGeneratedSource());
    }
}

export default ResourceDefinitionVisitor;
