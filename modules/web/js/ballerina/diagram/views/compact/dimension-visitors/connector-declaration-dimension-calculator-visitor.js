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
import SimpleBBox from './../../../../ast/simple-bounding-box';
import AbstractVisitor from './../../../abstract-visitor';
import SizingUtil from './../sizing-util';

/**
 * Dimension visitor class for connector declaration.
 *
 * @class ConnectorDeclarationDimensionCalculatorVisitor
 * */
class ConnectorDeclarationDimensionCalculatorVisitor extends AbstractVisitor {

    /**
     * Constructor for Abort statement dimensions
     * @param {object} options - options
     */
    constructor(options) {
        super(options);
        this.designer = _.get(options, 'designer');
        this.sizingUtil = new SizingUtil(this.getOptions());
    }

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
        const statementViewState = {};
        const statementContainerWidthPadding = this.designer.statementContainer.padding.left +
            this.designer.statementContainer.padding.right;
        const textWidth = this.sizingUtil.getTextWidth(node.getConnectorVariable(),
            this.designer.lifeLine.width,
            this.designer.lifeLine.width);

        // Set the dimensions for the statement box for connector declaration
        statementViewState.components = {};
        statementViewState.components['drop-zone'] = new SimpleBBox();
        statementViewState.components['drop-zone'].h = this.designer.statement.gutter.v;
        statementViewState.bBox = new SimpleBBox();
        statementViewState.bBox.h = this.designer.statement.height + statementViewState.components['drop-zone'].h;
        statementViewState.bBox.w = textWidth.w;
        statementViewState.variableTextTrimmed = textWidth.text;

        viewState.variableTextWidth = textWidth.w;
        viewState.variableTextTrimmed = textWidth.text;

        const statementContainerWidth = (this.designer.statementContainer.width + statementContainerWidthPadding) >
        viewState.variableTextWidth
            ? (this.designer.statementContainer.width + statementContainerWidthPadding)
            : viewState.variableTextWidth;
        const statementContainerHeight = this.designer.statementContainer.height;

        viewState.bBox.h = statementContainerHeight + (this.designer.lifeLine.head.height * 2);
        viewState.bBox.w = statementContainerWidth;

        components.statementContainer.h = statementContainerHeight;
        components.statementContainer.w = statementContainerWidth;
        components.statementViewState = statementViewState;

        viewState.components = components;
    }
}

export default ConnectorDeclarationDimensionCalculatorVisitor;
