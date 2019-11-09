// Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied. See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/transactions;
import ballerinax/java.jdbc;

type ResultCount record {
    int COUNTVAL;
};

function testLocalTransaction(string jdbcURL) returns @tainted [int, int, boolean, boolean] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    int returnVal = 0;
    int count;
    boolean committedBlockExecuted = false;
    boolean abortedBlockExecuted = false;
    transaction {
        _ = checkpanic testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                                "values ('James', 'Clerk', 200, 5000.75, 'USA')");
        _ = checkpanic testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                                "values ('James', 'Clerk', 200, 5000.75, 'USA')");
    } onretry {
        returnVal = -1;
    } committed {
        committedBlockExecuted = true;
    } aborted {
        abortedBlockExecuted = true;
    }
    //check whether update action is performed
    var dt = testDB->select("Select COUNT(*) as countval from Customers where registrationID = 200", ResultCount);
    count = getTableCountValColumn(dt);
    checkpanic testDB.stop();
    return [returnVal, count, committedBlockExecuted, abortedBlockExecuted];
}

function testTransactionRollback(string jdbcURL) returns @tainted [int, int, boolean] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    int returnVal = 0;
    int count;
    boolean stmtAfterFailureExecuted = false;

    transaction {
        var e1 = testDB->update("Insert into Customers (firstName,lastName,registrationID," +
                "creditLimit,country) values ('James', 'Clerk', 210, 5000.75, 'USA')");
        var e2 = testDB->update("Insert into Customers2 (firstName,lastName,registrationID," +
                "creditLimit,country) values ('James', 'Clerk', 210, 5000.75, 'USA')");
        stmtAfterFailureExecuted = true;

    } onretry {
        returnVal = -1;
    }
    //check whether update action is performed
    var dt = testDB->select("Select COUNT(*) as countval from Customers where registrationID = 210", ResultCount);
    count = getTableCountValColumn(dt);
    checkpanic testDB.stop();
    return [returnVal, count, stmtAfterFailureExecuted];
}

function testLocalTransactionUpdateWithGeneratedKeys(string jdbcURL) returns @tainted [int, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    int returnVal = 0;
    int count;
    transaction {
        var e1 = testDB->update("Insert into Customers " +
        "(firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 615, 5000.75, 'USA')");
        var e2 = testDB->update("Insert into Customers " +
        "(firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 615, 5000.75, 'USA')");
    } onretry {
        returnVal = -1;
    }
    //check whether update action is performed
    var dt = testDB->select("Select COUNT(*) as countval from Customers where registrationID = 615", ResultCount);
    count = getTableCountValColumn(dt);
    checkpanic testDB.stop();
    return [returnVal, count];
}

function testTransactionRollbackUpdateWithGeneratedKeys(string jdbcURL) returns @tainted [int, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    int returnVal = 0;
    int count;

    transaction {
        var e1 = testDB->update("Insert into Customers (firstName,lastName,registrationID," +
                "creditLimit,country) values ('James', 'Clerk', 618, 5000.75, 'USA')");
        var e2 = testDB->update("Insert into Customers2 (firstName,lastName,registrationID," +
                "creditLimit,country) values ('James', 'Clerk', 618, 5000.75, 'USA')");
    } onretry {
        returnVal = -1;
    }
    //check whether update action is performed
    var dt = testDB->select("Select COUNT(*) as countval from Customers where registrationID = 618", ResultCount);

    count = getTableCountValColumn(dt);
    checkpanic testDB.stop();
    return [returnVal, count];
}

function testLocalTransactionStoredProcedure(string jdbcURL) returns @tainted [int, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    int returnVal = 0;
    int count;

    transaction {
        var e1 = testDB->call("{call InsertPersonDataSuccessful(?, ?)}", (), 628, 628);
        var e2 = testDB->call("{call InsertPersonDataSuccessful(?, ?)}", (), 628, 628);
    } onretry {
        returnVal = -1;
    }
    //check whether update action is performed
    var dt = testDB->select("Select COUNT(*) as countval from Customers where registrationID = 628", ResultCount);
    count = getTableCountValColumn(dt);
    checkpanic testDB.stop();
    return [returnVal, count];
}

function testLocalTransactionRollbackStoredProcedure(string jdbcURL) returns @tainted [int, int, int, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 3}
    });

    int returnVal = 0;
    int count1;
    int count2;
    int count3;

    transaction {
        var e1 = testDB->call("{call InsertPersonDataSuccessful(?, ?)}", (), 629, 629);
        var e2 = testDB->call("{call InsertPersonDataFailure(?, ?)}", (), 631, 632);
    } onretry {
        returnVal = -1;
    }
    //check whether update action is performed
    var dt1 = testDB->select("Select COUNT(*) as countval from Customers where registrationID = 629",
    ResultCount);
    var dt2 = testDB->select("Select COUNT(*) as countval from Customers where registrationID = 631",
    ResultCount);
    var dt3 = testDB->select("Select COUNT(*) as countval from Customers where registrationID = 632",
    ResultCount);

    count1 = getTableCountValColumn(dt1);
    count2 = getTableCountValColumn(dt2);
    count3 = getTableCountValColumn(dt3);
    checkpanic testDB.stop();
    return [returnVal, count1, count2, count3];
}

function testLocalTransactionBatchUpdate(string jdbcURL) returns @tainted [int, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    int returnVal = 0;
    int count;

    //Batch 1
    jdbc:Parameter para1 = {sqlType: jdbc:TYPE_VARCHAR, value: "Alex"};
    jdbc:Parameter para2 = {sqlType: jdbc:TYPE_VARCHAR, value: "Smith"};
    jdbc:Parameter para3 = {sqlType: jdbc:TYPE_INTEGER, value: 611};
    jdbc:Parameter para4 = {sqlType: jdbc:TYPE_DOUBLE, value: 3400.5};
    jdbc:Parameter para5 = {sqlType: jdbc:TYPE_VARCHAR, value: "Colombo"};
    jdbc:Parameter?[] parameters1 = [para1, para2, para3, para4, para5];

    //Batch 2
    para1 = {sqlType: jdbc:TYPE_VARCHAR, value: "Alex"};
    para2 = {sqlType: jdbc:TYPE_VARCHAR, value: "Smith"};
    para3 = {sqlType: jdbc:TYPE_INTEGER, value: 611};
    para4 = {sqlType: jdbc:TYPE_DOUBLE, value: 3400.5};
    para5 = {sqlType: jdbc:TYPE_VARCHAR, value: "Colombo"};
    jdbc:Parameter?[] parameters2 = [para1, para2, para3, para4, para5];

    transaction {
        var e1 = testDB->batchUpdate("Insert into Customers " +
        "(firstName,lastName,registrationID,creditLimit,country) values (?,?,?,?,?)", false, parameters1, parameters2);
        var e2 = testDB->batchUpdate("Insert into Customers " +
        "(firstName,lastName,registrationID,creditLimit,country) values (?,?,?,?,?)", false, parameters1, parameters2);
    } onretry {
        returnVal = -1;
    }
    //check whether update action is performed
    var dt = testDB->select("Select COUNT(*) as countval from Customers where registrationID = 611", ResultCount);
    count = getTableCountValColumn(dt);
    checkpanic testDB.stop();
    return [returnVal, count];
}

function testLocalTransactionRollbackBatchUpdate(string jdbcURL) returns @tainted [int, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    int returnVal = 0;
    int count;

    //Batch 1
    jdbc:Parameter para1 = {sqlType: jdbc:TYPE_VARCHAR, value: "Alex"};
    jdbc:Parameter para2 = {sqlType: jdbc:TYPE_VARCHAR, value: "Smith"};
    jdbc:Parameter para3 = {sqlType: jdbc:TYPE_INTEGER, value: 612};
    jdbc:Parameter para4 = {sqlType: jdbc:TYPE_DOUBLE, value: 3400.5};
    jdbc:Parameter para5 = {sqlType: jdbc:TYPE_VARCHAR, value: "Colombo"};
    jdbc:Parameter?[] parameters1 = [para1, para2, para3, para4, para5];

    //Batch 2
    para1 = {sqlType: jdbc:TYPE_VARCHAR, value: "Alex"};
    para2 = {sqlType: jdbc:TYPE_VARCHAR, value: "Smith"};
    para3 = {sqlType: jdbc:TYPE_INTEGER, value: 612};
    para4 = {sqlType: jdbc:TYPE_DOUBLE, value: 3400.5};
    para5 = {sqlType: jdbc:TYPE_VARCHAR, value: "Colombo"};
    jdbc:Parameter?[] parameters2 = [para1, para2, para3, para4, para5];

    transaction {
        var e1 = testDB->batchUpdate("Insert into Customers " +
        "(firstName,lastName,registrationID,creditLimit,country) values (?,?,?,?,?)", false, parameters1, parameters2);
        var e2 = testDB->batchUpdate("Insert into Customers2 " +
        "(firstName,lastName,registrationID,creditLimit,country) values (?,?,?,?,?)", false, parameters1, parameters2);
    } onretry {
        returnVal = -1;
    }
    //check whether update action is performed
    var dt = testDB->select("Select COUNT(*) as countval from Customers where registrationID = 612", ResultCount);
    count = getTableCountValColumn(dt);
    checkpanic testDB.stop();
    return [returnVal, count];
}

function testTransactionAbort(string jdbcURL) returns @tainted [int, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    int returnVal = -1;
    int count;
    transaction {
        var e1 = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                                            "values ('James', 'Clerk', 220, 5000.75, 'USA')");

        var e2 = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                                            "values ('James', 'Clerk', 220, 5000.75, 'USA')");
        int i = 0;
        if (i == 0) {
            abort;
        }
        returnVal = 0;
    } onretry {
        returnVal = -1;
    }
    //check whether update action is performed
    var dt = testDB->select("Select COUNT(*) as countval from Customers where registrationID = 220", ResultCount);
    count = getTableCountValColumn(dt);
    checkpanic testDB.stop();
    return [returnVal, count];
}

int testTransactionErrorPanicRetVal = 0;
function testTransactionErrorPanic(string jdbcURL) returns @tainted [int, int, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    int returnVal = 0;
    int catchValue = 0;
    int count;
    var ret = trap testTransactionErrorPanicHelper(testDB);
    if (ret is error) {
        catchValue = -1;
    }
    //check whether update action is performed
    var dt = testDB->select("Select COUNT(*) as countval from Customers where registrationID = 260", ResultCount);
    count = getTableCountValColumn(dt);
    checkpanic testDB.stop();
    return [testTransactionErrorPanicRetVal, catchValue, count];
}

function testTransactionErrorPanicHelper(jdbc:Client testDB) {
    int returnVal = 0;
    transaction {
        var e1 = testDB->update("Insert into Customers (firstName,lastName," +
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

function testTransactionErrorPanicAndTrap(string jdbcURL) returns @tainted [int, int, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    int returnVal = 0;
    int catchValue = 0;
    int count;
    transaction {
        var e1 = testDB->update("Insert into Customers (firstName,lastName,registrationID," +
                 "creditLimit,country) values ('James', 'Clerk', 250, 5000.75, 'USA')");
        var ret = trap testTransactionErrorPanicAndTrapHelper(0);
        if (ret is error) {
            catchValue = -1;
        }
    } onretry {
        returnVal = -1;
    }
    //check whether update action is performed
    var dt = testDB->select("Select COUNT(*) as countval from Customers where registrationID = 250", ResultCount);
    count = getTableCountValColumn(dt);
    checkpanic testDB.stop();
    return [returnVal, catchValue, count];
}

function testTransactionErrorPanicAndTrapHelper(int i) {
    if (i == 0) {
        error err = error("error");
        panic err;
    }
}

function testTransactionCommitted(string jdbcURL) returns @tainted [int, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    int returnVal = 1;
    int count;
    transaction {
        var e1 = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit," +
               "country) values ('James', 'Clerk', 300, 5000.75, 'USA')");
        var e2 = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit," +
               "country) values ('James', 'Clerk', 300, 5000.75, 'USA')");
    } onretry {
        returnVal = -1;
    }
    //check whether update action is performed
    var dt = testDB->select("Select COUNT(*) as countval from Customers where registrationID = 300", ResultCount);
    count = getTableCountValColumn(dt);
    checkpanic testDB.stop();
    return [returnVal, count];
}

function testTwoTransactions(string jdbcURL) returns @tainted [int, int, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    int returnVal1 = 1;
    int returnVal2 = 1;
    int count;
    transaction {
        var e1 = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                            "values ('James', 'Clerk', 400, 5000.75, 'USA')");
        var e2 = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                            "values ('James', 'Clerk', 400, 5000.75, 'USA')");
    } onretry {
        returnVal1 = 0;
    }

    transaction {
        var e1 = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                            "values ('James', 'Clerk', 400, 5000.75, 'USA')");
        var e2 = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                            "values ('James', 'Clerk', 400, 5000.75, 'USA')");
    } onretry {
        returnVal2 = 0;
    }
    //check whether update action is performed
    var dt = testDB->select("Select COUNT(*) as countval from Customers where registrationID = 400", ResultCount);
    count = getTableCountValColumn(dt);
    checkpanic testDB.stop();
    return [returnVal1, returnVal2, count];
}

function testTransactionWithoutHandlers(string jdbcURL) returns @tainted int {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    transaction {
        var e1 = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) values " +
                                           "('James', 'Clerk', 350, 5000.75, 'USA')");
        var e2 = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) values " +
                                           "('James', 'Clerk', 350, 5000.75, 'USA')");
    }

    int count;
    //check whether update action is performed
    var dt = testDB->select("Select COUNT(*) as countval from Customers where registrationID = 350", ResultCount);
    count = getTableCountValColumn(dt);
    checkpanic testDB.stop();
    return count;
}

function testLocalTransactionFailed(string jdbcURL) returns @tainted [string, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    string a = "beforetx";
    int count = -1;

    var ret = trap testLocalTransactionFailedHelper(a, testDB);
    if (ret is string) {
        a = ret;
    } else {
        a = a + " trapped";
    }
    a = a + " afterTrx";
    var dtRet = testDB->select("Select COUNT(*) as countval from Customers where registrationID = 111", ResultCount);
    count = getTableCountValColumn(dtRet);
    checkpanic testDB.stop();
    return [a, count];
}

function testLocalTransactionFailedHelper(string status, jdbc:Client testDB) returns string {
    string a = status;
    transaction with retries = 4 {
        a = a + " inTrx";
        var e1 = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                        "values ('James', 'Clerk', 111, 5000.75, 'USA')");
        var e2 = testDB->update("Insert into Customers2 (firstName,lastName,registrationID,creditLimit,country) " +
                        "values ('Anne', 'Clerk', 111, 5000.75, 'USA')");
    } onretry {
        a = a + " onRetry";
    } aborted {
        a = a + " trxAborted";
    }
    return a;
}

function testLocalTransactionSuccessWithFailed(string jdbcURL) returns @tainted [string, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    string a = "beforetx";
    string | error ret = trap testLocalTransactionSuccessWithFailedHelper(a, testDB);
    if (ret is string) {
        a = ret;
    } else {
        a = a + "trapped";
    }
    a = a + " afterTrx";
    var dt = testDB->select("Select COUNT(*) as countval from Customers where registrationID = 222", ResultCount);
    int count = getTableCountValColumn(dt);
    checkpanic testDB.stop();
    return [a, count];
}

function testLocalTransactionSuccessWithFailedHelper(string status, jdbc:Client testDB) returns string {
    int i = 0;
    string a = status;
    transaction with retries = 4 {
        a = a + " inTrx";
        var e1 = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)" +
                                    " values ('James', 'Clerk', 222, 5000.75, 'USA')");
        if (i == 2) {
            var e2 = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                                        "values ('Anne', 'Clerk', 222, 5000.75, 'USA')");
        } else {
            var e3 = testDB->update("Insert into Customers2 (firstName,lastName,registrationID,creditLimit,country) " +
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

function testLocalTransactionFailedWithNextupdate(string jdbcURL) returns @tainted int {
    jdbc:Client testDB1;
    jdbc:Client testDB2;
    testDB1 = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    testDB2 = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    int i = 0;
    var ret = trap testLocalTransactionFailedWithNextupdateHelper(testDB1);
    if (ret is error) {
        i = -1;
    }
    var e = checkpanic testDB1->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                            "values ('James', 'Clerk', 12343, 5000.75, 'USA')");
    checkpanic testDB1.stop();

    var dt = testDB2->select("Select COUNT(*) as countval from Customers where registrationID = 12343",
    ResultCount);
    i = getTableCountValColumn(dt);
    checkpanic testDB2.stop();
    return i;
}

function testLocalTransactionFailedWithNextupdateHelper(jdbc:Client testDB) {
    transaction {
        var e = testDB->update("Insert into Customers (firstNamess,lastName,registrationID,creditLimit,country) " +
                                    "values ('James', 'Clerk', 1234, 5000.75, 'USA')");
    }
}

function testNestedTwoLevelTransactionSuccess(string jdbcURL) returns @tainted [int, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    int returnVal = 0;
    int count;
    transaction {
        var e = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                                "values ('James', 'Clerk', 333, 5000.75, 'USA')");
        testNestedTwoLevelTransactionSuccessParticipant(testDB);
    } onretry {
        returnVal = -1;
    }
    //check whether update action is performed
    var dt = testDB->select("Select COUNT(*) as countval from Customers where registrationID = 333", ResultCount);
    count = getTableCountValColumn(dt);
    checkpanic testDB.stop();
    return [returnVal, count];
}

@transactions:Participant {}
function testNestedTwoLevelTransactionSuccessParticipant(jdbc:Client testDB) {
    var e = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                                "values ('James', 'Clerk', 333, 5000.75, 'USA')");
}

function testNestedThreeLevelTransactionSuccess(string jdbcURL) returns @tainted [int, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    int returnVal = 0;
    int count;
    transaction {
        var e = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                                "values ('James', 'Clerk', 444, 5000.75, 'USA')");
        testNestedThreeLevelTransactionSuccessParticipant1(testDB);
    } onretry {
        returnVal = -1;
    }
    //check whether update action is performed
    var dt = testDB->select("Select COUNT(*) as countval from Customers where registrationID = 444", ResultCount);
    count = getTableCountValColumn(dt);
    checkpanic testDB.stop();
    return [returnVal, count];
}

@transactions:Participant {}
function testNestedThreeLevelTransactionSuccessParticipant1(jdbc:Client testDB) {
    var e = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                                "values ('James', 'Clerk', 444, 5000.75, 'USA')");
    testNestedThreeLevelTransactionSuccessParticipant2(testDB);
}

@transactions:Participant {}
function testNestedThreeLevelTransactionSuccessParticipant2(jdbc:Client testDB) {
    _ = checkpanic testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                                "values ('James', 'Clerk', 444, 5000.75, 'USA')");
}

function testNestedThreeLevelTransactionFailed(string jdbcURL) returns @tainted [int, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    int returnVal = 0;
    int count;
    var ret = trap testNestedThreeLevelTransactionFailedHelper(testDB);
    if (ret is int) {
        returnVal = ret;
    }
    //check whether update action is performed
    var dt = testDB->select("Select COUNT(*) as countval from Customers where registrationID = 555", ResultCount);
    count = getTableCountValColumn(dt);
    checkpanic testDB.stop();
    return [returnVal, count];
}

function testNestedThreeLevelTransactionFailedHelper(jdbc:Client testDB) returns int {
    int returnVal = 0;
    transaction {
        _ = checkpanic testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                                        "values ('James', 'Clerk', 555, 5000.75, 'USA')");
        testNestedThreeLevelTransactionFailedHelperParticipant1(testDB);
    } onretry {
        returnVal = -1;
    }
    return returnVal;
}

@transactions:Participant {}
function testNestedThreeLevelTransactionFailedHelperParticipant1(jdbc:Client testDB) {
    var e = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                                        "values ('James', 'Clerk', 555, 5000.75, 'USA')");
    testNestedThreeLevelTransactionFailedHelperParticipant2(testDB);
}

@transactions:Participant {}
function testNestedThreeLevelTransactionFailedHelperParticipant2(jdbc:Client testDB) {
    var e = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                                            "values ('James', 'Clerk', 555, 5000.75, 'USA')");
    testNestedThreeLevelTransactionFailedHelperParticipant3(testDB);
}

@transactions:Participant {}
function testNestedThreeLevelTransactionFailedHelperParticipant3(jdbc:Client testDB) {
    var e = testDB->update("Insert into Customers (invalidColumn,lastName,registrationID,creditLimit,country) " +
                                            "values ('James', 'Clerk', 555, 5000.75, 'USA')");
}

function testLocalTransactionWithSelectAndForeachIteration(string jdbcURL) returns @tainted [int, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 5}
    });

    var e1 = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                                "values ('James', 'Clerk', 900, 5000.75, 'USA')");
    var e2 = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                                "values ('James', 'Clerk', 900, 5000.75, 'USA')");

    int returnVal = 0;
    int count = -1;
    transaction {
        var dt1 = testDB->select("Select COUNT(*) as countval from Customers where registrationID = 900", ResultCount);
        if (dt1 is table<ResultCount>) {
            foreach var row in dt1 {
                count = row.COUNTVAL;
            }
        }
        var dt2 = testDB->select("Select COUNT(*) as countval from Customers where registrationID = 900", ResultCount);
        if (dt2 is table<ResultCount>) {
            foreach var row in dt2 {
                count = row.COUNTVAL;
            }
        }
    } onretry {
        returnVal = -1;
    }
    checkpanic testDB.stop();
    return [returnVal, count];
}

function testLocalTransactionWithUpdateAfterSelectAndForeachIteration(string jdbcURL) returns @tainted [int, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 5}
    });

    var e1 = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                                "values ('James', 'Clerk', 902, 5000.75, 'USA')");
    var e2 = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                                "values ('James', 'Clerk', 902, 5000.75, 'USA')");

    int returnVal = 0;
    int count = -1;
    transaction {
        var dt1 = testDB->select("Select COUNT(*) as countval from Customers where registrationID = 902", ResultCount);
        if (dt1 is table<ResultCount>) {
            foreach var row in dt1 {
                count = row.COUNTVAL;
            }
        }
        var e3 = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                                        "values ('James', 'Clerk', 902, 5000.75, 'USA')");
        var dt2 = testDB->select("Select COUNT(*) as countval from Customers where " +
            "registrationID = 902", ResultCount);
        if (dt2 is table<ResultCount>) {
            foreach var row in dt2 {
                count = row.COUNTVAL;
            }
        }
    } onretry {
        returnVal = -1;
    }
    checkpanic testDB.stop();
    return [returnVal, count];
}

function testLocalTransactionWithUpdateAfterSelectAndBreakingWhileIteration(string jdbcURL) returns @tainted [int, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 5}
    });

    var e1 = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                                "values ('James', 'Clerk', 903, 5000.75, 'USA')");
    var e2 = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                                "values ('James', 'Clerk', 903, 5000.75, 'USA')");

    int returnVal = 0;
    int count = -1;
    transaction {
        var dt1 = testDB->select("Select COUNT(*) as countval from Customers where " +
            "registrationID = 903", ResultCount);
        if (dt1 is table<ResultCount>) {
            while (dt1.hasNext()) {
                var rs = dt1.getNext();
                if (rs is ResultCount) {
                    count = rs.COUNTVAL;
                }
                break;
            }
        }
        var e3 = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                                        "values ('James', 'Clerk', 903, 5000.75, 'USA')");
        var dt2 = testDB->select("Select COUNT(*) as countval from Customers where " +
            "registrationID = 903", ResultCount);
        if (dt2 is table<ResultCount>) {
            foreach var row in dt2 {
                count = row.COUNTVAL;
            }
        }
    } onretry {
        returnVal = -1;
    }
    checkpanic testDB.stop();
    return [returnVal, count];
}

function testLocalTransactionWithUpdateAfterSelectAndTableClosure(string jdbcURL) returns @tainted [int, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 5}
    });

    var e1 = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                                "values ('James', 'Clerk', 904, 5000.75, 'USA')");
    var e2 = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                                "values ('James', 'Clerk', 904, 5000.75, 'USA')");

    int returnVal = 0;
    int count = -1;
    transaction {
        var dt1 = testDB->select("Select COUNT(*) as countval from Customers where " +
            "registrationID = 904", ResultCount);
        if (dt1 is table<ResultCount>) {
            dt1.close();
        }

        var e3 = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                                        "values ('James', 'Clerk', 904, 5000.75, 'USA')");
        var dt2 = testDB->select("Select COUNT(*) as countval from Customers where " +
            "registrationID = 904", ResultCount);
        if (dt2 is table<ResultCount>) {
            foreach var row in dt2 {
                count = row.COUNTVAL;
            }
        }
    } onretry {
        returnVal = -1;
    }
    checkpanic testDB.stop();
    return [returnVal, count];
}

function testLocalTransactionWithSelectAndHasNextIteration(string jdbcURL) returns @tainted [int, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 5}
    });

    var e1 = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                                "values ('James', 'Clerk', 901, 5000.75, 'USA')");
    var e2 = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                                "values ('James', 'Clerk', 901, 5000.75, 'USA')");

    int returnVal = 0;
    int count = -1;
    transaction {
        var dt1 = testDB->select("Select COUNT(*) as countval from Customers where " +
            "registrationID = 901", ResultCount);
        count = getTableCountValColumn(dt1);
        var dt2 = testDB->select("Select COUNT(*) as countval from Customers where " +
            "registrationID = 901", ResultCount);
        count = getTableCountValColumn(dt2);
    } onretry {
        returnVal = -1;
    }
    checkpanic testDB.stop();
    return [returnVal, count];
}

function testCloseConnectionPool(string jdbcURL) returns @tainted int {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    int count;
    var dt = testDB->select("SELECT COUNT(*) as countVal FROM INFORMATION_SCHEMA.SESSIONS", ResultCount);
    count = getTableCountValColumn(dt);
    checkpanic testDB.stop();
    return count;
}

function getTableCountValColumn(table<ResultCount> | error result) returns int {
    int count = -1;
    if (result is table<ResultCount>) {
        while (result.hasNext()) {
            var rs = result.getNext();
            if (rs is ResultCount) {
                count = rs.COUNTVAL;
            }
        }
        return count;
    }
    return -1;
}
