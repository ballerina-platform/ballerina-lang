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
        var components = {};

        //header component
        components['heading'] = new SimpleBBox();
        components['heading'].h = DesignerDefaults.panel.heading.height;

        //all statements
        components['statement'] = new SimpleBBox();
        var statementMaxWidth = DesignerDefaults.statement.width;
        var statementTotalHeight = DesignerDefaults.statement.height + DesignerDefaults.statement.gutter.v;

        _.forEach(node.filterChildren(BallerinaASTFactory.isStatement), function(child) {
            statementTotalHeight += child.viewState.bBox.h;
            if(child.viewState.bBox.w > statementMaxWidth){
                statementMaxWidth = child.viewState.bBox.w;
            }
        });
        
        components['statement'].h = statementTotalHeight;
        components['statement'].w = statementMaxWidth;

        components['life-line'] = new SimpleBBox();
        components['life-line'].h = components['statement'].h + DesignerDefaults.statement.gutter.v;
        components['life-line'].w = 0;

        //default worker
        components['default-worker'] = new SimpleBBox();
        components['default-worker'].h = (2 * DesignerDefaults.statement.height) + components['life-line'].h;
        components['default-worker'].w = components['statement'].w;

        var workersTotalWidth = components['default-worker'].w;
        var workersMaxHeight = components['default-worker'].h;

        _.forEach(node.filterChildren(BallerinaASTFactory.isWorkerDeclaration), function(child) {
            workersTotalWidth += child.viewState.bBox.w;
            if(child.viewState.bBox.h > workersMaxHeight){
                workersMaxHeight = child.viewState.bBox.w;
            }
        });

        components['body'] = new SimpleBBox();
        components['body'].h = ((DesignerDefaults.panel.body.height < components['life-line'].h)? components['life-line'].h:DesignerDefaults.panel.body.height)
                               + components['default-worker'].h + DesignerDefaults.panel.body.padding.top + DesignerDefaults.panel.body.padding.bottom;
        components['body'].w = components['default-worker'].w + workersTotalWidth + DesignerDefaults.panel.body.padding.right + DesignerDefaults.panel.body.padding.left;

        viewState.bBox.h = components['heading'].h + components['body'].h;
        viewState.bBox.w = components['heading'].w + components['body'].w;

        viewState.components = components;
        
        log.info('end visit FunctionDefinitionDimensionCalc');
    }
}

export default FunctionDefinitionDimensionCalculatorVisitor;
