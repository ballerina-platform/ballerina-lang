import ballerina/sql;
import ballerina/io;
import ballerina/runtime;

type ResultCount {
    int COUNTVAL,
};

function testLocalTransacton() returns (int, int) {
    endpoint sql:Client testDB {
        url:"hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR_TR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
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
    var temp = testDB->select("Select COUNT(*) as countval from Customers where registrationID = 200", ResultCount);
    table dt = check temp;
    while (dt.hasNext()) {
        var rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }
    _ = testDB->close();
    return (returnVal, count);
}

function testTransactonRollback() returns (int, int) {
    endpoint sql:Client testDB {
        url:"hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR_TR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
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
    var temp = testDB->select("Select COUNT(*) as countval from Customers where registrationID = 210", ResultCount);
    table dt = check temp;

    while (dt.hasNext()) {
        var rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }
    _ = testDB->close();
    return (returnVal, count);
}

function testTransactonAbort() returns (int, int) {
    endpoint sql:Client testDB {
        url:"hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR_TR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
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
    var temp = testDB->select("Select COUNT(*) as countval from Customers where registrationID = 220", ResultCount);
    var dt = check temp;
    while (dt.hasNext()) {
        var rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }
    _ = testDB->close();
    return (returnVal, count);
}

function testTransactonErrorThrow() returns (int, int, int) {
    endpoint sql:Client testDB {
        url:"hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR_TR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    int returnVal = 0;
    int catchValue = 0;
    int count;
    try {
        transaction {
            _ = testDB->update("Insert into Customers (firstName,lastName,
                      registrationID,creditLimit,country) values ('James', 'Clerk', 260, 5000.75, 'USA')");
            int i = 0;
            if (i == 0) {
                error err = {message:"error"};
                throw err;
            }
        } onretry {
            returnVal = -1;
        }
    } catch (error err) {
        catchValue = -1;
    }
    //check whether update action is performed
    var temp = testDB->select("Select COUNT(*) as countval from Customers where registrationID = 260", ResultCount);
    table dt = check temp;
    while (dt.hasNext()) {
        var rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }
    _ = testDB->close();
    return (returnVal, catchValue, count);
}

function testTransactionErrorThrowAndCatch() returns (int, int, int) {
    endpoint sql:Client testDB {
        url:"hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR_TR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    int returnVal = 0;
    int catchValue = 0;
    int count;
    transaction {
        _ = testDB->update("Insert into Customers (firstName,lastName,registrationID,
                 creditLimit,country) values ('James', 'Clerk', 250, 5000.75, 'USA')");
        int i = 0;
        try {
            if (i == 0) {
                error err = {message:"error"};
                throw err;
            }
        } catch (error err) {
            catchValue = -1;
        }
    } onretry {
        returnVal = -1;
    }
    //check whether update action is performed
    var temp = testDB->select("Select COUNT(*) as countval from Customers where registrationID = 250", ResultCount);
    table dt = check temp;
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }
    _ = testDB->close();
    return (returnVal, catchValue, count);
}

function testTransactonCommitted() returns (int, int) {
    endpoint sql:Client testDB {
        url:"hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR_TR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
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
    var temp = testDB->select("Select COUNT(*) as countval from Customers where registrationID = 300", ResultCount);
    table dt = check temp;
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }
    _ = testDB->close();
    return (returnVal, count);
}

function testTwoTransactons() returns (int, int, int) {
    endpoint sql:Client testDB {
        url:"hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR_TR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
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
    var temp = testDB->select("Select COUNT(*) as countval from Customers where registrationID = 400", ResultCount);
    table dt = check temp;
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }
    _ = testDB->close();
    return (returnVal1, returnVal2, count);
}

function testTransactonWithoutHandlers() returns (int) {
    endpoint sql:Client testDB {
        url:"hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR_TR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    transaction {
        _ = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) values
                                           ('James', 'Clerk', 350, 5000.75, 'USA')");
        _ = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) values
                                           ('James', 'Clerk', 350, 5000.75, 'USA')");
    }

    int count;
    //check whether update action is performed
    var temp = testDB->select("Select COUNT(*) as countval from Customers where
                                      registrationID = 350", ResultCount);
    table dt = check temp;
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }
    _ = testDB->close();
    return count;
}

function testLocalTransactionFailed() returns (string, int) {
    endpoint sql:Client testDB {
        url:"hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR_TR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    string a = "beforetx";
    int count = -1;
    try {
        transaction with retries = 4 {
            a = a + " inTrx";
            _ = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                        values ('James', 'Clerk', 111, 5000.75, 'USA')");
            _ = testDB->update("Insert into Customers2 (firstName,lastName,registrationID,creditLimit,country)
                        values ('Anne', 'Clerk', 111, 5000.75, 'USA')");
        } onretry {
            a = a + " inFld";
        }
    } catch (error e) {
        io:println(e);
        a = a + " inCatch";

    }
    a = a + " afterTrx";
    var temp = testDB->select("Select COUNT(*) as countval from Customers where registrationID = 111", ResultCount);
    table dt = check temp;
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }
    _ = testDB->close();
    return (a, count);
}

function testLocalTransactonSuccessWithFailed() returns (string, int) {
    endpoint sql:Client testDB {
        url:"hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR_TR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    string a = "beforetx";
    int count = -1;
    int i = 0;
    try {
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
    } catch (error e) {
        a = a + " inCatch";
    }
    a = a + " afterTrx";
    var temp = testDB->select("Select COUNT(*) as countval from Customers where registrationID = 222", ResultCount);
    table dt = check temp;
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }
    _ = testDB->close();
    return (a, count);
}

function testLocalTransactonFailedWithNextupdate() returns (int) {
    endpoint sql:Client testDB1 {
        url:"hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR_TR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    endpoint sql:Client testDB2 {
        url:"hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR_TR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    int i = 0;
    try {
        transaction {
            _ = testDB1->update("Insert into Customers (firstNamess,lastName,registrationID,creditLimit,country)
                            values ('James', 'Clerk', 1234, 5000.75, 'USA')");
        }
    } catch (error e){
        i = -1;
    }
    _ = testDB1->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                            values ('James', 'Clerk', 12343, 5000.75, 'USA')");

    _ = testDB1->close();

    var temp = testDB2->select("Select COUNT(*) as countval from Customers where registrationID = 12343", ResultCount);
    table dt = check temp;
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        i = rs.COUNTVAL;
    }
    _ = testDB2->close();
    return i;
}

function testNestedTwoLevelTransactonSuccess() returns (int, int) {
    endpoint sql:Client testDB {
        url:"hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR_TR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
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
    var temp = testDB->select("Select COUNT(*) as countval from Customers where registrationID = 333", ResultCount);
    table dt = check temp;
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }
    _ = testDB->close();
    return (returnVal, count);
}

function testNestedThreeLevelTransactonSuccess() returns (int, int) {
    endpoint sql:Client testDB {
        url:"hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR_TR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
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
    var temp = testDB->select("Select COUNT(*) as countval from Customers where registrationID = 444", ResultCount);
    table dt = check temp;
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }
    _ = testDB->close();
    return (returnVal, count);
}

function testNestedThreeLevelTransactonFailed() returns (int, int) {
    endpoint sql:Client testDB {
        url:"hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR_TR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    int returnVal = 0;
    int count;
    try {
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
    } catch (error e) {
        // ignore.
    }
    //check whether update action is performed
    var temp = testDB->select("Select COUNT(*) as countval from Customers where registrationID = 555", ResultCount);
    table dt = check temp;
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }
    _ = testDB->close();
    return (returnVal, count);
}

function testNestedThreeLevelTransactonFailedWithRetrySuccess() returns (int, int, string) {
    endpoint sql:Client testDB {
        url:"hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR_TR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    int returnVal = 0;
    int index = 0;
    string a = "start";
    int count;
    try {
        transaction {
            a = a + " txL1";
            _ = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                values ('James', 'Clerk', 666, 5000.75, 'USA')");
            transaction {
                a = a + " txL2";
                _ = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                values ('James', 'Clerk', 666, 5000.75, 'USA')");
                transaction with retries = 2{
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
    } catch (error e) {
        // ignore.
    }
    //check whether update action is performed
    var temp = testDB->select("Select COUNT(*) as countval from Customers where registrationID = 666", ResultCount);
    table dt = check temp;
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }
    _ = testDB->close();
    return (returnVal, count, a);
}

function testTransactionWithWorkers() returns (int) {
    endpoint sql:Client testDB {
        url:"hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR_TR",
        username:"SA",
        poolOptions:{maximumPoolSize:2}
    };

    transaction {
        invokeWorkers(testDB);
    }

    //check whether update action is performed
    int count;
    var temp = testDB->select("Select COUNT(*) as countval from Customers where registrationID = 834", ResultCount);
    table dt = check temp;
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }
    _ = testDB->close();
    return count;
}

function invokeWorkers(sql:Client testDBClient) {
    endpoint sql:Client testDB = testDBClient;


    worker w1 {
        _ = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                            values ('Anne', 'Clerk', 834, 5000.75, 'USA')");
    }

    worker w2 {
        runtime:sleepCurrentWorker(5000);
        _ = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                            values ('James', 'Clerk', 834, 5000.75, 'USA')");
    }

}

function testCloseConnectionPool() returns (int) {
    endpoint sql:Client testDB {
        url:"hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR_TR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    int count;
    var temp = testDB->select("SELECT COUNT(*) as countVal FROM INFORMATION_SCHEMA.SYSTEM_SESSIONS", ResultCount);
    table dt = check temp;
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }
    _ = testDB->close();
    return count;
}
