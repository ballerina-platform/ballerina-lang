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

class FunctionDefinitionPositionCalcVisitor {

    canVisitFunctionDefinitionPositionCalc(node) {
        log.debug('can visit FunctionDefinitionPositionCalc');
        return true;
    }

    beginVisitFunctionDefinitionPositionCalc(node) {
        let viewSate = node.getViewState();
        let bBox = viewSate.bBox;
        let parent = node.getParent();
        let panelChildren = parent.filterChildren(function (child) {
            return ASTFactory.isFunctionDefinition(child) ||
                ASTFactory.isServiceDefinition(child) || ASTFactory.isConnectorDefinition(child);
        });
        let heading = viewSate.components.heading;
        let body = viewSate.components.body;
        let currentFunctionIndex = _.findIndex(panelChildren, node);
        let statementContainer = viewSate.components.statementContainer;
        let defaultWorker = viewSate.components.defaultWorker;
        let x, y, headerX, headerY, bodyX, bodyY;
        if (currentFunctionIndex === 0) {
            headerX = DesignerDefaults.panel.wrapper.gutter.h;
            headerY = DesignerDefaults.panel.wrapper.gutter.v;
        } else if (currentFunctionIndex > 0) {
            let previousPanelBBox = panelChildren[currentFunctionIndex - 1].getViewState().bBox;
            headerX = DesignerDefaults.panel.wrapper.gutter.h;
            headerY = previousPanelBBox.y + previousPanelBBox.h + DesignerDefaults.panel.wrapper.gutter.v;
        } else {
            throw 'Invalid Index for Function Definition';
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

        statementContainer.x = bodyX + DesignerDefaults.innerPanel.body.padding.left;
        statementContainer.y = bodyY + DesignerDefaults.innerPanel.body.padding.top +
            DesignerDefaults.lifeLine.head.height;

        defaultWorker.x = statementContainer.x + (statementContainer.w - defaultWorker.w)/2;
        defaultWorker.y = statementContainer.y - DesignerDefaults.lifeLine.head.height;

        log.debug('begin visit FunctionDefinitionPositionCalc');
    }

    visitFunctionDefinitionPositionCalc(node) {
        log.debug('visit FunctionDefinitionPositionCalc');
    }

    endVisitFunctionDefinitionPositionCalc(node) {
        log.debug('end visit FunctionDefinitionPositionCalc');
    }
}

export default FunctionDefinitionPositionCalcVisitor;