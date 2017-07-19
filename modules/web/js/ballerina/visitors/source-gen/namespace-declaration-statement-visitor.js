/**
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import AbstractStatementSourceGenVisitor from './abstract-statement-source-gen-visitor';
import NamespaceDeclarationStatement from '../../ast/statements/namespace-declaration-statement';

/**
 * visitor class for namespace declaration statement
 * @class NamespaceDeclarationStatementVisitor
 * */
class NamespaceDeclarationStatementVisitor extends AbstractStatementSourceGenVisitor {
    /**
     * can visit the namespace declaration
     * @param {ASTNode} namespaceDeclarationStatement - namespace Declaration Statement
     * @return {boolean} true if true else false.
     * */
    canVisitNamespaceDeclarationStatement(namespaceDeclarationStatement) {
        return namespaceDeclarationStatement instanceof NamespaceDeclarationStatement;
    }

    /**
     * begin visiting the NamespaceDeclarationStatement
     * */
    beginVisitNamespaceDeclarationStatement(namespaceDeclarationStatement) {
        if (namespaceDeclarationStatement.whiteSpace.useDefault) {
            this.currentPrecedingIndentation = this.getCurrentPrecedingIndentation();
            this.replaceCurrentPrecedingIndentation(this.getIndentation());
        }

        const lineNumber = this.getTotalNumberOfLinesInSource() + 1;
        namespaceDeclarationStatement.setLineNumber(lineNumber, { doSilently: true });
        const constructedSourceSegment = namespaceDeclarationStatement.getStatementString();
        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);

        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);
        this.appendSource(constructedSourceSegment);
    }

    /**
     * end visiting NamespaceDeclarationStatement
     * */
    endVisitNamespaceDeclarationStatement(namespaceDeclarationStatement) {
        const constructedSourceSegment = ';' + namespaceDeclarationStatement.getWSRegion(4)
            + ((namespaceDeclarationStatement.whiteSpace.useDefault) ? this.currentPrecedingIndentation : '');
        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);

        this.appendSource(constructedSourceSegment);
        this.getParent().appendSource(this.getGeneratedSource());
    }
}

export default NamespaceDeclarationStatementVisitor;
