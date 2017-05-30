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
import {util} from './../sizing-utils';

class ConnectorActionDimensionCalculatorVisitor {

    canVisit(node) {
        return true;
    }

    beginVisit(node) {
    }

    visit(node) {
    }

    endVisit(node) {
        util.populatePanelDecoratorBBox(node, node.getActionName());
        let viewState = node.getViewState();

        //// Creating components for parameters of the connector action
        // Creating component for opening bracket of the parameters view.
        viewState.components.openingParameter = {};
        viewState.components.openingParameter.w = util.getTextWidth('(', 0).w;

        // Creating component for closing bracket of the parameters view.
        viewState.components.closingParameter = {};
        viewState.components.closingParameter.w = util.getTextWidth(')', 0).w;

        //// Creating components for return types of the function
        // Creating component for the Return type text.
        viewState.components.returnTypesIcon = {};
        viewState.components.returnTypesIcon.w = util.getTextWidth('returns', 0).w;

        // Creating component for opening bracket of the return types view.
        viewState.components.openingReturnType = {};
        viewState.components.openingReturnType.w = util.getTextWidth('(', 0).w;

        // Creating component for closing bracket of the return types view.
        viewState.components.closingReturnType = {};
        viewState.components.closingReturnType.w = util.getTextWidth(')', 0).w;

        // Calculate connector action definition full width
        viewState.bBox.w = this.parameterTypeWidth(node) + this.returnTypeWidth(node)
            + viewState.components.closingReturnType.w + viewState.components.openingReturnType.w
            + viewState.components.closingParameter.w + viewState.components.openingParameter.w
            + viewState.titleWidth + 14 + viewState.components.returnTypesIcon.w
            + (DesignerDefaults.panel.wrapper.gutter.h * 2) + 200
            + viewState.components['body'].w;
    }

    /**
     * Calculate Parameters' text width for Connector Action.
     * @param {ConnectorAction} node - Connector Action Node.
     * @return {number} width - return sum of widths of parameter texts.
     * */
    parameterTypeWidth(node) {
        let width = 0;
        if (node.getArguments().length > 0) {
            for (let i = 0; i < node.getArguments().length; i++) {
                width += util.getTextWidth(node.getArguments()[i].getParameterDefinitionAsString(), 0).w;
            }
        }

        return width;
    }

    /**
     * Calculate Return Parameters' text width for Connector Action.
     * @param {ConnectorAction} node - Connector Action Node.
     * @return {number} width - return sum of widths of parameter texts.
     * */
    returnTypeWidth(node) {
        let width = 0;
        if (node.getReturnTypes().length > 0) {
            for (let i = 0; i < node.getReturnTypes().length; i++) {
                width += util.getTextWidth(node.getReturnTypes()[i].getParameterDefinitionAsString(), 0).w;
            }
        }
        return width;
    }
}

export default ConnectorActionDimensionCalculatorVisitor;
