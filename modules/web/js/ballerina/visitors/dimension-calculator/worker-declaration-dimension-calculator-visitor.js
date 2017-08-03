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
import _ from 'lodash';
import * as DesignerDefaults from './../../configs/designer-defaults';
import SimpleBBox from './../../ast/simple-bounding-box';
import BallerinaASTFactory from './../../ast/ballerina-ast-factory';

/**
 * Dimension visitor class for Worker Declaration.
 *
 * @class WorkerDeclarationDimensionCalculatorVisitor
 * */
class WorkerDeclarationDimensionCalculatorVisitor {

    /**
     * can visit the visitor.
     *
     * @return {boolean} true.
     *
     * @memberOf WorkerDeclarationDimensionCalculatorVisitor
     * */
    canVisit() {
        return true;
    }

    /**
     * begin visiting the visitor.
     *
     * @memberOf WorkerDeclarationDimensionCalculatorVisitor
     * */
    beginVisit() {
    }

    /**
     * visit the visitor.
     *
     * @memberOf WorkerDeclarationDimensionCalculatorVisitor
     * */
    visit() {
    }

    /**
     * visit the visitor at the end.
     *
     * @param {ASTNode} node - Worker Declaration node.
     *
     * @memberOf WorkerDeclarationDimensionCalculatorVisitor
     * */
    endVisit(node) {
        const viewState = node.getViewState();
        const components = viewState.components;
        components.statementContainer = new SimpleBBox();
        const statementChildren = node.filterChildren(BallerinaASTFactory.isStatement);
        const statementContainerWidthPadding = DesignerDefaults.statementContainer.padding.left +
            DesignerDefaults.statementContainer.padding.right;
        let statementWidth = DesignerDefaults.lifeLine.width + statementContainerWidthPadding;
        let statementHeight = 0;
        const connectorDeclarationChildren = node.filterChildren(BallerinaASTFactory.isConnectorDeclaration);
        let widthExpansion = 0;
        let statementContainerWidthExpansion = 0;
        // Iterate over statement children
        _.forEach(statementChildren, (child) => {
            statementHeight += child.viewState.bBox.h;
            if ((child.viewState.bBox.w + statementContainerWidthPadding) > statementWidth) {
                statementWidth = child.viewState.bBox.w + statementContainerWidthPadding;
            }
            if (child.viewState.widthExpansion > statementContainerWidthExpansion) {
                statementContainerWidthExpansion = child.viewState.widthExpansion;
                widthExpansion = child.viewState.widthExpansion;
            }
        });
        // Iterate over connector declaration children
        _.forEach(connectorDeclarationChildren, (child) => {
            statementHeight += DesignerDefaults.statement.height + DesignerDefaults.statement.gutter.v;
            widthExpansion += (child.viewState.bBox.w + DesignerDefaults.blockStatement.heading.width);
            if (child.viewState.components.statementViewState.bBox.w > statementWidth) {
                statementWidth = child.viewState.components.statementViewState.bBox.w;
            }
        });
        viewState.heightExpansion = statementHeight;
        // Iterating to set the height of the connector
        if (connectorDeclarationChildren.length > 0) {
            if (statementHeight > (DesignerDefaults.statementContainer.height)) {
                _.forEach(connectorDeclarationChildren, (child) => {
                    child.viewState.components.statementContainer.h = statementHeight - (
                        DesignerDefaults.lifeLine.head.height + DesignerDefaults.lifeLine.footer.height +
                        (DesignerDefaults.variablesPane.leftRightPadding * 2)) -
                        DesignerDefaults.statement.padding.bottom;
                });
            } else {
                statementHeight = DesignerDefaults.statementContainer.height + (
                    DesignerDefaults.lifeLine.head.height + DesignerDefaults.lifeLine.footer.height +
                    (DesignerDefaults.variablesPane.leftRightPadding * 2)) + DesignerDefaults.statement.padding.bottom;
            }
        }

        /**
         * We add an extra gap to the statement container height, in order to maintain the gap between the
         * last statement's bottom margin and the default worker bottom rect's top margin
         */
        statementHeight += DesignerDefaults.statement.gutter.v * 2;

        viewState.bBox.h = statementHeight + (DesignerDefaults.lifeLine.head.height * 2);
        viewState.bBox.w = statementWidth;

        components.statementContainer.h = statementHeight;
        components.statementContainer.w = statementWidth;
        viewState.widthExpansion = widthExpansion;
        components.statementContainerWidthExpansion = statementContainerWidthExpansion;
    }
}

export default WorkerDeclarationDimensionCalculatorVisitor;
