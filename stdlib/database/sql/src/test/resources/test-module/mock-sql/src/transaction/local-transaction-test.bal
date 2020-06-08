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

import ballerina/sql;
import mockclient;

type ResultCount record {
    int COUNTVAL;
};

function testLocalTransaction(string jdbcURL, string user, string password) returns @tainted [int, int, boolean, boolean]|error? {
    mockclient:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    int retryVal = 0;
    boolean committedBlockExecuted = false;
    boolean abortedBlockExecuted = false;
    transaction {
        sql:ExecutionResult|sql:Error? res = dbClient->execute("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                                "values ('James', 'Clerk', 200, 5000.75, 'USA')");
        res = dbClient->execute("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                                "values ('James', 'Clerk', 200, 5000.75, 'USA')");
    } onretry {
        retryVal = -1;
    } committed {
        committedBlockExecuted = true;
    } aborted {
        abortedBlockExecuted = true;
    }
    //check whether update action is performed
    int count = check getCount(dbClient, "200");
    check dbClient.close();
    return [retryVal, count, committedBlockExecuted, abortedBlockExecuted];
}

function testTransactionRollback(string jdbcURL, string user, string password) returns @tainted [int, int, boolean]|error?{
    mockclient:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    int returnVal = 0;
    boolean stmtAfterFailureExecuted = false;

    transaction {
        var e1 = dbClient->execute("Insert into Customers (firstName,lastName,registrationID," +
                "creditLimit,country) values ('James', 'Clerk', 210, 5000.75, 'USA')");
        var e2 =  dbClient->execute("Insert into Customers2 (firstName,lastName,registrationID," +
                "creditLimit,country) values ('James', 'Clerk', 210, 5000.75, 'USA')");
        stmtAfterFailureExecuted = true;
    } onretry {
        returnVal = -1;
    }
    //check whether update action is performed
    int count = check getCount(dbClient, "210");
    check dbClient.close();
    return [returnVal, count, stmtAfterFailureExecuted];
}

function testLocalTransactionUpdateWithGeneratedKeys(string jdbcURL, string user, string password) returns @tainted [int, int]|error?{
    mockclient:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    int returnVal = 0;
    transaction {
        var e1 = dbClient->execute("Insert into Customers " +
         "(firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 615, 5000.75, 'USA')");
        var e2 =  dbClient->execute("Insert into Customers " +
        "(firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 615, 5000.75, 'USA')");
    } onretry {
        returnVal = -1;
    }
    //check whether update action is performed
    int count = check getCount(dbClient, "615");
    check dbClient.close();
    return [returnVal, count];
}

function testLocalTransactionRollbackWithGeneratedKeys(string jdbcURL, string user, string password) returns @tainted [int, int]|error?{
    mockclient:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    int returnVal = 0;
    transaction {
        var e1 = dbClient->execute("Insert into Customers " +
         "(firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 615, 5000.75, 'USA')");
        var e2 =  dbClient->execute("Insert into Customers2 " +
        "(firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 615, 5000.75, 'USA')");
    } onretry {
        returnVal = -1;
    }
    //check whether update action is performed
    int count = check getCount(dbClient, "615");
    check dbClient.close();
    return [returnVal, count];
}

function testTransactionAbort(string jdbcURL, string user, string password) returns @tainted [int, int, int]|error?{
    mockclient:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    int returnVal = 0;
    int abortVal = 0;
    transaction {
        var e1 = dbClient->execute("Insert into Customers " +
         "(firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 220, 5000.75, 'USA')");
        var e2 =  dbClient->execute("Insert into Customers " +
        "(firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 220, 5000.75, 'USA')");
        int i = 0;
        if (i == 0) {
            abort;
        }
        returnVal = 0;
    } onretry {
        returnVal = -1;
    } aborted {
        abortVal = -1;
    }
    //check whether update action is performed
    int count = check getCount(dbClient, "220");
    check dbClient.close();
    return [returnVal, abortVal, count];
}

int testTransactionErrorPanicRetVal = 0;
function testTransactionErrorPanic(string jdbcURL, string user, string password) returns @tainted [int, int, int]|error? {
    mockclient:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    int returnVal = 0;
    int catchValue = 0;
    var ret = trap testTransactionErrorPanicHelper(dbClient);
    if (ret is error) {
        catchValue = -1;
    }
    //check whether update action is performed
    int count = check getCount(dbClient, "260");
    check dbClient.close();
    return [testTransactionErrorPanicRetVal, catchValue, count];
}

function testTransactionErrorPanicHelper(mockclient:Client dbClient) {
    int returnVal = 0;
    transaction {
        var e1 = dbClient->execute("Insert into Customers (firstName,lastName," +
                              "registrationID,creditLimit,country) values ('James', 'Clerk', 260, 5000.75, 'USA')");
        int i = 0;
        if (i == 0) {
            error e = error("error");
            panic e;
        }
    } onretry {
        testTransactionErrorPanicRetVal = -1;
    }
}

function testTransactionErrorPanicAndTrap(string jdbcURL, string user, string password) returns @tainted [int, int, int]|error? {
    mockclient:Client dbClient = check new (url = jdbcURL, user = user, password = password);

    int returnVal = 0;
    int catchValue = 0;
    transaction {
        var e1 = dbClient->execute("Insert into Customers (firstName,lastName,registrationID," +
                 "creditLimit,country) values ('James', 'Clerk', 250, 5000.75, 'USA')");
        var ret = trap testTransactionErrorPanicAndTrapHelper(0);
        if (ret is error) {
            catchValue = -1;
        }
    } onretry {
        returnVal = -1;
    }
    //check whether update action is performed
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
    mockclient:Client dbClient = check new (url = jdbcURL, user = user, password = password);

    int returnVal1 = 1;
    int returnVal2 = 1;
    transaction {
        var e1 = dbClient->execute("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                            "values ('James', 'Clerk', 400, 5000.75, 'USA')");
        var e2 = dbClient->execute("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                            "values ('James', 'Clerk', 400, 5000.75, 'USA')");
    } onretry {
        returnVal1 = 0;
    }

    transaction {
        var e1 = dbClient->execute("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                            "values ('James', 'Clerk', 400, 5000.75, 'USA')");
        var e2 = dbClient->execute("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                            "values ('James', 'Clerk', 400, 5000.75, 'USA')");
    } onretry {
        returnVal2 = 0;
    }
    //check whether update action is performed
    int count = check getCount(dbClient, "400");
    check dbClient.close();
    return [returnVal1, returnVal2, count];
}

function testTransactionWithoutHandlers(string jdbcURL, string user, string password) returns @tainted [int]|error? {
    mockclient:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    transaction {
        var e1 = dbClient->execute("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                            "values ('James', 'Clerk', 350, 5000.75, 'USA')");
        var e2 = dbClient->execute("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                            "values ('James', 'Clerk', 350, 5000.75, 'USA')");
    }
    //check whether update action is performed
    int count = check getCount(dbClient, "350");
    check dbClient.close();
    return [count];
}

function testLocalTransactionFailed(string jdbcURL, string user, string password) returns @tainted [string, int]|error? {
    mockclient:Client dbClient = check new (url = jdbcURL, user = user, password = password);

    string a = "beforetx";

    var ret = trap testLocalTransactionFailedHelper(a, dbClient);
    if (ret is string) {
        a = ret;
    } else {
        a = a + " trapped";
    }
    a = a + " afterTrx";
    int count = check getCount(dbClient, "111");
    check dbClient.close();
    return [a, count];
}

function testLocalTransactionFailedHelper(string status, mockclient:Client dbClient) returns string {
    string a = status;
    transaction with retries = 4 {
        a = a + " inTrx";
        var e1 = dbClient->execute("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                        "values ('James', 'Clerk', 111, 5000.75, 'USA')");
        var e2 = dbClient->execute("Insert into Customers2 (firstName,lastName,registrationID,creditLimit,country) " +
                        "values ('Anne', 'Clerk', 111, 5000.75, 'USA')");
    } onretry {
        a = a + " onRetry";
    } aborted {
        a = a + " trxAborted";
    }
    return a;
}

function testLocalTransactionSuccessWithFailed(string jdbcURL, string user, string password) returns @tainted [string, int]|error? {
    mockclient:Client dbClient = check new (url = jdbcURL, user = user, password = password);

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

function testLocalTransactionSuccessWithFailedHelper(string status, mockclient:Client dbClient) returns string {
    int i = 0;
    string a = status;
    transaction with retries = 4 {
        a = a + " inTrx";
        var e1 = dbClient->execute("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)" +
                                    " values ('James', 'Clerk', 222, 5000.75, 'USA')");
        if (i == 2) {
            var e2 = dbClient->execute("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                                        "values ('Anne', 'Clerk', 222, 5000.75, 'USA')");
        } else {
            var e3 = dbClient->execute("Insert into Customers2 (firstName,lastName,registrationID,creditLimit,country) " +
                                        "values ('Anne', 'Clerk', 222, 5000.75, 'USA')");
        }
    } onretry {
        a = a + " onRetry";
        i = i + 1;
    } committed {
        a = a + " committed";
    }
    return a;
}

function getCount(mockclient:Client dbClient, string id) returns @tainted int|error{
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