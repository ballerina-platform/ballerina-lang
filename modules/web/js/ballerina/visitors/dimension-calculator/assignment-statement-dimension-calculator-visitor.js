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
import { util } from './../sizing-utils';
import SimpleBBox from './../../ast/simple-bounding-box';
import * as DesignerDefaults from './../../configs/designer-defaults';

/**
 * Dimension visitor class for assignment statement.
 *
 * @class AssignmentStatementDimensionCalculatorVisitor
 * */
class AssignmentStatementDimensionCalculatorVisitor {

    /**
     * can visit the visitor.
     *
     * @return {boolean} true.
     *
     * @memberOf AssignmentStatementDimensionCalculatorVisitor
     * */
    canVisit() {
        return true;
    }

    /**
     * begin visiting the visitor.
     *
     * @memberOf AssignmentStatementDimensionCalculatorVisitor
     * */
    beginVisit() {
    }

    /**
     * visit the visitor.
     *
     * @memberOf AssignmentStatementDimensionCalculatorVisitor
     * */
    visit() {
    }

    /**
     * visit the visitor at the end.
     *
     * @param {ASTNode} node - Assignment statement node.
     *
     * @memberOf AssignmentStatementDimensionCalculatorVisitor
     * */
    endVisit(node) {
        util.populateSimpleStatementBBox(node.getStatementString(), node.getViewState());
        if (node.getFactory().isConnectorInitExpression(node.getChildren()[1])) {
            AssignmentStatementDimensionCalculatorVisitor.calculateConnectorDeclarationDimension(node);
        }
    }

    /**
     * Calculate the connector declaration dimension
     * @param {AssignmentStatement} node - assignment statement node
     */
    static calculateConnectorDeclarationDimension(node) {
        const viewState = node.getViewState();
        const connectorDeclViewState = {
            bBox: new SimpleBBox(),
            components: {},
        };
        const components = {};

        const variableReferenceList = [];

        _.forEach(node.getChildren()[0].getChildren(), (child) => {
            variableReferenceList.push(child.getExpressionString());
        });

        components.statementContainer = new SimpleBBox();
        const statementContainerWidthPadding = DesignerDefaults.statementContainer.padding.left +
            DesignerDefaults.statementContainer.padding.right;
        const textWidth = util.getTextWidth(_.join(variableReferenceList, ', '),
            DesignerDefaults.lifeLine.width,
            DesignerDefaults.lifeLine.width);

        connectorDeclViewState.variableTextWidth = textWidth.w;
        connectorDeclViewState.variableTextTrimmed = textWidth.text;

        const statementContainerWidth = (DesignerDefaults.statementContainer.width + statementContainerWidthPadding) >
        connectorDeclViewState.variableTextWidth
            ? (DesignerDefaults.statementContainer.width + statementContainerWidthPadding)
            : connectorDeclViewState.variableTextWidth;
        const statementContainerHeight = DesignerDefaults.statementContainer.height;

        connectorDeclViewState.bBox.h = statementContainerHeight + (DesignerDefaults.lifeLine.head.height * 2);
        connectorDeclViewState.bBox.w = statementContainerWidth;

        components.statementContainer.h = statementContainerHeight;
        components.statementContainer.w = statementContainerWidth;

        connectorDeclViewState.components = components;
        viewState.connectorDeclViewState = connectorDeclViewState;
    }
}

export default AssignmentStatementDimensionCalculatorVisitor;
