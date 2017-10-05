import ballerina.data.sql;
import ballerina.lang.errors;
import ballerina.lang.datatables;

struct ResultCount {
    int COUNTVAL;
}

function testLocalTransacton () (int returnVal, int count) {
    sql:ConnectionProperties properties = {maximumPoolSize:1};
    sql:ClientConnector testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/", 0,
                                                            "TEST_SQL_CONNECTOR", "SA", "", properties);
    returnVal = 0;
    sql:Parameter[] parameters = [];
    transaction {
        testDB.update("Insert into Customers
                (firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 200, 5000.75, 'USA')",
                                   parameters);
        testDB.update("Insert into Customers
                (firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 200, 5000.75, 'USA')",
                                   parameters);
    } aborted {
        returnVal = -1;
    }
    //check whether update action is performed
    datatable dt = testDB.select("Select COUNT(*) as countval from Customers where registrationID = 200", parameters);
    errors:TypeCastError err;
    ResultCount rs;
    while (datatables:hasNext(dt)) {
        any dataStruct = datatables:next(dt);
        rs, err = (ResultCount)dataStruct;
        count = rs.COUNTVAL;
    }
    testDB.close();
    return;
}

function testTransactonRollback () (int returnVal, int count) {
    sql:ConnectionProperties properties = {maximumPoolSize:1};
    sql:ClientConnector testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/", 0,
                                                            "TEST_SQL_CONNECTOR", "SA", "", properties);
    returnVal = 0;
    sql:Parameter[] parameters = [];
    try {
        transaction {
            testDB.update("Insert into Customers (firstName,lastName,registrationID,
                creditLimit,country) values ('James', 'Clerk', 210, 5000.75, 'USA')", parameters);
            testDB.update("Insert into Customers2 (firstName,lastName,registrationID,
                creditLimit,country) values ('James', 'Clerk', 210, 5000.75, 'USA')", parameters);
        } aborted {
            returnVal = -1;
        }
    } catch (errors:Error e) {
        // ignore.
    }
    //check whether update action is performed
    datatable dt = testDB.select("Select COUNT(*) as countval from Customers where registrationID = 210", parameters);
    errors:TypeCastError err;
    ResultCount rs;
    while (datatables:hasNext(dt)) {
        any dataStruct = datatables:next(dt);
        rs, err = (ResultCount)dataStruct;
        count = rs.COUNTVAL;
    }
    testDB.close();
    return;
}

function testTransactonAbort () (int returnVal, int count) {
    sql:ConnectionProperties properties = {maximumPoolSize:1};
    sql:ClientConnector testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/", 0,
                                                            "TEST_SQL_CONNECTOR", "SA", "", properties);
    returnVal = 0;
    sql:Parameter[] parameters = [];
    transaction {
        int insertCount = testDB.update("Insert into Customers
                (firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 220, 5000.75, 'USA')",
                                                     parameters);

        insertCount = testDB.update("Insert into Customers
                (firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 220, 5000.75, 'USA')",
                                                 parameters);
        int i = 0;
        if (i == 0) {
            abort;
        }
    } aborted {
        returnVal = -1;
    }
    //check whether update action is performed
    datatable dt = testDB.select("Select COUNT(*) as countval from Customers where registrationID = 220", parameters);
    errors:TypeCastError err;
    ResultCount rs;
    while (datatables:hasNext(dt)) {
        any dataStruct = datatables:next(dt);
        rs, err = (ResultCount)dataStruct;
        count = rs.COUNTVAL;
    }
    testDB.close();
    return;
}

function testTransactonErrorThrow () (int returnVal, int catchValue, int count) {
    sql:ConnectionProperties properties = {maximumPoolSize:1};
    sql:ClientConnector testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/", 0,
                                                            "TEST_SQL_CONNECTOR", "SA", "", properties);
    returnVal = 0;
    catchValue = 0;
    sql:Parameter[] parameters = [];
    try {
        transaction {
            int insertCount = testDB.update("Insert into Customers (firstName,lastName,
                      registrationID,creditLimit,country) values ('James', 'Clerk', 260, 5000.75, 'USA')", parameters);
            int i = 0;
            if (i == 0) {
                errors:Error err = {msg:"error"};
                throw err;
            }
        } aborted {
            returnVal = -1;
        }
    } catch (errors:Error err) {
        catchValue = -1;
    }
    //check whether update action is performed
    datatable dt = testDB.select("Select COUNT(*) as countval from Customers where registrationID = 260", parameters);
    ResultCount rs;
    while (datatables:hasNext(dt)) {
        any dataStruct = datatables:next(dt);
        rs, _ = (ResultCount)dataStruct;
        count = rs.COUNTVAL;
    }
    testDB.close();
    return;
}

function testTransactionErrorThrowAndCatch () (int returnVal, int catchValue, int count) {
    sql:ConnectionProperties properties = {maximumPoolSize:1};
    sql:ClientConnector testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/", 0,
                                                            "TEST_SQL_CONNECTOR", "SA", "", properties);
    returnVal = 0;
    catchValue = 0;
    sql:Parameter[] parameters = [];
    transaction {
        int insertCount = testDB.update("Insert into Customers (firstName,lastName,registrationID,
                 creditLimit,country) values ('James', 'Clerk', 250, 5000.75, 'USA')", parameters);
        int i = 0;
        try {
            if (i == 0) {
                errors:Error err = {msg:"error"};
                throw err;
            }
        } catch (errors:Error err) {
            catchValue = -1;
        }
    } aborted {
        returnVal = -1;
    }
    //check whether update action is performed
    errors:TypeCastError err;
    ResultCount rs;
    datatable dt = testDB.select("Select COUNT(*) as countval from Customers where
                                   registrationID = 250", parameters);
    while (datatables:hasNext(dt)) {
        any dataStruct = datatables:next(dt);
        rs, err = (ResultCount)dataStruct;
        count = rs.COUNTVAL;
    }
    testDB.close();
    return;
}

function testTransactonCommitted () (int returnVal, int count) {
    sql:ConnectionProperties properties = {maximumPoolSize:1};
    sql:ClientConnector testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/", 0,
                                                            "TEST_SQL_CONNECTOR", "SA", "", properties);
    returnVal = 0;
    sql:Parameter[] parameters = [];
    transaction {
        testDB.update("Insert into Customers (firstName,lastName,registrationID,creditLimit,
               country) values ('James', 'Clerk', 300, 5000.75, 'USA')", parameters);
        testDB.update("Insert into Customers (firstName,lastName,registrationID,creditLimit,
               country) values ('James', 'Clerk', 300, 5000.75, 'USA')", parameters);
    } committed {
        returnVal = 1;
    }
    //check whether update action is performed
    datatable dt = testDB.select("Select COUNT(*) as countval from Customers where registrationID = 300", parameters);
    errors:TypeCastError err;
    ResultCount rs;
    while (datatables:hasNext(dt)) {
        any dataStruct = datatables:next(dt);
        rs, err = (ResultCount)dataStruct;
        count = rs.COUNTVAL;
    }
    testDB.close();
    return;
}

function testTransactonHandlerOrder () (int returnVal1, int returnVal2, int count) {
    sql:ConnectionProperties properties = {maximumPoolSize:1};
    sql:ClientConnector testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/", 0,
                                                            "TEST_SQL_CONNECTOR", "SA", "", properties);
    returnVal1 = 0;
    returnVal2 = 0;
    sql:Parameter[] parameters = [];
    transaction {
        testDB.update("Insert into Customers
            (firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 400, 5000.75, 'USA')",
                                   parameters);
        testDB.update("Insert into Customers
            (firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 400, 5000.75, 'USA')",
                                   parameters);
    } committed {
        returnVal1 = 1;
    } aborted {
        returnVal1 = -1;
    }

    transaction {
        testDB.update("Insert into Customers
            (firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 400, 5000.75, 'USA')",
                                   parameters);
        testDB.update("Insert into Customers
            (firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 400, 5000.75, 'USA')",
                                   parameters);
    } aborted {
        returnVal2 = -1;
    } committed {
        returnVal2 = 1;
    }
    //check whether update action is performed
    datatable dt = testDB.select("Select COUNT(*) as countval from Customers where registrationID = 400", parameters);
    ResultCount rs;
    while (datatables:hasNext(dt)) {
        any dataStruct = datatables:next(dt);
        rs, _ = (ResultCount)dataStruct;
        count = rs.COUNTVAL;
    }
    testDB.close();
    return returnVal1, returnVal2, count;
}

function testTransactonWithoutHandlers () (int count) {
    sql:ConnectionProperties properties = {maximumPoolSize:1};
    sql:ClientConnector testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/", 0,
                                                            "TEST_SQL_CONNECTOR", "SA", "", properties);
    sql:Parameter[] parameters = [];
    transaction {
        testDB.update("Insert into Customers
                        (firstName,lastName,registrationID,creditLimit,country) values
                                           ('James', 'Clerk', 350, 5000.75, 'USA')", parameters);
        testDB.update("Insert into Customers
                        (firstName,lastName,registrationID,creditLimit,country) values
                                           ('James', 'Clerk', 350, 5000.75, 'USA')", parameters);
    }
    //check whether update action is performed
    datatable dt = testDB.select("Select COUNT(*) as countval from Customers where
                                      registrationID = 350", parameters);
    ResultCount rs;
    while (datatables:hasNext(dt)) {
        any dataStruct = datatables:next(dt);
        rs, _ = (ResultCount)dataStruct;
        count = rs.COUNTVAL;
    }
    testDB.close();
    return;
}

function testLocalTransactionFailed () (string, int) {
    sql:ConnectionProperties properties = {maximumPoolSize:1};
    sql:ClientConnector testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/", 0,
                                                            "TEST_SQL_CONNECTOR", "SA", "", properties);
    sql:Parameter[] parameters = [];
    string a = "beforetx";
    int count = -1;
    try {
        transaction {
            a = a + " inTrx";
            testDB.update("Insert into Customers
               (firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 111, 5000.75, 'USA')",
               parameters);
            testDB.update("Insert into Customers2
               (firstName,lastName,registrationID,creditLimit,country) values ('Anne', 'Clerk', 111, 5000.75, 'USA')",
                                       parameters);
        } failed {
            a = a + " inFld";
            retry 4;
        } aborted {
            a = a + " inAbrt";
        } committed {
            a = a + " inCmt";
        }
    } catch (errors:Error e) {
        a = a + " inCatch";
    }
    a = a + " afterTrx";
    datatable dt = testDB.select("Select COUNT(*) as countval from Customers where
                                          registrationID = 111", parameters);
    ResultCount rs;
    while (datatables:hasNext(dt)) {
        any dataStruct = datatables:next(dt);
        rs, _ = (ResultCount)dataStruct;
        count = rs.COUNTVAL;
    }
    testDB.close();
    return a, count;
}

function testLocalTransactonSuccessWithFailed () (string, int) {
    sql:ConnectionProperties properties = {maximumPoolSize:1};
    sql:ClientConnector testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/", 0,
                                                        "TEST_SQL_CONNECTOR", "SA", "", properties);
    sql:Parameter[] parameters = [];
    string a = "beforetx";
    int count = -1;
    int i = 0;
    try {
        transaction {
            a = a + " inTrx";
                testDB.update("Insert into Customers
                (firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 222, 5000.75, 'USA')",
                parameters);
            if (i == 2 ){
                testDB.update("Insert into Customers
                (firstName,lastName,registrationID,creditLimit,country) values ('Anne', 'Clerk', 222, 5000.75, 'USA')",
                parameters);
            } else {
                testDB.update("Insert into Customers2
                (firstName,lastName,registrationID,creditLimit,country) values ('Anne', 'Clerk', 222, 5000.75, 'USA')",
                parameters);
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
    } catch (errors:Error e) {
        a = a + " inCatch";
    }
    a = a + " afterTrx";
    datatable dt = testDB.select("Select COUNT(*) as countval from Customers where registrationID = 222", parameters);
    ResultCount rs;
    while (datatables:hasNext(dt)) {
        any dataStruct = datatables:next(dt);
        rs, _ = (ResultCount)dataStruct;
        count = rs.COUNTVAL;
    }
    testDB.close();
    return a, count;
}
