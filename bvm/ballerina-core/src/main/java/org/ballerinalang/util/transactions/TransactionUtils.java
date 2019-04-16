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
package org.ballerinalang.util.transactions;

import org.ballerinalang.bre.bvm.BVMExecutor;
import org.ballerinalang.bre.bvm.Strand;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Utility methods used in transaction handling.
 *
 * @since 0.970.0
 */
public class TransactionUtils {

    public static BValue[] notifyTransactionBegin(Strand ctx, String globalTransactionId, String url,
                                                  String transactionBlockId, String protocol) {
        BValue[] args = {
                (globalTransactionId == null ? null : new BString(globalTransactionId)),
                new BString(transactionBlockId), new BString(url),
                new BString(protocol)
        };
        BValue[] returns = invokeCoordinatorFunction(ctx, TransactionConstants.COORDINATOR_BEGIN_TRANSACTION, args);
        checkTransactionCoordinatorError(returns[0], "error in global transaction start: ");
        return returns;
    }

    public static CoordinatorCommit notifyTransactionEnd(Strand ctx, String globalTransactionId,
            String transactionBlockId) {
        BValue[] args = {new BString(globalTransactionId), new BString(transactionBlockId)};
        BValue[] returns = invokeCoordinatorFunction(ctx, TransactionConstants.COORDINATOR_END_TRANSACTION, args);
        checkTransactionCoordinatorError(returns[0], "error in transaction end: ");

        switch (returns[0].getType().getTag()) {
            case TypeTags.STRING_TAG:
                String statusMessage = returns[0].stringValue();
                if (statusMessage.equals("committed")) {
                    return CoordinatorCommit.COMMITTED;
                }
                return CoordinatorCommit.ABORTED;
            default:
                throw new IllegalStateException("Transaction coordinator returned unexpected result upon trx end: "
                        + returns[0].stringValue());
        }
    }

    public static void notifyTransactionAbort(Strand ctx, String globalTransactionId, String transactionBlockId) {
        BValue[] args = {new BString(globalTransactionId), new BString(transactionBlockId)};
        invokeCoordinatorFunction(ctx, TransactionConstants.COORDINATOR_ABORT_TRANSACTION, args);
    }

    private static void checkTransactionCoordinatorError(BValue value, String errMsg) {
        if (value.getType().getTag() == TypeTags.ERROR_TAG) {
            throw new BallerinaException(errMsg + ((BError) value).getReason());
        }
    }

    private static BValue[] invokeCoordinatorFunction(Strand ctx, String functionName, BValue[] args) {
        PackageInfo packageInfo = ctx.programFile.getPackageInfo(TransactionConstants.COORDINATOR_PACKAGE);
        FunctionInfo functionInfo = packageInfo.getFunctionInfo(functionName);
        return BVMExecutor.executeFunction(functionInfo.getPackageInfo().getProgramFile(), functionInfo, args);
    }

    /**
     * Indicate status of distributed transactions.
     */
    public enum CoordinatorCommit {
        COMMITTED,
        ABORTED,
        ERROR;

        private String status;

        public String getStatus() {
            return this.status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
