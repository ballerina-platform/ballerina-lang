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
        const viewState = node.getViewState();
        const bBox = viewState.bBox;
        const parent = node.getParent();
        const parentViewState = parent.getViewState();
        const workers = _.filter(parent.getChildren(), child => ASTFactory.isWorkerDeclaration(child));
        const connectors = _.filter(parent.getChildren(),
            child => ASTFactory.isConnectorDeclaration(child)
            || (ASTFactory.isAssignmentStatement(child)
            && ASTFactory.isConnectorInitExpression(child.getChildren()[1])));
        const connectorIndex = _.findIndex(connectors, node);
        let x;

        if (ASTFactory.isServiceDefinition(parent) || ASTFactory.isConnectorDefinition(parent)) {
            x = this.positionPanelLevelConnectors(connectors, connectorIndex, parent);
        } else {
            x = this.positionInnerPanelLevelConnectors(connectors, connectorIndex, workers, parent);
        }
        let y = parentViewState.components.body.getTop() + DesignerDefaults.innerPanel.body.padding.top;

        if (parentViewState.components.variablesPane) {
            y += (parentViewState.components.variablesPane.h + DesignerDefaults.panel.body.padding.top);
        }

        bBox.x = x;
        bBox.y = y;

        viewState.components.statementContainer.x = x;
        viewState.components.statementContainer.y = y + DesignerDefaults.lifeLine.head.height;

        // Calculate the position of the statement box which denotes the position of the connector declaration
        const parentStatementContainer = parentViewState.components.statementContainer || {};
        const parentStatements = parent.filterChildren(child =>
        ASTFactory.isStatement(child) || ASTFactory.isExpression(child) || ASTFactory.isConnectorDeclaration(child));
        const currentIndex = _.findIndex(parentStatements, node);
        let statementBoxY;

        /**
         * Here we center the statement box based on the parent's statement container's dimensions
         * Always the statement container's width should be greater than the statements/expressions
         */
        if (parentStatementContainer.w < bBox.w) {
            const exception = {
                message: 'Invalid statement container width found, statement width should be ' +
                'greater than or equal to statement/ statement width ',
            };
            throw exception;
        }
        const statementBBox = viewState.components.statementViewState.bBox;
        const statementBoxX = parentStatementContainer.x + ((parentStatementContainer.w - statementBBox.w) / 2);
        if (currentIndex === 0) {
            statementBoxY = parentStatementContainer.y;
        } else if (currentIndex > 0) {
            const previousChild = parentStatements[currentIndex - 1];
            if (ASTFactory.isConnectorDeclaration(previousChild)) {
                statementBoxY = previousChild.getViewState().components.statementViewState.bBox.getBottom();
            } else {
                statementBoxY = previousChild.getViewState().bBox.getBottom();
            }
        } else {
            const exception = {
                message: `Invalid Index found for ${node.getType()}`,
            };
            throw exception;
        }

        viewState.components.statementViewState.bBox.x = statementBoxX;
        viewState.components.statementViewState.bBox.y = statementBoxY;
    }

    /**
     * visit the visitor.
     *
     * @memberOf ConnectorDeclarationPositionCalcVisitor
     * */
    visit() {
    }

    /**
     * visit the visitor at the end.
     *
     * @memberOf ConnectorDeclarationPositionCalcVisitor
     * */
    endVisit() {
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
                xPosition = parentNode.getViewState().bBox.getLeft() + DesignerDefaults.panel.body.padding.left;
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
            const previousConViewState = ASTFactory.isAssignmentStatement(previousConnector)
                ? previousConnector.getViewState().connectorDeclViewState : previousConnector.getViewState();
            const previousStatementContainer = previousConViewState.components.statementContainer;
            xPosition = previousStatementContainer.getRight() + DesignerDefaults.innerPanel.body.padding.left;
        }

        return xPosition;
    }
}

export default ConnectorDeclarationPositionCalcVisitor;
