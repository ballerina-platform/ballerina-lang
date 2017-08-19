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
import * as Utils from './utils';

/**
 * Position visitor class for Aborted statement.
 *
 * @class AbortedStatementPositionCalcVisitor
 * */
class AbortedStatementPositionCalcVisitor {

    /**
     * can visit the visitor.
     *
     * @return {boolean} true.
     *
     * @memberOf AbortedStatementPositionCalcVisitor
     * */
    canVisit() {
        log.debug('can visit AbortedStatementPositionCalcVisitor');
        return true;
    }

    /**
     * begin visiting the visitor.
     *
     * @param {ASTNode} node - Aborted statement node.
     *
     * @memberOf AbortedStatementPositionCalcVisitor
     * */
    beginVisit(node) {
        log.debug('visit AbortedStatementPositionCalcVisitor');
        Utils.getCompoundStatementChildPosition(node);
    }

    /**
     * visit the visitor.
     *
     * @memberOf AbortedStatementPositionCalcVisitor
     * */
    visit() {
        log.debug('visit AbortedStatementPositionCalcVisitor');
    }

    /**
     * visit the visitor at the end.
     *
     * @memberOf AbortedStatementPositionCalcVisitor
     * */
    endVisit() {
        log.debug('end visit AbortedStatementPositionCalcVisitor');
    }
}

export default AbortedStatementPositionCalcVisitor;
