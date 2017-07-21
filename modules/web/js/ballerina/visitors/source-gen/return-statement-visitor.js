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

import AbstractStatementSourceGenVisitor from './abstract-statement-source-gen-visitor';
import ReturnStatement from '../../ast/statements/return-statement';
import ASTFactory from '../../ast/ballerina-ast-factory';
import FunctionDefinitionVisitor from './function-definition-visitor';

/**
 * Source generation for return statement
 */
class ReturnStatementVisitor extends AbstractStatementSourceGenVisitor {

    /**
     * Can visit check for return statement
     * @param {ReturnStatement} returnStatement - return statement ASTNode
     * @return {boolean} true|false - whether the return statement can visit or not
     */
    canVisitReturnStatement(returnStatement) {
        return returnStatement instanceof ReturnStatement;
    }

    /**
     * Begin visit for return statement
     * @param {ReturnStatement} returnStatement - Return statement ASTNode
     */
    beginVisitReturnStatement(returnStatement) {
        /**
         * set the configuration start for the reply statement definition language construct
         * If we need to add additional parameters which are dynamically added to the configuration start
         * that particular source generation has to be constructed here
         */
        if (returnStatement.whiteSpace.useDefault) {
            this.currentPrecedingIndentation = this.getCurrentPrecedingIndentation();
            this.replaceCurrentPrecedingIndentation(this.getIndentation());
        }

        // Calculate the line number
        const lineNumber = this.getTotalNumberOfLinesInSource() + 1;
        returnStatement.setLineNumber(lineNumber, { doSilently: true });
        let constructedSourceSegment = '';
        this.appendSource('return' + returnStatement.getWSRegion(1));

        const children = returnStatement.children;
        let spaces = '';
        for (let i = 0; i < children.length; i++) {
            const child = children[i];
            const ws = (child.whiteSpace.useDefault ? ' ' : child.getWSRegion(0));
            if (ASTFactory.isLambdaExpression(child)) {
                child.children[0].accept(new FunctionDefinitionVisitor(this));
                if (i !== 0) {
                    constructedSourceSegment += ',' + ws;
                    spaces += ws + ',';
                }
            } else {
                if (i !== 0) {
                    constructedSourceSegment += ',' + ws;
                }
                constructedSourceSegment += child.getExpressionString();
            }
        }
        this.appendSource(constructedSourceSegment);
        constructedSourceSegment = 'return ' + constructedSourceSegment + spaces;

        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        // Increase the total number of lines
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);
    }

    /**
     * End visit for return statement
     * @param {ReturnStatement} returnStatement - Return statement ASTNode
     */
    endVisitReturnStatement(returnStatement) {
        const constructedSourceSegment = ';' + returnStatement.getWSRegion(3)
            + ((returnStatement.whiteSpace.useDefault) ? this.currentPrecedingIndentation : '');

        // Add the increased number of lines
        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);

        this.appendSource(constructedSourceSegment);
        const generatedSource = this.getGeneratedSource();
        this.getParent().appendSource(generatedSource);
        returnStatement.viewState.source = generatedSource;
    }
}

export default ReturnStatementVisitor;
