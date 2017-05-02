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

class ConnectorDeclarationPositionCalcVisitor {

    canVisit(node) {
        log.debug('can visit ConnectorDeclarationPositionCalcVisitor');
        return true;
    }

    beginVisit(node) {
        log.debug('begin visit ConnectorDeclarationPositionCalcVisitor');
        let viewState = node.getViewState();
        let bBox = viewState.bBox;
        let parent = node.getParent();
        let parentViewState = parent.getViewState();
        let workers = _.filter(parent.getChildren(), function (child) {
            return ASTFactory.isWorkerDeclaration(child);
        });
        let connectors = _.filter(parent.getChildren(), function (child) {
            return ASTFactory.isConnectorDeclaration(child);
        });
        let connectorIndex = _.findIndex(connectors, node);
        let x, y;

        /**
         * Always the first connector should place after the last worker or the default worker
         */
        if (connectorIndex === 0) {

            if (workers.length > 0) {
                x = workers[workers.length - 1].getViewState().components.statementContainer.getRight() +
                    DesignerDefaults.lifeLine.gutter.h;
            } else {
                x = parentViewState.components.statementContainer.getRight() +
                    DesignerDefaults.lifeLine.gutter.h;
            }
        } else if (connectorIndex > 0) {
            const previousConnector = workers[connectorIndex - 1];
            const previousStatementContainer = previousConnector.getViewState().components.statementContainer;
            x = previousStatementContainer.getRight() + DesignerDefaults.innerPanel.body.padding.left;
        } else {
            throw "Invalid index found for Connector Declaration";
        }

        y = parentViewState.components.body.getTop() + DesignerDefaults.innerPanel.body.padding.top;

        bBox.x = x;
        bBox.y = y;

        viewState.components.statementContainer.x = x;
        viewState.components.statementContainer.y = y + DesignerDefaults.lifeLine.head.height;
    }

    visit(node) {
        log.debug('visit ConnectorDeclarationPositionCalcVisitor');
    }

    endVisit(node) {
        log.debug('end visit ConnectorDeclarationPositionCalcVisitor');
    }
}

export default ConnectorDeclarationPositionCalcVisitor;
