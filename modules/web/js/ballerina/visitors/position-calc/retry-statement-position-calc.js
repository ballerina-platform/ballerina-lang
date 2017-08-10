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

/**
 * Position visitor class for Retry Statement.
 *
 * @class RetryStatementPositionCalcVisitor
 * */
class RetryStatementPositionCalcVisitor {

    /**
     * can visit the visitor.
     *
     * @return {boolean} true.
     *
     * @memberOf RetryStatementPositionCalcVisitor
     * */
    canVisit() {
        log.debug('can visit RetryStatementPositionCalc');
        return true;
    }

    /**
     * begin visiting the visitor.
     *
     * @param {ASTNode} node - Retry Statement node.
     *
     * @memberOf RetryStatementPositionCalcVisitor
     * */
    beginVisit(node) {
        log.debug('visit RetryStatementPositionCalc');
        PositioningUtils.getSimpleStatementPosition(node);
    }

    /**
     * visit the visitor.
     *
     * @memberOf RetryStatementPositionCalcVisitor
     * */
    visit() {
        log.debug('visit RetryStatementPositionCalc');
    }

    /**
     * visit the visitor at the end.
     *
     * @memberOf RetryStatementPositionCalcVisitor
     * */
    endVisit() {
        log.debug('end visit RetryStatementPositionCalc');
    }
}

export default RetryStatementPositionCalcVisitor;
