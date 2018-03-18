import ballerina.data.sql;

struct ResultCount {
    int COUNTVAL;
}

function testLocalTransacton () (int returnVal, int count) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_SQL_CONNECTOR_TR",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    returnVal = 0;
    transaction {
        _ = testDB -> update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                values ('James', 'Clerk', 200, 5000.75, 'USA')", null);
        _ = testDB -> update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                values ('James', 'Clerk', 200, 5000.75, 'USA')", null);
    } onretry {
        returnVal = -1;
    }
    //check whether update action is performed
    table dt = testDB -> select("Select COUNT(*) as countval from Customers where registrationID = 200", null,
                                 typeof ResultCount);
    while (dt.hasNext()) {
        var rs, err = (ResultCount)dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB -> close();
    return;
}

function testTransactonRollback () (int returnVal, int count) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_SQL_CONNECTOR_TR",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    returnVal = 0;
    try {
        transaction {
            _ = testDB -> update("Insert into Customers (firstName,lastName,registrationID,
                creditLimit,country) values ('James', 'Clerk', 210, 5000.75, 'USA')", null);
            _ = testDB -> update("Insert into Customers2 (firstName,lastName,registrationID,
                creditLimit,country) values ('James', 'Clerk', 210, 5000.75, 'USA')", null);
        } onretry {
            returnVal = -1;
        }
    } catch (error e) {
        // ignore.
    }
    //check whether update action is performed
    table dt = testDB -> select("Select COUNT(*) as countval from Customers where registrationID = 210", null,
                                 typeof ResultCount);
    while (dt.hasNext()) {
        var rs, err = (ResultCount)dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB -> close();
    return;
}

function testTransactonAbort () (int returnVal, int count) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_SQL_CONNECTOR_TR",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    returnVal = -1;
    transaction {
        int insertCount = testDB -> update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                            values ('James', 'Clerk', 220, 5000.75, 'USA')", null);

        insertCount = testDB -> update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                            values ('James', 'Clerk', 220, 5000.75, 'USA')", null);
        int i = 0;
        if (i == 0) {
            abort;
        }
        returnVal = 0;
    } onretry {
        returnVal = -1;
    }
    //check whether update action is performed
    table dt = testDB -> select("Select COUNT(*) as countval from Customers where registrationID = 220", null,
                                 typeof ResultCount);
    while (dt.hasNext()) {
        var rs, err = (ResultCount)dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB -> close();
    return;
}

function testTransactonErrorThrow () (int returnVal, int catchValue, int count) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_SQL_CONNECTOR_TR",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

	returnVal = 0;
    catchValue = 0;
    try {
        transaction {
            int insertCount = testDB -> update("Insert into Customers (firstName,lastName,
                      registrationID,creditLimit,country) values ('James', 'Clerk', 260, 5000.75, 'USA')", null);
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
    table dt = testDB -> select("Select COUNT(*) as countval from Customers where registrationID = 260", null,
                                 typeof ResultCount);
    while (dt.hasNext()) {
        var rs, err = (ResultCount)dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB -> close();
    return;
}

function testTransactionErrorThrowAndCatch () (int returnVal, int catchValue, int count) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_SQL_CONNECTOR_TR",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

	returnVal = 0;
    catchValue = 0;
    transaction {
        int insertCount = testDB -> update("Insert into Customers (firstName,lastName,registrationID,
                 creditLimit,country) values ('James', 'Clerk', 250, 5000.75, 'USA')", null);
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
    table dt = testDB -> select("Select COUNT(*) as countval from Customers where registrationID = 250", null,
                                 typeof ResultCount);
    while (dt.hasNext()) {
        var rs, err = (ResultCount)dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB -> close();
    return;
}

function testTransactonCommitted () (int returnVal, int count) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_SQL_CONNECTOR_TR",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

	returnVal = 1;
    transaction {
        _ = testDB -> update("Insert into Customers (firstName,lastName,registrationID,creditLimit,
               country) values ('James', 'Clerk', 300, 5000.75, 'USA')", null);
        _ = testDB -> update("Insert into Customers (firstName,lastName,registrationID,creditLimit,
               country) values ('James', 'Clerk', 300, 5000.75, 'USA')", null);
    } onretry {
        returnVal = -1;
    }
    //check whether update action is performed
    table dt = testDB -> select("Select COUNT(*) as countval from Customers where registrationID = 300", null,
                                 typeof ResultCount);
    while (dt.hasNext()) {
        var rs, err = (ResultCount)dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB -> close();
    return;
}

function testTwoTransactons () (int returnVal1, int returnVal2, int count) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_SQL_CONNECTOR_TR",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    returnVal1 = 1;
    returnVal2 = 1;
    transaction {
        _ = testDB -> update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                            values ('James', 'Clerk', 400, 5000.75, 'USA')", null);
        _ = testDB -> update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                            values ('James', 'Clerk', 400, 5000.75, 'USA')", null);
    } onretry {
        returnVal1 = 0;
    }

    transaction {
        _ = testDB -> update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                            values ('James', 'Clerk', 400, 5000.75, 'USA')", null);
        _ = testDB -> update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                            values ('James', 'Clerk', 400, 5000.75, 'USA')", null);
    } onretry {
        returnVal2 = 0;
    }
    //check whether update action is performed
    table dt = testDB -> select("Select COUNT(*) as countval from Customers where registrationID = 400", null,
                                 typeof ResultCount);
    while (dt.hasNext()) {
        var rs, err = (ResultCount)dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB -> close();
    return returnVal1, returnVal2, count;
}

function testTransactonWithoutHandlers () (int count) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_SQL_CONNECTOR_TR",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

	transaction {
        _ = testDB -> update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) values
                                           ('James', 'Clerk', 350, 5000.75, 'USA')", null);
        _ = testDB -> update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) values
                                           ('James', 'Clerk', 350, 5000.75, 'USA')", null);
    }
    //check whether update action is performed
    table dt = testDB -> select("Select COUNT(*) as countval from Customers where
                                      registrationID = 350", null, typeof ResultCount);
    while (dt.hasNext()) {
        var rs, err = (ResultCount)dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB -> close();
    return;
}

function testLocalTransactionFailed () (string, int) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_SQL_CONNECTOR_TR",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    string a = "beforetx";
    int count = -1;
    try {
        transaction with retries(4) {
            a = a + " inTrx";
            _ = testDB -> update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                        values ('James', 'Clerk', 111, 5000.75, 'USA')", null);
            _ = testDB -> update("Insert into Customers2 (firstName,lastName,registrationID,creditLimit,country)
                        values ('Anne', 'Clerk', 111, 5000.75, 'USA')", null);
        } onretry {
            a = a + " inFld";
        }
    } catch (error e) {
        a = a + " inCatch";
    }
    a = a + " afterTrx";
    table dt = testDB -> select("Select COUNT(*) as countval from Customers where registrationID = 111", null,
                                 typeof ResultCount);
    while (dt.hasNext()) {
        var rs, err = (ResultCount)dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB -> close();
    return a, count;
}

function testLocalTransactonSuccessWithFailed () (string, int) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_SQL_CONNECTOR_TR",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    string a = "beforetx";
    int count = -1;
    int i = 0;
    try {
        transaction with retries(4) {
            a = a + " inTrx";
            _ = testDB -> update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                            values ('James', 'Clerk', 222, 5000.75, 'USA')", null);
            if (i == 2) {
                _ = testDB -> update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                            values ('Anne', 'Clerk', 222, 5000.75, 'USA')", null);
            } else {
                _ = testDB -> update("Insert into Customers2 (firstName,lastName,registrationID,creditLimit,country)
                            values ('Anne', 'Clerk', 222, 5000.75, 'USA')", null);
            }
        } onretry {
            a = a + " inFld";
            i = i + 1;
        }
    } catch (error e) {
        a = a + " inCatch";
    }
    a = a + " afterTrx";
    table dt = testDB -> select("Select COUNT(*) as countval from Customers where registrationID = 222", null,
                                 typeof ResultCount);
    while (dt.hasNext()) {
        var rs, err = (ResultCount)dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB -> close();
    return a, count;
}

function testLocalTransactonFailedWithNextupdate () (int i) {
	endpoint sql:Client testDB1 {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_SQL_CONNECTOR_TR",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };
	
	endpoint sql:Client testDB2 {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_SQL_CONNECTOR_TR",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    i = 0;
    try {
        transaction {
            _ = testDB1 -> update("Insert into Customers (firstNamess,lastName,registrationID,creditLimit,country)
                            values ('James', 'Clerk', 1234, 5000.75, 'USA')", null);
        }
    } catch (error e){
        i = -1;
    }
    _ = testDB1 -> update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                            values ('James', 'Clerk', 12343, 5000.75, 'USA')", null);

    testDB1 -> close();

    table dt = testDB2 -> select("Select COUNT(*) as countval from Customers where registrationID = 12343", null,
                                  typeof ResultCount);
    while (dt.hasNext()) {
        var rs, err = (ResultCount)dt.getNext();
        i = rs.COUNTVAL;
    }
    testDB2 -> close();
    return;
}

function testNestedTwoLevelTransactonSuccess () (int returnVal, int count) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_SQL_CONNECTOR_TR",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };
	
    returnVal = 0;
    transaction {
        _ = testDB -> update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                values ('James', 'Clerk', 333, 5000.75, 'USA')", null);
        transaction {
            _ = testDB -> update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                values ('James', 'Clerk', 333, 5000.75, 'USA')", null);
        }
    } onretry {
        returnVal = -1;
    }
    //check whether update action is performed
    table dt = testDB -> select("Select COUNT(*) as countval from Customers where registrationID = 333", null,
                             typeof ResultCount);
    while (dt.hasNext()) {
        var rs, err = (ResultCount)dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB -> close();
    return;
}

function testNestedThreeLevelTransactonSuccess () (int returnVal, int count) {
	endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_SQL_CONNECTOR_TR",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };
	
    returnVal = 0;
    transaction {
        _ = testDB -> update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                values ('James', 'Clerk', 444, 5000.75, 'USA')", null);
        transaction {
            _ = testDB -> update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                values ('James', 'Clerk', 444, 5000.75, 'USA')", null);
            transaction {
                _ = testDB -> update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                values ('James', 'Clerk', 444, 5000.75, 'USA')", null);
            }
        }
    } onretry {
        returnVal = -1;
    }
    //check whether update action is performed
    table dt = testDB -> select("Select COUNT(*) as countval from Customers where registrationID = 444", null,
                             typeof ResultCount);
    while (dt.hasNext()) {
        var rs, err = (ResultCount)dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB -> close();
    return;
}

function testNestedThreeLevelTransactonFailed () (int returnVal, int count) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_SQL_CONNECTOR_TR",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    returnVal = 0;
    try {
        transaction {
            _ = testDB -> update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                values ('James', 'Clerk', 555, 5000.75, 'USA')", null);
            transaction {
                _ = testDB -> update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                values ('James', 'Clerk', 555, 5000.75, 'USA')", null);
                transaction {
                    _ = testDB -> update("Insert into Customers (invalidColumn,lastName,registrationID,creditLimit,country)
                                values ('James', 'Clerk', 555, 5000.75, 'USA')", null);
                }
            }
        } onretry {
            returnVal = -1;
        }
    } catch (error e) {
        // ignore.
    }
    //check whether update action is performed
    table dt = testDB -> select("Select COUNT(*) as countval from Customers where registrationID = 555", null,
                             typeof ResultCount);
    while (dt.hasNext()) {
        var rs, err = (ResultCount)dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB -> close();
    return;
}

function testNestedThreeLevelTransactonFailedWithRetrySuccess () (int returnVal, int count, string a) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_SQL_CONNECTOR_TR",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    returnVal = 0;
    int index = 0;
    a = "start";
    try {
        transaction {
            a = a + " txL1";
            _ = testDB -> update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                values ('James', 'Clerk', 666, 5000.75, 'USA')", null);
            transaction {
                a = a + " txL2";
                _ = testDB -> update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                values ('James', 'Clerk', 666, 5000.75, 'USA')", null);
                transaction with retries(2){
                    a = a + " txL3";
                    if (index == 1) {
                        a = a + " txL3_If";
                        _ = testDB -> update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                values ('James', 'Clerk', 666, 5000.75, 'USA')", null);
                    } else {
                        a = a + " txL3_Else";
                         _ = testDB -> update("Insert into Customers (invalidColumn,lastName,registrationID,creditLimit,country)
                                values ('James', 'Clerk', 666, 5000.75, 'USA')", null);
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
    table dt = testDB -> select("Select COUNT(*) as countval from Customers where registrationID = 666", null,
                             typeof ResultCount);
    while (dt.hasNext()) {
        var rs, err = (ResultCount)dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB -> close();
    return;
}


function testCloseConnectionPool () (int count) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_SQL_CONNECTOR_TR",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };
	
    table dt = testDB -> select("SELECT COUNT(*) as countVal FROM INFORMATION_SCHEMA.SYSTEM_SESSIONS", null,
                             typeof ResultCount);
    while (dt.hasNext()) {
        var rs, err = (ResultCount)dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB -> close();
    return;
}
