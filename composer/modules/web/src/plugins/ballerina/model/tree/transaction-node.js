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

import AbstractTransactionNode from './abstract-tree/transaction-node';

/**
 * class for the transaction node.
 * @extends AbstractTransactionNode
 * */
class TransactionNode extends AbstractTransactionNode {
    /**
     * Set alias to recognize the children.
     * */
    setChildrenCompoundStatus() {
        if (this.abortedBody) {
            this.abortedBody.viewState.compound = true;
        }
        if (this.committedBody) {
            this.committedBody.viewState.compound = true;
        }
        if (this.onRetryBody) {
            this.onRetryBody.viewState.compound = true;
        }
        if (this.transactionBody) {
            this.transactionBody.viewState.compound = true;
        }
    }
}

export default TransactionNode;
