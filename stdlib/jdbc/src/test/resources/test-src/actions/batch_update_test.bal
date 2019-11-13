// Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerinax/java.jdbc;

string jdbcUserName = "SA";
string jdbcPassword = "";

type ResultCount record {
    int COUNTVAL;
};

function testBatchUpdate(string jdbcURL) returns [int[], jdbc:Error?, int, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    //Batch 1
    jdbc:Parameter para1 = {sqlType: jdbc:TYPE_VARCHAR, value: "Alex"};
    jdbc:Parameter para2 = {sqlType: jdbc:TYPE_VARCHAR, value: "Smith"};
    jdbc:Parameter para3 = {sqlType: jdbc:TYPE_INTEGER, value: 20};
    jdbc:Parameter para4 = {sqlType: jdbc:TYPE_DOUBLE, value: 3400.5};
    jdbc:Parameter para5 = {sqlType: jdbc:TYPE_VARCHAR, value: "Colombo"};
    jdbc:Parameter?[] parameters1 = [para1, para2, para3, para4, para5];

    //Batch 2
    para1 = {sqlType: jdbc:TYPE_VARCHAR, value: "Alex"};
    para2 = {sqlType: jdbc:TYPE_VARCHAR, value: "Smith"};
    para3 = {sqlType: jdbc:TYPE_INTEGER, value: 20};
    para4 = {sqlType: jdbc:TYPE_DOUBLE, value: 3400.5};
    para5 = {sqlType: jdbc:TYPE_VARCHAR, value: "Colombo"};
    jdbc:Parameter?[] parameters2 = [para1, para2, para3, para4, para5];

    jdbc:BatchUpdateResult ret = testDB->batchUpdate("Insert into Customers (firstName,lastName,registrationID," +
                                     "creditLimit,country) values (?,?,?,?,?)", false, parameters1, parameters2);
    anydata[]? generatedKeys = ret.generatedKeys["CUSTOMERID"];
    int key1 = -1;
    int key2 = -1;
    if (generatedKeys is int[]) {
        key1 = generatedKeys[0];
        key2 = generatedKeys[1];
    }
    checkpanic testDB.stop();
    return [ret.updatedRowCount, ret.returnedError, key1, key2];
}

function testBatchUpdateSingleValParamArray(string jdbcURL) returns int[] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    string[] parameters1 = ["Harry"];

    string[] parameters2 = ["Ron"];

    string[][] arrayofParamArrays = [parameters1, parameters2];

    jdbc:BatchUpdateResult ret = testDB->batchUpdate("Insert into Customers (firstName) values (?)", false,
                                                    ...arrayofParamArrays);
    checkpanic testDB.stop();
    return ret.updatedRowCount;
}

type myBatchType string | int | float;

function testBatchUpdateWithValues(string jdbcURL) returns int[] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    //Batch 1
    myBatchType?[] parameters1 = ["Alex", "Smith", 20, 3400.5, "Colombo"];

    //Batch 2
    myBatchType?[] parameters2 = ["John", "Gates", 45, 2400.5, "NY"];

    jdbc:BatchUpdateResult ret = testDB->batchUpdate("Insert into Customers (firstName,lastName,registrationID," +
                            "creditLimit,country) values (?,?,?,?,?)", false, parameters1, parameters2);
    checkpanic testDB.stop();
    return ret.updatedRowCount;
}

function testBatchUpdateWithVariables(string jdbcURL) returns int[] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    //Batch 1
    string firstName1 = "Alex";
    string lastName1 = "Smith";
    int id = 20;
    float creditlimit = 3400.5;
    string city = "Colombo";

    myBatchType?[] parameters1 = [firstName1, lastName1, id, creditlimit, city];

    //Batch 2
    myBatchType?[] parameters2 = ["John", "Gates", 45, 2400.5, "NY"];

    jdbc:BatchUpdateResult ret = testDB->batchUpdate("Insert into Customers (firstName,lastName,registrationID," +
                            "creditLimit,country) values (?,?,?,?,?)", false, parameters1, parameters2);
    checkpanic testDB.stop();
    return ret.updatedRowCount;
}

function testBatchUpdateWithFailure(string jdbcURL) returns @tainted [int[], int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    //Batch 1
    jdbc:Parameter para0 = {sqlType: jdbc:TYPE_INTEGER, value: 111};
    jdbc:Parameter para1 = {sqlType: jdbc:TYPE_VARCHAR, value: "Alex"};
    jdbc:Parameter para2 = {sqlType: jdbc:TYPE_VARCHAR, value: "Smith"};
    jdbc:Parameter para3 = {sqlType: jdbc:TYPE_INTEGER, value: 20};
    jdbc:Parameter para4 = {sqlType: jdbc:TYPE_DOUBLE, value: 3400.5};
    jdbc:Parameter para5 = {sqlType: jdbc:TYPE_VARCHAR, value: "Colombo"};
    jdbc:Parameter?[] parameters1 = [para0, para1, para2, para3, para4, para5];

    //Batch 2
    para0 = {sqlType: jdbc:TYPE_INTEGER, value: 222};
    para1 = {sqlType: jdbc:TYPE_VARCHAR, value: "Alex"};
    para2 = {sqlType: jdbc:TYPE_VARCHAR, value: "Smith"};
    para3 = {sqlType: jdbc:TYPE_INTEGER, value: 20};
    para4 = {sqlType: jdbc:TYPE_DOUBLE, value: 3400.5};
    para5 = {sqlType: jdbc:TYPE_VARCHAR, value: "Colombo"};
    jdbc:Parameter?[] parameters2 = [para0, para1, para2, para3, para4, para5];

    //Batch 3
    para0 = {sqlType: jdbc:TYPE_INTEGER, value: 222};
    para1 = {sqlType: jdbc:TYPE_VARCHAR, value: "Alex"};
    para2 = {sqlType: jdbc:TYPE_VARCHAR, value: "Smith"};
    para3 = {sqlType: jdbc:TYPE_INTEGER, value: 20};
    para4 = {sqlType: jdbc:TYPE_DOUBLE, value: 3400.5};
    para5 = {sqlType: jdbc:TYPE_VARCHAR, value: "Colombo"};
    jdbc:Parameter?[] parameters3 = [para0, para1, para2, para3, para4, para5];

    //Batch 4
    para0 = {sqlType: jdbc:TYPE_INTEGER, value: 333};
    para1 = {sqlType: jdbc:TYPE_VARCHAR, value: "Alex"};
    para2 = {sqlType: jdbc:TYPE_VARCHAR, value: "Smith"};
    para3 = {sqlType: jdbc:TYPE_INTEGER, value: 20};
    para4 = {sqlType: jdbc:TYPE_DOUBLE, value: 3400.5};
    para5 = {sqlType: jdbc:TYPE_VARCHAR, value: "Colombo"};
    jdbc:Parameter?[] parameters4 = [para0, para1, para2, para3, para4, para5];

    jdbc:BatchUpdateResult ret = testDB->batchUpdate("Insert into Customers (customerId, firstName,lastName,registrationID," +
        "creditLimit, country) values (?,?,?,?,?,?)", false, parameters1, parameters2, parameters3, parameters4);
    int[] updateCount = ret.updatedRowCount;
    var dt = testDB->select("SELECT count(*) as countval from Customers where customerId in (111,222,333)",
        ResultCount);
    int count = -1;
    if (dt is table<ResultCount>) {
        foreach var x in dt {
            count = <@untainted>x.COUNTVAL;
        }
    }
    checkpanic testDB.stop();
    return [updateCount, count];
}

function testBatchUpdateWithNullParam(string jdbcURL) returns int[] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    jdbc:BatchUpdateResult ret = testDB->batchUpdate("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)" +
                                     "values ('Alex','Smith',20,3400.5,'Colombo')", false);
    int[] updateCount = ret.updatedRowCount;
    checkpanic testDB.stop();
    return updateCount;
}

function testFailedBatchUpdate(string jdbcURL) returns [string, int, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    int[] updateCount = [];
    string returnVal = "";
    //Batch 1
    jdbc:Parameter para1 = {sqlType: jdbc:TYPE_VARCHAR, value: "Alex"};
    jdbc:Parameter para2 = {sqlType: jdbc:TYPE_VARCHAR, value: "Smith"};
    jdbc:Parameter para3 = {sqlType: jdbc:TYPE_INTEGER, value: 20};
    jdbc:Parameter para4 = {sqlType: jdbc:TYPE_DOUBLE, value: 3400.5};
    jdbc:Parameter para5 = {sqlType: jdbc:TYPE_VARCHAR, value: "Colombo"};
    jdbc:Parameter?[] parameters1 = [para1, para2, para3, para4, para5];

    //Batch 2
    para1 = {sqlType: jdbc:TYPE_VARCHAR, value: "Alex"};
    para2 = {sqlType: jdbc:TYPE_VARCHAR, value: "Smith"};
    para3 = {sqlType: jdbc:TYPE_INTEGER, value: 20};
    para4 = {sqlType: jdbc:TYPE_DOUBLE, value: 3400.5};
    para5 = {sqlType: jdbc:TYPE_VARCHAR, value: "Colombo"};
    jdbc:Parameter?[] parameters2 = [para1, para2, para3, para4, para5];

    jdbc:BatchUpdateResult x = testDB->batchUpdate("Insert into CustData (firstName,lastName,registrationID," +
                                     "creditLimit,country) values (?,?,?,?,?)", false, parameters1, parameters2);

    int batch1Status = 0;
    int batch2Status = 0;
    error? e = x.returnedError;
    if (e is ()) {
        returnVal = returnVal + "success";
    } else {
        returnVal = returnVal + <string>e.detail()["message"];
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

function testErrorWithBatchUpdate(string jdbcURL) returns @tainted [string, string, boolean, boolean, boolean] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: jdbcUserName,
        password: jdbcPassword,
        poolOptions: {maximumPoolSize: 1}
    });

    //Batch 1
    jdbc:Parameter para1 = {sqlType: jdbc:TYPE_VARCHAR, value: "Alex"};
    jdbc:Parameter para2 = {sqlType: jdbc:TYPE_VARCHAR, value: "Smith"};
    jdbc:Parameter para3 = {sqlType: jdbc:TYPE_INTEGER, value: 20};
    jdbc:Parameter para4 = {sqlType: jdbc:TYPE_DOUBLE, value: 3400.5};
    jdbc:Parameter para5 = {sqlType: jdbc:TYPE_VARCHAR, value: "Colombo"};
    jdbc:Parameter?[] parameters1 = [para1, para2, para3, para4, para5];

    //Batch 2
    para1 = {sqlType: jdbc:TYPE_VARCHAR, value: "Alex"};
    para2 = {sqlType: jdbc:TYPE_VARCHAR, value: "Smith"};
    para3 = {sqlType: jdbc:TYPE_INTEGER, value: 20};
    para4 = {sqlType: jdbc:TYPE_DOUBLE, value: 3400.5};
    para5 = {sqlType: jdbc:TYPE_VARCHAR, value: "Colombo"};
    jdbc:Parameter?[] parameters2 = [para1, para2, para3, para4, para5];

    jdbc:BatchUpdateResult x = testDB->batchUpdate("Insert into CustData (firstName,lastName,registrationID,creditLimit,country)" +
                                     "values (?,?,?,?,?)", false, parameters1, parameters2);

    string returnVal = "";
    int[] updateCount = x.updatedRowCount;
    if (updateCount[0] == -3 && updateCount[1] == -3) {
        returnVal = "array values are -3 ";
    } else {
        returnVal = "success";
    }

    string reason = "";
    boolean isMessageExist = false;
    boolean isSqlErrorCodeExist = false;
    boolean isSqlStateExist = false;

    error? e = x.returnedError;
    if (e is ()) {
        returnVal = "success";
    } else {
        reason = e.reason();
        if (e.detail()["message"] is string) {
            isMessageExist = true;
        }
        if (e.detail()["sqlErrorCode"] is int) {
            isSqlErrorCodeExist = true;
        }
        if (e.detail()["sqlState"] is string) {
            isSqlStateExist = true;
        }
    }

    checkpanic testDB.stop();
    return [returnVal, reason, isMessageExist, isSqlErrorCodeExist, isSqlStateExist];
}
