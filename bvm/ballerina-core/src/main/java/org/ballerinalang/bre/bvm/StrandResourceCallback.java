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
package org.ballerinalang.bre.bvm;

import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.util.codegen.CallableUnitInfo.ChannelDetails;
import org.ballerinalang.util.transactions.TransactionLocalContext;

/**
 * VM callback implementation which can be used for resource execution.
 *
 * @since 0.985.0
 */
public class StrandResourceCallback extends StrandCallback {

    private CallableUnitCallback resourceCallback;
    private TransactionLocalContext transactionLocalContext;

    StrandResourceCallback(BType retType, CallableUnitCallback resourceCallback, ChannelDetails[] sendIns) {
        super(retType, sendIns, null);
        this.resourceCallback = resourceCallback;
    }

    @Override
    public void signal() {
        super.signal();
        if (super.getErrorVal() != null) {
            resourceCallback.notifyFailure(super.getErrorVal());
            return;
        }
        BRefType<?> retValue = super.getRefRetVal();
        if (retValue != null && retValue.getType().getTag() == TypeTags.ERROR_TAG) {
            resourceCallback.notifyFailure((BError) getRefRetVal());
            if (transactionLocalContext != null) {
                transactionLocalContext.notifyLocalRemoteParticipantFailure();
            }
            return;
        }
        resourceCallback.notifySuccess();
    }

    public void setTransactionLocalContext(TransactionLocalContext transactionLocalContext) {
        this.transactionLocalContext = transactionLocalContext;
    }
}
