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
import { util } from './../sizing-utils';

class StructDefinitionDimensionCalculatorVisitor {

    canVisit(node) {
        log.info('can visit StructDefinitionDimensionCalc');
        return true;
    }

    beginVisit(node) {
        log.info('begin visit StructDefinitionDimensionCalc');
    }

    visit(node) {
        log.info('visit StructDefinitionDimensionCalc');
    }

    _calculateChildrenHeight(children = []) {
        // return children.length * 20;
        const dimensions = children.map( () => {
            const childDimensions = new SimpleBBox();
            childDimensions.h = DesignerDefaults.structDefinitionStatement.height;
            childDimensions.w = DesignerDefaults.structDefinitionStatement.width;
            return childDimensions;
        });
        return dimensions;
    }

    endVisit(node) {
        var viewState = node.getViewState();

        var components = {};

        components['heading'] = new SimpleBBox();
        components['heading'].h = DesignerDefaults.panel.heading.height;

        components['body'] = new SimpleBBox();
        components['statements'] = this._calculateChildrenHeight(node.getChildren());
        if(node.viewState.collapsed) {
            components['body'].h = 0;
        } else {
            components['body'].h = components['statements'].reduce((a, b)=>{
                return {h: a.h + b.h};
            }, {h: 0}).h;

            components['body'].h = components['body'].h +
              DesignerDefaults.structDefinition.padding.top +
              DesignerDefaults.structDefinition.padding.bottom;

        }

        viewState.bBox.h = components['heading'].h + components['body'].h;
        viewState.bBox.w = components['heading'].w + components['body'].w;

        viewState.components = components;
    }
}

export default StructDefinitionDimensionCalculatorVisitor;
