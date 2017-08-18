/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.model.tree.statements.TransactionNode;

/**
 * @since 0.94
 */
public class BLangTransaction extends BLangStatement implements TransactionNode {
    public BLangBlockStmt transactionBody;
    public BLangBlockStmt failedBody;
    public BLangBlockStmt committedBody;
    public BLangBlockStmt abortedBody;

    public BLangTransaction(BLangBlockStmt transactionBody,
                            BLangBlockStmt failedBody,
                            BLangBlockStmt committedBody,
                            BLangBlockStmt abortedBody) {
        this.transactionBody = transactionBody;
        this.failedBody = failedBody;
        this.committedBody = committedBody;
        this.abortedBody = abortedBody;
    }

    @Override
    public BLangBlockStmt getTransactionBody() {
        return transactionBody;
    }

    @Override
    public BLangBlockStmt getFailedBody() {
        return failedBody;
    }

    @Override
    public BLangBlockStmt getCommittedBody() {
        return committedBody;
    }

    @Override
    public BLangBlockStmt getAbortedBody() {
        return abortedBody;
    }
}
