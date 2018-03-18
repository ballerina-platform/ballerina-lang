import ballerina.data.sql;

struct ResultCount {
    int COUNTVAL;
}

function testXAransactonSuccess () (int count1, int count2) {
	endpoint sql:Client testDB1 {
        database: sql:DB.H2_FILE,
        host: "./target/H2_1/",
        port: 0,
        name: "TestDB1",
        username: "sa",
        password: "",
        options: {maximumPoolSize:1, isXA:true}
    };
	
	endpoint sql:Client testDB2 {
        database: sql:DB.H2_FILE,
        host: "./target/H2_2/",
        port: 0,
        name: "TestDB2",
        username: "sa",
        password: "",
        options: {maximumPoolSize:1, isXA:true}
    };
	
    transaction {
        _ = testDB1 -> update("insert into Customers (customerId, name, creditLimit, country)
                                values (1, 'Anne', 1000, 'UK')", null);
        _ = testDB2 -> update("insert into Salary (id, value ) values (1, 1000)", null);
    }
    //check whether update action is performed
    table dt = testDB1 -> select("Select COUNT(*) as countval from Customers where customerId = 1 ",
                                  null, typeof ResultCount);
    while (dt.hasNext()) {
        var rs, _ = (ResultCount)dt.getNext();
        count1 = rs.COUNTVAL;
    }

    dt = testDB2 -> select("Select COUNT(*) as countval from Salary where id = 1", null, typeof ResultCount);
    while (dt.hasNext()) {
        var rs, _ = (ResultCount)dt.getNext();
        count2 = rs.COUNTVAL;
    }
    testDB1 -> close();
    testDB2 -> close();
    return;
}

function testXAransactonFailed1 () (int count1, int count2) {

	endpoint sql:Client testDB1 {
        database: sql:DB.H2_FILE,
        host: "./target/H2_1/",
        port: 0,
        name: "TestDB1",
        username: "sa",
        password: "",
        options: {maximumPoolSize:1, isXA:true}
    };
	
	endpoint sql:Client testDB2 {
        database: sql:DB.H2_FILE,
        host: "./target/H2_2/",
        port: 0,
        name: "TestDB2",
        username: "sa",
        password: "",
        options: {maximumPoolSize:1, isXA:true}
    };
	
    try {
        transaction {
            _ = testDB1 -> update("insert into Customers (customerId, name, creditLimit, country)
                                    values (2, 'John', 1000, 'UK')", null);
            _ = testDB2 -> update("insert into Salary (id, invalidColumn ) values (2, 1000)", null);
        }
    } catch (error e) {

    }
    //check whether update action is performed
    table dt = testDB1 -> select("Select COUNT(*) as countval from Customers where customerId = 2", null,
                                  typeof ResultCount);
    while (dt.hasNext()) {
        var rs, _ = (ResultCount)dt.getNext();
        count1 = rs.COUNTVAL;
    }

    dt = testDB2 -> select("Select COUNT(*) as countval from Salary where id = 2 ", null, typeof ResultCount);
    while (dt.hasNext()) {
        var rs, _ = (ResultCount)dt.getNext();
        count2 = rs.COUNTVAL;
    }
    testDB1 -> close();
    testDB2 -> close();
    return;
}

function testXAransactonFailed2 () (int count1, int count2) {

	endpoint sql:Client testDB1 {
        database: sql:DB.H2_FILE,
        host: "./target/H2_1/",
        port: 0,
        name: "TestDB1",
        username: "sa",
        password: "",
        options: {maximumPoolSize:1, isXA:true}
    };
	
	endpoint sql:Client testDB2 {
        database: sql:DB.H2_FILE,
        host: "./target/H2_2/",
        port: 0,
        name: "TestDB2",
        username: "sa",
        password: "",
        options: {maximumPoolSize:1, isXA:true}
    };
	
    try {
        transaction {
            _ = testDB1 -> update("insert into Customers (customerId, name, creditLimit, invalidColumn)
                                    values (2, 'John', 1000, 'UK')", null);
            _ = testDB2 -> update("insert into Salary (id, value ) values (2, 1000)", null);
        }
    } catch (error e) {

    }
    //check whether update action is performed
    table dt = testDB1 -> select("Select COUNT(*) as countval from Customers where customerId = 2",
                                  null, typeof ResultCount);
    while (dt.hasNext()) {
        var rs, _ = (ResultCount)dt.getNext();
        count1 = rs.COUNTVAL;
    }

    dt = testDB2 -> select("Select COUNT(*) as countval from Salary where id = 2 ", null, typeof ResultCount);
    while (dt.hasNext()) {
        var rs, _ = (ResultCount)dt.getNext();
        count2 = rs.COUNTVAL;
    }
    testDB1 -> close();
    testDB2 -> close();
    return;
}

function testXAransactonRetry () (int count1, int count2) {

	endpoint sql:Client testDB1 {
        database: sql:DB.H2_FILE,
        host: "./target/H2_1/",
        port: 0,
        name: "TestDB1",
        username: "sa",
        password: "",
        options: {maximumPoolSize:1, isXA:true}
    };
	
	endpoint sql:Client testDB2 {
        database: sql:DB.H2_FILE,
        host: "./target/H2_2/",
        port: 0,
        name: "TestDB2",
        username: "sa",
        password: "",
        options: {maximumPoolSize:1, isXA:true}
    };

    int i = 0;
    try {
        transaction {
            if (i == 2) {
                _ = testDB1 -> update("insert into Customers (customerId, name, creditLimit, country)
                        values (4, 'John', 1000, 'UK')", null);
            } else {
                _ = testDB1 -> update("insert into Customers (customerId, name, creditLimit, invalidColumn)
                        values (4, 'John', 1000, 'UK')", null);
            }
            _ = testDB2 -> update("insert into Salary (id, value ) values (4, 1000)", null);
        } onretry {
            i = i + 1;
        }
    } catch (error e) {
    }
    //check whether update action is performed
    table dt = testDB1 -> select("Select COUNT(*) as countval from Customers where customerId = 4",
                                        null, typeof ResultCount);
    while (dt.hasNext()) {
        var rs, _ = (ResultCount)dt.getNext();
        count1 = rs.COUNTVAL;
    }

    dt = testDB2 -> select("Select COUNT(*) as countval from Salary where id = 4", null, typeof ResultCount);
    while (dt.hasNext()) {
        var rs, _ = (ResultCount)dt.getNext();
        count2 = rs.COUNTVAL;
    }
    testDB1 -> close();
    testDB2 -> close();
    return;
}
