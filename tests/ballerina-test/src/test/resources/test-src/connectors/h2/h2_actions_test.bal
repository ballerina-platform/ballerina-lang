import ballerina/h2;
import ballerina/sql;
import ballerina/io;

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

    var val = testDB->select("select * from Customers where customerId=1 OR customerId=2", Customer);

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

    var insertCountRet = testDB->update("insert into Customers (customerId, name, creditLimit, country)
                                values (15, 'Anne', 1000, 'UK')");

    int insertCount = check insertCountRet;
    testDB.stop();
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

    var dtsRet = testDB->call("{call JAVAFUNC('select * from Customers where customerId=1')}", [Customer]);
    table[] dts = check dtsRet;

    string name;
    while (dts[0].hasNext()) {
        Customer rs = check <Customer>dts[0].getNext();
        name = rs.name;
    }
    testDB.stop();
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

    var x = testDB->updateWithGeneratedKeys("insert into Customers (name,
            creditLimit,country) values ('Sam', 1200, 'USA')", ());

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

    testDB.stop();
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

        var x = testDB->batchUpdate("Insert into Customers values (?,?,?,?)", parameters1, parameters2);
        match x {
            int[] data => {
                return data;
            }
            error err1 => {
                return [];
            }
        }
    } finally {
        testDB.stop();
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
        var temp = testDB->getProxyTable("Customers", Customer);
        match (temp) {
            table dt => {
                Customer c1 = {customerId:40, name:"Manuri", creditLimit:1000, country:"Sri Lanka"};
                Customer c2 = {customerId:41, name:"Devni", creditLimit:1000, country:"Sri Lanka"};

                var result1 = dt.add(c1);
                var result2 = dt.add(c2);
            }
            error e => return [];
        }
        var temp2 = testDB->select("SELECT  * from Customers where customerId=40 OR customerId=41", Customer);
        match (temp2) {
            table dt2 => {
                Customer[] customerArray;
                int i = 0;
                while (dt2.hasNext()) {
                    var rs = check <Customer>dt2.getNext();
                    Customer c = {customerId:rs.customerId, name:rs.name, creditLimit:rs.creditLimit, country:rs.country
                    };
                    customerArray[i] = c;
                    i++;
                }
                return customerArray;
            }
            error e => return [];
        }
    } finally {
        testDB.stop();
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

    _ = testDB->update("CREATE TABLE Customers2(customerId INTEGER NOT NULL IDENTITY,name  VARCHAR(300),
    creditLimit DOUBLE, country  VARCHAR(300), PRIMARY KEY (customerId))");

    var insertCountRet = testDB->update("insert into Customers2 (customerId, name, creditLimit, country)
                                values (15, 'Anne', 1000, 'UK')");
    int insertCount = check insertCountRet;
    io:println(insertCount);

    var x = testDB->select("SELECT  * from Customers2", Customer);
    table t = check x;

    json j = check <json>t;
    string s = j.toString();

    testDB.stop();
    return (insertCount, j.toString());
}

function testInitWithNilDbOptions() returns (int[]) {
    endpoint h2:Client testDB {
        path:"./target/H2Client/",
        name:"TestDBH2",
        username:"SA",
        password:"",
        poolOptions:{maximumPoolSize:1}
    };
    return selectFunction(testDB);
}

function testInitWithDbOptions() returns (int[]) {
    endpoint h2:Client testDB {
        path:"./target/H2Client/",
        name:"TestDBH2",
        username:"SA",
        password:"",
        poolOptions:{maximumPoolSize:1},
        dbOptions:{"IFEXISTS":true, "DB_CLOSE_ON_EXIT":false, "AUTO_RECONNECT":true, "ACCESS_MODE_DATA":"rw",
            "PAGE_SIZE":512}
    };
    return selectFunction(testDB);
}

function testInitWithInvalidDbOptions() returns (int[]) {
    endpoint h2:Client testDB {
        path:"./target/H2Client/",
        name:"TestDBH2",
        username:"SA",
        password:"",
        poolOptions:{maximumPoolSize:1},
        dbOptions:{"IFEXISTS":true, "DB_CLOSE_ON_EXIT":false, "AUTO_RECONNECT":true, "ACCESS_MODE_DATA":"rw",
            "PAGE_SIZE":512, "INVALID_PARAM":-1}
    };
    return selectFunction(testDB);
}

function selectFunction(h2:Client testDBClient) returns (int[]) {
    endpoint h2:Client testDB = testDBClient;
    try {
        var val = testDB->select("select * from Customers where customerId=1 OR customerId=2", Customer);

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
    } finally {
        testDB.stop();
    }
    return [];
}
