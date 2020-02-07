// Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied. See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/io;
import ballerina/jsonutils;
import ballerinax/java.jdbc;

public type Customer record {
    int customerId;
    string name;
    float creditLimit;
    string country;
};

public type Result record {
    int val;
};

function testSelect(string jdbcURL) returns @tainted int[] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var val = testDB->select("select * from Customers where customerId=1 OR customerId=2", Customer);

    int[] customerIds = [];

    if (val is table<Customer>) {
        int i = 0;
        while (val.hasNext()) {
            var rs = val.getNext();
            if (rs is Customer) {
                customerIds[i] = rs.customerId;
                i += 1;
            }
        }
    }
    checkpanic testDB.stop();
    return customerIds;
}

function testUpdate(string jdbcURL) returns int {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var insertCountRet = testDB->update("insert into Customers (customerId, name, creditLimit, country)" +
                                "values (15, 'Anne', 1000, 'UK')");
    int insertCount = 0;
    if (insertCountRet is jdbc:UpdateResult) {
        insertCount = insertCountRet.updatedRowCount;
    }
    checkpanic testDB.stop();
    return insertCount;
}

function testCall(string jdbcURL) returns @tainted string {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var ret = testDB->call("{call JAVAFUNC('select * from Customers where customerId=1')}", [Customer]);

    table<record {}>[] dts = [];
    if (ret is table<record {}>[]) {
        dts = ret;
    } else if (ret is ()) {
        return "nil";
    } else {
        error e = ret;
        return <string>e.detail()["message"];
    }

    string name = "";
    while (dts[0].hasNext()) {
        var rs = dts[0].getNext();
        if (rs is Customer) {
            name = rs.name;
        }
    }
    checkpanic testDB.stop();
    return name;
}

function testGeneratedKeyOnInsert(string jdbcURL) returns string | int {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    string | int returnVal = "";

    var x = testDB->update("insert into Customers (name, creditLimit,country) values ('Sam', 1200, 'USA')");

    if (x is jdbc:UpdateResult) {
        returnVal = x.updatedRowCount;
    } else {
        error e = x;
        returnVal = <string>e.detail()["message"];
    }

    checkpanic testDB.stop();
    return returnVal;
}

function testBatchUpdate(string jdbcURL) returns int[] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    int[] updateCount;
    string returnVal;
    //Batch 1
    jdbc:Parameter para1 = {sqlType: jdbc:TYPE_INTEGER, value: 10};
    jdbc:Parameter para2 = {sqlType: jdbc:TYPE_VARCHAR, value: "Smith"};
    jdbc:Parameter para3 = {sqlType: jdbc:TYPE_DOUBLE, value: 3400.5};
    jdbc:Parameter para4 = {sqlType: jdbc:TYPE_VARCHAR, value: "Australia"};
    jdbc:Parameter?[] parameters1 = [para1, para2, para3, para4];

    //Batch 2
    jdbc:Parameter para5 = {sqlType: jdbc:TYPE_INTEGER, value: 11};
    jdbc:Parameter para6 = {sqlType: jdbc:TYPE_VARCHAR, value: "John"};
    jdbc:Parameter para7 = {sqlType: jdbc:TYPE_DOUBLE, value: 3400.2};
    jdbc:Parameter para8 = {sqlType: jdbc:TYPE_VARCHAR, value: "UK"};
    jdbc:Parameter?[] parameters2 = [para5, para6, para7, para8];

    jdbc:BatchUpdateResult x = testDB->batchUpdate("Insert into Customers values (?,?,?,?)", false, parameters1, parameters2);
    checkpanic testDB.stop();
    return x.updatedRowCount;
}

function testUpdateInMemory(string jdbcURL) returns @tainted [int, string] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    _ = checkpanic testDB->update("CREATE TABLE Customers2(customerId INTEGER NOT NULL IDENTITY,name  VARCHAR(300)," +
    "creditLimit DOUBLE, country  VARCHAR(300), PRIMARY KEY (customerId))");

    var insertCountRet = testDB->update("insert into Customers2 (customerId, name, creditLimit, country) " +
                                "values (15, 'Anne', 1000, 'UK')");
    int insertCount = 0;
    if (insertCountRet is jdbc:UpdateResult) {
        insertCount = insertCountRet.updatedRowCount;
    }

    var x = testDB->select("SELECT  * from Customers2", Customer);
    string s = "";
    if (x is table<Customer>) {
        var res = jsonutils:fromTable(x);
        s = res.toJsonString();
    }

    checkpanic testDB.stop();
    return [insertCount, s];
}

function testInitWithNilDbOptions(string jdbcURL) returns int[] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });
    return selectFunction(testDB);
}

function testInitWithDbOptions(string jdbcURL) returns int[] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1},
        dbOptions: {
            "IFEXISTS": true,
            "DB_CLOSE_ON_EXIT": false,
            "AUTO_RECONNECT": true,
            "ACCESS_MODE_DATA": "rw",
            "PAGE_SIZE": 512
        }
    });
    return selectFunction(testDB);
}

function testInitWithInvalidDbOptions(string jdbcURL) returns int[] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1, dataSourceClassName: "org.h2.jdbcx.JdbcDataSource"},
        dbOptions: {"INVALID_PARAM": -1}
    });
    return selectFunction(testDB);
}

function testCloseConnectionPool(string jdbcURL)
returns @tainted int {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });
    var result = testDB->select("SELECT COUNT(*) FROM INFORMATION_SCHEMA.SESSIONS", Result);
    int count = -1;
    if (result is table<Result>) {
        while (result.hasNext()) {
            var rs = result.getNext();
            if (rs is Result) {
                count = rs.val;
            }
        }
    }
    checkpanic testDB.stop();
    return count;
}

function selectFunction(jdbc:Client testDB) returns int[] {
    var val = testDB->select("select * from Customers where customerId=1 OR customerId=2", Customer);

    int[] customerIds = [];
    if (val is table<Customer>) {
        int i = 0;
        while (val.hasNext()) {
            var rs = val.getNext();
            if (rs is Customer) {
                customerIds[i] = rs.customerId;
                i += 1;
            }
        }
    } else {
        customerIds = [];
    }
    checkpanic testDB.stop();
    return customerIds;
}

function testH2MemDBUpdate() returns [int, string] {
    jdbc:Client testDB = new ({
        url: "jdbc:h2:mem:TestMEMDB",
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var insertCountRet = testDB->update("CREATE TABLE student(id INTEGER,  name VARCHAR(30))");
    insertCountRet = testDB->update("insert into student (id, name) values (15, 'Anne')");
    var dt = testDB->select("Select * From student", ());

    string data = "";
    if (dt is table<record {}>) {
        var j = jsonutils:fromTable(dt);
        data = io:sprintf("%s", j.toJsonString());
    }
    int insertCount = 0;
    if (insertCountRet is jdbc:UpdateResult) {
        insertCount = insertCountRet.updatedRowCount;
    }
    checkpanic testDB.stop();
    return [insertCount, data];
}
