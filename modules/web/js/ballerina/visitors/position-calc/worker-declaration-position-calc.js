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
import WorkerDeclaration from './../../ast/worker-declaration';
import * as DesignerDefaults from './../../configs/designer-defaults';

class WorkerDeclarationPositionCalcVisitor {

    canVisit(node) {
        log.debug('can visit WorkerDeclarationPositionCalcVisitor');
        return true;
    }

    beginVisit(node) {
        log.debug('begin visit WorkerDeclarationPositionCalcVisitor');
        const viewState = node.getViewState();
        const bBox = viewState.bBox;
        const parent = node.getParent();
        const isInFork = ASTFactory.isForkJoinStatement(parent);
        const parentViewState = parent.getViewState();
        const workers = _.filter(parent.getChildren(), child => child instanceof WorkerDeclaration);
        const workerIndex = _.findIndex(workers, node);
        let x,
            y;

        if (workerIndex === 0) {
            /**
             * Always the first worker should place after the default worker lifeline. If in a case like fork-join
             * we keep the first worker right next to the parent statement boundary.
             */
            if (parentViewState.components.statementContainer) {
                /**
                 * Due to the model order of the ast, when worker declaration visits, position visitor for the parent's
                 * statements have not been calculated. so we need to use the width's of the parent
                 * statement container to get the x position
                 */
                x = parentViewState.components.body.getLeft() + DesignerDefaults.lifeLine.gutter.h +
                    parentViewState.components.statementContainer.w + DesignerDefaults.lifeLine.gutter.h;
            } else if (isInFork) {
                x = parentViewState.components.body.getLeft() + DesignerDefaults.fork.lifeLineGutterH +
                        (parentViewState.bBox.w - parentViewState.components.workers.w) / 2;
            } else {
                x = parentViewState.components.body.getLeft() + DesignerDefaults.lifeLine.gutter.h;
            }
        } else if (workerIndex > 0) {
            const previousWorker = workers[workerIndex - 1];
            const previousStatementContainer = previousWorker.getViewState().components.statementContainer;
            x = previousStatementContainer.getRight() +
                (isInFork ? DesignerDefaults.fork.lifeLineGutterH : DesignerDefaults.lifeLine.gutter.h);
        } else {
            throw 'Invalid index found for Worker Declaration';
        }
        y = parentViewState.components.body.getTop() +
            (isInFork ? DesignerDefaults.fork.padding.top : DesignerDefaults.innerPanel.body.padding.top);

        bBox.x = x;
        bBox.y = y;
        viewState.components.statementContainer.x = x;
        viewState.components.statementContainer.y = y + DesignerDefaults.lifeLine.head.height;
    }

    visit(node) {
        log.debug('visit WorkerDeclarationPositionCalcVisitor');
    }

    endVisit(node) {
        log.debug('end visit WorkerDeclarationPositionCalcVisitor');
    }
}

export default WorkerDeclarationPositionCalcVisitor;
