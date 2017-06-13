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
        return true;
    }

    beginVisit(node) {
    }

    visit(node) {
    }

    endVisit(node) {
        let viewState = node.getViewState();
        let containerW = DesignerDefaults.fork.lifeLineGutterH;
        const workers = node.filterChildren(function (child) {
            return ASTFactory.isWorkerDeclaration(child);
        });
        const join = node.filterChildren(function (child) {
            return ASTFactory.isJoinStatement(child);
        });
        const timeout = node.filterChildren(function (child) {
            return ASTFactory.isTimeoutStatement(child);
        });
        let childWithMaxHeight = _.maxBy(workers, function (child) {
            return child.getViewState().bBox.h;
        });

        let bodyH = (childWithMaxHeight ? childWithMaxHeight.getViewState().bBox.h : 0 ) +
            DesignerDefaults.statement.gutter.v * 2;

        let bodyInnerH = (childWithMaxHeight ? childWithMaxHeight.getViewState().components.statementContainer.h : 0 );

        _.forEach(workers, function (child) {
            child.viewState.bBox.h = bodyInnerH;
            child.viewState.components.statementContainer.h = bodyInnerH;
        });

        _.forEach(workers, function (child) {
            containerW += child.getViewState().bBox.w + DesignerDefaults.fork.lifeLineGutterH;
        });

        let dropZoneHeight = DesignerDefaults.statement.gutter.v;
        let bodyW;
        viewState.components['drop-zone'] = new SimpleBBox();
        viewState.components['drop-zone'].h = dropZoneHeight;

        let hOfLowerParts;
        if (join.length > 0 && timeout.length > 0) {
            const joinBBox = join[0].viewState.bBox;
            const joinStatementsBBox = join[0].viewState.components.statementContainer;
            const timeoutBBox = timeout[0].viewState.bBox;
            const timeoutStatementsBBox = timeout[0].viewState.components.statementContainer;
            if (joinBBox.h > timeoutBBox.h) {
                hOfLowerParts = joinBBox.h;
                timeoutBBox.h = hOfLowerParts;
                timeoutStatementsBBox.h = hOfLowerParts;
            } else {
                hOfLowerParts = timeoutBBox.h;
                joinBBox.h = hOfLowerParts;
                joinStatementsBBox.h = hOfLowerParts;
            }
            const halfW = Math.max(timeoutBBox.w, joinBBox.w, containerW / 2);

            joinBBox.w = halfW;
            joinStatementsBBox.w = halfW;
            timeoutBBox.w = halfW;
            timeoutStatementsBBox.w = halfW;
            bodyW = halfW * 2;

        } else if (join.length > 0) {
            const joinBBox = join[0].viewState.bBox;
            const joinStatementsBBox = join[0].viewState.components.statementContainer;
            hOfLowerParts = joinBBox.h;

            const newW = Math.max(joinBBox.w, containerW);
            joinBBox.w = newW;
            joinStatementsBBox.w = newW;
            bodyW = newW;
        } else {
            throw 'Missing join in a fork statement.';
        }

        viewState.components['body'] = new SimpleBBox(0, 0, bodyW, bodyH);
        viewState.components['workers'] = new SimpleBBox(0, 0, containerW, bodyH);
        viewState.bBox.h = bodyH + dropZoneHeight + DesignerDefaults.blockStatement.heading.height + hOfLowerParts;
        viewState.bBox.w = bodyW;
    }
}

export default ForkJoinStatementDimensionCalculatorVisitor;
