import ballerina/jdbc;

type ResultCount {
    int COUNTVAL,
};

function testXAransactonSuccess() returns (int, int) {
    endpoint jdbc:Client testDB1 {
        url:"jdbc:h2:file:./target/H2_1/TestDB1",
        username:"SA",
        poolOptions:{maximumPoolSize:1, isXA:true}
    };

    endpoint jdbc:Client testDB2 {
        url:"jdbc:h2:file:./target/H2_2/TestDB2",
        username:"SA",
        poolOptions:{maximumPoolSize:1, isXA:true}
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

function testXAransactonFailed1() returns (int, int) {

    endpoint jdbc:Client testDB1 {
        url:"jdbc:h2:file:./target/H2_1/TestDB1",
        username:"SA",
        poolOptions:{maximumPoolSize:1, isXA:true}
    };

    endpoint jdbc:Client testDB2 {
        url:"jdbc:h2:file:./target/H2_2/TestDB2",
        username:"SA",
        poolOptions:{maximumPoolSize:1, isXA:true}
    };

    try {
        transaction {
            _ = testDB1->update("insert into Customers (customerId, name, creditLimit, country)
                                    values (2, 'John', 1000, 'UK')");
            _ = testDB2->update("insert into Salary (id, invalidColumn ) values (2, 1000)");
        }
    } catch (error e) {

    }

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

function testXAransactonFailed2() returns (int, int) {

    endpoint jdbc:Client testDB1 {
        url:"jdbc:h2:file:./target/H2_1/TestDB1",
        username:"SA",
        poolOptions:{maximumPoolSize:1, isXA:true}
    };

    endpoint jdbc:Client testDB2 {
        url:"jdbc:h2:file:./target/H2_2/TestDB2",
        username:"SA",
        poolOptions:{maximumPoolSize:1, isXA:true}
    };

    try {
        transaction {
            _ = testDB1->update("insert into Customers (customerId, name, creditLimit, invalidColumn)
                                    values (2, 'John', 1000, 'UK')");
            _ = testDB2->update("insert into Salary (id, value ) values (2, 1000)");
        }
    } catch (error e) {

    }
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

function testXAransactonRetry() returns (int, int) {

    endpoint jdbc:Client testDB1 {
        url:"jdbc:h2:file:./target/H2_1/TestDB1",
        username:"SA",
        poolOptions:{maximumPoolSize:1, isXA:true}
    };

    endpoint jdbc:Client testDB2 {
        url:"jdbc:h2:file:./target/H2_2/TestDB2",
        username:"SA",
        poolOptions:{maximumPoolSize:1, isXA:true}
    };

    int i = 0;
    try {
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
    } catch (error e) {
    }
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
