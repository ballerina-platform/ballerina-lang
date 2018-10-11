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

import _ from 'lodash';
import AbstractWorkerNode from './abstract-tree/worker-node';

class WorkerNode extends AbstractWorkerNode {

    remove() {
        const parent = this.parent;
        const parentWorkerCount = parent.workers.length;
        const parentDefaultWorker = _.find(parent.workers, (worker) => {
            return worker.name.value === 'default';
        });

        // We need to determine the exact number of workers.
        // If the number of workers are two and there exist a worker with the name default
        // We transfer the statements in the default worker to the parent's body.
        /* if (parentWorkerCount === 2 &&
            parentDefaultWorker &&
            this.name.value !== 'default') {
            const statements = parent.body.getStatements().concat(parentDefaultWorker.body.statements);
            parent.body.setStatements(statements, true);
            parentDefaultWorker.remove();
            super.remove();
        } else {
            super.remove();
        } */
        super.remove();
    }
}

export default WorkerNode;
