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
import _ from 'lodash';
import * as DesignerDefaults from './../../configs/designer-defaults';
import ASTFactory from './../../ast/ballerina-ast-factory';
import {panel} from './../../configs/designer-defaults';
import {util} from './../sizing-utils';

class AnnotationDefinitionPositionCalcVisitor {
    /**
     * Check whether annotation definition position calc visitor can be visited.
     * @param {object} node.
     * @return {boolean}
     * */
    canVisit(node) {
        return true;
    }

    beginVisit(node) {
        let viewState = node.getViewState();
        let bBox = viewState.bBox;
        let parent = node.getParent();
        let panelChildren = parent.filterChildren(function (child) {
            return ASTFactory.isFunctionDefinition(child) ||
                ASTFactory.isServiceDefinition(child) ||
                ASTFactory.isConnectorDefinition(child) ||
                ASTFactory.isAnnotationDefinition(child) ||
                ASTFactory.isStructDefinition(child) ||
                ASTFactory.isPackageDefinition(child);
        });

        let heading = viewState.components.heading;
        let body = viewState.components.body;
        let currentAnnotationIndex = _.findIndex(panelChildren, node);
        let x, y, headerX, headerY, bodyX, bodyY;
        if (currentAnnotationIndex === 0) {
            headerX = DesignerDefaults.panel.wrapper.gutter.h;
            headerY = DesignerDefaults.panel.wrapper.gutter.v;
        } else if (currentAnnotationIndex > 0) {
            let previousServiceBBox = panelChildren[currentAnnotationIndex - 1].getViewState().bBox;
            headerY = previousServiceBBox.y + previousServiceBBox.h + DesignerDefaults.panel.wrapper.gutter.v;
            headerX = DesignerDefaults.panel.wrapper.gutter.h;
        } else {
            throw 'Invalid Index for Annotation Definition';
        }

        x = headerX;
        y = headerY;
        bodyX = headerX;
        bodyY = headerY + heading.h;

        bBox.x = x;
        bBox.y = y;
        heading.x = headerX;
        heading.y = headerY;
        body.x = bodyX;
        body.y = bodyY;

        /// Positioning parameters
        // Setting positions of function parameters.
        // Positioning the opening bracket component of the parameters.
        viewState.components.openingParameter.x = viewState.bBox.x + viewState.titleWidth
            + DesignerDefaults.panelHeading.iconSize.width + DesignerDefaults.panelHeading.iconSize.padding;
        viewState.components.openingParameter.y = viewState.bBox.y;

        viewState.attachments = {};
        // Positioning the resource parameters
        let nextXPositionOfParameter = viewState.components.openingParameter.x
            + viewState.components.openingParameter.w;
        if (node.getAttachmentPoints().length > 0) {
            for (let i = 0; i < node.getAttachmentPoints().length; i++) {
                let attachment = {
                    attachment: node.getAttachmentPoints()[i],
                    model: node
                };
                nextXPositionOfParameter = this.createPositionForTitleNode(attachment, nextXPositionOfParameter,
                    viewState.bBox.y);
            }
        }

        // Positioning the closing bracket component of the parameters.
        viewState.components.closingParameter.x = nextXPositionOfParameter + 110;
        viewState.components.closingParameter.y = viewState.bBox.y;
    }

    visit(node) {
        log.debug('visit AnnotationPositionCalc');
    }

    endVisit(node) {
        log.debug('end visit AnnotationPositionCalc');
    }

    /**
     * Sets positioning for a annotation attachment.
     *
     * @param {object} attachment - attachment point value.
     * @param {number} x  - The x position
     * @param {number} y  - The y position
     * @returns The x position of the next parameter node.
     *
     * @memberof AnnotationDefinitionPositionCalc
     */
    createPositionForTitleNode(attachment, x, y) {
        let viewState = attachment.model.getViewState();

        let childViewState = {
            viewState: {
                bBox: {
                    x: x,
                    y: y,
                    w: util.getTextWidth(attachment.attachment, 0).w,
                    h: DesignerDefaults.panelHeading.heading.height - 7
                },
                components: {
                    deleteIcon: {
                        x: x + util.getTextWidth(attachment.attachment, 0).w,
                        y: y,
                        w: DesignerDefaults.panelHeading.heading.height - 7,
                        h: DesignerDefaults.panelHeading.heading.height - 7
                    }
                },
                w: util.getTextWidth(attachment.attachment, 0).w,
                h: DesignerDefaults.panelHeading.heading.height - 7
            }
        };
        viewState.attachments[attachment.attachment] = childViewState;

        return (x + util.getTextWidth(attachment.attachment, 0).w) + childViewState.viewState.components.deleteIcon.w;
    }
}

export default AnnotationDefinitionPositionCalcVisitor;