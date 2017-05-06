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
import ASTFactory from './../../ast/ballerina-ast-factory';
import * as DesignerDefaults from './../../configs/designer-defaults';

class StructDefinitionPositionCalcVisitor {

    canVisit(node) {
        log.debug('can visit StructDefinitionPositionCalc');
        return true;
    }

    beginVisit(node) {

        let viewSate = node.getViewState();
        let bBox = viewSate.bBox;
        let parent = node.getParent();
        let panelChildren = parent.filterChildren(function (child) {
            return ASTFactory.isFunctionDefinition(child) ||
                ASTFactory.isServiceDefinition(child) ||
                ASTFactory.isConnectorDefinition(child) ||
                ASTFactory.isStructDefinition(child) ||
                ASTFactory.isAnnotationDefinition(child);
        });
        let heading = viewSate.components.heading;
        let body = viewSate.components.body;
        let currentStructIndex = _.findIndex(panelChildren, node);

        let x, y, headerX, headerY, bodyX, bodyY;
        if (currentStructIndex === 0) {
            headerX = DesignerDefaults.panel.wrapper.gutter.h;
            headerY = DesignerDefaults.panel.wrapper.gutter.v;
        } else if (currentStructIndex > 0) {
            let previousPanelBBox = panelChildren[currentStructIndex - 1].getViewState().bBox;
            headerX = DesignerDefaults.panel.wrapper.gutter.h;
            headerY = previousPanelBBox.y + previousPanelBBox.h + DesignerDefaults.panel.wrapper.gutter.v;
        } else {
            throw 'Invalid Index for Struct Definition';
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

        const { statements, contentOperations } = viewSate.components;

        contentOperations.x = bodyX + DesignerDefaults.structDefinition.padding.left;
        contentOperations.y = bodyY +  DesignerDefaults.structDefinition.padding.top;

        statements.forEach( (statement, i) => {
            const margin = DesignerDefaults.structDefinitionStatement.margin.bottom;
            statement.typeWrapper.x = contentOperations.x;
            statement.typeWrapper.y = contentOperations.y + DesignerDefaults.contentOperations.height +  i * (DesignerDefaults.structDefinitionStatement.height + margin);

            statement.typeText.x = statement.typeWrapper.x + DesignerDefaults.structDefinitionStatement.padding.left;
            statement.typeText.y = statement.typeWrapper.y + DesignerDefaults.structDefinitionStatement.height / 2;

            statement.identifierWrapper.x = statement.typeWrapper.x + statement.typeWrapper.w;
            statement.identifierWrapper.y = statement.typeWrapper.y;

            statement.identifierText.x = statement.identifierWrapper.x + DesignerDefaults.structDefinitionStatement.padding.left;
            statement.identifierText.y = statement.typeWrapper.y + DesignerDefaults.structDefinitionStatement.height / 2;

            statement.valueWrapper.x = statement.identifierWrapper.x + statement.identifierWrapper.w;
            statement.valueWrapper.y = statement.identifierWrapper.y;

            statement.valueText.x = statement.valueWrapper.x + DesignerDefaults.structDefinitionStatement.padding.left;
            statement.valueText.y = statement.identifierWrapper.y + DesignerDefaults.structDefinitionStatement.height / 2;

            statement.deleteButton.x = statement.typeWrapper.x + DesignerDefaults.structDefinitionStatement.width - DesignerDefaults.structDefinitionStatement.deleteButtonOffset;
            statement.deleteButton.y = statement.typeText.y;

        });
        log.debug('begin visit StructDefinitionPositionCalc');
    }

    visit(node) {
        log.debug('visit StructDefinitionPositionCalc');
    }

    endVisit(node) {
        log.debug('end visit StructDefinitionPositionCalc');
    }
}

export default StructDefinitionPositionCalcVisitor;
