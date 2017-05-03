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
                ASTFactory.isStructDefinition(child);
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

        const { statements } = viewSate.components;
        statements.forEach( (statement, i) => {
            statement.y = DesignerDefaults.structDefinition.padding.top  + statement.h * i;
            statement.x = DesignerDefaults.structDefinition.padding.left;
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
