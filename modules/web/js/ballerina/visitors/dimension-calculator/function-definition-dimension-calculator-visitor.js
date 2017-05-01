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
        /**
         * @type {{heading: SimpleBBox, body: SimpleBBox, statementContainer: SimpleBBox, defaultWorker: SimpleBBox}}
         */
        var components = {};

        components['heading'] = new SimpleBBox();
        components['heading'].h = DesignerDefaults.panel.heading.height;

        components['statementContainer'] = new SimpleBBox();
        components['defaultWorker'] = new SimpleBBox();
        var statementChildren = node.filterChildren(BallerinaASTFactory.isStatement);
        var statementWidth = DesignerDefaults.statementContainer.width;
        var statementHeight = DesignerDefaults.statementContainer.height;

        _.forEach(statementChildren, function(child) {
            statementHeight += child.viewState.bBox.h + DesignerDefaults.statement.gutter.v;
            if(child.viewState.bBox.w > statementWidth){
                statementWidth = child.viewState.bBox.w;
            }
        });

        /**
         * We add an extra gap to the statement container height, in order to maintain the gap between the
         * last statement's bottom margin and the default worker bottom rect's top margin
         */
        statementHeight += DesignerDefaults.statement.gutter.v;

        components['statementContainer'].h = statementHeight;
        components['statementContainer'].w = statementWidth;

        components['defaultWorker'].w = DesignerDefaults.lifeLine.width;
        components['defaultWorker'].h = DesignerDefaults.lifeLine.head.height +
            DesignerDefaults.lifeLine.footer.height + statementHeight;

        components['body'] = new SimpleBBox();
        components['body'].h = ((DesignerDefaults.panel.body.height < components['statementContainer'].h)?
                components['statementContainer'].h:DesignerDefaults.panel.body.height)
                               + DesignerDefaults.panel.body.padding.top + DesignerDefaults.panel.body.padding.bottom;

        components['body'].w = components['statementContainer'].w +
            DesignerDefaults.panel.body.padding.right + DesignerDefaults.panel.body.padding.left;

        viewState.bBox.h = components['heading'].h + components['body'].h;
        viewState.bBox.w = components['heading'].w + components['body'].w;

        viewState.components = components;
        
        log.info('end visit FunctionDefinitionDimensionCalc');
    }
}

export default FunctionDefinitionDimensionCalculatorVisitor;
