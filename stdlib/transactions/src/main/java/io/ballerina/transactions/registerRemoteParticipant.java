/*
 *  Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package io.ballerina.transactions;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.bre.bvm.StackFrame;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BFunctionPointer;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.transactions.TransactionConstants;
import org.ballerinalang.util.transactions.TransactionLocalContext;
import org.ballerinalang.util.transactions.TransactionResourceManager;

/**
 * Checks whether transactions is a nested transaction.
 *
 * @since 0.991.0
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "transactions",
        functionName = "registerRemoteParticipant",
        args = {@Argument(name = "transactionBlockId", type = TypeKind.STRING),
                @Argument(name = "committedFunc", type = TypeKind.FUNCTION),
                @Argument(name = "abortedFunc", type = TypeKind.FUNCTION)},
        returnType = { @ReturnType(type = TypeKind.MAP) }
)
public class registerRemoteParticipant extends BlockingNativeCallableUnit {
    public void execute(Context ctx) {

        TransactionLocalContext transactionLocalContext = ctx.getStrand().getLocalTransactionContext();
        if (transactionLocalContext == null) {
            // No transaction available to participate,
            // We have no business here. This is a no-op.
            return;
        }
        String transactionBlockId = ctx.getStringArgument(0);
        TransactionResourceManager transactionResourceManager = TransactionResourceManager.getInstance();
        BFunctionPointer fpCommitted = (BFunctionPointer) ctx.getRefArgument(0);
        BFunctionPointer fpAborted = (BFunctionPointer) ctx.getRefArgument(1);
        // Register committed and aborted function handler if exists.
        transactionResourceManager.registerParticipation(transactionLocalContext.getGlobalTransactionId(),
                                                         transactionBlockId, fpCommitted, fpAborted, ctx.getStrand());
        BMap<String, BValue> txDataStruct = new BMap<>();
        txDataStruct.put(TransactionConstants.TRANSACTION_ID,
                         new BString(transactionLocalContext.getGlobalTransactionId()));
        txDataStruct.put(TransactionConstants.REGISTER_AT_URL, new BString(transactionLocalContext.getURL()));
        txDataStruct.put(TransactionConstants.CORDINATION_TYPE, new BString(transactionLocalContext.getProtocol()));
        // This call frame is a transaction participant.
        ctx.getStrand().currentFrame.trxParticipant = StackFrame.TransactionParticipantType.REMOTE_PARTICIPANT;
        ctx.setReturnValues(txDataStruct);
    }
}
