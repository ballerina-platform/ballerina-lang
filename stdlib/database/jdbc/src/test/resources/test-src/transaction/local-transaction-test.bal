// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/lang.'transaction as transactions;
import ballerina/io;
import ballerina/java.jdbc;

type ResultCount record {
    int COUNTVAL;
};

function testLocalTransaction(string jdbcURL, string user, string password) returns @tainted [int, int, boolean, boolean]|error? {
   jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    int retryVal = -1;
    boolean committedBlockExecuted = false;
    transactions:Info transInfo;
    retry(1) transaction {
        var res = dbClient->execute("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                                "values ('James', 'Clerk', 200, 5000.75, 'USA')");
        res = dbClient->execute("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                                "values ('James', 'Clerk', 200, 5000.75, 'USA')");
        transInfo = transactions:info();
        var commitResult = commit;
        if(commitResult is ()){
            committedBlockExecuted = true;
        }
    }
    retryVal = transInfo.retryNumber;
    //check whether update action is performed
    int count = check getCount(dbClient, "200");
    check dbClient.close();
    return [retryVal, count, committedBlockExecuted];
}

boolean stmtAfterFailureExecutedRWC = false;
int retryValRWC = -1;
function testTransactionRollbackWithCheck(string jdbcURL, string user, string password) returns @tainted [int, int, boolean]|error?{
    jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    var result = testTransactionRollbackWithCheckHelper(dbClient);
    int count = check getCount(dbClient, "210");
    check dbClient.close();
    return [retryValRWC, count, stmtAfterFailureExecutedRWC];
}

function testTransactionRollbackWithCheckHelper(jdbc:Client dbClient) returns error?{
    transactions:Info transInfo;
    retry(1) transaction {
        transInfo = transactions:info();
        retryValRWC = transInfo.retryNumber;
        var e1 = check dbClient->execute("Insert into Customers (firstName,lastName,registrationID," +
                "creditLimit,country) values ('James', 'Clerk', 210, 5000.75, 'USA')");
        var e2 = check dbClient->execute("Insert into Customers2 (firstName,lastName,registrationID," +
                    "creditLimit,country) values ('James', 'Clerk', 210, 5000.75, 'USA')");
        stmtAfterFailureExecutedRWC  = true;
        check commit;
    }
}

function testTransactionRollbackWithRollback(string jdbcURL, string user, string password) returns @tainted [int, int, boolean]|error?{
   jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    int retryVal = -1;
    boolean stmtAfterFailureExecuted = false;
    transactions:Info transInfo;
    retry(1) transaction {
        transInfo = transactions:info();
        var e1 = dbClient->execute("Insert into Customers (firstName,lastName,registrationID," +
                "creditLimit,country) values ('James', 'Clerk', 211, 5000.75, 'USA')");
        if (e1 is error){
            rollback;
        } else {
            var e2 = dbClient->execute("Insert into Customers2 (firstName,lastName,registrationID," +
                        "creditLimit,country) values ('James', 'Clerk', 211, 5000.75, 'USA')");
            if (e2 is error){
                rollback;
                stmtAfterFailureExecuted  = true;
            } else {
                check commit;
            }
        }
    }
    retryVal = transInfo.retryNumber;
    int count = check getCount(dbClient, "211");
    check dbClient.close();
    return [retryVal, count, stmtAfterFailureExecuted];
}

function testLocalTransactionUpdateWithGeneratedKeys(string jdbcURL, string user, string password) returns @tainted [int, int]|error?{
   jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    int returnVal = 0;
    transactions:Info transInfo;
    retry (1) transaction {
        transInfo = transactions:info();
        var e1 = check dbClient->execute("Insert into Customers " +
         "(firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 615, 5000.75, 'USA')");
        var e2 =  check dbClient->execute("Insert into Customers " +
        "(firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 615, 5000.75, 'USA')");
        check commit;
    }
    returnVal = transInfo.retryNumber;
    //Check whether the update action is performed.
    int count = check getCount(dbClient, "615");
    check dbClient.close();
    return [returnVal, count];
}

int returnValRGK = 0;
function testLocalTransactionRollbackWithGeneratedKeys(string jdbcURL, string user, string password) returns @tainted [int, int]|error?{
    jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    var result = testLocalTransactionRollbackWithGeneratedKeysHelper(dbClient);
    //check whether update action is performed
    int count = check getCount(dbClient, "615");
    check dbClient.close();
    return [returnValRGK, count];
}

function testLocalTransactionRollbackWithGeneratedKeysHelper(jdbc:Client dbClient) returns error? {
    transactions:Info transInfo;
    retry(1) transaction {
        transInfo = transactions:info();
        returnValRGK = transInfo.retryNumber;
        var e1 = check dbClient->execute("Insert into Customers " +
         "(firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 615, 5000.75, 'USA')");
        var e2 = check dbClient->execute("Insert into Customers2 " +
        "(firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 615, 5000.75, 'USA')");
        check commit;
    }
}

function testTransactionAbort(string jdbcURL, string user, string password) returns @tainted [int, int, int]|error?{
   jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    transactions:Info transInfo;

    int abortVal = 0;
    var abortFunc = function(transactions:Info? info, error? cause, boolean willTry) {
        abortVal = -1;
    };

    retry(1) transaction {
        transInfo = transactions:info();
        transactions:onRollback(abortFunc);
        var e1 = dbClient->execute("Insert into Customers " +
         "(firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 220, 5000.75, 'USA')");
        var e2 =  dbClient->execute("Insert into Customers " +
        "(firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 220, 5000.75, 'USA')");
        int i = 0;
        if (i == 0) {
            rollback;
        } else {
            check commit;
        }
    }
    int returnVal = transInfo.retryNumber;
    //Check whether the update action is performed.
    int count = check getCount(dbClient, "220");
    check dbClient.close();
    return [returnVal, abortVal, count];
}

int testTransactionErrorPanicRetVal = 0;
function testTransactionErrorPanic(string jdbcURL, string user, string password) returns @tainted [int, int, int]|error? {
   jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    int returnVal = 0;
    int catchValue = 0;
    var ret = trap testTransactionErrorPanicHelper(dbClient);
    io:println(ret);
    if (ret is error) {
        catchValue = -1;
    }
    //Check whether the update action is performed.
    int count = check getCount(dbClient, "260");
    check dbClient.close();
    return [testTransactionErrorPanicRetVal, catchValue, count];
}

function testTransactionErrorPanicHelper(jdbc:Client dbClient) {
    int returnVal = 0;
    transactions:Info transInfo;
    retry(1) transaction {
        transInfo = transactions:info();
        var e1 = dbClient->execute("Insert into Customers (firstName,lastName," +
                              "registrationID,creditLimit,country) values ('James', 'Clerk', 260, 5000.75, 'USA')");
        int i = 0;
        if (i == 0) {
            error e = error("error");
            panic e;
        } else {
            var r = commit;
        }
    }
    io:println("exec");
    testTransactionErrorPanicRetVal = transInfo.retryNumber;
}

function testTransactionErrorPanicAndTrap(string jdbcURL, string user, string password) returns @tainted [int, int, int]|error? {
   jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);

    int catchValue = 0;
    transactions:Info transInfo;
    retry (1) transaction {
        transInfo = transactions:info();
        var e1 = dbClient->execute("Insert into Customers (firstName,lastName,registrationID," +
                 "creditLimit,country) values ('James', 'Clerk', 250, 5000.75, 'USA')");
        var ret = trap testTransactionErrorPanicAndTrapHelper(0);
        if (ret is error) {
            catchValue = -1;
        }
        check commit;
    }
    int returnVal = transInfo.retryNumber;
    //Check whether the update action is performed.
    int count = check getCount(dbClient, "250");
    check dbClient.close();
    return [returnVal, catchValue, count];
}

function testTransactionErrorPanicAndTrapHelper(int i) {
    if (i == 0) {
        error err = error("error");
        panic err;
    }
}

function testTwoTransactions(string jdbcURL, string user, string password) returns @tainted [int, int, int]|error? {
    jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);

     transactions:Info transInfo1;
     transactions:Info transInfo2;
     retry (1) transaction {
         transInfo1 = transactions:info();
         var e1 = check dbClient->execute("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                             "values ('James', 'Clerk', 400, 5000.75, 'USA')");
         var e2 = check dbClient->execute("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                             "values ('James', 'Clerk', 400, 5000.75, 'USA')");
         check commit;
     }
     int returnVal1 = transInfo1.retryNumber;

     retry(1) transaction {
         transInfo2 = transactions:info();
         var e1 = dbClient->execute("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                             "values ('James', 'Clerk', 400, 5000.75, 'USA')");
         var e2 = dbClient->execute("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                             "values ('James', 'Clerk', 400, 5000.75, 'USA')");
         check commit;
     }
     int returnVal2 = transInfo2.retryNumber;

     //Check whether the update action is performed.
     int count = check getCount(dbClient, "400");
     check dbClient.close();
     return [returnVal1, returnVal2, count];
 }

function testTransactionWithoutHandlers(string jdbcURL, string user, string password) returns @tainted [int]|error? {
   jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    transaction {
        var e1 = check dbClient->execute("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                            "values ('James', 'Clerk', 350, 5000.75, 'USA')");
        var e2 = check dbClient->execute("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                            "values ('James', 'Clerk', 350, 5000.75, 'USA')");
        check commit;
    }
    //Check whether the update action is performed.
    int count = check getCount(dbClient, "350");
    check dbClient.close();
    return [count];
}

function testLocalTransactionFailed(string jdbcURL, string user, string password) returns @tainted [string, int]|error? {
   jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);

    string a = "beforetx";

    var ret = trap testLocalTransactionFailedHelper(a, dbClient);
    if (ret is string) {
        a = ret;
    } else {
        a = ret.message() + " trapped";
    }
    a = a + " afterTrx";
    int count = check getCount(dbClient, "111");
    check dbClient.close();
    return [a, count];
}

function testLocalTransactionFailedHelper(string status,jdbc:Client dbClient) returns string|error {
    string a = status;
    transactions:Info transInfo;
    int i = 0;

    var onRollbackFunc = function(transactions:Info? info, error? cause, boolean willTry) {
        a = a + " trxAborted";
    };

    retry(2) transaction {
        a = a + " inTrx";
        transInfo = transactions:info();
        transactions:onRollback(onRollbackFunc);
        var e1 = check dbClient->execute("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                        "values ('James', 'Clerk', 111, 5000.75, 'USA')");
        var e2 = dbClient->execute("Insert into Customers2 (firstName,lastName,registrationID,creditLimit,country) " +
                        "values ('Anne', 'Clerk', 111, 5000.75, 'USA')");
        if(e2 is error){
           check getError(a);
        }
        check commit;
    }
    return a;
}

function getError(string message) returns error? {
    return error(message);
}

function testLocalTransactionSuccessWithFailed(string jdbcURL, string user, string password) returns @tainted [string, int]|error? {
   jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);

    string a = "beforetx";
    string | error ret = trap testLocalTransactionSuccessWithFailedHelper(a, dbClient);
    if (ret is string) {
        a = ret;
    } else {
        a = a + "trapped";
    }
    a = a + " afterTrx";
    int count = check getCount(dbClient, "222");
    check dbClient.close();
    return [a, count];
}

function testLocalTransactionSuccessWithFailedHelper(string status,jdbc:Client dbClient) returns string|error {
    int i = 0;
    string a = status;
    retry (3) transaction {
        i = i + 1;
        a = a + " inTrx";
        var e1 = check dbClient->execute("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)" +
                                    " values ('James', 'Clerk', 222, 5000.75, 'USA')");
        if (i == 3) {
            var e2 = check dbClient->execute("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                                        "values ('Anne', 'Clerk', 222, 5000.75, 'USA')");
        } else {
            var e3 = check dbClient->execute("Insert into Customers2 (firstName,lastName,registrationID,creditLimit,country) " +
                                        "values ('Anne', 'Clerk', 222, 5000.75, 'USA')");
        }
        check commit;
        a = a + " committed";
    }
    return a;
}

function getCount(jdbc:Client dbClient, string id) returns @tainted int|error{
    stream<ResultCount, error> streamData = <stream<ResultCount, error>> dbClient->query("Select COUNT(*) as " +
        "countval from Customers where registrationID = "+ id, ResultCount);
        record {|ResultCount value;|}? data = check streamData.next();
        check streamData.close();
        ResultCount? value = data?.value;
        if(value is ResultCount){
           return value.COUNTVAL;
        }
        return 0;
}
