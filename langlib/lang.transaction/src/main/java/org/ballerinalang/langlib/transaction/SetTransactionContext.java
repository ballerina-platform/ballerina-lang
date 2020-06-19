/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.transaction;

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.transactions.TransactionConstants;
import org.ballerinalang.jvm.transactions.TransactionLocalContext;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.jvm.values.api.BValueCreator;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.Map;

import static org.ballerinalang.jvm.transactions.TransactionConstants.TRANSACTION_PACKAGE_ID;
import static org.ballerinalang.util.BLangCompilerConstants.TRANSACTION_VERSION;

/**
 * Extern function transaction:setTransactionContext.
 *
 * @since 2.0.0-preview1
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.transaction", version = TRANSACTION_VERSION,
        functionName = "setTransactionContext",
        args = {
                @Argument(name = "transactionContext", type = TypeKind.RECORD),
                @Argument(name = "prevAttempt", type = TypeKind.UNION)
        },
        returnType = {@ReturnType(type = TypeKind.NIL)},
        isPublic = true
)
public class SetTransactionContext {

    public static void setTransactionContext(Strand strand, MapValue txDataStruct, Object prevAttemptInfo) {
        String globalTransactionId = txDataStruct.get(TransactionConstants.TRANSACTION_ID).toString();
        String transactionBlockId = txDataStruct.get(TransactionConstants.TRANSACTION_BLOCK_ID).toString();
        String url = txDataStruct.get(TransactionConstants.REGISTER_AT_URL).toString();
        String protocol = txDataStruct.get(TransactionConstants.CORDINATION_TYPE).toString();
        long retryNmbr = getRetryNumber(prevAttemptInfo);
        MapValue<BString, Object> trxContext = BallerinaValues.createRecordValue(TRANSACTION_PACKAGE_ID,
                "Info");
        Object[] trxContextData = new Object[] {
                BValueCreator.createArrayValue(globalTransactionId.getBytes()), retryNmbr, System.currentTimeMillis(),
                prevAttemptInfo
        };
        MapValue<BString, Object> infoRecord = BallerinaValues.createRecord(trxContext, trxContextData);
        TransactionLocalContext trxCtx = TransactionLocalContext
                .createTransactionParticipantLocalCtx(globalTransactionId, url, protocol, infoRecord);
        trxCtx.beginTransactionBlock(transactionBlockId);
        strand.transactionLocalContext = trxCtx;
    }

    private static long getRetryNumber(Object prevAttemptInfo) {
        if (prevAttemptInfo == null) {
            return 0;
        } else {
            Map<BString, Object> infoRecord = (Map<BString, Object>) prevAttemptInfo;
            Long retryNumber = (Long) infoRecord.get(StringUtils.fromString("retryNumber"));
            return retryNumber + 1;
        }
    }
}
