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
import AST from './../../ast/module';
import * as DesignerDefaults from './../../configs/designer-defaults';

class WorkerDeclarationPositionCalcVisitor {

    canVisitWorkerDeclarationPositionCalc(node) {
        log.debug('can visit ServiceDefinitionPositionCalc');
        return true;
    }

    beginVisitWorkerDeclarationPositionCalc(node) {
        let viewState = node.getViewState();
        let bBox = viewState.bBox;
        let parent = node.getParent();
        let parentViewState = parent.getViewState();
        let workers = _.filter(parent.getChildren(), function (child) {
            return child instanceof AST.WorkerDeclaration;
        });
        let workerIndex = _.findIndex(workers, node);
        let x, y, middleLineStartX, middleLineStartY, middleLineEndX, middleLineEndY, topRectX, topRectY, bottomRectX, bottomRectY;

        if (workerIndex === 0) {
            topRectX = parentViewState.components.body.x() + DesignerDefaults.innerPanel.body.padding.left;
            topRectY = parentViewState.components.body.y() + DesignerDefaults.innerPanel.body.padding.top;
        } else if (workerIndex > 0) {
            let previousWorker = workers[workerIndex - 1];
            let previousWorkerViewState = previousWorker.getViewState();
            topRectX = previousWorkerViewState.components.body.x() + DesignerDefaults.innerPanel.body.padding.left;
            topRectY = parentViewState.components.body.y() + DesignerDefaults.innerPanel.body.padding.top;
        } else {
            throw "Invalid index found for Worker Declaration";
        }

        x = topRectX;
        y = topRectY;
        bottomRectX = topRectX;
        bottomRectY = topRectY + DesignerDefaults.lifeLine.head.height + DesignerDefaults.lifeLine.line.height;
        middleLineStartX = topRectX + viewState.components.topRect.w()/2;
        middleLineStartY = topRectY + viewState.components.topRect.h();
        middleLineEndX = middleLineStartX;
        middleLineEndY = middleLineStartY + DesignerDefaults.lifeLine.line.height;

        bBox.x(x).y(y);
        (viewState.components.topRect).x(topRectX).y(topRectY);
        (viewState.components.bottomRect).x(bottomRectX).y(bottomRectY);
        viewState.components.line.topX = middleLineStartX;
        viewState.components.line.topY = middleLineStartY;
        viewState.components.line.bottomX = middleLineEndX;
        viewState.components.line.bottomY = middleLineEndY;

        log.debug('begin visit WorkerDeclarationPositionCalc');
    }

    visitWorkerDeclarationPositionCalc(node) {
        log.debug('visit WorkerDeclarationPositionCalc');
    }

    endVisitWorkerDeclarationPositionCalc(node) {
        log.debug('end visit WorkerDeclarationPositionCalc');
        window.console.log(node.getViewState());
    }
}

export default ServiceDefinitionPositionCalcVisitor;