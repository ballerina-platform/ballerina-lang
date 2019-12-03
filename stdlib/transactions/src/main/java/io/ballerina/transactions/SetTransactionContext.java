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

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.transactions.TransactionConstants;
import org.ballerinalang.jvm.transactions.TransactionLocalContext;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;


/**
 * Checks whether transactions is a nested transaction.
 *
 * @since 1.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "transactions",
        functionName = "setTransactionContext",
        args = {@Argument(name = "transactionContext", type = TypeKind.MAP)},
        returnType = {@ReturnType(type = TypeKind.VOID)}
)
public class SetTransactionContext {

    public static void setTransactionContext(Strand strand, MapValue txDataStruct) {
        String globalTransactionId = txDataStruct.get(TransactionConstants.TRANSACTION_ID).toString();
        String transactionBlockId = txDataStruct.get(TransactionConstants.TRANSACTION_BLOCK_ID).toString();
        String url = txDataStruct.get(TransactionConstants.REGISTER_AT_URL).toString();
        String protocol = txDataStruct.get(TransactionConstants.CORDINATION_TYPE).toString();
        TransactionLocalContext trxCtx = TransactionLocalContext
                .createTransactionParticipantLocalCtx(globalTransactionId, url, protocol);
        trxCtx.beginTransactionBlock(transactionBlockId);
        strand.transactionLocalContext = trxCtx;
    }
}
