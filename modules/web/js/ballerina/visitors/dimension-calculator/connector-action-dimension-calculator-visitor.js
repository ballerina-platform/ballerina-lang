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

class ConnectorActionDimensionCalculatorVisitor {

    canVisit(node) {
        log.debug('can visit ResourceDefinitionDimensionCalc');
        return true;
    }

    beginVisit(node) {
        log.debug('begin visit ResourceDefinitionDimensionCalc');
    }

    visit(node) {
        log.debug('visit ResourceDefinitionDimensionCalc');
    }

    endVisit(node) {
        var viewState = node.getViewState();
        var components = {};

        components['heading'] = new SimpleBBox();
        components['heading'].h = DesignerDefaults.panel.heading.height;

        components['statementContainer'] = new SimpleBBox();
        var statementChildren = node.filterChildren(BallerinaASTFactory.isStatement);
        var statementWidth = 0;
        var statementHeight = 0;

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

        components['body'] = new SimpleBBox();

        const workerLifeLineHeight = components['statementContainer'].h + DesignerDefaults.lifeLine.head.height * 2;
        if(node.viewState.collapsed) {
            components['body'].h = 0;
        } else {
            components['body'].h = ((DesignerDefaults.panel.body.height < workerLifeLineHeight)? workerLifeLineHeight:DesignerDefaults.panel.body.height)
                               + DesignerDefaults.panel.body.padding.top + DesignerDefaults.panel.body.padding.bottom;
        }
        components['body'].w = components['statementContainer'].w + DesignerDefaults.panel.body.padding.right + DesignerDefaults.panel.body.padding.left;

        viewState.bBox.h = components['heading'].h + components['body'].h;
        viewState.bBox.w = components['heading'].w + components['body'].w;

        viewState.components = components;
    }
}

export default ConnectorActionDimensionCalculatorVisitor;
