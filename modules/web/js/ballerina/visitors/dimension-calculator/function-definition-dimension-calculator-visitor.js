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
import BallerinaASTFactory from './../../ast/ballerina-ast-factory';

class FunctionDefinitionDimensionCalculatorVisitor {

    canVisit(node) {
        log.info('can visit FunctionDefinitionDimensionCalc');
        return true;
    }

    beginVisit(node) {
        log.info('begin visit FunctionDefinitionDimensionCalc');
    }

    visit(node) {
        log.info('visit FunctionDefinitionDimensionCalc');
    }

    endVisit(node) {
        var viewState = node.getViewState();
        var width = DesignerDefaults.functionDefinitionDimensions.width;
        var components = {};

        //--- components deciding height of the function definition bbox
        components['heading'] = new SimpleBBox();
        components['heading'].w = width;
        components['heading'].h = DesignerDefaults.functionDefinitionDimensions.panelHeading.height;

        components['body'] = new SimpleBBox();
        components['body'].w = width;
        components['body'].h = DesignerDefaults.functionDefinitionDimensions.panelBody.height;

        var height = 0;
        _.forEach(components, function(component) {
            height = height + component.h;
        });

        viewState.bBox.w = DesignerDefaults.functionDefinitionDimensions.width;
        viewState.bBox.h = height;

        //--- other components with no impact to function definition bbox
        components['statementBlock'] = new SimpleBBox();
        var statementChildren = node.filterChildren(BallerinaASTFactory.isStatement);
        var statementBlockWidth = 0;
        var statementBlockHeight = 0;

        _.forEach(statementChildren, function(child) {
            statementBlockHeight = statementBlockHeight + child.viewState.h;
            if(child.viewState.w > statementBlockWidth){
                statementBlockWidth = child.viewState.w;
            }
        });
        components['statementBlock'].h = statementBlockHeight;
        components['statementBlock'].w = statementBlockWidth;

        viewState.components = components;
        log.info('end visit FunctionDefinitionDimensionCalc');
    }
}

export default FunctionDefinitionDimensionCalculatorVisitor;
