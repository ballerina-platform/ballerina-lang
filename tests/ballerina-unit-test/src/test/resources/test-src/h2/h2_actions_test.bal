import ballerina/h2;
import ballerina/sql;
import ballerina/io;

public type Customer record {
    int customerId;
    string name;
    float creditLimit;
    string country;
};

public type Result record {
   int val;
};

function testSelect() returns (int[]) {
    endpoint h2:Client testDB {
        path: "./target/H2Client/",
        name: "TestDBH2",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    };

    var val = testDB->select("select * from Customers where customerId=1 OR customerId=2", Customer);

    int[] customerIds = [];

    if (val is table<Customer>) {
        int i = 0;
        while (val.hasNext()) {
            var rs = <Customer>val.getNext();
            if (rs is Customer) {
                customerIds[i] = rs.customerId;
                i += 1;
            }
        }
        return customerIds;
    } else {
        return [];
    }
}

function testUpdate() returns (int) {
    endpoint h2:Client testDB {
        path: "./target/H2Client/",
        name: "TestDBH2",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    };

    var insertCountRet = testDB->update("insert into Customers (customerId, name, creditLimit, country)
                                values (15, 'Anne', 1000, 'UK')");
    int insertCount = -1;
    if (insertCountRet is int) {
        insertCount = insertCountRet;
    }
    testDB.stop();
    return insertCount;
}

function testCall() returns (string) {
    endpoint h2:Client testDB {
        path: "./target/H2Client/",
        name: "TestDBH2",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    };

    var ret = testDB->call("{call JAVAFUNC('select * from Customers where customerId=1')}", [Customer]);

    table[] dts= [];
    if (ret is table[]) {
        dts = ret;
    } else if (ret is ()) {
        return "nil";
    } else if (ret is error) {
        return <string> ret.detail().message;
    }

    string name = "";
    while (dts[0].hasNext()) {
        var rs = <Customer>dts[0].getNext();
        if (rs is Customer) {
            name = rs.name;
        }
    }
    testDB.stop();
    return name;
}

function testGeneratedKeyOnInsert() returns (string) {
    endpoint h2:Client testDB {
        path: "./target/H2Client/",
        name: "TestDBH2",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    };

    string returnVal = "";

    var x = testDB->updateWithGeneratedKeys("insert into Customers (name,
            creditLimit,country) values ('Sam', 1200, 'USA')", ());

    if (x is (int, string[])) {
        int a;
        string[] b;
        (a, b) = x;
        returnVal = b[0];
    } else if (x is error) {
        returnVal = <string> x.detail().message;
    }

    testDB.stop();
    return returnVal;
}

function testBatchUpdate() returns (int[]) {
    endpoint h2:Client testDB {
        path: "./target/H2Client/",
        name: "TestDBH2",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    };

    int[] updateCount;
    string returnVal;
    //Batch 1
    sql:Parameter para1 = { sqlType: sql:TYPE_INTEGER, value: 10 };
    sql:Parameter para2 = { sqlType: sql:TYPE_VARCHAR, value: "Smith" };
    sql:Parameter para3 = { sqlType: sql:TYPE_DOUBLE, value: 3400.5 };
    sql:Parameter para4 = { sqlType: sql:TYPE_VARCHAR, value: "Australia" };
    sql:Parameter[] parameters1 = [para1, para2, para3, para4];

    //Batch 2
    sql:Parameter para5 = { sqlType: sql:TYPE_INTEGER, value: 11 };
    sql:Parameter para6 = { sqlType: sql:TYPE_VARCHAR, value: "John" };
    sql:Parameter para7 = { sqlType: sql:TYPE_DOUBLE, value: 3400.2 };
    sql:Parameter para8 = { sqlType: sql:TYPE_VARCHAR, value: "UK" };
    sql:Parameter[] parameters2 = [para5, para6, para7, para8];

    var x = testDB->batchUpdate("Insert into Customers values (?,?,?,?)", parameters1, parameters2);

    int [] ret = [];
    if (x is int[]) {
        ret = x;
    } else if (x is error) {
        ret = [];
    }
    testDB.stop();
    return ret;
}

function testUpdateInMemory() returns (int, string) {
    endpoint h2:Client testDB {
        name: "TestDB2H2",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    };

    _ = testDB->update("CREATE TABLE Customers2(customerId INTEGER NOT NULL IDENTITY,name  VARCHAR(300),
    creditLimit DOUBLE, country  VARCHAR(300), PRIMARY KEY (customerId))");

    var insertCountRet = testDB->update("insert into Customers2 (customerId, name, creditLimit, country)
                                values (15, 'Anne', 1000, 'UK')");
    int insertCount = -1;
    if (insertCountRet is int) {
        insertCount = insertCountRet;
    }
    io:println(insertCount);

    var x = testDB->select("SELECT  * from Customers2", Customer);
    string s = "";
    if (x is table) {
        var res = <json>x;
        if (res is json) {
            s = res.toString();
        }
    }

    testDB.stop();
    return (insertCount, s);
}

function testInitWithNilDbOptions() returns (int[]) {
    endpoint h2:Client testDB {
        path: "./target/H2Client/",
        name: "TestDBH2",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    };
    return selectFunction(testDB);
}

function testInitWithDbOptions() returns (int[]) {
    endpoint h2:Client testDB {
        path: "./target/H2Client/",
        name: "TestDBH2",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 },
        dbOptions: { "IFEXISTS": true, "DB_CLOSE_ON_EXIT": false, "AUTO_RECONNECT": true, "ACCESS_MODE_DATA": "rw",
            "PAGE_SIZE": 512 }
    };
    return selectFunction(testDB);
}

function testInitWithInvalidDbOptions() returns (int[]) {
    endpoint h2:Client testDB {
        path: "./target/H2Client/",
        name: "TestDBH2",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 },
        dbOptions: { "IFEXISTS": true, "DB_CLOSE_ON_EXIT": false, "AUTO_RECONNECT": true, "ACCESS_MODE_DATA": "rw",
            "PAGE_SIZE": 512, "INVALID_PARAM": -1 }
    };
    return selectFunction(testDB);
}

function testReInitEndpoint() returns int {
    endpoint h2:Client testDB {
        path: "./target/H2Client/",
        name: "TestDBH2",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    };

    h2:ClientEndpointConfiguration config = {
        path: "./target/H2Client/",
        name: "TestDBH2",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    };

    testDB.stop();

    testDB.init(config);

    var dt = testDB->select("select 1", Result);
    int count = -1;
    if (dt is table) {
        while (dt.hasNext()) {
            var rs = <Result>dt.getNext();
            if (rs is Result) {
                count = rs.val;
            }
        }
    }
    testDB.stop();

    return count;
}

function selectFunction(h2:Client testDBClient) returns (int[]) {
    endpoint h2:Client testDB = testDBClient;

    var val = testDB->select("select * from Customers where customerId=1 OR customerId=2", Customer);

    int[] customerIds = [];
    if (val is table<Customer>) {
        int i = 0;
            while (val.hasNext()) {
                var rs = <Customer>val.getNext();
                if (rs is Customer) {
                    customerIds[i] = rs.customerId;
                    i += 1;
                }
            }
    } else if (val is error) {
        customerIds = [];
    }
    testDB.stop();
    return customerIds;
}

function testH2MemDBUpdate() returns (int, string) {
    endpoint h2:Client testDB {
        name: "TestMEMDB",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    };

    var insertCountRet = testDB->update("CREATE TABLE student(id INTEGER,  name VARCHAR(30))");
    insertCountRet = testDB->update("insert into student (id, name) values (15, 'Anne')");
    var dt = testDB->select("Select * From student", ());

    string data = "";
    if (dt is table) {
        var j = <json>dt;
        if (j is json) {
            data = io:sprintf("%s", j);
        }
    }
    int insertCount = -1;
    if (insertCountRet is int) {
        insertCount = insertCountRet;
    }
    testDB.stop();
    return (insertCount, data);
}
