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

import AbstractForkJoinNode from './abstract-tree/fork-join-node';

class ForkJoinNode extends AbstractForkJoinNode {
    /**
     * Get join condition as a string.
     * @return {string} join condition.
     * */
    getJoinConditionString() {
        const forkJoinNode = this;
        const joinWorkers = forkJoinNode.getJoinedWorkerIdentifiers();
        const joinType = forkJoinNode.getJoinType();
        const joinCount = forkJoinNode.getJoinCount();

        let workers = '';
        for (let i = 0; i < joinWorkers.length; i++) {
            workers += joinWorkers[i].getValue();
            if (i < joinWorkers.length - 1) {
                workers += ', ';
            }
        }
        let condition;
        if (joinType === 'any') {
            condition = 'some' + (joinCount !== -1 ? joinCount : '') + workers;
        } else {
            condition = joinType + ' ' + workers;
        }
        return condition;
    }

    /**
     * set children alias.
     * */
    setChildrenAlias() {
        if (this.joinBody) {
            this.joinBody.viewState.alias = 'Join';
        }

        if (this.timeoutBody) {
            this.timeoutBody.viewState.alias = 'Timeout';
        }
    }
}

export default ForkJoinNode;
