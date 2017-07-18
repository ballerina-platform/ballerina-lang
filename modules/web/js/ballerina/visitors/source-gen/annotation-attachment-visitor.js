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
import AnnotationAttributeVisitor from './annotation-attribute-visitor';

/**
 * Source gen visitor for annotation attachment.
 *
 * @class AnnotationAttachmentVisitor
 * @extends {AbstractSourceGenVisitor}
 */
class AnnotationAttachmentVisitor extends AbstractSourceGenVisitor {
    /**
     * Creates an instance of AnnotationAttachmentVisitor.
     * @param {AbstractSourceGenVisitor} parent The parent visitor.
     * @param {boolean} [isFirstAnnotation=false] If its the first annotation.
     * @memberof AnnotationAttachmentVisitor
     */
    constructor(parent, isFirstAnnotation = false) {
        super(parent);
        this._isFirstAnnotation = isFirstAnnotation;
    }

    /**
     * Checks if the annotation attachment can be visited.
     *
     * @returns {boolean} true if can be visited, else false.
     * @memberof AnnotationAttachmentVisitor
     */
    canVisitAnnotationAttachment() {
        return true;
    }

    /**
     * Begins visiting annotation attachment.
     *
     * @param {AnnotationAttachment} annotationAttachment The node being visited.
     * @memberof AnnotationAttachmentVisitor
     */
    beginVisitAnnotationAttachment(annotationAttachment) {
        const useDefaultWS = annotationAttachment.whiteSpace.useDefault;
        if (useDefaultWS && !this._isFirstAnnotation) {
            this.currentPrecedingIndentation = this.getCurrentPrecedingIndentation();
            this.replaceCurrentPrecedingIndentation(this.getIndentation());
        }
        let constructedSourceSegment = '@';
        if (!(annotationAttachment.getPackageName() === undefined || annotationAttachment.getPackageName() === '')) {
            constructedSourceSegment += annotationAttachment.getPackageName() + ':';
        }
        constructedSourceSegment += annotationAttachment.getName() + annotationAttachment.getWSRegion(1);

        const hasChildren = annotationAttachment.getChildren().length > 0;
        constructedSourceSegment += '{' + ((!hasChildren && useDefaultWS) ? '' : annotationAttachment.getWSRegion(2));
        constructedSourceSegment += ((hasChildren && useDefaultWS) ? this.getIndentation() : '');
        this.appendSource(constructedSourceSegment);
        this.indent();
    }

    /**
     * Visits the body of the annotation attachment.
     * @memberof AnnotationAttachmentVisitor
     */
    visitAnnotationAttachment() {
    }

    /**
     * Ends visiting annotation atatchment
     *
     * @param {AnnotationAttachmentany} annotationAttachment The node being visited
     * @memberof AnnotationAttachmentVisitor
     */
    endVisitAnnotationAttachment(annotationAttachment) {
        this.outdent();
        this.appendSource('}' + annotationAttachment.getWSRegion(3));
        this.appendSource((annotationAttachment.whiteSpace.useDefault && !this._isFirstAnnotation)
                                ? this.currentPrecedingIndentation : '');
        this.getParent().appendSource(this.getGeneratedSource());
    }

    /**
    * Visits annotation attribute.
    * @param {AnnotationAttribute} annotationAttribute The annotation attribute child.
    */
    visitAnnotationAttribute(annotationAttribute) {
        const annotationAttributeVisitor = new AnnotationAttributeVisitor(this);
        annotationAttribute.accept(annotationAttributeVisitor);
    }
}
export default AnnotationAttachmentVisitor;
