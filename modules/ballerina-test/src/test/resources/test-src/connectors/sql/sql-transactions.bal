import ballerina.data.sql;

struct ResultCount {
    int COUNTVAL;
}

function testLocalTransacton () (int returnVal, int count) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                               0, "TEST_SQL_CONNECTOR_TR", "SA", "", {maximumPoolSize:1});
    }
    returnVal = 0;
    transaction {
        _ = testDB.update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                values ('James', 'Clerk', 200, 5000.75, 'USA')", null);
        _ = testDB.update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                values ('James', 'Clerk', 200, 5000.75, 'USA')", null);
    } aborted {
        returnVal = -1;
    }
    //check whether update action is performed
    datatable dt = testDB.select("Select COUNT(*) as countval from Customers where registrationID = 200", null);
    while (dt.hasNext()) {
        var rs, err = (ResultCount)dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB.close();
    return;
}

function testTransactonRollback () (int returnVal, int count) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                               0, "TEST_SQL_CONNECTOR_TR", "SA", "", {maximumPoolSize:1});
    }
    returnVal = 0;
    try {
        transaction {
            _ = testDB.update("Insert into Customers (firstName,lastName,registrationID,
                creditLimit,country) values ('James', 'Clerk', 210, 5000.75, 'USA')", null);
            _ = testDB.update("Insert into Customers2 (firstName,lastName,registrationID,
                creditLimit,country) values ('James', 'Clerk', 210, 5000.75, 'USA')", null);
        } aborted {
            returnVal = -1;
        }
    } catch (error e) {
        // ignore.
    }
    //check whether update action is performed
    datatable dt = testDB.select("Select COUNT(*) as countval from Customers where registrationID = 210", null);
    while (dt.hasNext()) {
        var rs, err = (ResultCount)dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB.close();
    return;
}

function testTransactonAbort () (int returnVal, int count) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                               0, "TEST_SQL_CONNECTOR_TR", "SA", "", {maximumPoolSize:1});
    }
    returnVal = 0;
    transaction {
        int insertCount = testDB.update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                            values ('James', 'Clerk', 220, 5000.75, 'USA')", null);

        insertCount = testDB.update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                            values ('James', 'Clerk', 220, 5000.75, 'USA')", null);
        int i = 0;
        if (i == 0) {
            abort;
        }
    } aborted {
        returnVal = -1;
    }
    //check whether update action is performed
    datatable dt = testDB.select("Select COUNT(*) as countval from Customers where registrationID = 220", null);
    while (dt.hasNext()) {
        var rs, err = (ResultCount)dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB.close();
    return;
}

function testTransactonErrorThrow () (int returnVal, int catchValue, int count) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
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
                error err = {msg:"error"};
                throw err;
            }
        } aborted {
            returnVal = -1;
        }
    } catch (error err) {
        catchValue = -1;
    }
    //check whether update action is performed
    datatable dt = testDB.select("Select COUNT(*) as countval from Customers where registrationID = 260", null);
    while (dt.hasNext()) {
        var rs, err = (ResultCount)dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB.close();
    return;
}

function testTransactionErrorThrowAndCatch () (int returnVal, int catchValue, int count) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
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
                error err = {msg:"error"};
                throw err;
            }
        } catch (error err) {
            catchValue = -1;
        }
    } aborted {
        returnVal = -1;
    }
    //check whether update action is performed
    datatable dt = testDB.select("Select COUNT(*) as countval from Customers where registrationID = 250", null);
    while (dt.hasNext()) {
        var rs, err = (ResultCount)dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB.close();
    return;
}

function testTransactonCommitted () (int returnVal, int count) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                        0, "TEST_SQL_CONNECTOR_TR", "SA", "", {maximumPoolSize:1});
    }
    returnVal = 0;
    transaction {
        _ = testDB.update("Insert into Customers (firstName,lastName,registrationID,creditLimit,
               country) values ('James', 'Clerk', 300, 5000.75, 'USA')", null);
        _ = testDB.update("Insert into Customers (firstName,lastName,registrationID,creditLimit,
               country) values ('James', 'Clerk', 300, 5000.75, 'USA')", null);
    } committed {
        returnVal = 1;
    }
    //check whether update action is performed
    datatable dt = testDB.select("Select COUNT(*) as countval from Customers where registrationID = 300", null);
    while (dt.hasNext()) {
        var rs, err = (ResultCount)dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB.close();
    return;
}

function testTransactonHandlerOrder () (int returnVal1, int returnVal2, int count) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                  0, "TEST_SQL_CONNECTOR_TR", "SA", "", {maximumPoolSize:1});
    }
    returnVal1 = 0;
    returnVal2 = 0;
    transaction {
        _ = testDB.update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                            values ('James', 'Clerk', 400, 5000.75, 'USA')", null);
        _ = testDB.update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                            values ('James', 'Clerk', 400, 5000.75, 'USA')", null);
    } committed {
        returnVal1 = 1;
    } aborted {
        returnVal1 = -1;
    }

    transaction {
        _ = testDB.update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                            values ('James', 'Clerk', 400, 5000.75, 'USA')", null);
        _ = testDB.update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                            values ('James', 'Clerk', 400, 5000.75, 'USA')", null);
    } aborted {
        returnVal2 = -1;
    } committed {
        returnVal2 = 1;
    }
    //check whether update action is performed
    datatable dt = testDB.select("Select COUNT(*) as countval from Customers where registrationID = 400", null);
    while (dt.hasNext()) {
        var rs, err = (ResultCount)dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB.close();
    return returnVal1, returnVal2, count;
}

function testTransactonWithoutHandlers () (int count) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                              0, "TEST_SQL_CONNECTOR_TR", "SA", "", {maximumPoolSize:1});
    }
    transaction {
        _ = testDB.update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) values
                                           ('James', 'Clerk', 350, 5000.75, 'USA')", null);
        _ = testDB.update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) values
                                           ('James', 'Clerk', 350, 5000.75, 'USA')", null);
    }
    //check whether update action is performed
    datatable dt = testDB.select("Select COUNT(*) as countval from Customers where
                                      registrationID = 350", null);
    while (dt.hasNext()) {
        var rs, err = (ResultCount)dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB.close();
    return;
}

function testLocalTransactionFailed () (string, int) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                             0, "TEST_SQL_CONNECTOR_TR", "SA", "", {maximumPoolSize:1});
    }
    string a = "beforetx";
    int count = -1;
    try {
        transaction {
            a = a + " inTrx";
            _ = testDB.update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                        values ('James', 'Clerk', 111, 5000.75, 'USA')", null);
            _ = testDB.update("Insert into Customers2 (firstName,lastName,registrationID,creditLimit,country)
                        values ('Anne', 'Clerk', 111, 5000.75, 'USA')", null);
        } failed {
            a = a + " inFld";
            retry 4;
        } aborted {
            a = a + " inAbrt";
        } committed {
            a = a + " inCmt";
        }
    } catch (error e) {
        a = a + " inCatch";
    }
    a = a + " afterTrx";
    datatable dt = testDB.select("Select COUNT(*) as countval from Customers where registrationID = 111", null);
    while (dt.hasNext()) {
        var rs, err = (ResultCount)dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB.close();
    return a, count;
}

function testLocalTransactonSuccessWithFailed () (string, int) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                           0, "TEST_SQL_CONNECTOR_TR", "SA", "", {maximumPoolSize:1});
    }
    string a = "beforetx";
    int count = -1;
    int i = 0;
    try {
        transaction {
            a = a + " inTrx";
                _ = testDB.update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                            values ('James', 'Clerk', 222, 5000.75, 'USA')", null);
            if (i == 2 ){
                _ = testDB.update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                            values ('Anne', 'Clerk', 222, 5000.75, 'USA')", null);
            } else {
                _ = testDB.update("Insert into Customers2 (firstName,lastName,registrationID,creditLimit,country)
                            values ('Anne', 'Clerk', 222, 5000.75, 'USA')", null);
            }
        } failed {
            a = a + " inFld";
            i = i + 1;
            retry 4;
        } aborted {
            a = a + " inAbrt";
        } committed {
            a = a + " inCmt";
        }
    } catch (error e) {
        a = a + " inCatch";
    }
    a = a + " afterTrx";
    datatable dt = testDB.select("Select COUNT(*) as countval from Customers where registrationID = 222", null);
    while (dt.hasNext()) {
        var rs, err = (ResultCount)dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB.close();
    return a, count;
}
