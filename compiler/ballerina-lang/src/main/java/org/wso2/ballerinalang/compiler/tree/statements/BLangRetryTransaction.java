/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.ballerinalang.compiler.tree.statements;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.RetryTransactionNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangRetrySpec;

/**
 * {@code BLangRetryTransaction} represents a retry transaction statement within a transaction in Ballerina.
 *
 * @since 1.3.0
 */
public class BLangRetryTransaction extends BLangStatement implements RetryTransactionNode {

    public BLangRetrySpec retrySpec;
    public BLangTransaction transaction;

    public BLangRetrySpec getRetrySpec() {
        return retrySpec;
    }

    public void setRetrySpec(BLangRetrySpec retrySpec) {
        this.retrySpec = retrySpec;
    }

    public BLangTransaction getTransaction() {
        return transaction;
    }

    public void setTransaction(BLangTransaction transaction) {
        this.transaction = transaction;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.RETRY_TRANSACTION;
    }

    @Override
    public String toString() {
        return "Retry" + retrySpec + transaction;
    }
}
