/*
*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.runtime.transactions;

import javax.transaction.xa.Xid;

/**
 * Represents unique id for distributed transactions.
 *
 * @since 1.0
 */
public class XATransactionID implements Xid {

    private int formatId;
    private byte[] branchQualifier;
    private byte[] globalTransactionId;

    public XATransactionID(int formatId, byte[] branchQualifier, byte[] globalTransactionId) {
        this.formatId = formatId;
        this.branchQualifier = branchQualifier;
        this.globalTransactionId = globalTransactionId;
    }

    @Override
    public int getFormatId() {
        return formatId;
    }

    @Override
    public byte[] getGlobalTransactionId() {
        return globalTransactionId;
    }

    @Override
    public byte[] getBranchQualifier() {
        return branchQualifier;
    }
}
