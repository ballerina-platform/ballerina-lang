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
import BallerinaASTFactory from './../../ast/ballerina-ast-factory';

class ConnectorDeclarationDimensionCalculatorVisitor {

    canVisit(node) {
        log.debug('can visit ConnectorDeclarationDimensionCalculatorVisitor');
        return true;
    }

    beginVisit(node) {
        log.debug('begin visit ConnectorDeclarationDimensionCalculatorVisitor');
    }

    visit(node) {
        log.debug('visit ConnectorDeclarationDimensionCalculatorVisitor');
    }

    endVisit(node) {
        log.debug('end visit ConnectorDeclarationDimensionCalculatorVisitor');
        var viewState = node.getViewState();
        var components = {};

        components['statementContainer'] = new SimpleBBox();
        var statementContainerWidth = DesignerDefaults.statementContainer.width;
        var statementContainerHeight = DesignerDefaults.statementContainer.height;

        viewState.bBox.h = statementContainerHeight + DesignerDefaults.lifeLine.head.height * 2;
        viewState.bBox.w = statementContainerWidth;

        components['statementContainer'].h = statementContainerHeight;
        components['statementContainer'].w = statementContainerWidth;

        viewState.components = components;
    }
}

export default ConnectorDeclarationDimensionCalculatorVisitor;