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
import AST from './../../ast/module';
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
        let services = _.filter(parent.getChildren(), function (child) {
            return child instanceof AST.FunctionDefinition;
        });
        let headerBBox = viewSate.components.header;
        let bodyBBox = viewSate.components.body;
        let currentServiceIndex = _.findIndex(services, node);
        let x, y, headerX, headerY, bodyX, bodyY;
        if (currentServiceIndex === 0) {
            headerX = DesignerDefaults.panel.wrapper.gutter.h;
            headerY = DesignerDefaults.panel.wrapper.gutter.v;
        } else if (currentServiceIndex > 0) {
            let previousServiceBBox = services[currentServiceIndex - 1].getViewState().bBox;
            headerX = DesignerDefaults.panel.wrapper.gutter.h;
            headerY = previousServiceBBox.y() + previousServiceBBox.h() + DesignerDefaults.panel.wrapper.gutter.v;
        } else {
            throw 'Invalid Index for Function Definition';
        }

        x = headerX;
        y = headerY;
        bodyX = headerX;
        bodyY = headerY + headerBBox.h();

        bBox.x(x).y(y);
        headerBBox.x(headerX).y(headerY);
        bodyBBox.x(bodyX).y(bodyY);

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