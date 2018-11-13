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

import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructureTypeInfo;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangFunctions;

import static org.ballerinalang.bre.bvm.BLangVMErrors.STRUCT_GENERIC_ERROR;
import static org.ballerinalang.util.BLangConstants.BALLERINA_BUILTIN_PKG;

/**
 * Utility methods used in transaction handling.
 *
 * @since 0.970.0
 */
public class TransactionUtils {

    public static BValue[] notifyTransactionBegin(WorkerExecutionContext ctx, String globalTransactionId, String url,
            int transactionBlockId, String protocol) {
        BValue[] args = {
                (globalTransactionId == null ? null : new BString(globalTransactionId)),
                new BInteger(transactionBlockId), new BString(url),
                new BString(protocol)
        };
        BValue[] returns = invokeCoordinatorFunction(ctx, TransactionConstants.COORDINATOR_BEGIN_TRANSACTION, args);
        checkTransactionCoordinatorError(returns[0], ctx, "error in transaction start: ");
        return returns;
    }

    @SuppressWarnings("unchecked")
    public static CoordinatorCommit notifyTransactionEnd(WorkerExecutionContext ctx, String globalTransactionId,
            int transactionBlockId) {
        BValue[] args = {new BString(globalTransactionId), new BInteger(transactionBlockId)};
        BValue[] returns = invokeCoordinatorFunction(ctx, TransactionConstants.COORDINATOR_END_TRANSACTION, args);
        checkTransactionCoordinatorError(returns[0], ctx, "error in transaction end: ");

        switch (returns[0].getType().getTag()) {
            case TypeTags.STRING_TAG:
                String statusMessage = returns[0].stringValue();
                if (statusMessage.equals("committed")) {
                    return CoordinatorCommit.COMMITTED;
                }
                return CoordinatorCommit.ABORTED;
            case TypeTags.MAP_TAG:
                // todo: refine this, I just guessed what would come in case of an error.
                // is this the error?
                CoordinatorCommit error = CoordinatorCommit.ERROR;
                BMap<String, BValue> errorMap = (BMap<String, BValue>) returns[0];
                error.setStatus("error str taken from map");
                return error;
            default:
                throw new IllegalStateException("Transaction coordinator returned unexpected result upon trx end: "
                        + returns[0].stringValue());
        }
    }

    public static void notifyTransactionAbort(WorkerExecutionContext ctx, String globalTransactionId,
            int transactionBlockId) {
        BValue[] args = {new BString(globalTransactionId), new BInteger(transactionBlockId)};
        invokeCoordinatorFunction(ctx, TransactionConstants.COORDINATOR_ABORT_TRANSACTION, args);
    }

    public static boolean isInitiator(WorkerExecutionContext ctx, String globalTransactionId,
            int transactionBlockId) {
        BValue[] args = {new BString(globalTransactionId), new BInteger(transactionBlockId)};
        BValue[] returns = invokeCoordinatorFunction(ctx, TransactionConstants.COORDINATOR_IS_INITIATOR, args);
        return ((BBoolean) returns[0]).booleanValue();
    }

    private static void checkTransactionCoordinatorError(BValue value, WorkerExecutionContext ctx, String errMsg) {
        if (value.getType().getTag() == TypeTags.OBJECT_TYPE_TAG
                || value.getType().getTag() == TypeTags.RECORD_TYPE_TAG) {
            PackageInfo errorPackageInfo = ctx.programFile.getPackageInfo(BALLERINA_BUILTIN_PKG);
            StructureTypeInfo errorStructInfo = errorPackageInfo.getStructInfo(STRUCT_GENERIC_ERROR);
            if (((BStructureType) value.getType()).getTypeInfo().equals(errorStructInfo)) {
                throw new BallerinaException(
                        errMsg + ((BMap<String, BValue>) value).get(BLangVMErrors.ERROR_MESSAGE_FIELD));
            }
        }
    }

    private static BValue[] invokeCoordinatorFunction(WorkerExecutionContext ctx, String functionName, BValue[] args) {
        PackageInfo packageInfo = ctx.programFile.getPackageInfo(TransactionConstants.COORDINATOR_PACKAGE);
        FunctionInfo functionInfo = packageInfo.getFunctionInfo(functionName);
        return BLangFunctions.invokeCallable(functionInfo, args);
    }

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
