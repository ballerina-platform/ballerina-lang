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

class TimeoutStatementPositionCalcVisitor {

    canVisit(node) {
        log.debug('can visit TimeoutStatementPositionCalcVisitor');
        return true;
    }

    beginVisit(node) {
        log.debug('visit TimeoutStatementPositionCalcVisitor');
        let viewState = node.getViewState();
        let bBox = viewState.bBox;
        const parent = node.getParent();
        const parentViewState = parent.getViewState();
        const forkBBox = parentViewState.components.body;
        bBox.x = forkBBox.x + forkBBox.w / 2;
        bBox.y = forkBBox.getBottom();
        viewState.components.statementContainer.x = bBox.x;
        viewState.components.statementContainer.y = bBox.y + DesignerDefaults.blockStatement.heading.height;
    }

    visit(node) {
        log.debug('visit TimeoutStatementPositionCalcVisitor');
    }

    endVisit(node) {
        log.debug('end visit TimeoutStatementPositionCalcVisitor');
    }
}

export default TimeoutStatementPositionCalcVisitor;
