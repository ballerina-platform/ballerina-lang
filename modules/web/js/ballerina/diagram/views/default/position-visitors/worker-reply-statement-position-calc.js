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
import { util } from './../sizing-utils';

/**
 * Position visitor class for Worker Reply Statement.
 *
 * @class WorkerReplyStatementPositionCalcVisitor
 * */
class WorkerReplyStatementPositionCalcVisitor {

    /**
     * can visit the visitor.
     *
     * @return {boolean} true.
     *
     * @memberOf WorkerReplyStatementPositionCalcVisitor
     * */
    canVisit() {
        log.debug('can visit WorkerReplyStatementPositionCalcVisitor');
        return true;
    }

    /**
     * begin visiting the visitor.
     *
     * @param {ASTNode} node - Worker Reply Statement node.
     *
     * @memberOf WorkerReplyStatementPositionCalcVisitor
     * */
    beginVisit(node) {
        log.debug('begin visit WorkerReplyStatementPositionCalcVisitor');
        util.syncWorkerReplyDimension(node);
        PositioningUtils.getSimpleStatementPosition(node);
    }

    /**
     * visit the visitor.
     *
     * @memberOf WorkerReplyStatementPositionCalcVisitor
     * */
    visit() {
        log.debug('visit WorkerReplyStatementPositionCalcVisitor');
    }

    /**
     * visit the visitor at the end.
     *
     * @memberOf WorkerReplyStatementPositionCalcVisitor
     * */
    endVisit() {
        log.debug('end visit WorkerReplyStatementPositionCalcVisitor');
    }
}

export default WorkerReplyStatementPositionCalcVisitor;
