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
import ASTFactory from './../../ast/ballerina-ast-factory';

class ForkJoinStatementDimensionCalculatorVisitor {

    canVisit(node) {
        log.debug('Can Visit ForkJoinStatementDimensionCalculatorVisitor');
        return true;
    }

    beginVisit(node) {
        log.debug('Can Visit ForkJoinStatementDimensionCalculatorVisitor');
    }

    visit(node) {
        log.debug('Visit ForkJoinStatementDimensionCalculatorVisitor');
    }

    endVisit(node) {
        log.debug('End Visit ForkJoinStatementDimensionCalculatorVisitor');
        let viewState = node.getViewState();
        let components = {};
        let statementWidth = DesignerDefaults.fork.lifeLineGutterH;
        let statementHeight = 0;
        const workers = node.filterChildren(function (child) {
            return ASTFactory.isWorkerDeclaration(child);
        });
        const join = node.filterChildren(function (child) {
            return ASTFactory.isJoinStatement(child);
        });
        let childWithMaxHeight = _.maxBy(workers, function (child) {
            return child.getViewState().bBox.h;
        });

        statementHeight = childWithMaxHeight.getViewState().bBox.h;

        _.forEach(workers, function (child) {
            statementWidth += child.getViewState().bBox.w + DesignerDefaults.fork.lifeLineGutterH;
        });

        let dropZoneHeight = DesignerDefaults.statement.gutter.v;
        viewState.components['drop-zone'] = new SimpleBBox();
        viewState.components['drop-zone'].h = dropZoneHeight;

        let h = statementHeight + DesignerDefaults.statement.gutter.v * 2;

        viewState.components['body'] = new SimpleBBox(0, 0, statementWidth, h);
        h += dropZoneHeight + DesignerDefaults.blockStatement.heading.height;

        if (join.length > 0) {
            const joinBBox = join[0].viewState.bBox;
            const joinStatementsBBox = join[0].viewState.components.statementContainer;
            h += joinBBox.h;
            const halfW = statementWidth / 2;
            if (joinBBox.w > halfW) {
                statementWidth = joinBBox.w * 2;
            } else {
                joinBBox.w = halfW;
                joinStatementsBBox.w = halfW;
            }


        }
        viewState.bBox.h = h;
        viewState.bBox.w = statementWidth;

    }
}

export default ForkJoinStatementDimensionCalculatorVisitor;

