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
import log from 'log';
import EventChannel from 'event_channel';
import AbstractSourceGenVisitor from './abstract-source-gen-visitor';
import StatementVisitorFactory from './statement-visitor-factory';
import ConnectorDeclarationVisitor from './connector-declaration-visitor';
import VariableDeclarationVisitor from './variable-declaration-visitor';
import WorkerDeclarationVisitor from './worker-declaration-visitor';

/**
 * @param parent
 * @constructor
 */
class FunctionDefinitionVisitor extends AbstractSourceGenVisitor {
    constructor(parent) {
        super(parent);
    }

    canVisitFunctionDefinition(functionDefinition) {
        return true;
    }

    beginVisitFunctionDefinition(functionDefinition) {
        /**
         * set the configuration start for the function definition language construct
         * If we need to add additional parameters which are dynamically added to the configuration start
         * that particular source generation has to be constructed here
         */
        const useDefaultWS = functionDefinition.whiteSpace.useDefault;
        if (useDefaultWS) {
            this.currentPrecedingIndentation = this.getCurrentPrecedingIndentation();
            this.replaceCurrentPrecedingIndentation('\n' + this.getIndentation());
        }
        const functionReturnTypes = functionDefinition.getReturnTypesAsString();
        let functionReturnTypesSource;
        if (!_.isEmpty(functionReturnTypes)) {
            functionReturnTypesSource = '(' + functionDefinition.getWSRegion(5) + functionDefinition.getReturnTypesAsString() + ')';
        }

        let constructedSourceSegment = '';
        _.forEach(functionDefinition.getChildrenOfType(functionDefinition.getFactory().isAnnotation), (annotationNode) => {
            if (annotationNode.isSupported()) {
                constructedSourceSegment += annotationNode.toString()
                  + ((annotationNode.whiteSpace.useDefault) ? this.getIndentation() : '');
            }
        });
        constructedSourceSegment += ((functionDefinition.isNative() ? 'native' + functionDefinition.getWSRegion(0) : ''));
        constructedSourceSegment += 'function' + functionDefinition.getWSRegion(1)
            + functionDefinition.getFunctionName() + functionDefinition.getWSRegion(2) + '(' + functionDefinition.getWSRegion(3)
            + functionDefinition.getArgumentsAsString() + ')';
        constructedSourceSegment += (!_.isNil(functionReturnTypesSource)
            ? (functionDefinition.getWSRegion(4) + functionReturnTypesSource) : '');
        constructedSourceSegment += functionDefinition.getWSRegion(6) +
            (functionDefinition.isNative() ? '' : '{') + functionDefinition.getWSRegion(7);
        constructedSourceSegment += (useDefaultWS) ? this.getIndentation() : '';
        this.appendSource(constructedSourceSegment);
        this.indent();
        log.debug('Begin Visit FunctionDefinition');
    }

    visitFunctionDefinition(functionDefinition) {
        log.debug('Visit FunctionDefinition');
    }

    endVisitFunctionDefinition(functionDefinition) {
        this.outdent();
        this.appendSource((functionDefinition.isNative() ? ';' : '}') + functionDefinition.getWSRegion(8));
        this.appendSource((functionDefinition.whiteSpace.useDefault) ?
                      this.currentPrecedingIndentation : '');
        this.getParent().appendSource(this.getGeneratedSource());
        log.debug('End Visit FunctionDefinition');
    }

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

    visitConnectorDeclaration(connectorDeclaration) {
        const connectorDeclarationVisitor = new ConnectorDeclarationVisitor(this);
        connectorDeclaration.accept(connectorDeclarationVisitor);
    }

    visitVariableDeclaration(variableDeclaration) {
        const variableDeclarationVisitor = new VariableDeclarationVisitor(this);
        variableDeclaration.accept(variableDeclarationVisitor);
    }

    visitWorkerDeclaration(workerDeclaration) {
        const workerDeclarationVisitor = new WorkerDeclarationVisitor(this);
        workerDeclaration.accept(workerDeclarationVisitor);
    }
}

export default FunctionDefinitionVisitor;
