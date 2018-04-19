import ballerina/sql;
import ballerina/io;
import ballerina/runtime;

type ResultCount {
    int COUNTVAL,
};

type CustomersTrx {
    string firstName,
    string lastName,
    int registrationID,
    float creditLimit,
    string country,
};

type CustomersTrx2 {
    int customerId,
    string firstName,
    string lastName,
    int registrationID,
    float creditLimit,
    string country,
};

function testLocalTransacton() returns (int, int) {
    endpoint sql:Client testDB {
        url:"hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR_TR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    var temp0 = testDB->getProxyTable("CustomersTrx", CustomersTrx);
    table dt0 = check temp0;

    int returnVal = 0;
    int count;
    transaction {
        CustomersTrx c1 = {firstName:"James", lastName:"Clerk", registrationID:200, creditLimit:5000.75,
            country:"USA"};
        CustomersTrx c2 = {firstName:"James", lastName:"Clerk", registrationID:200, creditLimit:5000.75,
            country:"USA"};
        var result1 = dt0.add(c1);
        var result2 = dt0.add(c2);
    } onretry {
        returnVal = -1;
    }
    //check whether update action is performed
    var temp = testDB->select("Select COUNT(*) as countval from CustomersTrx where registrationID = 200", ResultCount);
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

    var temp0 = testDB->getProxyTable("CustomersTrx", CustomersTrx);
    var temp1 = testDB->getProxyTable("CustomersTrx2", CustomersTrx2);

    table dt0 = check temp0;
    table dt1 = check temp1;

    transaction {
        CustomersTrx c1 = {firstName:"James", lastName:"Clerk", registrationID:295, creditLimit:5000.75,
            country:"USA"};
        CustomersTrx2 c2 = {customerId:1, firstName:"James", lastName:"Clerk", registrationID:295, creditLimit:5000.75,
            country:"USA"};
        var result1 = dt0.add(c1);
        var result2 = dt1.add(c2);

    } onretry {
        returnVal = -1;
    }


    //check whether update action is performed
    var temp = testDB->select("Select COUNT(*) as countval from CustomersTrx where registrationID = 295", ResultCount);

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

    var temp0 = testDB->getProxyTable("CustomersTrx", CustomersTrx);
    table dt0 = check temp0;

    transaction {
        CustomersTrx c1 = {firstName:"James", lastName:"Clerk", registrationID:220, creditLimit:5000.75,
            country:"USA"};
        CustomersTrx c2 = {firstName:"James", lastName:"Clerk", registrationID:220, creditLimit:5000.75,
            country:"USA"};
        var result1 = dt0.add(c1);
        var result2 = dt0.add(c2);
        int i = 0;
        if (i == 0) {
            abort;
        }
        returnVal = 0;
    } onretry {
        returnVal = -1;
    }
    //check whether update action is performed
    var temp = testDB->select("Select COUNT(*) as countval from CustomersTrx where registrationID = 220", ResultCount);
    table dt = check temp;

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
    var temp0 = testDB->getProxyTable("CustomersTrx", CustomersTrx);
    table dt0 = check temp0;

    try {
        transaction {
            CustomersTrx c1 = {firstName:"James", lastName:"Clerk", registrationID:260, creditLimit:5000.75,
                country:"USA"};
            var result = dt0.add(c1);
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
    var temp = testDB->select("Select COUNT(*) as countval from CustomersTrx where registrationID = 260", ResultCount);
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
    var temp0 = testDB->getProxyTable("CustomersTrx", CustomersTrx);
    table dt0 = check temp0;

    transaction {
        CustomersTrx c1 = {firstName:"James", lastName:"Clerk", registrationID:250, creditLimit:5000.75,
            country:"USA"};
        var result = dt0.add(c1);
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
    var temp = testDB->select("Select COUNT(*) as countval from CustomersTrx where registrationID = 250", ResultCount);
    table dt = check temp;

    while (dt.hasNext()) {
        var rs = check <ResultCount>dt.getNext();
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
    var temp0 = testDB->getProxyTable("CustomersTrx", CustomersTrx);

    table dt0 = check temp0;
    transaction {
        CustomersTrx c1 = {firstName:"James", lastName:"Clerk", registrationID:300, creditLimit:5000.75,
            country:"USA"};
        CustomersTrx c2 = {firstName:"James", lastName:"Clerk", registrationID:300, creditLimit:5000.75,
            country:"USA"};
        var result1 = dt0.add(c1);
        var result2 = dt0.add(c2);
    } onretry {
        returnVal = -1;
    }
    //check whether update action is performed
    var temp = testDB->select("Select COUNT(*) as countval from CustomersTrx where registrationID = 300", ResultCount);
    table dt = check temp;
    while (dt.hasNext()) {
        var rs = check <ResultCount>dt.getNext();
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
    var temp0 = testDB->getProxyTable("CustomersTrx", CustomersTrx);
    table dt0 = check temp0;
    transaction {
        CustomersTrx c1 = {firstName:"James", lastName:"Clerk", registrationID:400, creditLimit:5000.75,
            country:"USA"};
        CustomersTrx c2 = {firstName:"James", lastName:"Clerk", registrationID:400, creditLimit:5000.75,
            country:"USA"};
        var result1 = dt0.add(c1);
        var result2 = dt0.add(c2);
    } onretry {
        returnVal1 = 0;
    }

    transaction {
        CustomersTrx c1 = {firstName:"James", lastName:"Clerk", registrationID:400, creditLimit:5000.75,
            country:"USA"};
        CustomersTrx c2 = {firstName:"James", lastName:"Clerk", registrationID:400, creditLimit:5000.75,
            country:"USA"};
        var result1 = dt0.add(c1);
        var result2 = dt0.add(c2);
    } onretry {
        returnVal2 = 0;
    }
    //check whether update action is performed
    var temp = testDB->select("Select COUNT(*) as countval from CustomersTrx where registrationID = 400", ResultCount);
    table dt = check temp;
    while (dt.hasNext()) {
        var rs = check <ResultCount>dt.getNext();
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

    var temp0 = testDB->getProxyTable("CustomersTrx", CustomersTrx);
    table dt0 = check temp0;

    transaction {
        CustomersTrx c1 = {firstName:"James", lastName:"Clerk", registrationID:350, creditLimit:5000.75,
            country:"USA"};
        CustomersTrx c2 = {firstName:"James", lastName:"Clerk", registrationID:350, creditLimit:5000.75,
            country:"USA"};
        var result1 = dt0.add(c1);
        var result2 = dt0.add(c2);
    }

    int count;
    //check whether update action is performed
    var temp = testDB->select("Select COUNT(*) as countval from CustomersTrx where
                                      registrationID = 350", ResultCount);
    table dt = check temp;
    while (dt.hasNext()) {
        var rs = check <ResultCount>dt.getNext();
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
    var temp0 = testDB->getProxyTable("CustomersTrx", CustomersTrx);
    var temp1 = testDB->getProxyTable("CustomersTrx2", CustomersTrx2);

    table dt0 = check temp0;
    table dt1 = check temp1;
    try {
        transaction with retries = 4 {
            a = a + " inTrx";
            CustomersTrx c1 = {firstName:"James", lastName:"Clerk", registrationID:111, creditLimit:5000.75,
                country:"USA"};
            CustomersTrx2 c2 = {customerId:1, firstName:"James", lastName:"Clerk", registrationID:111, creditLimit:
            5000.75,
                country:"USA"};
            var result1 = dt0.add(c1);
            var result2 = dt1.add(c2);
        } onretry {
            a = a + " inFld";
        }
    } catch (error e) {
        io:println(e);
        a = a + " inCatch";

    }
    a = a + " afterTrx";
    var temp = testDB->select("Select COUNT(*) as countval from CustomersTrx where registrationID = 111", ResultCount);
    table dt = check temp;

    while (dt.hasNext()) {
        var rs = check <ResultCount>dt.getNext();
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
    var temp0 = testDB->getProxyTable("CustomersTrx", CustomersTrx);
    var temp1 = testDB->getProxyTable("CustomersTrx2", CustomersTrx2);

    table dt0 = check temp0;
    table dt1 = check temp1;
    try {
        transaction with retries = 4 {
            a = a + " inTrx";
            CustomersTrx c1 = {firstName:"James", lastName:"Clerk", registrationID:222, creditLimit:5000.75,
                country:"USA"};
            var result1 = dt0.add(c1);
            if (i == 2) {
                CustomersTrx c2 = {firstName:"James", lastName:"Clerk", registrationID:222, creditLimit:5000.75,
                    country:"USA"};
                var result2 = dt0.add(c2);

            } else {
                CustomersTrx2 c3 = {customerId:1, firstName:"James", lastName:"Clerk", registrationID:222, creditLimit:
                5000.75,
                    country:"USA"};
                var result3 = dt1.add(c3);
            }
        } onretry {
            a = a + " inFld";
            i = i + 1;
        }
    } catch (error e) {
        a = a + " inCatch";
    }
    a = a + " afterTrx";
    var temp = testDB->select("Select COUNT(*) as countval from CustomersTrx where registrationID = 222", ResultCount);
    table dt = check temp;

    while (dt.hasNext()) {
        var rs = check <ResultCount>dt.getNext();
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

    var temp1 = testDB1->getProxyTable("CustomersTrx2", CustomersTrx2);
    table dt1 = check temp1;
    try {
        transaction {
            CustomersTrx2 c1 = {customerId:1, firstName:"James", lastName:"Clerk", registrationID:1234, creditLimit:
            5000.75,
                country:"USA"};
            var result1 = dt1.add(c1);
        }
    } catch (error e){
        i = -1;
    }

    CustomersTrx2 c2 = {customerId:2, firstName:"James", lastName:"Clerk", registrationID:12343, creditLimit:5000.75,
        country:"USA"};
    var result2 = dt1.add(c2);

    _ = testDB1->close();

    var temp = testDB2->select("Select COUNT(*) as countval from CustomersTrx2 where registrationID = 12343",
        ResultCount);
    table dt = check temp;
    while (dt.hasNext()) {
        var rs = check <ResultCount>dt.getNext();
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
    var temp0 = testDB->getProxyTable("CustomersTrx", CustomersTrx);
    table dt0 = check temp0;
    transaction {
        CustomersTrx c1 = {firstName:"James", lastName:"Clerk", registrationID:333, creditLimit:5000.75,
            country:"USA"};
        var result1 = dt0.add(c1);
        transaction {
            CustomersTrx c2 = {firstName:"James", lastName:"Clerk", registrationID:333, creditLimit:5000.75,
                country:"USA"};
            var result2 = dt0.add(c2);
        }
    } onretry {
        returnVal = -1;
    }
    //check whether update action is performed
    var temp = testDB->select("Select COUNT(*) as countval from CustomersTrx where registrationID = 333",
        ResultCount);
    table dt = check temp;
    while (dt.hasNext()) {
        var rs = check <ResultCount>dt.getNext();
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
    var temp0 = testDB->getProxyTable("CustomersTrx", CustomersTrx);
    table dt0 = check temp0;
    transaction {
        CustomersTrx c1 = {firstName:"James", lastName:"Clerk", registrationID:444, creditLimit:5000.75,
            country:"USA"};
        var result1 = dt0.add(c1);
        transaction {
            CustomersTrx c2 = {firstName:"James", lastName:"Clerk", registrationID:444, creditLimit:5000.75,
                country:"USA"};
            var result2 = dt0.add(c2);
            transaction {
                CustomersTrx c3 = {firstName:"James", lastName:"Clerk", registrationID:444, creditLimit:5000.75,
                    country:"USA"};
                var result3 = dt0.add(c3);
            }
        }
    } onretry {
        returnVal = -1;
    }
    //check whether update action is performed
    var temp = testDB->select("Select COUNT(*) as countval from CustomersTrx where registrationID = 444", ResultCount);
    table dt = check temp;
    while (dt.hasNext()) {
        var rs = check <ResultCount>dt.getNext();
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
    var temp0 = testDB->getProxyTable("CustomersTrx", CustomersTrx);
    var temp1 = testDB->getProxyTable("CustomersTrx2", CustomersTrx2);

    table dt0 = check temp0;
    table dt1 = check temp1;
    try {
        transaction {
            CustomersTrx c1 = {firstName:"James", lastName:"Clerk", registrationID:555, creditLimit:5000.75,
                country:"USA"};
            var result1 = dt0.add(c1);
            transaction {
                CustomersTrx c2 = {firstName:"James", lastName:"Clerk", registrationID:444, creditLimit:5000.75,
                    country:"USA"};
                var result2 = dt0.add(c2);
                transaction {
                    CustomersTrx2 c3 = {customerId:1, firstName:"James", lastName:"Clerk", registrationID:1234,
                        creditLimit:5000.75,
                        country:"USA"};
                    var result3 = dt1.add(c3);
                }
            }
        } onretry {
            returnVal = -1;
        }
    } catch (error e) {
        // ignore.
    }
    //check whether update action is performed
    var temp = testDB->select("Select COUNT(*) as countval from CustomersTrx where registrationID = 555", ResultCount);
    table dt = check temp;
    while (dt.hasNext()) {
        var rs = check <ResultCount>dt.getNext();
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
    var temp0 = testDB->getProxyTable("CustomersTrx", CustomersTrx);
    var temp1 = testDB->getProxyTable("CustomersTrx2", CustomersTrx2);

    table dt0 = check temp0;
    table dt1 = check temp1;
    try {
        transaction {
            a = a + " txL1";
            CustomersTrx c1 = {firstName:"James", lastName:"Clerk", registrationID:666, creditLimit:5000.75,
                country:"USA"};
            var result1 = dt0.add(c1);
            transaction {
                a = a + " txL2";
                CustomersTrx c2 = {firstName:"James", lastName:"Clerk", registrationID:666, creditLimit:5000.75,
                    country:"USA"};
                var result2 = dt0.add(c2);
                transaction with retries = 2{
                    a = a + " txL3";
                    if (index == 1) {
                        a = a + " txL3_If";
                        CustomersTrx c3 = {firstName:"James", lastName:"Clerk", registrationID:666, creditLimit:5000.75,
                            country:"USA"};
                        var result3 = dt0.add(c3);
                    } else {
                        a = a + " txL3_Else";
                        CustomersTrx2 c4 = {customerId:1, firstName:"James", lastName:"Clerk", registrationID:666,
                            creditLimit:5000.75, country:"USA"};
                        var result4 = dt1.add(c4);
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
    var temp = testDB->select("Select COUNT(*) as countval from CustomersTrx where registrationID = 666", ResultCount);
    table dt = check temp;
    while (dt.hasNext()) {
        var rs = check <ResultCount>dt.getNext();
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
    var temp = testDB->select("Select COUNT(*) as countval from CustomersTrx where registrationID = 834", ResultCount);
    table dt = check temp;
    while (dt.hasNext()) {
        var rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }
    _ = testDB->close();
    return count;
}

function invokeWorkers(sql:Client testDBClient) {
    endpoint sql:Client testDB = testDBClient;


    worker w1 {
        var temp0 = testDB->getProxyTable("CustomersTrx", CustomersTrx);
        table dt0 = check temp0;
        CustomersTrx c1 = {firstName:"James", lastName:"Clerk", registrationID:834, creditLimit:5000.75, country:"USA"};
        var result1 = dt0.add(c1);
    }

    worker w2 {
        var temp0 = testDB->getProxyTable("CustomersTrx", CustomersTrx);
        table dt0 = check temp0;
        runtime:sleepCurrentWorker(5000);
        CustomersTrx c2 = {firstName:"James", lastName:"Clerk", registrationID:834, creditLimit:5000.75, country:"USA"};
        var result2 = dt0.add(c2);
    }

}
