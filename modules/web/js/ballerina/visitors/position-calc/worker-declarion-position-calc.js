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
        let x, y;

        if (workerIndex === 0) {
            /**
             * Always the first worker should place after the default worker lifeline. If in a case like fork-join
             * we keep the first worker right next to the parent statement boundary.
             */
            if (parentViewState.components.defaultWorker) {
                x = parentViewState.components.statementContainer.getRight() +
                    DesignerDefaults.lifeLine.gutter.h;
            } else {
                x = parentViewState.components.body.getLeft() + DesignerDefaults.lifeLine.gutter.h;
            }
        } else if (workerIndex > 0) {
            const previousWorker = workers[workerIndex - 1];
            const previousStatementContainer = previousWorker.getViewState().components.statementContainer;
            x = previousStatementContainer.x + DesignerDefaults.innerPanel.body.padding.left;
        } else {
            throw "Invalid index found for Worker Declaration";
        }
        y = parentViewState.components.body.getTop() + DesignerDefaults.innerPanel.body.padding.top;

        bBox.x = x;
        bBox.y = y;
        //viewState.components.statementContainer.x = x;
        //viewState.components.statementContainer.y = y + DesignerDefaults.lifeLine.head.height;
    }

    visitWorkerDeclarationPositionCalc(node) {
    }

    endVisitWorkerDeclarationPositionCalc(node) {
    }
}

export default WorkerDeclarationPositionCalcVisitor;
