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
import VariableDefinitionStatementVisitor from './variable-definition-statement-visitor';

/**
 * Source generation visitor for Struct definition
 */
class StructDefinitionVisitor extends AbstractSourceGenVisitor {

    /**
     * Can visit check for the struct definition
     * @return {boolean} true|false
     */
    canVisitStructDefinition() {
        return true;
    }

    /**
     * Begin visit for the resource definition
     * @param {StructDefinition} structDefinition - struct definition node
     */
    beginVisitStructDefinition(structDefinition) {
        const useDefaultWS = structDefinition.whiteSpace.useDefault;
        if (useDefaultWS) {
            this.currentPrecedingIndentation = this.getCurrentPrecedingIndentation();
            this.replaceCurrentPrecedingIndentation('\n' + this.getIndentation());
        }

        // Adding annotations
        let constructedSourceSegment = '';
        structDefinition.getChildrenOfType(structDefinition.getFactory().isAnnotationAttachment).forEach(
            (annotationAttachment) => {
                const annotationAttachmentVisitor = new AnnotationAttachmentVisitor(this);
                annotationAttachment.accept(annotationAttachmentVisitor);
            });

        const lineNumber = this.getTotalNumberOfLinesInSource()
            + this.getEndLinesInSegment(constructedSourceSegment) + 1;
        structDefinition.setLineNumber(lineNumber, { doSilently: true });

        constructedSourceSegment += 'struct' + structDefinition.getWSRegion(0)
              + structDefinition.getStructName() + structDefinition.getWSRegion(1)
              + '{' + structDefinition.getWSRegion(2) + ((useDefaultWS) ? this.getIndentation() : '');

        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);

        this.appendSource(constructedSourceSegment);
        this.indent();
        _.forEach(structDefinition.getVariableDefinitionStatements(), (variableDefStatement) => {
            const varDefVisitor = new VariableDefinitionStatementVisitor(this);
            variableDefStatement.accept(varDefVisitor);
        });
    }

    /**
     * Visit Struct Definition
     */
    visitStructDefinition() {
    }

    /**
     * End visit for struct definition source generation
     * @param {ASTNode} structDefinition - struct definition ASTNode
     */
    endVisitStructDefinition(structDefinition) {
        this.outdent();
        const constructedSourceSegment = '}' + structDefinition.getWSRegion(3) +
            ((structDefinition.whiteSpace.useDefault) ? this.currentPrecedingIndentation : '');
        this.appendSource(constructedSourceSegment);
        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);
        this.getParent().appendSource(this.getGeneratedSource());
    }
}

export default StructDefinitionVisitor;
