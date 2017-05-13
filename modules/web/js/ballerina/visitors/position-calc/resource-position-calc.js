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
import * as DesignerDefaults from './../../configs/designer-defaults';
import AST from './../../ast/module';
import ASTFactory from './../../ast/ballerina-ast-factory';
import * as PositioningUtils from './utils';
import { util } from './../sizing-utils';

class ResourceDefinitionPositionCalcVisitor {

    canVisit(node) {
        log.debug('can visit ResourceDefinitionPositionCalc');
        return true;
    }

    beginVisit(node) {
        PositioningUtils.populateInnerPanelDecoratorBBoxPosition(node);

        //// Positioning parameters(resource parameters)
        // Setting positions of resource parameters.
        let viewState = node.getViewState();

        // Positioning the openning bracket component of the parameters.
        viewState.components.openingParameter.x = viewState.bBox.x + viewState.titleWidth;
        viewState.components.openingParameter.y = viewState.bBox.y + viewState.components.annotation.h;

        // Positioning the Parameters text component.
        viewState.components.parametersText.x = viewState.components.openingParameter.x + viewState.components.openingParameter.w;
        viewState.components.parametersText.y = viewState.bBox.y + viewState.components.annotation.h;

        // Positioning the resource parameters
        let nextXPositionOfParameter = viewState.components.parametersText.x + viewState.components.parametersText.w;
        if (node.getArgumentParameterDefinitionHolder().getChildren().length > 0) {
            for (let i = 0; i < node.getArgumentParameterDefinitionHolder().getChildren().length; i++) {
                let resourceParameter = node.getArgumentParameterDefinitionHolder().getChildren()[i];
                nextXPositionOfParameter = this.createPositioningForParameter(resourceParameter, nextXPositionOfParameter, viewState.bBox.y + viewState.components.annotation.h);
            }
        }

        // Positioning the closing brack component of the parameters.
        viewState.components.closingParameter.x = nextXPositionOfParameter;
        viewState.components.closingParameter.y = viewState.bBox.y + viewState.components.annotation.h;
    }

    visit(node) {
    }

    endVisit(node) {
    }

    /**
     * Sets positioning for a resource parameter.
     * 
     * @param {ResourceParameter} parameter The resource parameter node.
     * @param {number} x The x position
     * @param {number} y The y position
     * @returns The x position of the next parameter node.
     * 
     * @memberof ResourceDefinitionPositionCalcVisitor
     */
    createPositioningForParameter(parameter, x, y) {
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

export default ResourceDefinitionPositionCalcVisitor;
