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

class TryCatchStatementDimensionCalculatorVisitor {

    canVisit(node) {
        log.debug('Can Visit TryCatchStatementDimensionCalculatorVisitor');
        return true;
    }

    beginVisit(node) {
        log.debug('Can Visit TryCatchStatementDimensionCalculatorVisitor');
    }

    visit(node) {
        log.debug('Visit TryCatchStatementDimensionCalculatorVisitor');
    }

    endVisit(node) {
        log.debug('End Visit TryCatchStatementDimensionCalculatorVisitor');
        let viewState = node.getViewState();
        let components = {};
        let statementWidth = 0;
        let statementHeight = 0;
        let sortedChildren = _.sortBy(node.getChildren(), function (child) {
            return child.getViewState().bBox.w;
        });

        if (sortedChildren.length <= 0) {
            throw 'Invalid number of children for if-else statement';
        }
        let childWithMaxWidth = sortedChildren[sortedChildren.length -1];
        statementWidth = childWithMaxWidth.getViewState().bBox.w;

        _.forEach(node.getChildren(), function (child) {
            /**
             * Re adjust the width of all the other children
             */
            if (child.id !== childWithMaxWidth.id) {
                child.getViewState().components.statementContainer.w = childWithMaxWidth.getViewState().components.statementContainer.w;
                child.getViewState().bBox.w = childWithMaxWidth.getViewState().bBox.w;
            }
            statementHeight += child.getViewState().bBox.h;
        });

        viewState.bBox.h = statementHeight + DesignerDefaults.statement.gutter.v;
        viewState.bBox.w = statementWidth;
    }
}

export default TryCatchStatementDimensionCalculatorVisitor;
