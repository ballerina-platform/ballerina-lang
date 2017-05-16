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
import * as PositioningUtils from './utils';

class ConnectorActionPositionCalcVisitor {

    canVisit(node) {
        log.debug('can visit ConnectorActionPositionCalcVisitor');
        return true;
    }

    beginVisit(node) {
        PositioningUtils.populateInnerPanelDecoratorBBoxPosition(node);
        let viewState = node.getViewState();
        //// Positioning parameters
        // Setting positions of function parameters.
        // Positioning the opening bracket component of the parameters.
        viewState.components.openingParameter.x = viewState.bBox.x + viewState.titleWidth
            + DesignerDefaults.panelHeading.iconSize.width + DesignerDefaults.panelHeading.iconSize.padding;
        viewState.components.openingParameter.y = viewState.bBox.y + viewState.components.annotation.h;

        // Positioning the resource parameters
        let nextXPositionOfParameter = viewState.components.openingParameter.x +
            viewState.components.openingParameter.w;
        if (node.getArguments().length > 0) {
            for (let i = 0; i < node.getArguments().length; i++) {
                let argument = node.getArguments()[i];
                nextXPositionOfParameter = this.createPositionForTitleNode(argument, nextXPositionOfParameter,
                    viewState.bBox.y + viewState.components.annotation.h);
            }
        }

        // Positioning the closing bracket component of the parameters.
        viewState.components.closingParameter.x = nextXPositionOfParameter + 110;
        viewState.components.closingParameter.y = viewState.bBox.y + viewState.components.annotation.h;

        //// Positioning return types
        // Setting positions of return types.
        // Positioning the Return type icon component.
        viewState.components.returnTypesIcon.x = viewState.components.closingParameter.x
            + viewState.components.closingParameter.w + 10;
        viewState.components.returnTypesIcon.y = viewState.bBox.y + viewState.components.annotation.h + 18;

        // Positioning the opening bracket component of the return types.
        viewState.components.openingReturnType.x = viewState.components.returnTypesIcon.x
            + viewState.components.returnTypesIcon.w;
        viewState.components.openingReturnType.y = viewState.bBox.y + viewState.components.annotation.h;

        // Positioning the parameters
        let nextXPositionOfReturnType = viewState.components.openingReturnType.x
            + viewState.components.openingReturnType.w;
        if (node.getReturnTypes().length > 0) {
            for (let i = 0; i < node.getReturnTypes().length; i++) {
                let returnType = node.getReturnTypes()[i];
                nextXPositionOfReturnType = this.createPositionForTitleNode(returnType, nextXPositionOfReturnType,
                    viewState.bBox.y + viewState.components.annotation.h);
            }
        }

        // Positioning the closing bracket component of the parameters.
        viewState.components.closingReturnType.x = nextXPositionOfReturnType + 110;
        viewState.components.closingReturnType.y = viewState.bBox.y + viewState.components.annotation.h;
        log.debug('begin visit ConnectorActionPositionCalcVisitor');
    }

    visit(node) {
        log.debug('visit ConnectorActionPositionCalcVisitor');
    }

    endVisit(node) {
        log.debug('end visit ConnectorActionPositionCalcVisitor');
    }

    /**
     * Sets positioning for a resource parameter.
     *
     * @param {object} parameter - The resource parameter node.
     * @param {number} x - The x position
     * @param {number} y - The y position
     * @returns The x position of the next parameter node.
     *
     * @memberof ConnectorActionPositionCalcVisitor
     */
    createPositionForTitleNode(parameter, x, y) {
        let viewState = parameter.getViewState();
        // Positioning the parameter
        viewState.bBox.x = x;
        viewState.bBox.y = y;

        // Positioning the delete icon
        viewState.components.deleteIcon.x = x + viewState.w;
        viewState.components.deleteIcon.y = y;

        return viewState.components.deleteIcon.x + viewState.components.deleteIcon.w;
    }
}

export default ConnectorActionPositionCalcVisitor;
