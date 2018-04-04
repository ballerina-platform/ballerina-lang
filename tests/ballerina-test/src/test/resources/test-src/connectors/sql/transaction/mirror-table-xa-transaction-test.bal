import ballerina/sql;

struct ResultCount {
    int COUNTVAL;
}

struct CustomersTrx {
    int customerId;
    string name;
    float creditLimit;
    string country;
}

struct SalaryTrx {
    int id;
    float value;
}

function testXATransactionSuccess () returns (int, int) {
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

    table dt0 =? testDB1 -> mirror("CustomersTrx", typeof CustomersTrx);
    table dt1 =? testDB2 -> mirror("SalaryTrx", typeof SalaryTrx);

    transaction {
        CustomersTrx c1 = {customerId:1, name:"Anne", creditLimit:1000, country:"UK"};
        SalaryTrx s1 = {id:1, value: 1000};
        
        var result1 = dt0.add(c1);
        var result2 = dt1.add(s1);
    }

    int count1;
    int count2;
    //check whether update action is performed
    table dt =? testDB1 -> select("Select COUNT(*) as countval from CustomersTrx where customerId = 1 ",
                                  null, typeof ResultCount);
    while (dt.hasNext()) {
        var rs =? <ResultCount>dt.getNext();
        count1 = rs.COUNTVAL;
    }

    dt =? testDB2 -> select("Select COUNT(*) as countval from SalaryTrx where id = 1", null, typeof ResultCount);
    while (dt.hasNext()) {
        var rs =? <ResultCount>dt.getNext();
        count2 = rs.COUNTVAL;
    }
    _ = testDB1 -> close();
    _ = testDB2 -> close();
    return (count1, count2);
}

function testXATransactionFailed1 () returns (int, int) {

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

    table dt0 =? testDB1 -> mirror("CustomersTrx", typeof CustomersTrx);
    table dt1 =? testDB2 -> mirror("SalaryTrx", typeof SalaryTrx);
    
    try {
        transaction {
            CustomersTrx c1 = {customerId:2, name:"John", creditLimit:1000, country:"UK"};
            SalaryTrx s1 = {id:20, value: 1000};

            var result1 = dt0.add(c1);
            var result2 = dt1.add(s1);
        }
    } catch (error e) {

    }

    int count1;
    int count2;
    //check whether update action is performed
    table dt =? testDB1 -> select("Select COUNT(*) as countval from CustomersTrx where customerId = 2", null,
                                  typeof ResultCount);
    while (dt.hasNext()) {
        var rs =? <ResultCount>dt.getNext();
        count1 = rs.COUNTVAL;
    }

    dt =? testDB2 -> select("Select COUNT(*) as countval from SalaryTrx where id = 2 ", null, typeof ResultCount);
    while (dt.hasNext()) {
        var rs =? <ResultCount>dt.getNext();
        count2 = rs.COUNTVAL;
    }
    _ = testDB1 -> close();
    _ = testDB2 -> close();
    return (count1, count2);
}

function testXATransactionFailed2 () returns (int, int) {

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

    table dt0 =? testDB1 -> mirror("CustomersTrx", typeof CustomersTrx);
    table dt1 =? testDB2 -> mirror("SalaryTrx", typeof SalaryTrx);
    try {
        transaction {
            CustomersTrx c1 = {customerId:30, name:"John", creditLimit:1000, country:"UK"};
            SalaryTrx s1 = {id:3, value: 1000};

            var result1 = dt0.add(c1);
            var result2 = dt1.add(s1);
        }
    } catch (error e) {

    }
    //check whether update action is performed
    table dt =? testDB1 -> select("Select COUNT(*) as countval from CustomersTrx where customerId = 2",
                                  null, typeof ResultCount);
    int count1;
    int count2;
    while (dt.hasNext()) {
        var rs =? <ResultCount>dt.getNext();
        count1 = rs.COUNTVAL;
    }

    dt =? testDB2 -> select("Select COUNT(*) as countval from SalaryTrx where id = 2 ", null, typeof ResultCount);
    while (dt.hasNext()) {
        var rs =? <ResultCount>dt.getNext();
        count2 = rs.COUNTVAL;
    }
    _ = testDB1 -> close();
    _ = testDB2 -> close();
    return (count1, count2);
}

function testXATransactionRetry () returns (int, int) {

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
    table dt0 =? testDB1 -> mirror("CustomersTrx", typeof CustomersTrx);
    table dt1 =? testDB2 -> mirror("SalaryTrx", typeof SalaryTrx);

    try {
        transaction {
            CustomersTrx c1 = {customerId:30, name:"John", creditLimit:1000, country:"UK"};
            CustomersTrx c2 = {customerId:4, name:"John", creditLimit:1000, country:"UK"};
            SalaryTrx s1 = {id:4, value: 1000};
            if (i == 2) {
                var result1 = dt0.add(c2);
            } else {
                var result2 = dt0.add(c1);
            }
            var result3 = dt1.add(s1);
        } onretry {
            i = i + 1;
        }
    } catch (error e) {
    }
    //check whether update action is performed
    table dt =? testDB1 -> select("Select COUNT(*) as countval from CustomersTrx where customerId = 4",
                                  null, typeof ResultCount);
    int count1;
    int count2;

    while (dt.hasNext()) {
        var rs =? <ResultCount>dt.getNext();
        count1 = rs.COUNTVAL;
    }

    dt =? testDB2 -> select("Select COUNT(*) as countval from SalaryTrx where id = 4", null, typeof ResultCount);
    while (dt.hasNext()) {
        var rs =? <ResultCount>dt.getNext();
        count2 = rs.COUNTVAL;
    }
    _ = testDB1 -> close();
    _ = testDB2 -> close();
    return (count1, count2);
}
