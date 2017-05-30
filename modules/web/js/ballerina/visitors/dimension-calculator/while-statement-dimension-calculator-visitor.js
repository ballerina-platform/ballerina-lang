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
import {util} from './../sizing-utils';

class WhileStatementDimensionCalculatorVisitor {

    canVisit(node) {
        log.debug('Can Visit WhileStatementDimensionCalculatorVisitor');
        return true;
    }

    beginVisit(node) {
        log.debug('Can Visit WhileStatementDimensionCalculatorVisitor');
    }

    visit(node) {
        log.debug('Visit WhileStatementDimensionCalculatorVisitor');
    }

    endVisit(node) {
        log.debug('End Visit WhileStatementDimensionCalculatorVisitor');
        let viewState = node.getViewState();
        let expression = node.getCondition();
        let bBox = viewState.bBox;
        let components = {};
        let statementContainerWidth = 0;
        let statementContainerHeight = 0;
        let children = node.getChildren();
        components['statementContainer'] = new SimpleBBox();

        _.forEach(children, function (child) {
            statementContainerHeight += child.getViewState().bBox.h;
            if (child.getViewState().bBox.w > statementContainerWidth) {
                statementContainerWidth = child.getViewState().bBox.w;
            }
        });

        /**
         * We add an extra padding to the statement container height to keep the space bet ween the statement's
         * bottom margin and the last child statement
         */
        statementContainerHeight += (statementContainerHeight > 0 ? DesignerDefaults.statement.gutter.v :
        DesignerDefaults.blockStatement.body.height - DesignerDefaults.blockStatement.heading.height);
        statementContainerWidth = getStatementContainerWidth(statementContainerWidth);

        let dropZoneHeight = DesignerDefaults.statement.gutter.v;
        components['drop-zone'] = new SimpleBBox();
        components['drop-zone'].h = dropZoneHeight;

        bBox.w = statementContainerWidth;
        bBox.h = statementContainerHeight +
            DesignerDefaults.blockStatement.heading.height + dropZoneHeight;

        components['statementContainer'].h = statementContainerHeight;
        components['statementContainer'].w = statementContainerWidth;

        // for compound statement like while we need to render condition expression
        // we will calculate the width of the expression and adjest the block statement
        if(expression != undefined){
            // see how much space we have to draw the condition
            let available = statementContainerWidth - DesignerDefaults.blockStatement.heading.width - 10;            
            components['expression'] = util.getTextWidth(expression,0,available);
        }        

        viewState.components = components;
    }
}

function getStatementContainerWidth(currentWidth) {
    let newWidth;
    if (currentWidth > 0) {
        newWidth = currentWidth + DesignerDefaults.blockStatement.body.padding.left +
            DesignerDefaults.blockStatement.body.padding.right;
    } else {
        newWidth = DesignerDefaults.blockStatement.width;
    }

    return newWidth;
}

export default WhileStatementDimensionCalculatorVisitor;
