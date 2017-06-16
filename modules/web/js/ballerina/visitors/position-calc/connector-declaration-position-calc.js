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

/**
 * Position visitor class for Connector Declaration.
 *
 * @class ConnectorDeclarationPositionCalcVisitor
 * */
class ConnectorDeclarationPositionCalcVisitor {

    /**
     * can visit the visitor.
     *
     * @return {boolean} true.
     *
     * @memberOf ConnectorDeclarationPositionCalcVisitor
     * */
    canVisit() {
        log.debug('can visit ConnectorDeclarationPositionCalcVisitor');
        return true;
    }

    /**
     * begin visiting the visitor.
     *
     * @param {ASTNode} node - Connector Declaration node.
     *
     * @memberOf ConnectorDeclarationPositionCalcVisitor
     * */
    beginVisit(node) {
        log.debug('begin visit ConnectorDeclarationPositionCalcVisitor');
        const viewState = node.getViewState();
        const bBox = viewState.bBox;
        const parent = node.getParent();
        const parentViewState = parent.getViewState();
        const workers = _.filter(parent.getChildren(), child => ASTFactory.isWorkerDeclaration(child));
        const connectors = _.filter(parent.getChildren(), child => ASTFactory.isConnectorDeclaration(child));
        const connectorIndex = _.findIndex(connectors, node);
        let x;

        if (ASTFactory.isServiceDefinition(parent) || ASTFactory.isConnectorDefinition(parent)) {
            x = this.positionPanelLevelConnectors(connectors, connectorIndex, parent);
        } else {
            x = this.positionInnerPanelLevelConnectors(connectors, connectorIndex, workers, parent);
        }

        const y = parentViewState.components.body.getTop() + DesignerDefaults.innerPanel.body.padding.top;

        bBox.x = x;
        bBox.y = y;

        viewState.components.statementContainer.x = x;
        viewState.components.statementContainer.y = y + DesignerDefaults.lifeLine.head.height;
    }

    /**
     * visit the visitor.
     *
     * @memberOf ConnectorDeclarationPositionCalcVisitor
     * */
    visit() {
        log.debug('visit ConnectorDeclarationPositionCalcVisitor');
    }

    /**
     * visit the visitor at the end.
     *
     * @memberOf ConnectorDeclarationPositionCalcVisitor
     * */
    endVisit() {
        log.debug('end visit ConnectorDeclarationPositionCalcVisitor');
    }

    /**
     * Position panel level connectors.
     *
     * @param {array} connectors - collection of connectors.
     * @param {number} connectorIndex - connector index.
     * @param {ASTNode} parentNode - parent node.
     *
     * @return {number} x position.
     *
     * @memberOf ConnectorDeclarationPositionCalcVisitor
     * */
    positionPanelLevelConnectors(connectors, connectorIndex, parentNode) {
        let xPosition;
        const innerPanelNodes = parentNode.filterChildren(child =>
            ASTFactory.isResourceDefinition(child) || ASTFactory.isConnectorAction(child));

        if (connectorIndex === 0) {
            if (innerPanelNodes.length === 0) {
                xPosition = parentNode.getViewState().bBox.getLeft() + DesignerDefaults.lifeLine.gutter.h;
            } else {
                xPosition = parentNode.getViewState().bBox.getLeft() + innerPanelNodes[0].getViewState().bBox.w +
                    DesignerDefaults.lifeLine.gutter.h + DesignerDefaults.panel.body.padding.left;
            }
        } else {
            xPosition = connectors[connectorIndex - 1].getViewState().components.statementContainer.getRight() +
                DesignerDefaults.lifeLine.gutter.h;
        }

        return xPosition;
    }

    /**
     * Position inner panel level connectors.
     *
     * @param {array} connectors - collection of connectors.
     * @param {number} connectorIndex - connector index.
     * @param {array} workers - collection of workers.
     * @param {ASTNode} parentNode - parent node.
     *
     * @return {number} - x position
     *
     * @memberOf ConnectorDeclarationPositionCalcVisitor
     * */
    positionInnerPanelLevelConnectors(connectors, connectorIndex, workers, parentNode) {
        const parentViewState = parentNode.getViewState();
        let xPosition;
        if (connectorIndex === 0) {
            if (workers.length > 0) {
                /**
                 * Due to the model order in ast, at the moment, workers and the parent's statement positioning have not
                 * calculated. Therefore we need to consider the widths of them to get the connector x position
                 */
                let totalWorkerStmtContainerWidth = 0;
                _.forEach(workers, (worker) => {
                    totalWorkerStmtContainerWidth += worker.getViewState().components.statementContainer.w;
                });
                xPosition = parentViewState.components.body.getLeft() + DesignerDefaults.lifeLine.gutter.h +
                    parentViewState.components.statementContainer.w + totalWorkerStmtContainerWidth +
                    (DesignerDefaults.lifeLine.gutter.h * (workers.length + 1));
            } else {
                xPosition = parentViewState.components.statementContainer.getRight() +
                    DesignerDefaults.lifeLine.gutter.h;
            }
        } else if (connectorIndex > 0) {
            const previousConnector = connectors[connectorIndex - 1];
            const previousStatementContainer = previousConnector.getViewState().components.statementContainer;
            xPosition = previousStatementContainer.getRight() + DesignerDefaults.innerPanel.body.padding.left;
        }

        return xPosition;
    }
}

export default ConnectorDeclarationPositionCalcVisitor;
