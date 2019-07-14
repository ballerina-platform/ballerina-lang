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

import ballerinax/jdbc;
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

function testSelectData() returns string {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });
    string returnData = "";
    var x = testDB->select("SELECT Name from Customers where registrationID = 1", ());
    json j = getJsonConversionResult(x);
    returnData = io:sprintf("%s", j);
    checkpanic testDB.stop();
    return returnData;
}

function testErrorWithSelectData() returns string {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });
    string retVal;
    var x = testDB->select("SELECT Name from Customers where registrationID = 1", ());

    if (x is table<record {}>) {
        var jsonConversionResult = typedesc<json>.constructFrom(x);
        if (jsonConversionResult is json) {
            retVal = io:sprintf("%s", jsonConversionResult);
        } else {
            retVal =  <string> jsonConversionResult.detail()["message"];
        }
    } else {
        error e = x;
        retVal = io:sprintf("%s", e);
    }

    checkpanic testDB.stop();
    return retVal;
}

function testGeneratedKeyOnInsert() returns int|string {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    int|string ret = "";

    var x = testDB->update("insert into Customers (name,lastName,
                             registrationID,creditLimit,country) values ('Mary', 'Williams', 3, 5000.75, 'USA')");

    if (x is jdbc:UpdateResult) {
        ret = x.generatedKeys.length();
    } else {
        error e = x;
        ret = <string> e.detail()["message"];
    }

    checkpanic testDB.stop();
    return ret;
}

function testGeneratedKeyOnInsertError() returns int|string {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TestDBH2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    int|string ret = "";

    var x = testDB->update("insert into Customers (name,lastName,
                             registrationID,creditLimit,country) values ('Mary', 'Williams', 3, 5000.75, 'USA')");

    if (x is jdbc:UpdateResult) {
        ret = x.generatedKeys.length();
    } else {
        error e = x;
        ret = io:sprintf("%s", e);
    }

    checkpanic testDB.stop();
    return ret;
}

function testUpdateReslt() returns int|string {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    int|string ret = "";

    var x = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                         values ('James', 'Clerk', 3, 5000.75, 'USA')");
    checkpanic testDB.stop();
    if (x is jdbc:UpdateResult) {
        x.updatedRowCount = 0;
    } else {
        error e = x;
        ret = <string> e.detail()["message"];
    }
    return ret;
}

function testBatchUpdate() returns [string, int, int] {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    int[] updateCount = [];
    string returnVal = "";
    //Batch 1
    jdbc:Parameter para1 = { sqlType: jdbc:TYPE_VARCHAR, value: "Alex" };
    jdbc:Parameter para2 = { sqlType: jdbc:TYPE_VARCHAR, value: "Smith" };
    jdbc:Parameter para3 = { sqlType: jdbc:TYPE_INTEGER, value: 20 };
    jdbc:Parameter para4 = { sqlType: jdbc:TYPE_DOUBLE, value: 3400.5 };
    jdbc:Parameter para5 = { sqlType: jdbc:TYPE_VARCHAR, value: "Colombo" };
    jdbc:Parameter?[] parameters1 = [para1, para2, para3, para4, para5];

    //Batch 2
    para1 = { sqlType: jdbc:TYPE_VARCHAR, value: "Alex" };
    para2 = { sqlType: jdbc:TYPE_VARCHAR, value: "Smith" };
    para3 = { sqlType: jdbc:TYPE_INTEGER, value: 20 };
    para4 = { sqlType: jdbc:TYPE_DOUBLE, value: 3400.5 };
    para5 = { sqlType: jdbc:TYPE_VARCHAR, value: "Colombo" };
    jdbc:Parameter?[] parameters2 = [para1, para2, para3, para4, para5];

    jdbc:BatchUpdateResult x = testDB->batchUpdate("Insert into CustData (firstName,lastName,registrationID,creditLimit,country)
                                     values (?,?,?,?,?)",false, parameters1, parameters2);

    int batch1Status = 0;
    int batch2Status = 0;
    error? e = x.returnedError;
    if (e is ()) {
        returnVal = returnVal + "success";
    } else {
        returnVal = returnVal + <string> e.detail()["message"];
    }
    updateCount = x.updatedRowCount;
    if (updateCount[0] == -3 && updateCount[1] == -3) {
        returnVal = returnVal + "failure";
        batch1Status = updateCount[0];
        batch2Status = updateCount[1];
    } else {
        returnVal = returnVal + "success";
    }
    checkpanic testDB.stop();
    return [returnVal, batch1Status, batch2Status];
}

function testErrorWithBatchUpdate() returns string {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    int[] updateCount = [];
    string returnVal = "";
    //Batch 1
    jdbc:Parameter para1 = { sqlType: jdbc:TYPE_VARCHAR, value: "Alex" };
    jdbc:Parameter para2 = { sqlType: jdbc:TYPE_VARCHAR, value: "Smith" };
    jdbc:Parameter para3 = { sqlType: jdbc:TYPE_INTEGER, value: 20 };
    jdbc:Parameter para4 = { sqlType: jdbc:TYPE_DOUBLE, value: 3400.5 };
    jdbc:Parameter para5 = { sqlType: jdbc:TYPE_VARCHAR, value: "Colombo" };
    jdbc:Parameter?[] parameters1 = [para1, para2, para3, para4, para5];

    //Batch 2
    para1 = { sqlType: jdbc:TYPE_VARCHAR, value: "Alex" };
    para2 = { sqlType: jdbc:TYPE_VARCHAR, value: "Smith" };
    para3 = { sqlType: jdbc:TYPE_INTEGER, value: 20 };
    para4 = { sqlType: jdbc:TYPE_DOUBLE, value: 3400.5 };
    para5 = { sqlType: jdbc:TYPE_VARCHAR, value: "Colombo" };
    jdbc:Parameter?[] parameters2 = [para1, para2, para3, para4, para5];

    var x = testDB->batchUpdate("Insert into CustData (firstName,lastName,registrationID,creditLimit,country)
                                     values (?,?,?,?,?)", false, parameters1, parameters2);

    updateCount = x.updatedRowCount;
    if (updateCount[0] == -3 && updateCount[1] == -3) {
        returnVal = "array values are -3 ";
    } else {
        returnVal = "success";
    }

    error? e = x.returnedError;
    if (e is ()) {
        returnVal = "success";
    } else {
        returnVal = returnVal + io:sprintf("%s", e);
    }
    checkpanic testDB.stop();
    return returnVal;
}

function testInvalidArrayofQueryParameters() returns @tainted string {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    string returnData = "";
    xml x1 = xml `<book>The Lost World</book>`;
    xml x2 = xml `<book>The Lost World2</book>`;
    xml[] xmlDataArray = [x1, x2];
    jdbc:Parameter para0 = { sqlType: jdbc:TYPE_INTEGER, value: xmlDataArray };
    var x = testDB->select("SELECT FirstName from Customers where registrationID in (?)", (), para0);

    if (x is table<record {}>) {
        var j = typedesc<json>.constructFrom(x);
        if (j is json) {
            returnData = io:sprintf("%s", j);
        } else {
            error e = j;
            returnData = e.reason();
        }
    } else {
        error e = x;
        returnData = <string> e.detail()["message"];
    }
    checkpanic testDB.stop();
    return returnData;
}

function testErrorWithInvalidArrayofQueryParameters() returns string {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    string returnData = "";
    xml x1 = xml `<book>The Lost World</book>`;
    xml x2 = xml `<book>The Lost World2</book>`;
    xml[] xmlDataArray = [x1, x2];
    jdbc:Parameter para0 = { sqlType: jdbc:TYPE_INTEGER, value: xmlDataArray };
    var x = testDB->select("SELECT FirstName from Customers where registrationID in (?)", (), para0);

    if (x is table<record {}>) {
        var j = typedesc<json>.constructFrom(x);
        if (j is json) {
            returnData = io:sprintf("%s", j);
        } else {
            error e = j;
            returnData = e.reason();
        }
    } else {
        error e = x;
        returnData = io:sprintf("%s", e);
    }
    checkpanic testDB.stop();
    return returnData;
}

function testCheckApplicationErrorType() returns [boolean, boolean, boolean] {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    string returnData = "";
    xml x1 = xml `<book>The Lost World</book>`;
    xml x2 = xml `<book>The Lost World2</book>`;
    xml[] xmlDataArray = [x1, x2];
    jdbc:Parameter para0 = { sqlType: jdbc:TYPE_INTEGER, value: xmlDataArray };
    var x = testDB->select("SELECT FirstName from Customers where registrationID in (?)", (), para0);

    boolean isError = false;
    boolean isJdbcError = false;
    boolean isApplicationError = false;

    if (x is error) {
        isError = true;
    }
    if (x is jdbc:Error) {
        isJdbcError = true;
    }
    if (x is jdbc:ApplicationError) {
        isApplicationError = true;
    }
    checkpanic testDB.stop();
    return [isError, isJdbcError, isApplicationError];
}

function testCheckDatabaseErrorType() returns [boolean, boolean, boolean] {
    jdbc:Client testDB = new({
            url: "jdbc:h2:file:./target/tempdb/TEST_SQL_CONNECTOR_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });
    json retVal;
    var x = testDB->select("SELECT Name from Customers where registrationID = 1", ());

    boolean isError = false;
    boolean isJdbcError = false;
    boolean isDatabaseError = false;

    if (x is error) {
        isError = true;
    }
    if (x is jdbc:Error) {
        isJdbcError = true;
    }
    if (x is jdbc:DatabaseError) {
        isDatabaseError = true;
    }
    checkpanic testDB.stop();
    return [isError, isJdbcError, isDatabaseError];
}

function getJsonConversionResult(table<record {}>|error tableOrError) returns json {
    json retVal;
    if (tableOrError is table<record {}>) {
        var jsonConversionResult = typedesc<json>.constructFrom(tableOrError);
        if (jsonConversionResult is json) {
            retVal = jsonConversionResult;
        } else {
            retVal = { "Error": <string> jsonConversionResult.detail()["message"] };
        }
    } else {
        retVal = { "Error": <string> tableOrError.detail()["message"] };
    }
    return retVal;
}
