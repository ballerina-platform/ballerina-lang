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
import {util} from './../sizing-utils';
import {blockStatement} from '../../configs/designer-defaults';
import SimpleBBox from './../../ast/simple-bounding-box';

class JoinStatementDimensionCalculatorVisitor {

    canVisit(node) {
        log.debug('Can Visit JoinStatementDimensionCalculatorVisitor');
        return true;
    }

    beginVisit(node) {
        log.debug('Can Visit JoinStatementDimensionCalculatorVisitor');
    }

    visit(node) {
        log.debug('Visit JoinStatementDimensionCalculatorVisitor');
    }

    endVisit(node) {
        log.debug('End Visit JoinStatementDimensionCalculatorVisitor');
        util.populateCompoundStatementChild(node);
        const viewState = node.getViewState();
        const components = viewState.components;
        const paramW = util.getTextWidth(node.getParameterAsString(), 3);
        components['param'] = new SimpleBBox(0, 0, paramW.w, 0);
        const joinTypeW = util.getTextWidth(node.getJoinType(), 3);
        const widthOfText = paramW.w + joinTypeW.w + blockStatement.heading.width
            + blockStatement.heading.paramSeparatorOffsetX + blockStatement.heading.paramSeparatorOffsetX
            + blockStatement.heading.paramEndOffsetX;
        viewState.bBox.w = Math.max(viewState.bBox.w, widthOfText);
    }
}

export default JoinStatementDimensionCalculatorVisitor;
