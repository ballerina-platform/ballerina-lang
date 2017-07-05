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

/**
 * Source generation for the Import declaration
 */
class ImportDeclarationVisitor extends AbstractSourceGenVisitor {

    /**
     * Can visit check for the import declaration
     * @return {boolean} true|false
     */
    canVisitImportDeclaration() {
        return true;
    }

    /**
     * Begin visit for the Import Declaration source generation
     * @param {ImportDeclaration} importDeclaration - Import Declaration ASTNode
     */
    beginVisitImportDeclaration(importDeclaration) {
        // Calculate the line number
        const lineNumber = this.getTotalNumberOfLinesInSource() + 1;
        importDeclaration.setLineNumber(lineNumber, { doSilently: true });

        let constructedSourceSegment = ((importDeclaration.whiteSpace.useDefault) ? this.getIndentation() : '')
          + 'import'
          + importDeclaration.getWSRegion(0)
          + importDeclaration.getPackageName()
          + ((importDeclaration.whiteSpace.useDefault
            && _.isNil(importDeclaration.getAsName())) ? '' : importDeclaration.getWSRegion(1));
        if (!_.isNil(importDeclaration.getAsName())) {
            constructedSourceSegment += (
                'as' + importDeclaration.getWSRegion(2)
                + importDeclaration.getAsName()
                + importDeclaration.getWSRegion(3)
            );
        }

        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        // Increase the total number of lines
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);

        this.appendSource(constructedSourceSegment);
    }

    /**
     * Visit Import Declaration
     */
    visitImportDeclaration() {
    }

    /**
     * End visit for the Import Declaration source generation
     * @param {ImportDeclaration} importDeclaration - Import Declaration ASTNode
     */
    endVisitImportDeclaration(importDeclaration) {
        const nodeIndex = importDeclaration.getNodeIndex();
        const root = importDeclaration.getParent();
        const nextNode = _.nth(root.getChildren(), nodeIndex + 1);
        let tailingWS = importDeclaration.getWSRegion(4);
        if (!_.isNil(nextNode) && root.getFactory().isImportDeclaration(nextNode)
                && nextNode.whiteSpace.useDefault) {
            tailingWS = '\n';
        } else if (importDeclaration.whiteSpace.useDefault &&
                        !_.isNil(nextNode) && !root.getFactory().isImportDeclaration(nextNode)
                        && !nextNode.whiteSpace.useDefault) {
            tailingWS = '\n\n';
        }

        const constructedSourceSegment = ';' + tailingWS;

        // Add the increased number of lines
        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);

        this.appendSource(constructedSourceSegment);
        this.getParent().appendSource(this.getGeneratedSource());
    }
}

export default ImportDeclarationVisitor;
