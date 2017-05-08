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
import { util } from './../sizing-utils';

class StructDefinitionDimensionCalculatorVisitor {

    canVisit(node) {
        log.info('can visit StructDefinitionDimensionCalc');
        return true;
    }

    beginVisit(node) {
        log.info('begin visit StructDefinitionDimensionCalc');
    }

    visit(node) {
        log.info('visit StructDefinitionDimensionCalc');
    }

    _calculateChildrenDimensions(children = [], components) {
        const dimensions = children.map( () => {
            components.body.h += DesignerDefaults.structDefinitionStatement.height;

            const childDimensions = {};
            childDimensions.typeWrapper = new SimpleBBox();
            childDimensions.typeWrapper.h = DesignerDefaults.structDefinitionStatement.height;
            childDimensions.typeWrapper.w = DesignerDefaults.structDefinitionStatement.width / 3;
            childDimensions.typeText = new SimpleBBox();
            childDimensions.typeText.h = DesignerDefaults.structDefinitionStatement.height;
            childDimensions.typeText.w = DesignerDefaults.structDefinitionStatement.width / 3;

            childDimensions.identifierWrapper = new SimpleBBox();
            childDimensions.identifierWrapper.h = DesignerDefaults.structDefinitionStatement.height;
            childDimensions.identifierWrapper.w = DesignerDefaults.structDefinitionStatement.width / 3;
            childDimensions.identifierText = new SimpleBBox();
            childDimensions.identifierText.h = DesignerDefaults.structDefinitionStatement.height;
            childDimensions.identifierText.w = DesignerDefaults.structDefinitionStatement.width / 3;

            childDimensions.valueWrapper = new SimpleBBox();
            childDimensions.valueWrapper.h = DesignerDefaults.structDefinitionStatement.height;
            childDimensions.valueWrapper.w = DesignerDefaults.structDefinitionStatement.width / 3;
            childDimensions.valueText = new SimpleBBox();
            childDimensions.valueText.h = DesignerDefaults.structDefinitionStatement.height;
            childDimensions.valueText.w = DesignerDefaults.structDefinitionStatement.width / 3;

            childDimensions.deleteButton = new SimpleBBox();

            return childDimensions;
        });
        return dimensions;
    }

    endVisit(node) {
        var viewState = node.getViewState();

        var components = {};

        components.heading = new SimpleBBox();
        components.heading.h = DesignerDefaults.panel.heading.height;

        components.body = new SimpleBBox();
        components.statements = this._calculateChildrenDimensions(node.getChildren(), components);
        components.contentOperations = new SimpleBBox();
        components.contentOperations.w = DesignerDefaults.contentOperations.width + 1;
        components.contentOperations.h = DesignerDefaults.contentOperations.height;

        if(node.viewState.collapsed) {
            components.body.h = 0;
        } else {
            components.body.h += DesignerDefaults.structDefinition.padding.top +
              DesignerDefaults.structDefinition.padding.bottom + DesignerDefaults.contentOperations.height;
        }

        viewState.bBox.h = components.heading.h + components.body.h;
        viewState.components = components;
    }
}

export default StructDefinitionDimensionCalculatorVisitor;
