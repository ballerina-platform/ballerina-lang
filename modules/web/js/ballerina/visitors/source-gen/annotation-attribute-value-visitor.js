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
import AbstractSourceGenVisitor from './abstract-source-gen-visitor';
import AnnotationAttachmentVisitor from './annotation-attachment-visitor';
import ASTFactory from '../../ast/ballerina-ast-factory';
import BValueVisitor from './b-value-visitor';

/**
 * Source gen visitor for annotation attribute value.
 *
 * @class AnnotationAttributeValueVisitor
 * @extends {AbstractSourceGenVisitor}
 */
class AnnotationAttributeValueVisitor extends AbstractSourceGenVisitor {
    /**
     * Checks if annotation attribute value visitor.
     *
     * @returns {boolean} true if can be visited, else false.
     * @memberof AnnotationAttributeValueVisitor
     */
    canVisitAnnotationAttributeValue() {
        return true;
    }

    /**
     * Begins visiting annotation attribute value visiting.
     *
     * @param {AnnotationAttributeValue} annotationAttributeValue The node to visit.
     * @memberof AnnotationAttributeValueVisitor
     */
    beginVisitAnnotationAttributeValue(annotationAttributeValue) {
        this.appendSource(annotationAttributeValue.getWSRegion(3));
        if (ASTFactory.isAnnotationAttributeValue(annotationAttributeValue.getParent()) &&
                                                                    annotationAttributeValue.getParent().isArray()) {
            this.indent();
        }
    }

    /**
     * Visits body of the annotation attribute value.
     * @memberof AnnotationAttributeValueVisitor
     */
    visitAnnotationAttributeValue() {
    }

    /**
     * Ends visiting annotation attribute value.
     *
     * @param {AnnotationAttributeValue} annotationAttributeValue The node which was visited.
     * @memberof AnnotationAttributeValueVisitor
     */
    endVisitAnnotationAttributeValue(annotationAttributeValue) {
        // If parent is an array
        if (ASTFactory.isAnnotationAttributeValue(annotationAttributeValue.getParent())) {
            this.outdent();
            if (annotationAttributeValue.getParent().getChildren().length - 1 !==
                annotationAttributeValue.getParent().getIndexOfChild(annotationAttributeValue)) {
                this.appendSource(',');
            }
        }

        // If parent is not an array
        if (!ASTFactory.isAnnotationAttributeValue(annotationAttributeValue.getParent())) {
            this.getParent().appendSource(this.getGeneratedSource());
        }
    }

    /**
     * Visiting the annotation attachment
     *
     * @param {AnnotationAttachment} annotationAttachment The annotation attachment to visit.
     * @memberof AnnotationAttributeValueVisitor
     */
    visitAnnotationAttachment(annotationAttachment) {
        if (ASTFactory.isAnnotationAttributeValue(annotationAttachment.getParent().getParent()) &&
                                                            annotationAttachment.getParent().getParent().isArray()) {
            this.appendSource('\n');
        }
        const annotationAttachmentVisitor = new AnnotationAttachmentVisitor(this);
        annotationAttachment.accept(annotationAttachmentVisitor);
    }

    /**
     * Visits the b-value node.
     *
     * @param {BValue} bValue The value to visit.
     * @memberof AnnotationAttributeValueVisitor
     */
    visitBValue(bValue) {
        if (ASTFactory.isAnnotationAttributeValue(bValue.getParent().getParent())) {
            this.appendSource('\n' + this.getIndentation());
        }

        const bValueVisitor = new BValueVisitor(this);
        bValue.accept(bValueVisitor);
    }
}

export default AnnotationAttributeValueVisitor;
