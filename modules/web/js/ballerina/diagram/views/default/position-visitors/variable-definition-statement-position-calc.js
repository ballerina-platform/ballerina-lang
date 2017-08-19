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
import * as PositioningUtils from './utils';
import PositionCalculatorVisitor from '../position-calculator-visitor';

/**
 * Position visitor class for Variable Definition Statement.
 *
 * @class VariableDefinitionStatementPositionCalcVisitor
 * */
class VariableDefinitionStatementPositionCalcVisitor {

    /**
     * can visit the visitor.
     *
     * @return {boolean} true.
     *
     * @memberOf VariableDefinitionStatementPositionCalcVisitor
     * */
    canVisit() {
        log.debug('can visit VariableDefinitionStatementPositionCalc');
        return true;
    }

    /**
     * begin visiting the visitor.
     *
     * @param {ASTNode} node - Variable Definition Statement node.
     *
     * @memberOf VariableDefinitionStatementPositionCalcVisitor
     * */
    beginVisit(node) {
        log.debug('visit VariableDefinitionStatementPositionCalc');
        PositioningUtils.getSimpleStatementPosition(node);
    }

    /**
     * visit the visitor.
     *
     * @memberOf VariableDefinitionStatementPositionCalcVisitor
     * */
    visit(node) {
        log.debug('visit VariableDefinitionStatementPositionCalc');
        // TODO: this visit can be removed making all lambdas children of the node.
        node.getLambdaChildren().forEach(f => f.accept(new PositionCalculatorVisitor()));
    }

    /**
     * visit the visitor at the end.
     *
     * @memberOf VariableDefinitionStatementPositionCalcVisitor
     * */
    endVisit() {
        log.debug('end visit VariableDefinitionStatementPositionCalc');
    }
}

export default VariableDefinitionStatementPositionCalcVisitor;
