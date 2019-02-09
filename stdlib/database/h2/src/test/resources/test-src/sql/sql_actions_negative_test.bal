// Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/sql;
import ballerina/h2;
import ballerina/io;

type ResultCustomers record {
    string FIRSTNAME;
};

type Person record {
    int id;
    string name;
};

type ResultCustomers2 record {
    string FIRSTNAME;
    string LASTNAME;
};

function testSelectData() returns (string) {
    h2:Client testDB = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });
    string returnData = "";
    var x = testDB->select("SELECT Name from Customers where registrationID = 1", ());
    json j = getJsonConversionResult(x);
    returnData = io:sprintf("%s", j);
    testDB.stop();
    return returnData;
}

function testGeneratedKeyOnInsert() returns (string) {
    h2:Client testDB = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    string ret = "";
    string[] generatedID = [];
    int insertCount;
    var x = testDB->updateWithGeneratedKeys("insert into Customers (name,lastName,
                             registrationID,creditLimit,country) values ('Mary', 'Williams', 3, 5000.75, 'USA')", ());
    if (x is (int, string[])) {
        (_, generatedID) = x;
        ret = generatedID[0];
    } else {
        ret = string.convert(x.detail().message);
    }
    testDB.stop();
    return ret;
}

function testCallProcedure() returns (string) {
    h2:Client testDB = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });
    string returnData = "";
    var x = trap testDB->call("{call InsertPersonDataInfo(100,'James')}", ());

    if (x is table<record {}>[]) {
        var j = json.convert(x[0]);
        if (j is json) {
            returnData = io:sprintf("%s", j);
        } else {
            returnData = j.reason();
        }
    } else if (x is ()) {
        returnData = "";
    } else {
        returnData = x.reason();
    }
    testDB.stop();
    return returnData;
}

function testBatchUpdate() returns (string) {
    h2:Client testDB = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    int[] updateCount = [];
    string returnVal = "";
    //Batch 1
    sql:Parameter para1 = { sqlType: sql:TYPE_VARCHAR, value: "Alex" };
    sql:Parameter para2 = { sqlType: sql:TYPE_VARCHAR, value: "Smith" };
    sql:Parameter para3 = { sqlType: sql:TYPE_INTEGER, value: 20 };
    sql:Parameter para4 = { sqlType: sql:TYPE_DOUBLE, value: 3400.5 };
    sql:Parameter para5 = { sqlType: sql:TYPE_VARCHAR, value: "Colombo" };
    sql:Parameter?[] parameters1 = [para1, para2, para3, para4, para5];

    //Batch 2
    para1 = { sqlType: sql:TYPE_VARCHAR, value: "Alex" };
    para2 = { sqlType: sql:TYPE_VARCHAR, value: "Smith" };
    para3 = { sqlType: sql:TYPE_INTEGER, value: 20 };
    para4 = { sqlType: sql:TYPE_DOUBLE, value: 3400.5 };
    para5 = { sqlType: sql:TYPE_VARCHAR, value: "Colombo" };
    sql:Parameter?[] parameters2 = [para1, para2, para3, para4, para5];

    var x = trap testDB->batchUpdate("Insert into CustData (firstName,lastName,registrationID,creditLimit,country)
                                     values (?,?,?,?,?)", parameters1, parameters2);
    if (x is int[]) {
        updateCount = x;
        if (updateCount[0] == -3 && updateCount[1] == -3) {
            returnVal = "failure";
        } else {
            returnVal = "success";
        }
    } else {
        returnVal = string.convert(x.detail().message);
    }
    testDB.stop();
    return returnVal;
}

function testInvalidArrayofQueryParameters() returns (string) {
    h2:Client testDB = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    string returnData = "";
    xml x1 = xml `<book>The Lost World</book>`;
    xml x2 = xml `<book>The Lost World2</book>`;
    xml[] xmlDataArray = [x1, x2];
    sql:Parameter para0 = { sqlType: sql:TYPE_INTEGER, value: xmlDataArray };
    var x = trap testDB->select("SELECT FirstName from Customers where registrationID in (?)", (), para0);

    if (x is table<record {}>) {
        var j = json.convert(x);
        if (j is json) {
            returnData = io:sprintf("%s", j);
        } else {
            returnData = j.reason();
        }
    } else {
        returnData = string.convert(x.detail().message);
    }
    testDB.stop();
    return returnData;
}

function testCallProcedureWithMultipleResultSetsAndLowerConstraintCount(
             ) returns ((string, string)|error|()) {
    h2:Client testDB = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    var ret = testDB->call("{call SelectPersonDataMultiple()}", [ResultCustomers]);
    (string, string)|error|() retVal = ();
    if (ret is table<record {}>[]) {
        string firstName1 = "";
        string firstName2 = "";
        while (ret[0].hasNext()) {
            var rs = ret[0].getNext();
            if (rs is ResultCustomers) {
                firstName1 = rs.FIRSTNAME;
            }
        }
        while (ret[1].hasNext()) {
            var rs = ret[1].getNext();
            if (rs is ResultCustomers) {
                firstName2 = rs.FIRSTNAME;
            }
        }
        retVal = (firstName1, firstName2);
    } else if (ret is ()) {
        retVal = ("", "");
    } else {
        retVal = ret;
    }
    testDB.stop();
    return retVal;
}

function testCallProcedureWithMultipleResultSetsAndHigherConstraintCount(string jdbcUrl, string userName, string
    password) returns ((string, string)|error|()) {
    h2:Client testDB = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    var ret = testDB->call("{call SelectPersonDataMultiple()}", [ResultCustomers, ResultCustomers2, Person]);

    (string, string)|error|() retVal = ();
    if (ret is table<record {}>[]) {
        string firstName1 = "";
        string firstName2 = "";
        while (ret[0].hasNext()) {
            var rs = ret[0].getNext();
            if (rs is ResultCustomers) {
                firstName1 = rs.FIRSTNAME;
            }
        }
        while (ret[1].hasNext()) {
            var rs = ret[1].getNext();
            if (rs is ResultCustomers) {
                firstName2 = rs.FIRSTNAME;
            }
        }
        retVal = (firstName1, firstName2);
    } else if (ret is ()) {
        retVal = ("", "");
    } else {
        retVal = ret;
    }
    testDB.stop();
    return retVal;
}

function testCallProcedureWithMultipleResultSetsAndNilConstraintCount()
             returns (string|(string, string)|error) {
    h2:Client testDB = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    var ret = testDB->call("{call SelectPersonDataMultiple()}", ());
    string|(string, string)|error retVal;
    if (ret is table<record {}>[]) {
        string firstName1 = "";
        string firstName2 = "";
        while (ret[0].hasNext()) {
            var rs = ret[0].getNext();
            if (rs is ResultCustomers) {
                firstName1 = rs.FIRSTNAME;
            }
        }
        while (ret[1].hasNext()) {
            var rs = ret[1].getNext();
            if (rs is ResultCustomers) {
                firstName2 = rs.FIRSTNAME;
            }
        }
        retVal = (firstName1, firstName2);
    } else if (ret is ()) {
        retVal = "nil";
    } else {
        retVal = ret;
    }
    testDB.stop();
    return retVal;
}

function getJsonConversionResult(table<record {}>|error tableOrError) returns json {
    json retVal;
    if (tableOrError is table<record {}>) {
        var jsonConversionResult = json.convert(tableOrError);
        if (jsonConversionResult is json) {
            retVal = jsonConversionResult;
        } else {
            retVal = {"Error" : string.convert(jsonConversionResult.detail().message)};
        }
    } else {
        retVal = {"Error" : string.convert(tableOrError.detail().message)};
    }
    return retVal;
}
