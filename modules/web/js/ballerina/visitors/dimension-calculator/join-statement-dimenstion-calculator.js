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
import log from "log";
import {util} from "./../sizing-utils";

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
        // let viewState = node.getViewState();
        // let components = {};
        // const h = _.sumBy(node.children, child => child.viewState.bBox.h);
        // const w = _.maxBy(node.children, child => child.viewState.bBox.w).viewState.bBox.w;
        // components['statementContainer'] = new SimpleBBox();
        // components['statementContainer'].h = h;
        // components['statementContainer'].w = w;
        // viewState.bBox.w = w;
        // viewState.bBox.h = h + DesignerDefaults.blockStatement.heading.height + DesignerDefaults.statement.gutter.v;
        // viewState.components = components;

        util.populateCompoundStatementChild(node);
    }
}

export default JoinStatementDimensionCalculatorVisitor;
