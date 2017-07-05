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

import AbstractStatementSourceGenVisitor from './abstract-statement-source-gen-visitor';
import VariableDefinitionStatement from '../../ast/statements/variable-definition-statement';

/**
 * Source Generation visitor for Variable definition statement
 */
class VariableDefinitionStatementVisitor extends AbstractStatementSourceGenVisitor {

    /**
     * Can visit check for variable definition statement visitor
     * @param {VariableDefinitionStatement} variableDefinitionStatement - variable definition ASTNode
     * @return {boolean} true|false
     */
    canVisitVariableDefinitionStatement(variableDefinitionStatement) {
        return variableDefinitionStatement instanceof VariableDefinitionStatement;
    }

    /**
     * Begin visit for variable definition statement source generation
     * @param {VariableDefinitionStatement} variableDefinitionStatement - variable definition ASTNode
     */
    beginVisitVariableDefinitionStatement(variableDefinitionStatement) {
        if (variableDefinitionStatement.whiteSpace.useDefault) {
            this.currentPrecedingIndentation = this.getCurrentPrecedingIndentation();
            this.replaceCurrentPrecedingIndentation(this.getIndentation());
        }

        // Calculate the line number
        const lineNumber = this.getTotalNumberOfLinesInSource() + 1;
        variableDefinitionStatement.setLineNumber(lineNumber, { doSilently: true });

        const constructedSource = variableDefinitionStatement.getStatementString();
        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSource);

        // Increase total number of lines
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);
        this.appendSource(constructedSource);
    }

    /**
     * End visit for variable definition statement source generation
     * @param {VariableDefinitionStatement} variableDefinitionStatement - variable definition ASTNode
     */
    endVisitVariableDefinitionStatement(variableDefinitionStatement) {
        const constructedSourceSegment = ';' + variableDefinitionStatement.getWSRegion(4)
            + ((variableDefinitionStatement.whiteSpace.useDefault) ? this.currentPrecedingIndentation : '');

        // Add the increased number of lines
        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);

        this.appendSource(constructedSourceSegment);
        this.getParent().appendSource(this.getGeneratedSource());
    }
}

export default VariableDefinitionStatementVisitor;
