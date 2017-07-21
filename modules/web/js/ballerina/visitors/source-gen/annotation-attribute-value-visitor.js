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
    canVisitAnnotationAttributeValue(annotationAttributeValue) {
        // prevent visiting nested instances (when this is an array value)
        // this is to have control while iterating those to insert commas, etc.
        return _.isNil(this.node) || (this.node !== annotationAttributeValue.getParent());
    }

    /**
     * Begins visiting annotation attribute value visiting.
     *
     * @param {AnnotationAttributeValue} annotationAttributeValue The node to visit.
     * @memberof AnnotationAttributeValueVisitor
     */
    beginVisitAnnotationAttributeValue(annotationAttributeValue) {
        this.node = annotationAttributeValue;
        this.isParentAttribValArray = ASTFactory.isAnnotationAttributeValue(this.node.getParent())
            && this.node.getParent().isArray();
        this.appendSource(annotationAttributeValue.isArray() ? '[' : '');
    }

    /**
     * Visits body of the annotation attribute value.
     * @memberof AnnotationAttributeValueVisitor
     */
    visitAnnotationAttributeValue(annotationAttributeValue) {
        // override default visit mechanism to keep track of no of children
        // this is needed for adding comma logic
        const attributeValues = annotationAttributeValue.getChildrenOfType(annotationAttributeValue
                                    .getFactory().isAnnotationAttributeValue);
        if (_.isArray(attributeValues)) {
            attributeValues.forEach((attributeVal, index) => {
                if (index !== 0) {
                    this.appendSource(',');
                }
                const annotationAttributeVisitor = new AnnotationAttributeValueVisitor(this);
                attributeVal.accept(annotationAttributeVisitor);
            });
        }
    }

    /**
     * Ends visiting annotation attribute value.
     *
     * @param {AnnotationAttributeValue} annotationAttributeValue The node which was visited.
     * @memberof AnnotationAttributeValueVisitor
     */
    endVisitAnnotationAttributeValue(annotationAttributeValue) {
        this.appendSource(annotationAttributeValue.isArray() ? ']' : '');
        this.getParent().appendSource(this.getGeneratedSource());
    }

    /**
     * Visiting the annotation attachment
     *
     * @param {AnnotationAttachment} annotationAttachment The annotation attachment to visit.
     * @memberof AnnotationAttributeValueVisitor
     */
    visitAnnotationAttachment(annotationAttachment) {
        if (this.isParentAttribValArray) {
            this.appendSource(this.node.whiteSpace.useDefault ? ' ' : this.node.getWSRegion(3));
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
        if (this.isParentAttribValArray) {
            this.appendSource(this.node.whiteSpace.useDefault ? ' ' : this.node.getWSRegion(3));
        }
        const bValueVisitor = new BValueVisitor(this);
        bValue.accept(bValueVisitor);
        if (this.isParentAttribValArray) {
            this.appendSource(this.node.whiteSpace.useDefault ? '' : this.node.getWSRegion(4));
        }
    }
}

export default AnnotationAttributeValueVisitor;
