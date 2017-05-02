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
import {util} from './../sizing-utils'

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

        components['heading'] = new SimpleBBox();
        components['heading'].h = DesignerDefaults.panel.heading.height;

        components['statementContainer'] = new SimpleBBox();
        var statementChildren = node.filterChildren(BallerinaASTFactory.isStatement);
        var statementWidth = 0;
        var statementHeight = 0;

        _.forEach(statementChildren, function(child) {
            statementHeight += child.viewState.bBox.h;
            if(child.viewState.bBox.w > statementWidth){
                statementWidth = child.viewState.bBox.w;
            }
        });

        //if statement width is 0 set default
        if(statementWidth == 0){
            statementWidth = DesignerDefaults.statement.width;
        }

        /* Lets add an extra gap to the bottom of the lifeline. 
           This will prevent last statement touching bottom box of the life line.*/
        statementHeight += DesignerDefaults.lifeLine.gutter.v;

        // If the lifeline is two short we will set a minimum height.
        if(statementHeight < DesignerDefaults.lifeLine.line.height){
            statementHeight = DesignerDefaults.lifeLine.line.height;
        }


        components['statementContainer'].h = statementHeight;
        components['statementContainer'].w = statementWidth;
        components['body'] = new SimpleBBox();

        let workerChildren = node.filterChildren(function (child) {
            return BallerinaASTFactory.isWorkerDeclaration(child);
        });

        let connectorChildren = node.filterChildren(function (child) {
            return BallerinaASTFactory.isConnectorDeclaration(child);
        });

        const highestStatementContainerHeight = util.getHighestStatementContainer(workerChildren);
        const workerLifeLineHeight = components['statementContainer'].h + DesignerDefaults.lifeLine.head.height * 2;
        const highestLifeLineHeight = highestStatementContainerHeight + DesignerDefaults.lifeLine.head.height * 2;

        var workerWidth = 0;
        _.forEach(workerChildren, function(child) {
            workerWidth += child.viewState.bBox.w + DesignerDefaults.lifeLine.gutter.h;
            child.getViewState().bBox.h = _.max([components['statementContainer'].h, highestStatementContainerHeight]) +
                DesignerDefaults.lifeLine.head.height * 2;
            child.getViewState().components.statementContainer.h = _.max([components['statementContainer'].h,
                highestStatementContainerHeight]);
        });

        _.forEach(connectorChildren, function(child) {
            child.getViewState().bBox.h = _.max([components['statementContainer'].h, highestStatementContainerHeight]) +
                DesignerDefaults.lifeLine.head.height * 2;
            child.getViewState().components.statementContainer.h = _.max([components['statementContainer'].h,
                highestStatementContainerHeight]);
        });

        //following is to handle node collapsed for panels.
        if(node.viewState.collapsed) {
            components['body'].h = 0;
        } else {
            components['body'].h = _.max([workerLifeLineHeight, highestLifeLineHeight])
                                   + DesignerDefaults.panel.body.padding.top + DesignerDefaults.panel.body.padding.bottom;
        }
        /**
         * If the current default worker's statement container height is less than the highest worker's statement container
         * we set the default statement container height to the highest statement container's height
         */
        components['statementContainer'].h = _.max([components['statementContainer'].h, highestStatementContainerHeight]);

        components['body'].w = components['statementContainer'].w + DesignerDefaults.panel.body.padding.right +
            DesignerDefaults.panel.body.padding.left + workerWidth;

        viewState.bBox.h = components['heading'].h + components['body'].h;
        viewState.bBox.w = components['heading'].w + components['body'].w;

        viewState.components = components;
    }
}

export default FunctionDefinitionDimensionCalculatorVisitor;
