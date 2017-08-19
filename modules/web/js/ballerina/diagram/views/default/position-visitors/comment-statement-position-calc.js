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
 * Position visitor class for Comment Statement.
 *
 * @class CommentStatementPositionCalcVisitor
 * */
class CommentStatementPositionCalcVisitor {

    /**
     * can visit the visitor.
     *
     * @return {boolean} true.
     *
     * @memberOf CommentStatementPositionCalcVisitor
     * */
    canVisit() {
        log.debug('can visit CommentStatementPositionCalcVisitor');
        return true;
    }

    /**
     * begin visiting the visitor.
     *
     * @param {ASTNode} node - Comment Statement node.
     *
     * @memberOf CommentStatementPositionCalcVisitor
     * */
    beginVisit(node) {
        log.debug('begin visit CommentStatementPositionCalcVisitor');
        PositioningUtils.getSimpleStatementPosition(node);
    }

    /**
     * visit the visitor.
     *
     * @memberOf CommentStatementPositionCalcVisitor
     * */
    visit() {
        log.debug('visit CommentStatementPositionCalcVisitor');
    }

    /**
     * visit the visitor at the end.
     *
     * @memberOf CommentStatementPositionCalcVisitor
     * */
    endVisit() {
        log.debug('end visit CommentStatementPositionCalcVisitor');
    }
}

export default CommentStatementPositionCalcVisitor;
