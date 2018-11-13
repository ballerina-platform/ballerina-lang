import ballerina/h2;
import ballerina/io;
import ballerina/runtime;
import ballerina/sql;

type ResultCount record {
    int COUNTVAL;
};

function testLocalTransaction() returns (int, int) {
    endpoint h2:Client testDB {
        path: "./target/tempdb/",
        name: "TEST_SQL_CONNECTOR_TR",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    };

    int returnVal = 0;
    int count;
    transaction {
        _ = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                values ('James', 'Clerk', 200, 5000.75, 'USA')");
        _ = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                values ('James', 'Clerk', 200, 5000.75, 'USA')");
    } onretry {
        returnVal = -1;
    }
    //check whether update action is performed
    table dt = check testDB->select("Select COUNT(*) as countval from Customers where registrationID = 200", ResultCount
    );
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB.stop();
    return (returnVal, count);
}

function testTransactionRollback() returns (int, int) {
    endpoint h2:Client testDB {
        path: "./target/tempdb/",
        name: "TEST_SQL_CONNECTOR_TR",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    };

    int returnVal = 0;
    int count;

    transaction {
        _ = testDB->update("Insert into Customers (firstName,lastName,registrationID,
                creditLimit,country) values ('James', 'Clerk', 210, 5000.75, 'USA')");
        _ = testDB->update("Insert into Customers2 (firstName,lastName,registrationID,
                creditLimit,country) values ('James', 'Clerk', 210, 5000.75, 'USA')");

    } onretry {
        returnVal = -1;
    }
    //check whether update action is performed
    table dt = check testDB->select("Select COUNT(*) as countval from Customers where registrationID = 210", ResultCount
    );

    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB.stop();
    return (returnVal, count);
}

function testLocalTransactionUpdateWithGeneratedKeys() returns (int, int) {
    endpoint h2:Client testDB {
        path: "./target/tempdb/",
        name: "TEST_SQL_CONNECTOR_TR",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    };

    int returnVal = 0;
    int count;
    transaction {
        _ = testDB->updateWithGeneratedKeys("Insert into Customers
        (firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 615, 5000.75, 'USA')", ());
        _ = testDB->updateWithGeneratedKeys("Insert into Customers
        (firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 615, 5000.75, 'USA')", ());
    } onretry {
        returnVal = -1;
    }
    //check whether update action is performed
    table dt = check testDB->select("Select COUNT(*) as countval from Customers where registrationID = 615", ResultCount
    );
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB.stop();
    return (returnVal, count);
}

function testTransactionRollbackUpdateWithGeneratedKeys() returns (int, int) {
    endpoint h2:Client testDB {
        path: "./target/tempdb/",
        name: "TEST_SQL_CONNECTOR_TR",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    };

    int returnVal = 0;
    int count;

    transaction {
        _ = testDB->updateWithGeneratedKeys("Insert into Customers (firstName,lastName,registrationID,
                creditLimit,country) values ('James', 'Clerk', 618, 5000.75, 'USA')", ());
        _ = testDB->updateWithGeneratedKeys("Insert into Customers2 (firstName,lastName,registrationID,
                creditLimit,country) values ('James', 'Clerk', 618, 5000.75, 'USA')", ());
    } onretry {
        returnVal = -1;
    }
    //check whether update action is performed
    table dt = check testDB->select("Select COUNT(*) as countval from Customers where registrationID = 618", ResultCount
    );

    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB.stop();
    return (returnVal, count);
}

function testLocalTransactionStoredProcedure() returns (int, int) {
    endpoint h2:Client testDB {
        path: "./target/tempdb/",
        name: "TEST_SQL_CONNECTOR_TR",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    };

    int returnVal = 0;
    int count;

    transaction {
        _ = testDB->call("{call InsertPersonDataSuccessful(?, ?)}", (), 628, 628);
        _ = testDB->call("{call InsertPersonDataSuccessful(?, ?)}", (), 628, 628);
    } onretry {
        returnVal = -1;
    }
    //check whether update action is performed
    table dt = check testDB->select("Select COUNT(*) as countval from Customers where registrationID = 628", ResultCount
    );
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB.stop();
    return (returnVal, count);
}

function testLocalTransactionRollbackStoredProcedure() returns (int, int, int, int) {
    endpoint h2:Client testDB {
        path: "./target/tempdb/",
        name: "TEST_SQL_CONNECTOR_TR",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 3 }
    };

    int returnVal = 0;
    int count1;
    int count2;
    int count3;

    transaction {
        _ = testDB->call("{call InsertPersonDataSuccessful(?, ?)}", (), 629, 629);
        _ = testDB->call("{call InsertPersonDataFailure(?, ?)}", (), 631, 632);
    } onretry {
        returnVal = -1;
    }
    //check whether update action is performed
    table dt1 = check testDB->select("Select COUNT(*) as countval from Customers where registrationID = 629",
        ResultCount);
    table dt2 = check testDB->select("Select COUNT(*) as countval from Customers where registrationID = 631",
        ResultCount);
    table dt3 = check testDB->select("Select COUNT(*) as countval from Customers where registrationID = 632",
        ResultCount);

    while (dt1.hasNext()) {
        ResultCount rs = check <ResultCount>dt1.getNext();
        count1 = rs.COUNTVAL;
    }
    while (dt2.hasNext()) {
        ResultCount rs = check <ResultCount>dt2.getNext();
        count2 = rs.COUNTVAL;
    }
    while (dt3.hasNext()) {
        ResultCount rs = check <ResultCount>dt3.getNext();
        count3 = rs.COUNTVAL;
    }
    testDB.stop();
    return (returnVal, count1, count2, count3);
}

function testLocalTransactionBatchUpdate() returns (int, int) {
    endpoint h2:Client testDB {
        path: "./target/tempdb/",
        name: "TEST_SQL_CONNECTOR_TR",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    };

    int returnVal = 0;
    int count;

    //Batch 1
    sql:Parameter para1 = { sqlType: sql:TYPE_VARCHAR, value: "Alex" };
    sql:Parameter para2 = { sqlType: sql:TYPE_VARCHAR, value: "Smith" };
    sql:Parameter para3 = { sqlType: sql:TYPE_INTEGER, value: 611 };
    sql:Parameter para4 = { sqlType: sql:TYPE_DOUBLE, value: 3400.5 };
    sql:Parameter para5 = { sqlType: sql:TYPE_VARCHAR, value: "Colombo" };
    sql:Parameter[] parameters1 = [para1, para2, para3, para4, para5];

    //Batch 2
    para1 = { sqlType: sql:TYPE_VARCHAR, value: "Alex" };
    para2 = { sqlType: sql:TYPE_VARCHAR, value: "Smith" };
    para3 = { sqlType: sql:TYPE_INTEGER, value: 611 };
    para4 = { sqlType: sql:TYPE_DOUBLE, value: 3400.5 };
    para5 = { sqlType: sql:TYPE_VARCHAR, value: "Colombo" };
    sql:Parameter[] parameters2 = [para1, para2, para3, para4, para5];

    transaction {
        int[] updateCount1 = check testDB->batchUpdate("Insert into Customers
        (firstName,lastName,registrationID,creditLimit,country) values (?,?,?,?,?)", parameters1, parameters2);
        int[] updateCount2 = check testDB->batchUpdate("Insert into Customers
        (firstName,lastName,registrationID,creditLimit,country) values (?,?,?,?,?)", parameters1, parameters2);
    } onretry {
        returnVal = -1;
    }
    //check whether update action is performed
    table dt = check testDB->select("Select COUNT(*) as countval from Customers where registrationID = 611", ResultCount
    );
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB.stop();
    return (returnVal, count);
}

function testLocalTransactionRollbackBatchUpdate() returns (int, int) {
    endpoint h2:Client testDB {
        path: "./target/tempdb/",
        name: "TEST_SQL_CONNECTOR_TR",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    };

    int returnVal = 0;
    int count;

    //Batch 1
    sql:Parameter para1 = { sqlType: sql:TYPE_VARCHAR, value: "Alex" };
    sql:Parameter para2 = { sqlType: sql:TYPE_VARCHAR, value: "Smith" };
    sql:Parameter para3 = { sqlType: sql:TYPE_INTEGER, value: 612 };
    sql:Parameter para4 = { sqlType: sql:TYPE_DOUBLE, value: 3400.5 };
    sql:Parameter para5 = { sqlType: sql:TYPE_VARCHAR, value: "Colombo" };
    sql:Parameter[] parameters1 = [para1, para2, para3, para4, para5];

    //Batch 2
    para1 = { sqlType: sql:TYPE_VARCHAR, value: "Alex" };
    para2 = { sqlType: sql:TYPE_VARCHAR, value: "Smith" };
    para3 = { sqlType: sql:TYPE_INTEGER, value: 612 };
    para4 = { sqlType: sql:TYPE_DOUBLE, value: 3400.5 };
    para5 = { sqlType: sql:TYPE_VARCHAR, value: "Colombo" };
    sql:Parameter[] parameters2 = [para1, para2, para3, para4, para5];

    transaction {
        int[] updateCount1 = check testDB->batchUpdate("Insert into Customers
        (firstName,lastName,registrationID,creditLimit,country) values (?,?,?,?,?)", parameters1, parameters2);
        int[] updateCount2 = check testDB->batchUpdate("Insert into Customers2
        (firstName,lastName,registrationID,creditLimit,country) values (?,?,?,?,?)", parameters1, parameters2);
    } onretry {
        returnVal = -1;
    }
    //check whether update action is performed
    table dt = check testDB->select("Select COUNT(*) as countval from Customers where registrationID = 612", ResultCount
    );
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB.stop();
    return (returnVal, count);
}

function testTransactionAbort() returns (int, int) {
    endpoint h2:Client testDB {
        path: "./target/tempdb/",
        name: "TEST_SQL_CONNECTOR_TR",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    };

    int returnVal = -1;
    int count;
    transaction {
        _ = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                            values ('James', 'Clerk', 220, 5000.75, 'USA')");

        _ = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                            values ('James', 'Clerk', 220, 5000.75, 'USA')");
        int i = 0;
        if (i == 0) {
            abort;
        }
        returnVal = 0;
    } onretry {
        returnVal = -1;
    }
    //check whether update action is performed
    table dt = check testDB->select("Select COUNT(*) as countval from Customers where registrationID = 220", ResultCount
    );
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB.stop();
    return (returnVal, count);
}

function testTransactionErrorPanic() returns (int, int, int) {
    endpoint h2:Client testDB {
        path: "./target/tempdb/",
        name: "TEST_SQL_CONNECTOR_TR",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    };

    int returnVal = 0;
    int catchValue = 0;
    int count;
    var ret = trap testTransactionErrorPanicHelper(testDB);
    if (ret is int) {
        returnVal = ret;
    } else if (ret is error) {
        catchValue = -1;
    }
    //check whether update action is performed
    table dt = check testDB->select("Select COUNT(*) as countval from Customers where registrationID = 260", ResultCount
    );
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB.stop();
    return (returnVal, catchValue, count);
}

function testTransactionErrorPanicHelper(h2:Client db) returns int {
    endpoint h2:Client testDB = db;
    int returnVal = 0;
    transaction {
        _ = testDB->update("Insert into Customers (firstName,lastName,
                              registrationID,creditLimit,country) values ('James', 'Clerk', 260, 5000.75, 'USA')");
        int i = 0;
        if (i == 0) {
            error e =  error("error");
            panic e;
        }
    } onretry {
        returnVal = -1;
    }
    return returnVal;
}

function testTransactionErrorPanicAndTrap() returns (int, int, int) {
    endpoint h2:Client testDB {
        path: "./target/tempdb/",
        name: "TEST_SQL_CONNECTOR_TR",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    };

    int returnVal = 0;
    int catchValue = 0;
    int count;
    transaction {
        _ = testDB->update("Insert into Customers (firstName,lastName,registrationID,
                 creditLimit,country) values ('James', 'Clerk', 250, 5000.75, 'USA')");
        var ret = trap testTransactionErrorPanicAndTrapHelper(0);
        if (ret is error) {
            catchValue = -1;
        }
    } onretry {
        returnVal = -1;
    }
    //check whether update action is performed
    table dt = check testDB->select("Select COUNT(*) as countval from Customers where registrationID = 250", ResultCount
    );
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB.stop();
    return (returnVal, catchValue, count);
}

function testTransactionErrorPanicAndTrapHelper(int i) {
    if (i == 0) {
        error err = error("error" );
        panic err;
    }
}

function testTransactionCommitted() returns (int, int) {
    endpoint h2:Client testDB {
        path: "./target/tempdb/",
        name: "TEST_SQL_CONNECTOR_TR",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    };

    int returnVal = 1;
    int count;
    transaction {
        _ = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,
               country) values ('James', 'Clerk', 300, 5000.75, 'USA')");
        _ = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,
               country) values ('James', 'Clerk', 300, 5000.75, 'USA')");
    } onretry {
        returnVal = -1;
    }
    //check whether update action is performed
    table dt = check testDB->select("Select COUNT(*) as countval from Customers where registrationID = 300", ResultCount
    );
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB.stop();
    return (returnVal, count);
}

function testTwoTransactions() returns (int, int, int) {
    endpoint h2:Client testDB {
        path: "./target/tempdb/",
        name: "TEST_SQL_CONNECTOR_TR",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    };

    int returnVal1 = 1;
    int returnVal2 = 1;
    int count;
    transaction {
        _ = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                            values ('James', 'Clerk', 400, 5000.75, 'USA')");
        _ = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                            values ('James', 'Clerk', 400, 5000.75, 'USA')");
    } onretry {
        returnVal1 = 0;
    }

    transaction {
        _ = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                            values ('James', 'Clerk', 400, 5000.75, 'USA')");
        _ = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                            values ('James', 'Clerk', 400, 5000.75, 'USA')");
    } onretry {
        returnVal2 = 0;
    }
    //check whether update action is performed
    table dt = check testDB->select("Select COUNT(*) as countval from Customers where registrationID = 400", ResultCount
    );
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB.stop();
    return (returnVal1, returnVal2, count);
}

function testTransactionWithoutHandlers() returns (int) {
    endpoint h2:Client testDB {
        path: "./target/tempdb/",
        name: "TEST_SQL_CONNECTOR_TR",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    };

    transaction {
        _ = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) values
                                           ('James', 'Clerk', 350, 5000.75, 'USA')");
        _ = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) values
                                           ('James', 'Clerk', 350, 5000.75, 'USA')");
    }

    int count;
    //check whether update action is performed
    table dt = check testDB->select("Select COUNT(*) as countval from Customers where registrationID = 350", ResultCount
    );
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB.stop();
    return count;
}

function testLocalTransactionFailed() returns (string, int) {
    endpoint h2:Client testDB {
        path: "./target/tempdb/",
        name: "TEST_SQL_CONNECTOR_TR",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    };

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
    if (dtRet is table) {
        while (dtRet.hasNext()) {
            var rs = <ResultCount>dtRet.getNext();
            if (rs is ResultCount) {
                count = rs.COUNTVAL;
            }
        }
    }
    testDB.stop();
    return (a, count);
}

function testLocalTransactionFailedHelper(string status, h2:Client db) returns string {
    endpoint h2:Client testDB = db;
    string a = status;
    transaction with retries = 4 {
        a = a + " inTrx";
        _ = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                        values ('James', 'Clerk', 111, 5000.75, 'USA')");
        _ = testDB->update("Insert into Customers2 (firstName,lastName,registrationID,creditLimit,country)
                        values ('Anne', 'Clerk', 111, 5000.75, 'USA')");
    } onretry {
        a = a + " inFld";
    }
    return a;
}

function testLocalTransactionSuccessWithFailed() returns (string, int) {
    endpoint h2:Client testDB {
        path: "./target/tempdb/",
        name: "TEST_SQL_CONNECTOR_TR",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    };

    string a = "beforetx";
    int count = -1;
    string|error ret = trap testLocalTransactionSuccessWithFailedHelper(a, testDB);
    if (ret is string) {
        a = ret;
    } else {
        a =  a + "trapped";
    }
    a = a + " afterTrx";
    var dt = testDB->select("Select COUNT(*) as countval from Customers where registrationID = 222", ResultCount
    );
    if (dt is table) {
        while (dt.hasNext()) {
            var rs = <ResultCount>dt.getNext();
            if (rs is ResultCount) {
                count = rs.COUNTVAL;
            }
        }
    }
    testDB.stop();
    return (a, count);
}

function testLocalTransactionSuccessWithFailedHelper(string status, h2:Client db) returns string {
    endpoint h2:Client testDB = db;
    int i = 0;
    string a = status;
    transaction with retries = 4 {
        a = a + " inTrx";
        _ = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                    values ('James', 'Clerk', 222, 5000.75, 'USA')");
        if (i == 2) {
            _ = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                        values ('Anne', 'Clerk', 222, 5000.75, 'USA')");
        } else {
            _ = testDB->update("Insert into Customers2 (firstName,lastName,registrationID,creditLimit,country)
                                        values ('Anne', 'Clerk', 222, 5000.75, 'USA')");
        }
    } onretry {
        a = a + " inFld";
        i = i + 1;
    }
    return a;
}

function testLocalTransactionFailedWithNextupdate() returns (int) {
    endpoint h2:Client testDB1 {
        path: "./target/tempdb/",
        name: "TEST_SQL_CONNECTOR_TR",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    };

    endpoint h2:Client testDB2 {
        path: "./target/tempdb/",
        name: "TEST_SQL_CONNECTOR_TR",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    };

    int i = 0;
    var ret = trap testLocalTransactionFailedWithNextupdateHelper(testDB1);
    if (ret is error) {
        i = -1;
    }
    _ = testDB1->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                            values ('James', 'Clerk', 12343, 5000.75, 'USA')");
    testDB1.stop();

    table dt = check testDB2->select("Select COUNT(*) as countval from Customers where registrationID = 12343",
        ResultCount);
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        i = rs.COUNTVAL;
    }
    testDB2.stop();
    return i;
}

function testLocalTransactionFailedWithNextupdateHelper(h2:Client db) {
    endpoint h2:Client testDB = db;
    transaction {
        _ = testDB->update("Insert into Customers (firstNamess,lastName,registrationID,creditLimit,country)
                                    values ('James', 'Clerk', 1234, 5000.75, 'USA')");
    }
}

function testNestedTwoLevelTransactionSuccess() returns (int, int) {
    endpoint h2:Client testDB {
        path: "./target/tempdb/",
        name: "TEST_SQL_CONNECTOR_TR",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    };

    int returnVal = 0;
    int count;
    transaction {
        _ = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                values ('James', 'Clerk', 333, 5000.75, 'USA')");
        transaction {
            _ = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                values ('James', 'Clerk', 333, 5000.75, 'USA')");
        }
    } onretry {
        returnVal = -1;
    }
    //check whether update action is performed
    table dt = check testDB->select("Select COUNT(*) as countval from Customers where registrationID = 333", ResultCount
    );
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB.stop();
    return (returnVal, count);
}

function testNestedThreeLevelTransactionSuccess() returns (int, int) {
    endpoint h2:Client testDB {
        path: "./target/tempdb/",
        name: "TEST_SQL_CONNECTOR_TR",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    };

    int returnVal = 0;
    int count;
    transaction {
        _ = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                values ('James', 'Clerk', 444, 5000.75, 'USA')");
        transaction {
            _ = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                values ('James', 'Clerk', 444, 5000.75, 'USA')");
            transaction {
                _ = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                values ('James', 'Clerk', 444, 5000.75, 'USA')");
            }
        }
    } onretry {
        returnVal = -1;
    }
    //check whether update action is performed
    table dt = check testDB->select("Select COUNT(*) as countval from Customers where registrationID = 444", ResultCount
    );
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB.stop();
    return (returnVal, count);
}

function testNestedThreeLevelTransactionFailed() returns (int, int) {
    endpoint h2:Client testDB {
        path: "./target/tempdb/",
        name: "TEST_SQL_CONNECTOR_TR",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    };

    int returnVal = 0;
    int count;
    var ret = trap testNestedThreeLevelTransactionFailedHelper(testDB);
    if (ret is int) {
        returnVal =  ret;
    }
    //check whether update action is performed
    var dt = testDB->select("Select COUNT(*) as countval from Customers where registrationID = 555", ResultCount
    );
    if (dt is table) {
        while (dt.hasNext()) {
            var rs = <ResultCount>dt.getNext();
            if (rs is ResultCount) {
                count = rs.COUNTVAL;
            }
        }
    }
    testDB.stop();
    return (returnVal, count);
}

function testNestedThreeLevelTransactionFailedHelper(h2:Client db) returns int {
    endpoint h2:Client testDB = db;
    int returnVal = 0;
    transaction {
        _ = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                        values ('James', 'Clerk', 555, 5000.75, 'USA')");
        transaction {
            _ = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                            values ('James', 'Clerk', 555, 5000.75, 'USA')");
            transaction {
            _ = testDB->update("Insert into Customers (invalidColumn,lastName,registrationID,creditLimit,country)
                                            values ('James', 'Clerk', 555, 5000.75, 'USA')");
            }
        }
    } onretry {
        returnVal = -1;
    }
    return returnVal;
}

function testNestedThreeLevelTransactionFailedWithRetrySuccess() returns (int, int, string) {
    endpoint h2:Client testDB {
        path: "./target/tempdb/",
        name: "TEST_SQL_CONNECTOR_TR",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    };

    int returnVal = 0;
    string a = "start";
    int count;
    var ret = trap testNestedThreeLevelTransactionFailedWithRetrySuccessHelper(a, testDB);
    if (ret is (string, int)) {
        (a, returnVal) = ret;
    }
    //check whether update action is performed
    var dt = testDB->select("Select COUNT(*) as countval from Customers where registrationID = 666", ResultCount
    );
    if (dt is table) {
        while (dt.hasNext()) {
            var rs = <ResultCount>dt.getNext();
            if (rs is ResultCount) {
                count = rs.COUNTVAL;
            }
        }
    }
    testDB.stop();
    return (returnVal, count, a);
}

function testNestedThreeLevelTransactionFailedWithRetrySuccessHelper(string status, h2:Client db) returns (string, int) {
    endpoint h2:Client testDB = db;
    int returnVal = 0;
    int index = 0;
    string a = status;
    transaction {
        a = a + " txL1";
        _ = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                        values ('James', 'Clerk', 666, 5000.75, 'USA')");
        transaction {
            a = a + " txL2";
            _ = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                            values ('James', 'Clerk', 666, 5000.75, 'USA')");
            transaction with retries = 2 {
                a = a + " txL3";
                if (index == 1) {
                    a = a + " txL3_If";
                    _ = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                                    values ('James', 'Clerk', 666, 5000.75, 'USA')");
                } else {
                    a = a + " txL3_Else";
                    _ = testDB->update("Insert into Customers (invalidColumn,lastName,registrationID,creditLimit,country)
                                                    values ('James', 'Clerk', 666, 5000.75, 'USA')");
                }
            } onretry {
                a = a + " txL3_Failed";
                index = index + 1;
            }
        }
    } onretry {
        a = a + " txL1_Falied";
        returnVal = -1;
    }
    return (a, returnVal);
}

function testLocalTransactionWithSelectAndForeachIteration() returns (int, int) {
    endpoint h2:Client testDB {
        path: "./target/tempdb/",
        name: "TEST_SQL_CONNECTOR_TR",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 5 }
    };

    _ = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                values ('James', 'Clerk', 900, 5000.75, 'USA')");
    _ = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                values ('James', 'Clerk', 900, 5000.75, 'USA')");

    int returnVal = 0;
    int count;
    transaction {
        table<ResultCount> dt1 = check testDB->select("Select COUNT(*) as countval from Customers where
            registrationID = 900", ResultCount);
        foreach row in dt1 {
            count = row.COUNTVAL;
        }

        table<ResultCount> dt2 = check testDB->select("Select COUNT(*) as countval from Customers where
            registrationID = 900", ResultCount);
        foreach row in dt2 {
            count = row.COUNTVAL;
        }
    } onretry {
        returnVal = -1;
    }
    testDB.stop();
    return (returnVal, count);
}

function testLocalTransactionWithSelectAndHasNextIteration() returns (int, int) {
    endpoint h2:Client testDB {
        path: "./target/tempdb/",
        name: "TEST_SQL_CONNECTOR_TR",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 5 }
    };

    _ = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                values ('James', 'Clerk', 901, 5000.75, 'USA')");
    _ = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                values ('James', 'Clerk', 901, 5000.75, 'USA')");

    int returnVal = 0;
    int count;
    transaction {
        table<ResultCount> dt1 = check testDB->select("Select COUNT(*) as countval from Customers where
            registrationID = 901", ResultCount);
        while (dt1.hasNext()) {
            ResultCount rs = check <ResultCount>dt1.getNext();
            count = rs.COUNTVAL;
        }

        table<ResultCount> dt2 = check testDB->select("Select COUNT(*) as countval from Customers where
            registrationID = 901", ResultCount);
        while (dt2.hasNext()) {
            ResultCount rs = check <ResultCount>dt2.getNext();
            count = rs.COUNTVAL;
        }
    } onretry {
        returnVal = -1;
    }
    testDB.stop();
    return (returnVal, count);
}

function testCloseConnectionPool() returns (int) {
    endpoint h2:Client testDB {
        path: "./target/tempdb/",
        name: "TEST_SQL_CONNECTOR_TR",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    };

    int count;
    table dt = check testDB->select("SELECT COUNT(*) as countVal FROM INFORMATION_SCHEMA.SESSIONS", ResultCount);
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB.stop();
    return count;
}
