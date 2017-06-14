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

class AnnotationAttributePositionCalcVisitor {
    canVisit(node) {
        log.debug('can visit AnnotationAttributePositionCalcVisitor');
        return true;
    }

    beginVisit(node) {
        const parent = node.getParent();
        const viewState = node.getViewState();
        const parentViewState = parent.getViewState();
        const parentBBox = parentViewState.bBox;
        const bBox = viewState.bBox;
        let x,
            y;

        const attributes = _.filter(parent.getChildren(), child => ASTFactory.isAnnotationAttributeDefinition(child));

        const currentAttributeIndex = _.findIndex(attributes, node);
        if (currentAttributeIndex === 0) {
            x = parentBBox.x + DesignerDefaults.panel.body.padding.left;
            y = parentBBox.y + DesignerDefaults.panel.heading.height
                + DesignerDefaults.panel.body.padding.top
                + DesignerDefaults.annotationAttributeDefinition.body.height
                + DesignerDefaults.annotationAttributeDefinition.body.padding.bottom
                + parentViewState.components.annotation.h;
        } else if (currentAttributeIndex > 0) {
            const previousAttributeBBox = attributes[currentAttributeIndex - 1].getViewState().bBox;
            x = parentBBox.x + DesignerDefaults.panel.body.padding.left;
            y = parentBBox.y
                + DesignerDefaults.annotationAttributeDefinition.body.padding.bottom
                + previousAttributeBBox.h
                + (previousAttributeBBox.y - parentBBox.y);
        }

        bBox.x = x;
        bBox.y = y;
    }

    visit(node) {
    }

    endVisit(node) {
    }
}

export default AnnotationAttributePositionCalcVisitor;
