import ballerina/h2;
import ballerina/io;
import ballerina/sql;

public type Customer {
    int customerId,
    string name,
    float creditLimit,
    string country,
};

function testSelect() returns (int[]) {
    endpoint h2:Client testDB {
        path:"./target/H2Client/",
        name:"TestDBH2",
        username:"SA",
        password:"",
        poolOptions:{maximumPoolSize:1}
    };

    var val = testDB -> select("select * from Customers where customerId=1 OR customerId=2", (), typeof Customer);

    int[] customerIds;
    match (val) {
        table dt => {
            int i = 0;
            while (dt.hasNext()) {
                Customer rs = check <Customer>dt.getNext();
                customerIds[i] = rs.customerId;
                i++;
            }
            return customerIds;
        }
        error err => return [];
    }
}

function testUpdate() returns (int) {
    endpoint h2:Client testDB {
        path:"./target/H2Client/",
        name:"TestDBH2",
        username:"SA",
        password:"",
        poolOptions:{maximumPoolSize:1}
    };

    var insertCountRet = testDB -> update("insert into Customers (customerId, name, creditLimit, country)
                                values (15, 'Anne', 1000, 'UK')", ());

    int insertCount = check insertCountRet;
    _ = testDB -> close();
    return insertCount;
}

function testCall() returns (string) {
    endpoint h2:Client testDB {
        path:"./target/H2Client/",
        name:"TestDBH2",
        username:"SA",
        password:"",
        poolOptions:{maximumPoolSize:1}
    };

    var dtsRet = testDB -> call("{call JAVAFUNC('select * from Customers where customerId=1')}", (), typeof Customer);
    table[] dts = check dtsRet;

    string name;
    while (dts[0].hasNext()) {
        Customer rs = check <Customer>dts[0].getNext();
        name = rs.name;
    }
    _ = testDB -> close();
    return name;
}

function testGeneratedKeyOnInsert() returns (string) {
    endpoint h2:Client testDB {
        path:"./target/H2Client/",
        name:"TestDBH2",
        username:"SA",
        password:"",
        poolOptions:{maximumPoolSize:1}
    };

    string returnVal;

    var x = testDB -> updateWithGeneratedKeys("insert into Customers (name,
            creditLimit,country) values ('Sam', 1200, 'USA')", (), ());

    match x {
        (int, string[]) y => {
            int a;
            string[] b;
            (a, b) = y;
            returnVal = b[0];
        }
        error err1 => {
            returnVal = err1.message;
        }
    }

    _ = testDB -> close();
    return returnVal;
}

function testBatchUpdate() returns (int[]) {
    endpoint h2:Client testDB {
        path:"./target/H2Client/",
        name:"TestDBH2",
        username:"SA",
        password:"",
        poolOptions:{maximumPoolSize:1}
    };

    int[] updateCount;
    string returnVal;
    try {
        //Batch 1
        sql:Parameter para1 = {sqlType:sql:TYPE_INTEGER, value:10};
        sql:Parameter para2 = {sqlType:sql:TYPE_VARCHAR, value:"Smith"};
        sql:Parameter para3 = {sqlType:sql:TYPE_DOUBLE, value:3400.5};
        sql:Parameter para4 = {sqlType:sql:TYPE_VARCHAR, value:"Australia"};
        sql:Parameter[] parameters1 = [para1, para2, para3, para4];

        //Batch 2
        sql:Parameter para5 = {sqlType:sql:TYPE_INTEGER, value:11};
        sql:Parameter para6 = {sqlType:sql:TYPE_VARCHAR, value:"John"};
        sql:Parameter para7 = {sqlType:sql:TYPE_DOUBLE, value:3400.2};
        sql:Parameter para8 = {sqlType:sql:TYPE_VARCHAR, value:"UK"};
        sql:Parameter[] parameters2 = [para5, para6, para7, para8];
        sql:Parameter[][] parameters = [parameters1, parameters2];

        var x = testDB -> batchUpdate("Insert into Customers values (?,?,?,?)", parameters);
        match x {
            int[] data => {
                return data;
            }
            error err1 => {
                return [];
            }
        }
    } finally {
        _ = testDB -> close();
    }
    return [];
}

function testAddToMirrorTable() returns (Customer[]) {
    endpoint h2:Client testDB {
        path:"./target/H2Client/",
        name:"TestDBH2",
        username:"SA",
        password:"",
        poolOptions:{maximumPoolSize:1}
    };

    try {
        var temp = testDB -> mirror("Customers", typeof Customer);
        match (temp) {
            table dt => {
                Customer c1 = {customerId:40, name:"Manuri", creditLimit:1000, country:"Sri Lanka"};
                Customer c2 = {customerId:41, name:"Devni", creditLimit:1000, country:"Sri Lanka"};

                var result1 = dt.add(c1);
                var result2 = dt.add(c2);
            }
            error e => return [];
        }
        var temp2 = testDB -> select("SELECT  * from Customers where customerId=40 OR customerId=41", (), typeof Customer);
        match (temp2) {
            table dt2 => {
                Customer[] customerArray;
                int i = 0;
                while (dt2.hasNext()) {
                    var rs = check <Customer>dt2.getNext();
                    Customer c = {customerId:rs.customerId, name:rs.name, creditLimit:rs.creditLimit, country:rs.country};
                    customerArray[i] = c;
                    i++;
                }
                return customerArray;
            }
            error e => return [];
        }
    } finally {
        _ = testDB -> close();
    }
    return [];
}

function testUpdateInMemory() returns (int, string) {
    endpoint h2:Client testDB {
        name:"TestDB2H2",
        username:"SA",
        password:"",
        poolOptions:{maximumPoolSize:1}
    };

    _ = testDB -> update("CREATE TABLE IF NOT EXISTS Customers(customerId INTEGER NOT NULL IDENTITY,
    name  VARCHAR(300),creditLimit DOUBLE,country  VARCHAR(300),PRIMARY KEY (customerId) )", ());

    var insertCountRet = testDB -> update("insert into Customers (customerId, name, creditLimit, country)
                                values (15, 'Anne', 1000, 'UK')", ());
    var x = testDB -> select("SELECT  * from Customers", (), typeof Customer);
    table t = check x;

    json j = check <json>t;

    string s = j.toString() but { () => "" };

    int insertCount = check insertCountRet;
    _ = testDB -> close();
    return (insertCount, s);
}
