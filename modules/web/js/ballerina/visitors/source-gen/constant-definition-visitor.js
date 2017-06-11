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
import log from 'log';
import AbstractSourceGenVisitor from './abstract-source-gen-visitor';

/**
 * Visitor for a constant definition.
 * @param {ASTVisitor} parent - Parent visitor.
 * @constructor
 */
class ConstantDefinitionVisitor extends AbstractSourceGenVisitor {
    constructor(parent) {
        super(parent);
    }

    canVisitConstantDefinition(constantDefinition) {
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

        // Adding annotations
        let constructedSourceSegment = '';
        for(const annotationNode of constantDefinition.getChildrenOfType(constantDefinition.getFactory().isAnnotation)) {
            if (annotationNode.isSupported()) {
                constructedSourceSegment += annotationNode.toString()
                    + ((annotationNode.whiteSpace.useDefault) ? this.getIndentation() : '');
            }
        }

        constructedSourceSegment += constantDefinition.getConstantDefinitionAsString();
        this.appendSource(constructedSourceSegment);
        log.debug('Begin Visit ConstantDefinition');
    }

    visitConstantDefinition(constantDefinition) {
        log.debug('Visit ConstantDefinition');
    }

    endVisitConstantDefinition(constantDefinition) {
        this.appendSource(';' + constantDefinition.getWSRegion(5));
        this.appendSource((constantDefinition.whiteSpace.useDefault)
            ? this.currentPrecedingIndentation : '');
        this.getParent().appendSource(this.getGeneratedSource());
        log.debug('End Visit ConstantDefinition');
    }
}

export default ConstantDefinitionVisitor;
