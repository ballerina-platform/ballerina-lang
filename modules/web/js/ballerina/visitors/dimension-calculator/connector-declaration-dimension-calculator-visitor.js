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
import * as DesignerDefaults from './../../configs/designer-defaults';
import SimpleBBox from './../../ast/simple-bounding-box';
import { util } from './../sizing-utils';

/**
 * Dimension visitor class for connector declaration.
 *
 * @class ConnectorDeclarationDimensionCalculatorVisitor
 * */
class ConnectorDeclarationDimensionCalculatorVisitor {

    /**
     * can visit the visitor.
     *
     * @return {boolean} true.
     *
     * @memberOf ConnectorDeclarationDimensionCalculatorVisitor
     * */
    canVisit() {
        return true;
    }

    /**
     * begin visiting the visitor.
     *
     * @memberOf ConnectorDeclarationDimensionCalculatorVisitor
     * */
    beginVisit() {
    }

    /**
     * visit the visitor.
     *
     * @memberOf ConnectorDeclarationDimensionCalculatorVisitor
     * */
    visit() {
    }

    /**
     * visit the visitor at the end.
     *
     * @param {ASTNode} node - Connector declaration node.
     *
     * @memberOf ConnectorDeclarationDimensionCalculatorVisitor
     * */
    endVisit(node) {
        const viewState = node.getViewState();
        const components = {};

        components.statementContainer = new SimpleBBox();
        const statementContainerWidthPadding = DesignerDefaults.statementContainer.padding.left +
            DesignerDefaults.statementContainer.padding.right;
        const textWidth = util.getTextWidth(node.getConnectorVariable(),
            DesignerDefaults.lifeLine.width,
            DesignerDefaults.lifeLine.width);
        viewState.variableTextWidth = textWidth.w;
        viewState.variableTextTrimmed = textWidth.text;

        const statementContainerWidth = (DesignerDefaults.statementContainer.width + statementContainerWidthPadding) >
        viewState.variableTextWidth
            ? (DesignerDefaults.statementContainer.width + statementContainerWidthPadding)
            : viewState.variableTextWidth;
        const statementContainerHeight = DesignerDefaults.statementContainer.height;

        viewState.bBox.h = statementContainerHeight + (DesignerDefaults.lifeLine.head.height * 2);
        viewState.bBox.w = statementContainerWidth;

        components.statementContainer.h = statementContainerHeight;
        components.statementContainer.w = statementContainerWidth;

        viewState.components = components;
    }
}

export default ConnectorDeclarationDimensionCalculatorVisitor;
