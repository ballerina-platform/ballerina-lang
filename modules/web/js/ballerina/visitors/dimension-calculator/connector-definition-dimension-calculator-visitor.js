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
import {util} from './../sizing-utils';

class ConnectorDefinitionDimensionCalculatorVisitor {

    canVisit(node) {
        return true;
    }

    beginVisit(node) {
    }

    visit(node) {
    }

    endVisit(node) {
        util.populateOuterPanelDecoratorBBox(node);

        let viewState = node.getViewState();

        const textWidth = util.getTextWidth(node.getConnectorName());
        viewState.titleWidth = textWidth.w;
        viewState.trimmedTitle = textWidth.text;

        //// Creating components for parameters of the connector definition
        // Creating component for opening bracket of the parameters view.
        viewState.components.openingParameter = {};
        viewState.components.openingParameter.w = util.getTextWidth('(', 0).w;

        // Creating component for closing bracket of the parameters view.
        viewState.components.closingParameter = {};
        viewState.components.closingParameter.w = util.getTextWidth(')', 0).w;

        let parameterWidths = this.connectorParameterWidths(node) +
            +viewState.components.closingParameter.w + viewState.components.openingParameter.w
            + viewState.titleWidth + 14
            + (DesignerDefaults.panel.wrapper.gutter.h * 2) + 200
            + DesignerDefaults.panel.body.padding.left + DesignerDefaults.panel.body.padding.right;

        let childrenWidth = this.maxConnectorActionWidth(node)
            + DesignerDefaults.panel.body.padding.left + DesignerDefaults.panel.body.padding.right;

        // Calculate connector definition full width
        viewState.bBox.w = childrenWidth > parameterWidths ? childrenWidth : parameterWidths;
    }

    /**
     * Calculate Connector Definition parameter text widths.
     * @param {ConnectorDefinition} node - Connector Definition Node.
     * @return {number} width - return sum of widths of parameter texts.
     * */
    connectorParameterWidths(node) {
        let width = 0;
        for (let i = 0; i < node.getArguments().length; i++) {
            width += util.getTextWidth(node.getArguments()[i].getParameterDefinitionAsString(), 0).w;
        }
        return width;
    }

    /**
     * Calculate Max width of Connector Actions.
     * @param {ConnectorDefinition} node - Connector Definition Node.
     * @return {number} width - Max width among all the connector actions.
     * */
    maxConnectorActionWidth(node) {
        let width = 0;
        for (let i = 0; i < node.getConnectorActionDefinitions().length; i++) {
            if (width < node.getConnectorActionDefinitions()[i].getViewState().bBox.w) {
                width = node.getConnectorActionDefinitions()[i].getViewState().bBox.w;
            }
        }
        return width;
    }
}

export default ConnectorDefinitionDimensionCalculatorVisitor;
