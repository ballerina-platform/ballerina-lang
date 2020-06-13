//// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
////
//// WSO2 Inc. licenses this file to you under the Apache License,
//// Version 2.0 (the "License"); you may not use this file except
//// in compliance with the License.
//// You may obtain a copy of the License at
////
//// http://www.apache.org/licenses/LICENSE-2.0
////
//// Unless required by applicable law or agreed to in writing,
//// software distributed under the License is distributed on an
//// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
//// KIND, either express or implied.  See the License for the
//// specific language governing permissions and limitations
//// under the License.
//
//# This map is used for caching transaction that are initiated.
//map<TwoPhaseCommitTransaction> initiatedTransactions = {};
//
//public function startTransaction(string transactionBlockId, Info? prevAttempt = ()) returns string {
//    string transactionId = "";
//    TransactionContext|error txnContext = createTransactionContext(coordinationType, transactionBlockId);
//    if (txnContext is error) {
//        panic txnContext;
//    } else {
//
//        transactionId = txnContext.transactionId;
//        setTransactionContext(txnContext, prevAttempt);
//    }
//    return transactionId;
//}
//
//# A new transaction context is created by calling this function. At this point, a transaction object
//# corresponding to the coordinationType will also be created and stored as an initiated transaction.
//#
//# + coordinationType - The type of the coordination relevant to the transaction block for which this TransactionContext
//#                      is being created for.
//# + transactionBlockId - The ID of the transaction block.
//# + return - TransactionContext if the coordination type is valid or an error in case of an invalid coordination type.
//function createTransactionContext(string coordinationType, string transactionBlockId) returns TransactionContext|error {
//    if (!isValidCoordinationType(coordinationType)) {
//        string msg = "Invalid-Coordination-Type:" + coordinationType;
//        log:printError(msg);
//        error err = error(msg);
//        return err;
//    } else {
//        TwoPhaseCommitTransaction txn = new(system:uuid(), transactionBlockId, coordinationType = coordinationType);
//        string txnId = txn.transactionId;
//        txn.isInitiated = true;
//        initiatedTransactions[txnId] = txn;
//        TransactionContext txnContext = {
//            transactionId:txnId,
//            transactionBlockId:transactionBlockId,
//            coordinationType:coordinationType,
//            registerAtURL:"http://" + coordinatorHost + ":" + coordinatorPort.toString() +
//                initiatorCoordinatorBasePath + "/" + transactionBlockId + registrationPath
//        };
//        log:printInfo("Created transaction: " + txnId);
//        return txnContext;
//    }
//}
