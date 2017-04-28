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
import ASTFactory from './../../ast/ballerina-ast-factory';
import * as DesignerDefaults from './../../configs/designer-defaults';

class StatementPositionCalcVisitor {

    canVisitAssignmentStatementPositionCalc(node) {
        log.debug('can visit StatementPositionCalc');
        return true;
    }

    beginVisitAssignmentStatementPositionCalc(node) {
        log.debug('begin visit StatementPositionCalc');
        let viewState = node.getViewState();
        let bBox = viewState.bBox;
        let parent = node.getParent();
        let parentViewState = parent.getViewState();
        let parentStatementContainer = parentViewState.component.statementContainer;
        let parentStatements = _.filter(parent.getChildren(), function (child) {
            return ASTFactory.isStatement(child);
        });
        let statementIndex = _.findIndex(parentStatements, node);
        let x, y;

        /**
         * Statements are positioned based on the positioning of the respective statement container of the parent
         * Therefore all the positioning logic takes in to consider, the positioning and the dimensions of the
         * Particular statement container
         */

        if (bBox.w() > parentStatementContainer.w()) {
            /**
             * Ideally the statementContainerW <= statementWidth, otherwise the calculations have been off
             * Ideally we won't reach this
             */
            throw "Statement Container width cannot be less than Statement's Width";
        }

        /**
         * X position calculation, centers the statement horizontally inside the statement container
         */
        x = parentStatementContainer.x() + (parentStatementContainer.w() - bBox.w())/2;

        if (statementIndex === 0) {
            y = parentStatementContainer.y() + DesignerDefaults.statement.gutter.v;
        } else if (statementIndex > 0) {
            y = parentStatements[statementIndex - 1].getViewState().bBox.y() +
                parentStatements[statementIndex - 1].getViewState().bBox.h() + DesignerDefaults.statement.gutter.v
        } else {
            throw "Invalid Index found for Statement";
        }

        bBox.x(x);
        bBox.y(y);
    }

    visitAssignmentStatementPositionCalc(node) {
        log.debug('visit StatementPositionCalc');
    }

    endVisitAssignmentStatementPositionCalc(node) {
        log.debug('end visit StatementPositionCalc');
    }
}

export default StatementPositionCalcVisitor;
