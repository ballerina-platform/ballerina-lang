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
import SimpleBBox from './../../ast/simple-bounding-box';

class FunctionDefinitionDimensionCalcVisitor {

    canVisitFunctionDefinitionDimensionCalc(node) {
        log.info('can visit FunctionDefinitionDimensionCalc');
        return true;
    }

    beginVisitFunctionDefinitionDimensionCalc(node) {
        log.info('begin visit FunctionDefinitionDimensionCalc');
    }

    visitFunctionDefinitionDimensionCalc(node) {
        log.info('visit FunctionDefinitionDimensionCalc');
    }

    endVisitFunctionDefinitionDimensionCalc(node) {
        var viewState = node.getViewState();
        var width = DesignerDefaults.functionDefinitionDimensions.width;
        var components = {};
        components['heading'] = new SimpleBBox();
        components['heading'].w = width;
        components['heading'].h = DesignerDefaults.functionDefinitionDimensions.panelHeading.height;

        components['body'] = new SimpleBBox();
        components['body'].w = width;
        components['body'].h = DesignerDefaults.functionDefinitionDimensions.panelBody.height;

        viewState.components = components;

        var height = 0;
        height = _.forEach(viewState.components, function(component) { 
            return height + component.h;
        });
        viewState.bBox.w = DesignerDefaults.functionDefinitionDimensions.width;
        viewState.bBox.h = height;

        log.info('end visit FunctionDefinitionDimensionCalc');
    }
}

export default FunctionDefinitionDimensionCalcVisitor;