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

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.transactions.TransactionConstants;
import org.ballerinalang.jvm.transactions.TransactionLocalContext;
import org.ballerinalang.jvm.transactions.TransactionResourceManager;
import org.ballerinalang.jvm.values.FPValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import static io.ballerina.transactions.RegisterLocalParticipant.STRUCT_TYPE_TRANSACTION_CONTEXT;
import static org.ballerinalang.jvm.runtime.RuntimeConstants.GLOBAL_TRANSACTION_ID;
import static org.ballerinalang.jvm.runtime.RuntimeConstants.TRANSACTION_URL;
import static org.ballerinalang.jvm.transactions.TransactionConstants.TRANSACTION_PACKAGE_ID;

/**
 * Checks whether transactions is a nested transaction.
 *
 * @since 1.0
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

public class RegisterRemoteParticipant {
    
    public static Object registerRemoteParticipant(Strand strand, String transactionBlockId, FPValue fpCommitted,
                                               FPValue fpAborted) {
        String gTransactionId = (String) strand.getProperty(GLOBAL_TRANSACTION_ID);
        if (gTransactionId == null) {
            // No transaction available to participate,
            // We have no business here. This is a no-op.
            return null;
        }

        // Create transaction context and store in the strand.
        TransactionLocalContext transactionLocalContext = TransactionLocalContext
                .create(gTransactionId, strand.getProperty(TRANSACTION_URL).toString(), "2pc");
        strand.transactionLocalContext = transactionLocalContext;

        // Register committed and aborted function handler if exists.
        TransactionResourceManager transactionResourceManager = TransactionResourceManager.getInstance();
        transactionResourceManager.registerParticipation(transactionLocalContext.getGlobalTransactionId(),
                                                         transactionBlockId, fpCommitted, fpAborted, strand);
        MapValue<String, Object> trxContext = BallerinaValues.createRecordValue(TRANSACTION_PACKAGE_ID,
                                                                                STRUCT_TYPE_TRANSACTION_CONTEXT);
        Object[] trxContextData = new Object[] {
                TransactionConstants.DEFAULT_CONTEXT_VERSION, transactionLocalContext.getGlobalTransactionId(),
                transactionBlockId, transactionLocalContext.getProtocol(), transactionLocalContext.getURL()
        };
        return BallerinaValues.createRecord(trxContext, trxContextData);
    }
}
