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
import { util } from './../sizing-utils';

/**
 * Dimension visitor class for Worker Invocation Statement.
 *
 * @class WorkerInvocationStatementDimensionCalculatorVisitor
 * */
class WorkerInvocationStatementDimensionCalculatorVisitor {

    /**
     * can visit the visitor.
     *
     * @return {boolean} true.
     *
     * @memberOf WorkerInvocationStatementDimensionCalculatorVisitor
     * */
    canVisit() {
        return true;
    }

    /**
     * begin visiting the visitor.
     *
     * @memberOf WorkerInvocationStatementDimensionCalculatorVisitor
     * */
    beginVisit() {
    }

    /**
     * visit the visitor.
     *
     * @memberOf WorkerInvocationStatementDimensionCalculatorVisitor
     * */
    visit() {
    }

    /**
     * visit the visitor at the end.
     *
     * @param {ASTNode} node - Worker Invocation Statement node.
     *
     * @memberOf WorkerInvocationStatementDimensionCalculatorVisitor
     * */
    endVisit(node) {
        util.populateSimpleStatementBBox(node.getStatementString(), node.getViewState());
    }
}

export default WorkerInvocationStatementDimensionCalculatorVisitor;
