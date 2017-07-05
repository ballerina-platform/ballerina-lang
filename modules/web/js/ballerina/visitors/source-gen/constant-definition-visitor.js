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

/**
 * Visitor for a constant definition source generation
 */
class ConstantDefinitionVisitor extends AbstractSourceGenVisitor {

    /**
     * Can visit check for the constant Definition
     * @return {boolean} true|false - whether the constant definition can visit or not
     */
    canVisitConstantDefinition() {
        return true;
    }

    /**
     * @param {ConstantDefinition} constantDefinition - The constant definition to start visiting.
     */
    beginVisitConstantDefinition(constantDefinition) {
        const useDefaultWS = constantDefinition.whiteSpace.useDefault;
        if (useDefaultWS) {
            this.currentPrecedingIndentation = this.getCurrentPrecedingIndentation();
            this.replaceCurrentPrecedingIndentation('\n' + this.getIndentation());
        }
        // Calculate the line number
        const lineNumber = this.getTotalNumberOfLinesInSource() + 1;
        constantDefinition.setLineNumber(lineNumber, { doSilently: true });

        // Adding annotations
        let constructedSourceSegment = '';
        const astFactory = constantDefinition.getFactory();
        for (const annotationNode of constantDefinition.getChildrenOfType(astFactory.isAnnotation)) {
            if (annotationNode.isSupported()) {
                constructedSourceSegment += annotationNode.toString()
                    + ((annotationNode.whiteSpace.useDefault) ? this.getIndentation() : '');
            }
        }

        constructedSourceSegment += constantDefinition.getConstantDefinitionAsString();

        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        // Increase the total number of lines
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);

        this.appendSource(constructedSourceSegment);
    }

    /**
     * Visit the constant definition
     */
    visitConstantDefinition() {
    }

    /**
     * End visit for constant Definition statement
     * @param {ConstantDefinition} constantDefinition - constant Definition statement ASTNode
     */
    endVisitConstantDefinition(constantDefinition) {
        const constructedSourceSegment = ';' + constantDefinition.getWSRegion(5)
            + ((constantDefinition.whiteSpace.useDefault) ? this.currentPrecedingIndentation : '');

        // Add the increased number of lines
        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);

        this.appendSource(constructedSourceSegment);
        this.getParent().appendSource(this.getGeneratedSource());
    }
}

export default ConstantDefinitionVisitor;
