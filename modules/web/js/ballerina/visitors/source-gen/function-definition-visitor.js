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

    canVisitFunctionDefinition() {
        return true;
    }

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
            functionReturnTypesSource = ( prependSpace ? ' ' : functionDefinition.getWSRegion(4));
            functionReturnTypesSource += '(' + functionDefinition.getWSRegion(5)
                                            + functionDefinition.getReturnTypesAsString() + ')';
        }

        let constructedSourceSegment = '';
        // generate source for annotation attachmments
        _.forEach(functionDefinition.getChildrenOfType(functionDefinition.getFactory().isAnnotation),
            (annotationNode) => {
                if (annotationNode.isSupported()) {
                    constructedSourceSegment += annotationNode.toString()
                        + ((annotationNode.whiteSpace.useDefault) ? this.getIndentation() : '');
                }
            });
        // start creating complete source for function def
        constructedSourceSegment += ((functionDefinition.isNative() 
                    ? 'native' + functionDefinition.getWSRegion(0) : ''));
        constructedSourceSegment += 'function' + functionDefinition.getWSRegion(1)
                    + functionDefinition.getFunctionName() + functionDefinition.getWSRegion(2)
                    + '(' + functionDefinition.getWSRegion(3)
                    + functionDefinition.getArgumentsAsString() + ')';
        constructedSourceSegment += (!_.isNil(functionReturnTypesSource) ? functionReturnTypesSource : '');
        constructedSourceSegment += functionDefinition.getWSRegion(6)
                    + (functionDefinition.isNative() ? '' : '{') + functionDefinition.getWSRegion(7);
        constructedSourceSegment += (useDefaultWS) ? this.getIndentation() : '';
        this.appendSource(constructedSourceSegment);
        this.indent();
    }

    visitFunctionDefinition() {
    }

    endVisitFunctionDefinition(functionDefinition) {
        this.outdent();
        this.appendSource((functionDefinition.isNative() ? ';' : '}') + functionDefinition.getWSRegion(8));
        this.appendSource((functionDefinition.whiteSpace.useDefault) ?
                      this.currentPrecedingIndentation : '');
        this.getParent().appendSource(this.getGeneratedSource());
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
