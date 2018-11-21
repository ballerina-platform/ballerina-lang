import ballerina/h2;

type ResultCount record {
    int COUNTVAL;
};

function testXATransactonSuccess() returns (int, int) {
    endpoint h2:Client testDB1 {
        path: "./target/H2_1/",
        name: "TestDB1",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1, isXA: true }
    };

    endpoint h2:Client testDB2 {
        path: "./target/H2_2/",
        name: "TestDB2",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1, isXA: true }
    };

    transaction {
        _ = testDB1->update("insert into Customers (customerId, name, creditLimit, country)
                                values (1, 'Anne', 1000, 'UK')");
        _ = testDB2->update("insert into Salary (id, value ) values (1, 1000)");
    }

    int count1;
    int count2;
    //check whether update action is performed
    table dt = check testDB1->select("Select COUNT(*) as countval from Customers where customerId = 1 ", ResultCount);
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count1 = rs.COUNTVAL;
    }

    dt = check testDB2->select("Select COUNT(*) as countval from Salary where id = 1", ResultCount);
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count2 = rs.COUNTVAL;
    }
    testDB1.stop();
    testDB2.stop();
    return (count1, count2);
}

function testXATransactonSuccessWithDataSource() returns (int, int) {
    endpoint h2:Client testDB1 {
        path: "./target/H2_1/",
        name: "TestDB1",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1, isXA: true, dataSourceClassName: "org.h2.jdbcx.JdbcDataSource" }
    };

    endpoint h2:Client testDB2 {
        path: "./target/H2_2/",
        name: "TestDB2",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1, isXA: true, dataSourceClassName: "org.h2.jdbcx.JdbcDataSource" }
    };

    transaction {
        _ = testDB1->update("insert into Customers (customerId, name, creditLimit, country)
                                values (10, 'Anne', 1000, 'UK')");
        _ = testDB2->update("insert into Salary (id, value ) values (10, 1000)");
    }

    int count1;
    int count2;
    //check whether update action is performed
    table dt = check testDB1->select("Select COUNT(*) as countval from Customers where customerId = 10 ", ResultCount);
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count1 = rs.COUNTVAL;
    }

    dt = check testDB2->select("Select COUNT(*) as countval from Salary where id = 10", ResultCount);
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count2 = rs.COUNTVAL;
    }
    testDB1.stop();
    testDB2.stop();
    return (count1, count2);
}

function testXATransactonSuccessWithH2Client() returns (int, int) {
    endpoint h2:Client testDB1 {
        path: "./target/H2_1/",
        name: "TestDB1",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1, isXA: true, dataSourceClassName: "org.h2.jdbcx.JdbcDataSource" }
    };

    endpoint h2:Client testDB2 {
        path: "./target/H2_2/",
        name: "TestDB2",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1, isXA: true, dataSourceClassName: "org.h2.jdbcx.JdbcDataSource" }
    };

    transaction {
        _ = testDB1->update("insert into Customers (customerId, name, creditLimit, country)
                                values (11, 'Anne', 1000, 'UK')");
        _ = testDB2->update("insert into Salary (id, value ) values (11, 1000)");
    }

    int count1;
    int count2;
    //check whether update action is performed
    table dt = check testDB1->select("Select COUNT(*) as countval from Customers where customerId = 11", ResultCount);
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count1 = rs.COUNTVAL;
    }

    dt = check testDB2->select("Select COUNT(*) as countval from Salary where id = 11", ResultCount);
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count2 = rs.COUNTVAL;
    }
    testDB1.stop();
    testDB2.stop();
    return (count1, count2);
}

function testXATransactonFailed1() returns (int, int) {

    endpoint h2:Client testDB1 {
        path: "./target/H2_1/",
        name: "TestDB1",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1, isXA: true }
    };

    endpoint h2:Client testDB2 {
        path: "./target/H2_2/",
        name: "TestDB2",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1, isXA: true }
    };

    _ = trap testXATransactonFailed1Helper(testDB1, testDB2);

    int count1;
    int count2;
    //check whether update action is performed
    table dt = check testDB1->select("Select COUNT(*) as countval from Customers where customerId = 2", ResultCount);
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count1 = rs.COUNTVAL;
    }

    dt = check testDB2->select("Select COUNT(*) as countval from Salary where id = 2 ", ResultCount);
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count2 = rs.COUNTVAL;
    }
    testDB1.stop();
    testDB2.stop();
    return (count1, count2);
}

function testXATransactonFailed1Helper(h2:Client db1, h2:Client db2) {
    endpoint h2:Client testDB1 = db1;
    endpoint h2:Client testDB2 = db2;
    transaction {
        _ = testDB1->update("insert into Customers (customerId, name, creditLimit, country)
                                    values (2, 'John', 1000, 'UK')");
        _ = testDB2->update("insert into Salary (id, invalidColumn ) values (2, 1000)");
    }
}

function testXATransactonFailed2() returns (int, int) {

    endpoint h2:Client testDB1 {
        path: "./target/H2_1/",
        name: "TestDB1",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1, isXA: true }
    };

    endpoint h2:Client testDB2 {
        path: "./target/H2_2/",
        name: "TestDB2",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1, isXA: true }
    };
    _ = trap testXATransactonFailed2Helper(testDB1, testDB2);
    //check whether update action is performed
    table dt = check testDB1->select("Select COUNT(*) as countval from Customers where customerId = 2", ResultCount);
    int count1;
    int count2;
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count1 = rs.COUNTVAL;
    }

    dt = check testDB2->select("Select COUNT(*) as countval from Salary where id = 2 ", ResultCount);
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count2 = rs.COUNTVAL;
    }
    testDB1.stop();
    testDB2.stop();
    return (count1, count2);
}

function testXATransactonFailed2Helper(h2:Client db1, h2:Client db2) {
    endpoint h2:Client testDB1 = db1;
    endpoint h2:Client testDB2 = db2;
    transaction {
        _ = testDB1->update("insert into Customers (customerId, name, creditLimit, invalidColumn)
                                    values (2, 'John', 1000, 'UK')");
        _ = testDB2->update("insert into Salary (id, value ) values (2, 1000)");
    }
}

function testXATransactonRetry() returns (int, int) {

    endpoint h2:Client testDB1 {
        path: "./target/H2_1/",
        name: "TestDB1",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1, isXA: true }
    };

    endpoint h2:Client testDB2 {
        path: "./target/H2_2/",
        name: "TestDB2",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1, isXA: true }
    };

    _ = trap testXATransactonRetryHelper(testDB1, testDB2);
    //check whether update action is performed
    table dt = check testDB1->select("Select COUNT(*) as countval from Customers where customerId = 4",
        ResultCount);
    int count1;
    int count2;

    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count1 = rs.COUNTVAL;
    }

    dt = check testDB2->select("Select COUNT(*) as countval from Salary where id = 4", ResultCount);
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count2 = rs.COUNTVAL;
    }
    testDB1.stop();
    testDB2.stop();
    return (count1, count2);
}

function testXATransactonRetryHelper(h2:Client db1, h2:Client db2) {
    endpoint h2:Client testDB1 = db1;
    endpoint h2:Client testDB2 = db2;
    int i = 0;
    transaction {
        if (i == 2) {
            _ = testDB1->update("insert into Customers (customerId, name, creditLimit, country)
                        values (4, 'John', 1000, 'UK')");
        } else {
            _ = testDB1->update("insert into Customers (customerId, name, creditLimit, invalidColumn)
                        values (4, 'John', 1000, 'UK')");
        }
        _ = testDB2->update("insert into Salary (id, value ) values (4, 1000)");
    } onretry {
        i = i + 1;
    }
}
