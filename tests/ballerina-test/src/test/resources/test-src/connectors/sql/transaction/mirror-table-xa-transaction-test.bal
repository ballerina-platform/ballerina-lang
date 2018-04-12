import ballerina/sql;

type ResultCount {
    int COUNTVAL,
};

type CustomersTrx {
    int customerId,
    string name,
    float creditLimit,
    string country,
};

type SalaryTrx {
    int id,
    float value,
};

function testXATransactionSuccess () returns (int, int) {
    endpoint sql:Client testDB1 {
        url: "h2:file:./target/H2_1/TestDB1",
        username: "SA",
        poolOptions: {maximumPoolSize:1, isXA:true}
    };

    endpoint sql:Client testDB2 {
        url: "h2:file:./target/H2_2/TestDB2",
        username: "SA",
        poolOptions: {maximumPoolSize:1, isXA:true}
    };

    var temp0 = testDB1 -> mirror("CustomersTrx", CustomersTrx);
    var temp1 = testDB2 -> mirror("SalaryTrx", SalaryTrx);
    
    table dt0 = check temp0;
    table dt1 = check temp1;

    transaction {
        CustomersTrx c1 = {customerId:1, name:"Anne", creditLimit:1000, country:"UK"};
        SalaryTrx s1 = {id:1, value: 1000};
        
        var result1 = dt0.add(c1);
        var result2 = dt1.add(s1);
    }

    int count1;
    int count2;
    //check whether update action is performed
    var temp = testDB1 -> select("Select COUNT(*) as countval from CustomersTrx where customerId = 1 ",
                                  ResultCount);
    table dt = check temp;
    while (dt.hasNext()) {
        var rs = check <ResultCount>dt.getNext();
        count1 = rs.COUNTVAL;
    }

    temp = testDB2 -> select("Select COUNT(*) as countval from SalaryTrx where id = 1", ResultCount);
    dt = check temp;
    while (dt.hasNext()) {
        var rs = check <ResultCount>dt.getNext();
        count2 = rs.COUNTVAL;
    }
    _ = testDB1 -> close();
    _ = testDB2 -> close();
    return (count1, count2);
}

function testXATransactionFailed1 () returns (int, int) {

    endpoint sql:Client testDB1 {
        url: "h2:file:./target/H2_1/TestDB1",
        username: "SA",
        poolOptions: {maximumPoolSize:1, isXA:true}
    };

    endpoint sql:Client testDB2 {
        url: "h2:file:./target/H2_2/TestDB2",
        username: "SA",
        poolOptions: {maximumPoolSize:1, isXA:true}
    };

    var temp0 = testDB1 -> mirror("CustomersTrx", CustomersTrx);
    var temp1 = testDB2 -> mirror("SalaryTrx", SalaryTrx);

    table dt0 = check temp0;
    table dt1 = check temp1;

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
    var temp = testDB1 -> select("Select COUNT(*) as countval from CustomersTrx where customerId = 2", ResultCount);
    table dt = check temp;
    while (dt.hasNext()) {
        var rs = check <ResultCount>dt.getNext();
        count1 = rs.COUNTVAL;
    }

    temp = testDB2 -> select("Select COUNT(*) as countval from SalaryTrx where id=20 AND value = 1000", ResultCount);

    dt = check temp;
    while (dt.hasNext()) {
        var rs = check <ResultCount>dt.getNext();
        count2 = rs.COUNTVAL;
    }
    _ = testDB1 -> close();
    _ = testDB2 -> close();
    return (count1, count2);
}

function testXATransactionFailed2 () returns (int, int) {

    endpoint sql:Client testDB1 {
        url: "h2:file:./target/H2_1/TestDB1",
        username: "SA",
        poolOptions: {maximumPoolSize:1, isXA:true}
    };

    endpoint sql:Client testDB2 {
        url: "h2:file:./target/H2_2/TestDB2",
        username: "SA",
        poolOptions: {maximumPoolSize:1, isXA:true}
    };

    var temp0 = testDB1 -> mirror("CustomersTrx", CustomersTrx);
    var temp1 = testDB2 -> mirror("SalaryTrx", SalaryTrx);

    table dt0 = check temp0;
    table dt1 = check temp1;
    
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
    var temp = testDB1 -> select("Select COUNT(*) as countval from CustomersTrx where customerId = 30 AND name = 'John'",
        ResultCount);

    table dt = check temp;
    int count1;
    int count2;
    while (dt.hasNext()) {
        var rs = check <ResultCount>dt.getNext();
        count1 = rs.COUNTVAL;
    }

    temp = testDB2 -> select("Select COUNT(*) as countval from SalaryTrx where id = 3 ", ResultCount);

    dt = check temp;
    
    while (dt.hasNext()) {
        var rs = check <ResultCount>dt.getNext();
        count2 = rs.COUNTVAL;
    }
    _ = testDB1 -> close();
    _ = testDB2 -> close();
    return (count1, count2);
}

function testXATransactionRetry () returns (int, int) {

    endpoint sql:Client testDB1 {
        url: "h2:file:./target/H2_1/TestDB1",
        username: "SA",
        poolOptions: {maximumPoolSize:1, isXA:true}
    };

    endpoint sql:Client testDB2 {
        url: "h2:file:./target/H2_2/TestDB2",
        username: "SA",
        poolOptions: {maximumPoolSize:1, isXA:true}
    };

    int i = 0;
    var temp0 = testDB1 -> mirror("CustomersTrx", CustomersTrx);
    var temp1 = testDB2 -> mirror("SalaryTrx", SalaryTrx);

    table dt0 = check temp0;
    table dt1 = check temp1;

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
    var temp = testDB1 -> select("Select COUNT(*) as countval from CustomersTrx where customerId = 4",
                                  ResultCount);
    table dt = check temp;
    int count1;
    int count2;

    while (dt.hasNext()) {
        var rs = check <ResultCount>dt.getNext();
        count1 = rs.COUNTVAL;
    }

    temp = testDB2 -> select("Select COUNT(*) as countval from SalaryTrx where id = 4",ResultCount);
    dt = check temp;
    
    while (dt.hasNext()) {
        var rs = check <ResultCount>dt.getNext();
        count2 = rs.COUNTVAL;
    }
    _ = testDB1 -> close();
    _ = testDB2 -> close();
    return (count1, count2);
}
