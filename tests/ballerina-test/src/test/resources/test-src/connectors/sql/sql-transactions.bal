import ballerina.data.sql;

struct ResultCount {
    int COUNTVAL;
}

function testLocalTransacton () (int returnVal, int count) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                               0, "TEST_SQL_CONNECTOR_TR", "SA", "", {maximumPoolSize:1});
    }
    returnVal = 0;
    transaction {
        _ = testDB.update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                values ('James', 'Clerk', 200, 5000.75, 'USA')", null);
        _ = testDB.update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                values ('James', 'Clerk', 200, 5000.75, 'USA')", null);
    } failed {
        returnVal = -1;
    }
    //check whether update action is performed
    table dt = testDB.select("Select COUNT(*) as countval from Customers where registrationID = 200", null,
                                 typeof ResultCount);
    while (dt.hasNext()) {
        var rs, err = (ResultCount)dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB.close();
    return;
}

function testTransactonRollback () (int returnVal, int count) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                               0, "TEST_SQL_CONNECTOR_TR", "SA", "", {maximumPoolSize:1});
    }
    returnVal = 0;
    try {
        transaction {
            _ = testDB.update("Insert into Customers (firstName,lastName,registrationID,
                creditLimit,country) values ('James', 'Clerk', 210, 5000.75, 'USA')", null);
            _ = testDB.update("Insert into Customers2 (firstName,lastName,registrationID,
                creditLimit,country) values ('James', 'Clerk', 210, 5000.75, 'USA')", null);
        } failed {
            returnVal = -1;
        }
    } catch (error e) {
        // ignore.
    }
    //check whether update action is performed
    table dt = testDB.select("Select COUNT(*) as countval from Customers where registrationID = 210", null,
                                 typeof ResultCount);
    while (dt.hasNext()) {
        var rs, err = (ResultCount)dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB.close();
    return;
}

function testTransactonAbort () (int returnVal, int count) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                               0, "TEST_SQL_CONNECTOR_TR", "SA", "", {maximumPoolSize:1});
    }
    returnVal = -1;
    transaction {
        int insertCount = testDB.update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                            values ('James', 'Clerk', 220, 5000.75, 'USA')", null);

        insertCount = testDB.update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                            values ('James', 'Clerk', 220, 5000.75, 'USA')", null);
        int i = 0;
        if (i == 0) {
            abort;
        }
        returnVal = 0;
    } failed {
        returnVal = -1;
    }
    //check whether update action is performed
    table dt = testDB.select("Select COUNT(*) as countval from Customers where registrationID = 220", null,
                                 typeof ResultCount);
    while (dt.hasNext()) {
        var rs, err = (ResultCount)dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB.close();
    return;
}

function testTransactonErrorThrow () (int returnVal, int catchValue, int count) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                                 0, "TEST_SQL_CONNECTOR_TR", "SA", "", {maximumPoolSize:1});
    }
    returnVal = 0;
    catchValue = 0;
    try {
        transaction {
            int insertCount = testDB.update("Insert into Customers (firstName,lastName,
                      registrationID,creditLimit,country) values ('James', 'Clerk', 260, 5000.75, 'USA')", null);
            int i = 0;
            if (i == 0) {
                error err = {message:"error"};
                throw err;
            }
        } failed {
            returnVal = -1;
        }
    } catch (error err) {
        catchValue = -1;
    }
    //check whether update action is performed
    table dt = testDB.select("Select COUNT(*) as countval from Customers where registrationID = 260", null,
                                 typeof ResultCount);
    while (dt.hasNext()) {
        var rs, err = (ResultCount)dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB.close();
    return;
}

function testTransactionErrorThrowAndCatch () (int returnVal, int catchValue, int count) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                                   0, "TEST_SQL_CONNECTOR_TR", "SA", "", {maximumPoolSize:1});
    }
    returnVal = 0;
    catchValue = 0;
    transaction {
        int insertCount = testDB.update("Insert into Customers (firstName,lastName,registrationID,
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
    } failed {
        returnVal = -1;
    }
    //check whether update action is performed
    table dt = testDB.select("Select COUNT(*) as countval from Customers where registrationID = 250", null,
                                 typeof ResultCount);
    while (dt.hasNext()) {
        var rs, err = (ResultCount)dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB.close();
    return;
}

function testTransactonCommitted () (int returnVal, int count) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                                        0, "TEST_SQL_CONNECTOR_TR", "SA", "", {maximumPoolSize:1});
    }
    returnVal = 1;
    transaction {
        _ = testDB.update("Insert into Customers (firstName,lastName,registrationID,creditLimit,
               country) values ('James', 'Clerk', 300, 5000.75, 'USA')", null);
        _ = testDB.update("Insert into Customers (firstName,lastName,registrationID,creditLimit,
               country) values ('James', 'Clerk', 300, 5000.75, 'USA')", null);
    } failed {
        returnVal = -1;
    }
    //check whether update action is performed
    table dt = testDB.select("Select COUNT(*) as countval from Customers where registrationID = 300", null,
                                 typeof ResultCount);
    while (dt.hasNext()) {
        var rs, err = (ResultCount)dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB.close();
    return;
}

function testTwoTransactons () (int returnVal1, int returnVal2, int count) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                                  0, "TEST_SQL_CONNECTOR_TR", "SA", "", {maximumPoolSize:1});
    }
    returnVal1 = 1;
    returnVal2 = 1;
    transaction {
        _ = testDB.update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                            values ('James', 'Clerk', 400, 5000.75, 'USA')", null);
        _ = testDB.update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                            values ('James', 'Clerk', 400, 5000.75, 'USA')", null);
    } failed {
        returnVal1 = 0;
    }

    transaction {
        _ = testDB.update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                            values ('James', 'Clerk', 400, 5000.75, 'USA')", null);
        _ = testDB.update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                            values ('James', 'Clerk', 400, 5000.75, 'USA')", null);
    } failed {
        returnVal2 = 0;
    }
    //check whether update action is performed
    table dt = testDB.select("Select COUNT(*) as countval from Customers where registrationID = 400", null,
                                 typeof ResultCount);
    while (dt.hasNext()) {
        var rs, err = (ResultCount)dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB.close();
    return returnVal1, returnVal2, count;
}

function testTransactonWithoutHandlers () (int count) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                              0, "TEST_SQL_CONNECTOR_TR", "SA", "", {maximumPoolSize:1});
    }
    transaction {
        _ = testDB.update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) values
                                           ('James', 'Clerk', 350, 5000.75, 'USA')", null);
        _ = testDB.update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) values
                                           ('James', 'Clerk', 350, 5000.75, 'USA')", null);
    }
    //check whether update action is performed
    table dt = testDB.select("Select COUNT(*) as countval from Customers where
                                      registrationID = 350", null, typeof ResultCount);
    while (dt.hasNext()) {
        var rs, err = (ResultCount)dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB.close();
    return;
}

function testLocalTransactionFailed () (string, int) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                             0, "TEST_SQL_CONNECTOR_TR", "SA", "", {maximumPoolSize:1});
    }
    string a = "beforetx";
    int count = -1;
    try {
        transaction with retries(4) {
            a = a + " inTrx";
            _ = testDB.update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                        values ('James', 'Clerk', 111, 5000.75, 'USA')", null);
            _ = testDB.update("Insert into Customers2 (firstName,lastName,registrationID,creditLimit,country)
                        values ('Anne', 'Clerk', 111, 5000.75, 'USA')", null);
        } failed {
            a = a + " inFld";
        }
    } catch (error e) {
        a = a + " inCatch";
    }
    a = a + " afterTrx";
    table dt = testDB.select("Select COUNT(*) as countval from Customers where registrationID = 111", null,
                                 typeof ResultCount);
    while (dt.hasNext()) {
        var rs, err = (ResultCount)dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB.close();
    return a, count;
}

function testLocalTransactonSuccessWithFailed () (string, int) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                           0, "TEST_SQL_CONNECTOR_TR", "SA", "", {maximumPoolSize:1});
    }
    string a = "beforetx";
    int count = -1;
    int i = 0;
    try {
        transaction with retries(4) {
            a = a + " inTrx";
            _ = testDB.update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                            values ('James', 'Clerk', 222, 5000.75, 'USA')", null);
            if (i == 2) {
                _ = testDB.update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                            values ('Anne', 'Clerk', 222, 5000.75, 'USA')", null);
            } else {
                _ = testDB.update("Insert into Customers2 (firstName,lastName,registrationID,creditLimit,country)
                            values ('Anne', 'Clerk', 222, 5000.75, 'USA')", null);
            }
        } failed {
            a = a + " inFld";
            i = i + 1;
        }
    } catch (error e) {
        a = a + " inCatch";
    }
    a = a + " afterTrx";
    table dt = testDB.select("Select COUNT(*) as countval from Customers where registrationID = 222", null,
                                 typeof ResultCount);
    while (dt.hasNext()) {
        var rs, err = (ResultCount)dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB.close();
    return a, count;
}

function testLocalTransactonFailedWithNextupdate () (int i) {
    endpoint<sql:ClientConnector> testDB1 {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                   0, "TEST_SQL_CONNECTOR_TR", "SA", "", {maximumPoolSize:1});
    }

    endpoint<sql:ClientConnector> testDB2 {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                   0, "TEST_SQL_CONNECTOR_TR", "SA", "", {maximumPoolSize:1});
    }
    i = 0;
    try {
        transaction {
            _ = testDB1.update("Insert into Customers (firstNamess,lastName,registrationID,creditLimit,country)
                            values ('James', 'Clerk', 1234, 5000.75, 'USA')", null);
        }
    } catch (error e){
        i = -1;
    }
    _ = testDB1.update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                            values ('James', 'Clerk', 12343, 5000.75, 'USA')", null);

    testDB1.close();

    table dt = testDB2.select("Select COUNT(*) as countval from Customers where registrationID = 12343", null,
                                  typeof ResultCount);
    while (dt.hasNext()) {
        var rs, err = (ResultCount)dt.getNext();
        i = rs.COUNTVAL;
    }
    testDB2.close();
    return;
}
