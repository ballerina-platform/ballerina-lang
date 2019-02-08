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

import ballerina/sql;
import ballerina/h2;
import ballerina/io;

type Info record {
   string firstName;
};

public type Result record {
    int val;
};

function testGlobalConnectionPoolSingleDestination() returns json[] {
    return drainGlobalPool("TEST_SQL_CONNECTION_POOL_1");
}

function drainGlobalPool(string dbName) returns json[] {
    h2:Client testDB1 = new({
            path: "./target/tempdb/",
            name: dbName,
            username: "SA",
            password: ""
        });
    h2:Client testDB2 = new({
            path: "./target/tempdb/",
            name: dbName,
            username: "SA",
            password: ""
        });
    h2:Client testDB3 = new({
            path: "./target/tempdb/",
            name: dbName,
            username: "SA",
            password: ""
        });
    h2:Client testDB4 = new({
            path: "./target/tempdb/",
            name: dbName,
            username: "SA",
            password: ""
        });
    h2:Client testDB5 = new({
            path: "./target/tempdb/",
            name: dbName,
            username: "SA",
            password: ""
        });

    (table<record {}>|error)[] resultArray = [];
    resultArray[0] = testDB1->select("SELECT FirstName from Customers where registrationID = 1", ());
    resultArray[1] = testDB1->select("SELECT FirstName from Customers where registrationID = 2", ());

    resultArray[2] = testDB2->select("SELECT FirstName from Customers where registrationID = 1", ());
    resultArray[3] = testDB2->select("SELECT FirstName from Customers where registrationID = 1", ());

    resultArray[4] = testDB3->select("SELECT FirstName from Customers where registrationID = 2", ());
    resultArray[5] = testDB3->select("SELECT FirstName from Customers where registrationID = 2", ());

    resultArray[6] = testDB4->select("SELECT FirstName from Customers where registrationID = 1", ());
    resultArray[7] = testDB4->select("SELECT FirstName from Customers where registrationID = 1", ());

    resultArray[8] = testDB5->select("SELECT FirstName from Customers where registrationID = 1", ());
    resultArray[9] = testDB5->select("SELECT FirstName from Customers where registrationID = 1", ());

    resultArray[10] = testDB5->select("SELECT FirstName from Customers where registrationID = 1", ());

    json[] returnArray = [];
    int i = 0;
    foreach var x in resultArray {
        returnArray[i] = getJsonConversionResult(x);
        i+=1;
    }

    // All 5 clients are supposed to use the same pool. Default maximum no of connections is 10.
    // Since each select operation hold up one connection each, the last select operation should
    // return an error
    return returnArray;
}

function testGlobalConnectionPoolsMultipleDestinations() returns (json[], json[]) {
    var errorFromFristDestination = drainGlobalPool("TEST_SQL_CONNECTION_POOL_1");
    var errorFromSecondDestination = drainGlobalPool("TEST_SQL_CONNECTION_POOL_2");
    return (errorFromFristDestination, errorFromSecondDestination);
}

function closeTable(table<record{}>|error? t) {
    if (t is table<record{}>) {
        _ = t.close();
    }
}

function testGlobalConnectionPoolSingleDestinationConcurrent() returns json[][] {
    worker w1 returns (table<record{}>|error,table<record{}>|error) {
        return testGlobalConnectionPoolConcurrentHelper1();
    }

    worker w2 returns (table<record{}>|error,table<record{}>|error) {
        return testGlobalConnectionPoolConcurrentHelper1();
    }

    worker w3 returns (table<record{}>|error,table<record{}>|error) {
        return testGlobalConnectionPoolConcurrentHelper1();
    }

    worker w4 returns (table<record{}>|error,table<record{}>|error) {
        return testGlobalConnectionPoolConcurrentHelper1();
    }

    record {
        (table<record{}>|error, table<record{}>|error) w1;
        (table<record{}>|error, table<record{}>|error) w2;
        (table<record{}>|error, table<record{}>|error) w3;
        (table<record{}>|error, table<record{}>|error) w4;
    } results = wait {w1, w2, w3, w4};

    var t = testGlobalConnectionPoolConcurrentHelper2();

    json[][] returnArray = [];
    returnArray[0] = getJsonConversionResultOfTuple(results.w1);
    returnArray[1] = getJsonConversionResultOfTuple(results.w2);
    returnArray[2] = getJsonConversionResultOfTuple(results.w3);
    returnArray[3] = getJsonConversionResultOfTuple(results.w4);
    returnArray[4] = t;

    // All 5 clients are supposed to use the same pool. Default maximum no of connections is 10.
    // Since each select operation hold up one connection each, the last select operation should
    // return an error
    return returnArray;
}

function closeTables((table<record{}>|error?, table<record{}>|error?) t) {
    table<record{}>|error? x; table<record{}>|error? y;
    (x, y) = t;
    closeTable(x);
    closeTable(y);
}

function testGlobalConnectionPoolConcurrentHelper1() returns (table<record{}>|error,table<record{}>|error) {
    h2:Client testDB1 = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_1",
            username: "SA",
            password: ""
        });
    var dt1 = testDB1->select("SELECT FirstName from Customers where registrationID = 1", ());
    var dt2 = testDB1->select("SELECT FirstName from Customers where registrationID = 2", ());
    return (dt1, dt2);
}

function testGlobalConnectionPoolConcurrentHelper2() returns json[] {
    h2:Client testDB1 = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_1",
            username: "SA",
            password: ""
        });
    json[] returnArray = [];
    var dt1 = testDB1->select("SELECT FirstName from Customers where registrationID = 1", ());
    var dt2 = testDB1->select("SELECT FirstName from Customers where registrationID = 2", ());
    var dt3 = testDB1->select("SELECT FirstName from Customers where registrationID = 1", ());
    returnArray[0] = getJsonConversionResult(dt1);
    returnArray[1] = getJsonConversionResult(dt2);
    returnArray[2] = getJsonConversionResult(dt3);

    return returnArray;
}

sql:PoolOptions poolOptions1 = { maximumPoolSize: 5, connectionTimeout: 1000 };
function testLocalSharedConnectionPoolConfigSingleDestination() returns json[] {
    h2:Client testDB1 = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_1",
            username: "SA",
            password: "",
            poolOptions: poolOptions1
        });
    h2:Client testDB2 = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_1",
            username: "SA",
            password: "",
            poolOptions: poolOptions1
        });
    h2:Client testDB3 = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_1",
            username: "SA",
            password: "",
            poolOptions: poolOptions1
        });
    h2:Client testDB4 = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_1",
            username: "SA",
            password: "",
            poolOptions: poolOptions1
        });
    h2:Client testDB5 = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_1",
            username: "SA",
            password: "",
            poolOptions: poolOptions1
        });
    (table<record {}>|error)[] resultArray = [];
    resultArray[0] = testDB1->select("SELECT FirstName from Customers where registrationID = 1", ());
    resultArray[1] = testDB2->select("SELECT FirstName from Customers where registrationID = 1", ());
    resultArray[2] = testDB3->select("SELECT FirstName from Customers where registrationID = 2", ());
    resultArray[3] = testDB4->select("SELECT FirstName from Customers where registrationID = 1", ());
    resultArray[4] = testDB5->select("SELECT FirstName from Customers where registrationID = 1", ());
    resultArray[5] = testDB5->select("SELECT FirstName from Customers where registrationID = 2", ());

    json[] returnArray = [];
    int i = 0;
    foreach var x in resultArray {
        returnArray[i] = getJsonConversionResult(x);
        i+=1;
    }

    // All 5 clients are supposed to use the same pool created with the configurations given by the
    // custom pool options. Since each select operation holds up one connection each, the last select
    // operation should return an error
    return returnArray;
}

sql:PoolOptions poolOptions2 = { maximumPoolSize: 3, connectionTimeout: 1000 };
function testLocalSharedConnectionPoolConfigMultipleDestinations() returns json[] {
    h2:Client testDB1 = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_1",
            username: "SA",
            password: "",
            poolOptions: poolOptions2
        });
    h2:Client testDB2 = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_1",
            username: "SA",
            password: "",
            poolOptions: poolOptions2
        });
    h2:Client testDB3 = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_1",
            username: "SA",
            password: "",
            poolOptions: poolOptions2
        });
    h2:Client testDB4 = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_2",
            username: "SA",
            password: "",
            poolOptions: poolOptions2
        });
    h2:Client testDB5 = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_2",
            username: "SA",
            password: "",
            poolOptions: poolOptions2
        });
    h2:Client testDB6 = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_2",
            username: "SA",
            password: "",
            poolOptions: poolOptions2
        });

    (table<record {}>|error)[] resultArray = [];
    resultArray[0] = testDB1->select("SELECT FirstName from Customers where registrationID = 1", ());
    resultArray[1] = testDB2->select("SELECT FirstName from Customers where registrationID = 1", ());
    resultArray[2] = testDB3->select("SELECT FirstName from Customers where registrationID = 2", ());
    resultArray[3] = testDB3->select("SELECT FirstName from Customers where registrationID = 1", ());

    resultArray[4] = testDB4->select("SELECT FirstName from Customers where registrationID = 1", ());
    resultArray[5] = testDB5->select("SELECT FirstName from Customers where registrationID = 2", ());
    resultArray[6] = testDB6->select("SELECT FirstName from Customers where registrationID = 2", ());
    resultArray[7] = testDB6->select("SELECT FirstName from Customers where registrationID = 1", ());

    json[] returnArray = [];
    int i = 0;
    foreach var x in resultArray {
        returnArray[i] = getJsonConversionResult(x);
        i+=1;
    }

    // All 5 clients are supposed to use the same pool created with the configurations given by the
    // custom pool options. Since each select operation holds up one connection each, the last select
    // operation should return an error
    return returnArray;
}

function testShutDownUnsharedLocalConnectionPool() returns (json, json) {
    h2:Client testDB = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_1",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 2, connectionTimeout: 1000 }
        });

    var result = testDB->select("SELECT FirstName from Customers where registrationID = 1", ());
    json retVal1 = getJsonConversionResult(result);
    _ = h2:releaseConnectionPool(testDB);
    var resultAfterPoolShutDown = testDB->select("SELECT FirstName from Customers where registrationID = 1", ());
    json retVal2 = getJsonConversionResult(resultAfterPoolShutDown);
    return (retVal1, retVal2);
}

sql:PoolOptions poolOptions3 = { maximumPoolSize: 1, connectionTimeout: 1000 };
function testShutDownSharedConnectionPool() returns (json, json, json, json) {
    h2:Client testDB1 = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_1",
            username: "SA",
            password: "",
            poolOptions: poolOptions3
        });

    h2:Client testDB2 = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_1",
            username: "SA",
            password: "",
            poolOptions: poolOptions3
        });

    var result1 = testDB1->select("SELECT FirstName from Customers where registrationID = 1", ());
    json retVal1 = getJsonConversionResult(result1);

    var result2 = testDB2->select("SELECT FirstName from Customers where registrationID = 2", ());
    json retVal2 = getJsonConversionResult(result2);

    _ = h2:releaseConnectionPool(testDB1);

    var result3 = testDB2->select("SELECT FirstName from Customers where registrationID = 2", ());
    json retVal3 = getJsonConversionResult(result3);

    var result4 = testDB1->select("SELECT FirstName from Customers where registrationID = 2", ());
    json retVal4 = getJsonConversionResult(result4);

    return (retVal1, retVal2, retVal3, retVal4);
}

sql:PoolOptions poolOptions4 = { maximumPoolSize: 1, connectionTimeout: 1000 };
function testShutDownPoolCorrespondingToASharedPoolConfig() returns (json, json, json, json) {
    h2:Client testDB1 = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_1",
            username: "SA",
            password: "",
            poolOptions: poolOptions3
        });

    h2:Client testDB2 = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_2",
            username: "SA",
            password: "",
            poolOptions: poolOptions3
        });
    var result1 = testDB1->select("SELECT FirstName from Customers where registrationID = 1", ());
    json retVal1 = getJsonConversionResult(result1);

    var result2 = testDB2->select("SELECT FirstName from Customers where registrationID = 2", ());
    json retVal2 = getJsonConversionResult(result2);

    _ = h2:releaseConnectionPool(testDB1);

    var result3 = testDB2->select("SELECT FirstName from Customers where registrationID = 2", ());
    json retVal3 = getJsonConversionResult(result3);

    var result4 = testDB1->select("SELECT FirstName from Customers where registrationID = 2", ());
    json retVal4 = getJsonConversionResult(result4);

    return (retVal1, retVal2, retVal3, retVal4);
}

function testShutDwonGlobalPool() returns (json, error?, json) {
    h2:Client testDB = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_1",
            username: "SA",
            password: ""
        });

    var result1 = testDB->select("SELECT FirstName from Customers where registrationID = 1", ());
    json retVal1 = getJsonConversionResult(result1);

    var retVal2 = h2:releaseConnectionPool(testDB);

    var result2 = testDB->select("SELECT FirstName from Customers where registrationID = 1", ());
    json retVal3 = getJsonConversionResult(result2);

    return (retVal1, retVal2, retVal3);
}

function getJsonConversionResultOfTuple((table<record{}>|error, table<record{}>|error) t) returns json[] {
    table<record{}>|error x; table<record{}>|error y;
    (x, y) = t;
    json[] returnArray = [];
    returnArray[0] = getJsonConversionResult(x);
    returnArray[1] = getJsonConversionResult(y);
    return returnArray;
}

function getJsonConversionResult(table<record {}>|error tableOrError) returns json {
    json retVal = {};
    if (tableOrError is table<record {}>) {
        var jsonConversionResult = json.convert(tableOrError);
        if (jsonConversionResult is json) {
            retVal = jsonConversionResult;
            _ = io:sprintf("%s", retVal);
        } else {
            retVal = {"Error" : string.convert(jsonConversionResult.detail().message)};
        }
    } else {
        retVal = {"Error" : string.convert(tableOrError.detail().message)};
    }
    return retVal;
}
