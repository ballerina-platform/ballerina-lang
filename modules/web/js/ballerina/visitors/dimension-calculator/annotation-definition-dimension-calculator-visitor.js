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

import log from 'log';
import * as DesignerDefaults from './../../configs/designer-defaults';
import SimpleBBox from './../../ast/simple-bounding-box';
import {util} from './../sizing-utils';

class AnnotationDefinitionDimensionCalculatorVisitor {
    canVisit(node) {
        log.debug("can visit AnnotationDefinitionDimensionCalc");
        return true;
    }

    beginVisit(node) {
        log.debug("beign visit AnnotationDefinitionDimensionCalc");
    }

    visit(node) {
        log.debug("visit AnnotationDefinitionDimensionCalc");
    }

    endVisit(node) {
        util.populateOuterPanelDecoratorBBox(node);
        let viewState = node.getViewState();

        this.annotationAttributeDefinitionDimension(node);

        viewState.bBox.h = viewState.components['heading'].h + viewState.components['body'].h
            + viewState.components['annotation'].h;
        viewState.bBox.w = viewState.components['body'].w;

        const textWidth = util.getTextWidth(node.getAnnotationName());
        viewState.titleWidth = textWidth.w;
        viewState.trimmedTitle = textWidth.text;

        //// Creating components for parameters of the annotation
        // Creating component for opening bracket of the parameters view.
        viewState.components.openingParameter = {};
        viewState.components.openingParameter.w = util.getTextWidth('(', 0).w;

        // Creating component for closing bracket of the parameters view.
        viewState.components.closingParameter = {};
        viewState.components.closingParameter.w = util.getTextWidth(')', 0).w;

        let componentWidth = viewState.components['heading'].w > viewState.components['body'].w
            ? viewState.components['heading'].w : viewState.components['body'].w;

        viewState.bBox.w = componentWidth +
            this.annotationAttachmentPointWidth(node) +
            viewState.titleWidth + 14 + (DesignerDefaults.panel.wrapper.gutter.h * 2) + 100;
    }

    /**
     * Calculate Attachment point text width for annotation attachments.
     * @param {AnnotationDefinition} node - Annotation Definition Node.
     * @return {number} width - return sum of the widths of attachment texts.
     * */
    annotationAttachmentPointWidth(node) {
        let width = 0;
        if (node.getAttachmentPoints().length > 0) {
            for (let i = 0; i < node.getAttachmentPoints().length; i++) {
                width += util.getTextWidth(node.getAttachmentPoints()[i], 0).w;
            }
        }

        return width;
    }

    /**
     * Get the max width among children
     * @param {object} node - children node to get the max width.
     * @return {number} maxWidthAmongChildren - Max width among children
     * */
    getMaxWidthOfChildren(node) {
        let maxWidthAmongChildren = 0;
        node.children.forEach(function (child, index) {
            if (maxWidthAmongChildren < child.viewState.textLength.w) {
                maxWidthAmongChildren = child.viewState.textLength.w;
            }
        });
        return maxWidthAmongChildren;
    }

    /**
     * Calculate Annotation Attribute Definition Dimension.
     * @param {object} node - annotation definition node.
     * */
    annotationAttributeDefinitionDimension(node) {
        let bodyHeight = node.viewState.components['body'].h;
        let bodyWidth = node.viewState.components['body'].w;
        let maxWidthAmongChildren = this.getMaxWidthOfChildren(node);
        node.children.forEach(function (child, index) {
            bodyHeight += child.viewState.bBox.h
                + DesignerDefaults.annotationAttributeDefinition.body.padding.bottom;

            if (maxWidthAmongChildren > child.viewState.bBox.w) {
                child.viewState.bBox.w = maxWidthAmongChildren
                    + DesignerDefaults.annotationAttributeDefinition.body.padding.bottom;
            }

            if (child.viewState.bBox.w > bodyWidth) {
                bodyWidth = child.viewState.bBox.w;
            }
        });

        if (node.viewState.collapsed) {
            node.viewState.components['body'].h = 0;
        } else {
            node.viewState.components['body'].h = bodyHeight;
        }
    }
}

export default AnnotationDefinitionDimensionCalculatorVisitor;
