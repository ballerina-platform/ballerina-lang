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
 * Position visitor class for Reply statement.
 *
 * @class ReplyStatementPositionCalcVisitor
 * */
class ReplyStatementPositionCalcVisitor {

    /**
     * can visit the visitor.
     *
     * @return {boolean} true.
     *
     * @memberOf ReplyStatementPositionCalcVisitor
     * */
    canVisit() {
        log.debug('can visit ReplyStatementPositionCalc');
        return true;
    }

    /**
     * begin visiting the visitor.
     *
     * @param {ASTNode} node - Reply statement node.
     *
     * @memberOf ReplyStatementPositionCalcVisitor
     * */
    beginVisit(node) {
        log.debug('visit ReplyStatementPositionCalc');
        PositioningUtils.getSimpleStatementPosition(node);
    }

    /**
     * visit the visitor.
     *
     * @memberOf ReplyStatementPositionCalcVisitor
     * */
    visit() {
        log.debug('visit ReplyStatementPositionCalc');
    }

    /**
     * visit the visitor at the end.
     *
     * @memberOf ReplyStatementPositionCalcVisitor
     * */
    endVisit() {
        log.debug('end visit ReplyStatementPositionCalc');
    }
}

export default ReplyStatementPositionCalcVisitor;
