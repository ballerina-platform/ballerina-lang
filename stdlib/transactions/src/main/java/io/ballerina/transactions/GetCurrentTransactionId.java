/*
 *  Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.jvm.transactions.TransactionLocalContext;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Extern function ballerina.transactions:GetCurrentTransactionId.
 *
 * @since 0.970.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "transactions",
        functionName = "getCurrentTransactionId",
        returnType = {@ReturnType(type = TypeKind.STRING)}
)
public class GetCurrentTransactionId {

    public static String getCurrentTransactionId(Strand strand) {
        String currentTransactionId = "";
        TransactionLocalContext transactionLocalContext = strand.transactionLocalContext;
        if (transactionLocalContext != null) {
            currentTransactionId = transactionLocalContext.getGlobalTransactionId() + ":" + transactionLocalContext
                    .getCurrentTransactionBlockId();
        }
        return currentTransactionId;
    }
}
