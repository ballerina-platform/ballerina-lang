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
import ballerina/runtime;

type Info record {
   string firstName;
};

public type Result record {
    int val;
};

function testGlobalConnectionPoolSingleDestination() returns (int|string?)[] {
    return drainGlobalPool("TEST_SQL_CONNECTION_POOL_GLOBAL_1");
}

function drainGlobalPool(string dbName) returns (int|string?)[] {
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

    (table<record {}>|error?)[] resultArray = [];
    resultArray[0] = testDB1->select("select count(*) from Customers where registrationID = 1", Result);
    resultArray[1] = testDB1->select("select count(*) from Customers where registrationID = 2", Result);

    resultArray[2] = testDB2->select("select count(*) from Customers where registrationID = 1", Result);
    resultArray[3] = testDB2->select("select count(*) from Customers where registrationID = 1", Result);

    resultArray[4] = testDB3->select("select count(*) from Customers where registrationID = 2", Result);
    resultArray[5] = testDB3->select("select count(*) from Customers where registrationID = 2", Result);

    resultArray[6] = testDB4->select("select count(*) from Customers where registrationID = 1", Result);
    resultArray[7] = testDB4->select("select count(*) from Customers where registrationID = 1", Result);

    resultArray[8] = testDB5->select("select count(*) from Customers where registrationID = 1", Result);
    resultArray[9] = testDB5->select("select count(*) from Customers where registrationID = 1", Result);

    resultArray[10] = testDB5->select("select count(*) from Customers where registrationID = 1", Result);

    (int|string?)[] returnArray = [];
    int i = 0;
    // Connections will be released here as we fully consume the data in the following conversion function calls
    foreach var x in resultArray {
        returnArray[i] = getTableCountValColumn(x);
        i+=1;
    }
    // All 5 clients are supposed to use the same pool. Default maximum no of connections is 10.
    // Since each select operation hold up one connection each, the last select operation should
    // return an error
    return returnArray;
}

function testGlobalConnectionPoolsMultipleDestinations() returns [(int|string?)[], (int|string?)[]] {
    var errorFromFristDestination = drainGlobalPool("TEST_SQL_CONNECTION_POOL_GLOBAL_1");
    var errorFromSecondDestination = drainGlobalPool("TEST_SQL_CONNECTION_POOL_GLOBAL_2");
    return [errorFromFristDestination, errorFromSecondDestination];
}

function closeTable(table<record{}>|error? t) {
    if (t is table<record{}>) {
        _ = t.close();
    }
}

function testGlobalConnectionPoolSingleDestinationConcurrent() returns (int|string?)[][] {
    worker w1 returns [table<record{}>|error,table<record{}>|error] {
        return testGlobalConnectionPoolConcurrentHelper1();
    }

    worker w2 returns [table<record{}>|error,table<record{}>|error] {
        return testGlobalConnectionPoolConcurrentHelper1();
    }

    worker w3 returns [table<record{}>|error,table<record{}>|error] {
        return testGlobalConnectionPoolConcurrentHelper1();
    }

    worker w4 returns [table<record{}>|error,table<record{}>|error] {
        return testGlobalConnectionPoolConcurrentHelper1();
    }

    record {
        [table<record{}>|error, table<record{}>|error] w1;
        [table<record{}>|error, table<record{}>|error] w2;
        [table<record{}>|error, table<record{}>|error] w3;
        [table<record{}>|error, table<record{}>|error] w4;
    } results = wait {w1, w2, w3, w4};

    var t = testGlobalConnectionPoolConcurrentHelper2();

    (int|string?)[][] returnArray = [];
    // Connections will be released here as we fully consume the data in the following conversion function calls
    returnArray[0] = getTableCountValColumnOfTuple(results.w1);
    returnArray[1] = getTableCountValColumnOfTuple(results.w2);
    returnArray[2] = getTableCountValColumnOfTuple(results.w3);
    returnArray[3] = getTableCountValColumnOfTuple(results.w4);
    returnArray[4] = t;

    // All 5 clients are supposed to use the same pool. Default maximum no of connections is 10.
    // Since each select operation hold up one connection each, the last select operation should
    // return an error
    return returnArray;
}

function closeTables([table<record{}>|error?, table<record{}>|error?] t) {
    table<record{}>|error? x; table<record{}>|error? y;
    [x, y] = t;
    closeTable(x);
    closeTable(y);
}

function testGlobalConnectionPoolConcurrentHelper1() returns [table<record{}>|error,table<record{}>|error] {
    h2:Client testDB1 = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_GLOBAL_1",
            username: "SA",
            password: ""
        });
    var dt1 = testDB1->select("select count(*) from Customers where registrationID = 1", Result);
    var dt2 = testDB1->select("select count(*) from Customers where registrationID = 2", Result);
    return [dt1, dt2];
}

function testGlobalConnectionPoolConcurrentHelper2() returns (int|string?)[] {
    h2:Client testDB1 = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_GLOBAL_1",
            username: "SA",
            password: ""
        });
    (int|string?)[] returnArray = [];
    var dt1 = testDB1->select("select count(*) from Customers where registrationID = 1", Result);
    var dt2 = testDB1->select("select count(*) from Customers where registrationID = 2", Result);
    var dt3 = testDB1->select("select count(*) from Customers where registrationID = 1", Result);
    // Connections will be released here as we fully consume the data in the following conversion function calls
    returnArray[0] = getTableCountValColumn(dt1);
    returnArray[1] = getTableCountValColumn(dt2);
    returnArray[2] = getTableCountValColumn(dt3);

    return returnArray;
}

function testLocalSharedConnectionPoolConfigSingleDestination() returns (int|string?)[] {
    h2:Client testDB1;
    h2:Client testDB2;
    h2:Client testDB3;
    h2:Client testDB4;
    h2:Client testDB5;
    sql:PoolOptions poolOptions1 = { maximumPoolSize: 5, connectionTimeout: 1000, validationTimeout: 1000 };

    testDB1 = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_LOCAL_SHARED_1",
            username: "SA",
            password: "",
            poolOptions: poolOptions1
        });
    testDB2 = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_LOCAL_SHARED_1",
            username: "SA",
            password: "",
            poolOptions: poolOptions1
        });
    testDB3 = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_LOCAL_SHARED_1",
            username: "SA",
            password: "",
            poolOptions: poolOptions1
        });
    testDB4 = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_LOCAL_SHARED_1",
            username: "SA",
            password: "",
            poolOptions: poolOptions1
        });
    testDB5 = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_LOCAL_SHARED_1",
            username: "SA",
            password: "",
            poolOptions: poolOptions1
        });
    (table<record {}>|error?)[] resultArray = [];
    resultArray[0] = testDB1->select("select count(*) from Customers where registrationID = 1", Result);
    resultArray[1] = testDB2->select("select count(*) from Customers where registrationID = 1", Result);
    resultArray[2] = testDB3->select("select count(*) from Customers where registrationID = 2", Result);
    resultArray[3] = testDB4->select("select count(*) from Customers where registrationID = 1", Result);
    resultArray[4] = testDB5->select("select count(*) from Customers where registrationID = 1", Result);
    resultArray[5] = testDB5->select("select count(*) from Customers where registrationID = 2", Result);

    (int|string)?[] returnArray = [];
    int i = 0;
    // Connections will be released here as we fully consume the data in the following conversion function calls
    foreach var x in resultArray {
        returnArray[i] = getTableCountValColumn(x);
        i+=1;
    }

    checkpanic testDB1.stop();
    checkpanic testDB2.stop();
    checkpanic testDB3.stop();
    checkpanic testDB4.stop();
    checkpanic testDB5.stop();

    // All 5 clients are supposed to use the same pool created with the configurations given by the
    // custom pool options. Since each select operation holds up one connection each, the last select
    // operation should return an error
    return returnArray;
}

function testLocalSharedConnectionPoolConfigMultipleDestinations() returns (int|string?)[] {
    h2:Client testDB1;
    h2:Client testDB2;
    h2:Client testDB3;
    h2:Client testDB4;
    h2:Client testDB5;
    h2:Client testDB6;
    sql:PoolOptions poolOptions2 = { maximumPoolSize: 3, connectionTimeout: 1000, validationTimeout: 1000 };

    // One pool will be created to these clients.
    testDB1 = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_LOCAL_SHARED_2",
            username: "SA",
            password: "",
            poolOptions: poolOptions2
        });
    testDB2 = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_LOCAL_SHARED_2",
            username: "SA",
            password: "",
            poolOptions: poolOptions2
        });
    testDB3 = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_LOCAL_SHARED_2",
            username: "SA",
            password: "",
            poolOptions: poolOptions2
        });
    // Another pool will be created to these clients.
    testDB4 = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_LOCAL_SHARED_3",
            username: "SA",
            password: "",
            poolOptions: poolOptions2
        });
    testDB5 = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_LOCAL_SHARED_3",
            username: "SA",
            password: "",
            poolOptions: poolOptions2
        });
    testDB6 = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_LOCAL_SHARED_3",
            username: "SA",
            password: "",
            poolOptions: poolOptions2
        });

    (table<record {}>|error?)[] resultArray = [];
    resultArray[0] = testDB1->select("select count(*) from Customers where registrationID = 1", Result);
    resultArray[1] = testDB2->select("select count(*) from Customers where registrationID = 1", Result);
    resultArray[2] = testDB3->select("select count(*) from Customers where registrationID = 2", Result);
    resultArray[3] = testDB3->select("select count(*) from Customers where registrationID = 1", Result);

    resultArray[4] = testDB4->select("select count(*) from Customers where registrationID = 1", Result);
    resultArray[5] = testDB5->select("select count(*) from Customers where registrationID = 2", Result);
    resultArray[6] = testDB6->select("select count(*) from Customers where registrationID = 2", Result);
    resultArray[7] = testDB6->select("select count(*) from Customers where registrationID = 1", Result);

    (int|string)?[] returnArray = [];
    int i = 0;
    // Connections will be released here as we fully consume the data in the following conversion function calls
    foreach var x in resultArray {
        returnArray[i] = getTableCountValColumn(x);
        i+=1;
    }

    checkpanic testDB1.stop();
    checkpanic testDB2.stop();
    checkpanic testDB3.stop();
    checkpanic testDB4.stop();
    checkpanic testDB5.stop();
    checkpanic testDB6.stop();

    // Since max pool size is 3, the last select function call going through each pool should fail.
    return returnArray;
}

function testLocalSharedConnectionPoolCreateClientAfterShutdown() returns [int|string, int|string, int|string, int|string] {
    h2:Client testDB1;
    h2:Client testDB2;
    h2:Client testDB3;
    h2:Client testDB4;
    sql:PoolOptions poolOptions5 = { maximumPoolSize: 2, connectionTimeout: 1000, validationTimeout: 1000 };

    testDB1 = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_LOCAL_SHARED_4",
            username: "SA",
            password: "",
            poolOptions: poolOptions5
        });
    testDB2 = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_LOCAL_SHARED_4",
            username: "SA",
            password: "",
            poolOptions: poolOptions5
        });

    var dt1 = testDB1->select("SELECT count(*) from Customers where registrationID = 1", Result);
    var dt2 = testDB2->select("SELECT count(*) from Customers where registrationID = 1", Result);
    int|string result1 = getTableCountValColumn(dt1);
    int|string result2 = getTableCountValColumn(dt2);

    // Since both clients are stopped the pool is supposed to shutdown.
    checkpanic testDB1.stop();
    checkpanic testDB2.stop();

    // This call should return an error as pool is shutdown
    var dt3 = testDB1->select("SELECT count(*) from Customers where registrationID = 1", Result);
    int|string result3 = getTableCountValColumn(dt3);

    // Now a new pool should be created
    testDB3 = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_LOCAL_SHARED_4",
            username: "SA",
            password: "",
            poolOptions: poolOptions5
        });

    // This call should be successful
    var dt4 = testDB3->select("SELECT count(*) from Customers where registrationID = 1", Result);
    int|string result4 = getTableCountValColumn(dt4);

    checkpanic testDB3.stop();

    return [result1, result2, result3, result4];
}

function testLocalSharedConnectionPoolStopInitInterleave() returns int|string {
    sql:PoolOptions poolOptions = { maximumPoolSize: 2, connectionTimeout: 1000, validationTimeout: 1000 };

    worker w1 {
        testLocalSharedConnectionPoolStopInitInterleaveHelper1(poolOptions);
    }
    worker w2 returns int|string {
        return testLocalSharedConnectionPoolStopInitInterleaveHelper2(poolOptions);
    }

    wait w1;
    int|string result = wait w2;
    return result;
}

function testLocalSharedConnectionPoolStopInitInterleaveHelper1(sql:PoolOptions poolOptions) {
    h2:Client testDB1 = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_LOCAL_SHARED_5",
            username: "SA",
            password: "",
            poolOptions: poolOptions
        });
    runtime:sleep(10);

    checkpanic testDB1.stop();
}

function testLocalSharedConnectionPoolStopInitInterleaveHelper2(sql:PoolOptions poolOptions) returns int|string {
    h2:Client testDB2;
    runtime:sleep(10);
    testDB2 = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_LOCAL_SHARED_5",
            username: "SA",
            password: "",
            poolOptions: poolOptions
        });

    var dt = testDB2->select("SELECT COUNT(*) from Customers where registrationID = 1", Result);
    int|string count = getTableCountValColumn(dt);
    checkpanic testDB2.stop();

    return count;
}

function testShutDownUnsharedLocalConnectionPool() returns [int|string, int|string] {
    h2:Client testDB = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_LOCAL_SHARED_6",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 2, connectionTimeout: 1000, validationTimeout: 1000 }
        });

    var result = testDB->select("select count(*) from Customers where registrationID = 1", Result);
    int|string retVal1 = getTableCountValColumn(result);
    // Pool should be shutdown as the only client using it is stopped.
    checkpanic testDB.stop();
    // This should result in an error return.
    var resultAfterPoolShutDown = testDB->select("select count(*) from Customers where registrationID = 1", Result);
    int|string retVal2 = getTableCountValColumn(resultAfterPoolShutDown);
    return [retVal1, retVal2];
}

function testShutDownSharedConnectionPool() returns [int|string, int|string, int|string, int|string, int|string] {
    h2:Client testDB1;
    h2:Client testDB2;
    h2:Client testDB3;
    sql:PoolOptions poolOptions3 = { maximumPoolSize: 1, connectionTimeout: 1000, validationTimeout: 1000 };

    testDB1 = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_LOCAL_SHARED_7",
            username: "SA",
            password: "",
            poolOptions: poolOptions3
        });

    testDB2 = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_LOCAL_SHARED_7",
            username: "SA",
            password: "",
            poolOptions: poolOptions3
        });

    var result1 = testDB1->select("select count(*) from Customers where registrationID = 1", Result);
    int|string retVal1 = getTableCountValColumn(result1);

    var result2 = testDB2->select("select count(*) from Customers where registrationID = 2", Result);
    int|string retVal2 = getTableCountValColumn(result2);

    // Only one client is closed so pool should not shutdown.
    checkpanic testDB1.stop();

    // This should be successful as pool is still up.
    var result3 = testDB2->select("select count(*) from Customers where registrationID = 2", Result);
    int|string retVal3 = getTableCountValColumn(result3);

    // This should fail because, even though the pool is up, this client was stopped
    var result4 = testDB1->select("select count(*) from Customers where registrationID = 2", Result);
    int|string retVal4 = getTableCountValColumn(result4);

    // Now pool should be shutdown as the only remaining client is stopped.
    checkpanic testDB2.stop();

    // This should fail because this client is stopped.
    var result5 = testDB2->select("select count(*) from Customers where registrationID = 2", Result);
    int|string retVal5 = getTableCountValColumn(result4);

    return [retVal1, retVal2, retVal3, retVal4, retVal5];
}

function testShutDownPoolCorrespondingToASharedPoolConfig() returns [int|string, int|string, int|string, int|string] {
    h2:Client testDB1;
    h2:Client testDB2;
    sql:PoolOptions poolOptions4 = { maximumPoolSize: 1, connectionTimeout: 1000, validationTimeout: 1000 };

    // One pool is created for this client.
    testDB1 = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_LOCAL_SHARED_8",
            username: "SA",
            password: "",
            poolOptions: poolOptions4
        });

    // Another pool is created for this client, because, even though both are using the same pool config,
    // they point to different databases.
    testDB2 = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_LOCAL_SHARED_9",
            username: "SA",
            password: "",
            poolOptions: poolOptions4
        });
    var result1 = testDB1->select("select count(*) from Customers where registrationID = 1", Result);
    int|string retVal1 = getTableCountValColumn(result1);

    var result2 = testDB2->select("select count(*) from Customers where registrationID = 2", Result);
    int|string retVal2 = getTableCountValColumn(result2);

    // This should result in stopping the pool used by this client as it was the only client using that pool.
    checkpanic testDB1.stop();

    // This should be successful as the pool belonging to this client is up.
    var result3 = testDB2->select("select count(*) from Customers where registrationID = 2", Result);
    int|string retVal3 = getTableCountValColumn(result3);

    // This should fail because this client was stopped.
    var result4 = testDB1->select("select count(*) from Customers where registrationID = 2", Result);
    int|string retVal4 = getTableCountValColumn(result4);

    checkpanic testDB2.stop();

    return [retVal1, retVal2, retVal3, retVal4];
}

function testStopClientUsingGlobalPool() returns [int|string, int|string] {
    // This client doesn't have pool config specified therefore, global pool will be used.
    h2:Client testDB = new({
            path: "./target/tempdb/",
            name: "TEST_SQL_CONNECTION_POOL_GLOBAL_1",
            username: "SA",
            password: ""
        });

    var result1 = testDB->select("select count(*) from Customers where registrationID = 1", Result);
    int|string retVal1 = getTableCountValColumn(result1);

    // This will merely stop this client and will not have any effect on the pool because it is the global pool.
    checkpanic testDB.stop();

    // This should fail because this client was stopped, even though the pool is up.
    var result2 = testDB->select("select count(*) from Customers where registrationID = 1", Result);
    int|string retVal2 = getTableCountValColumn(result2);

    return [retVal1, retVal2];
}

function testLocalConnectionPoolShutDown() returns [int|string, int|string] {
    int|string count1 = getOpenConnectionCount("TEST_SQL_CONNECTION_POOL_LOCAL_SHARED_1");
    int|string count2 = getOpenConnectionCount("TEST_SQL_CONNECTION_POOL_LOCAL_SHARED_2");
    return [count1, count2];
}

function getOpenConnectionCount(string dbName) returns int|string {
    h2:Client testDB = new({
            path: "./target/tempdb/",
            name: dbName,
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1, connectionTimeout: 1000, validationTimeout: 1000 }
        });
    var dt = testDB->select("SELECT COUNT(*) FROM INFORMATION_SCHEMA.SESSIONS", Result);
    int|string count = getTableCountValColumn(dt);
    io:println(count);
    checkpanic testDB.stop();
    return count;
}

function getTableCountValColumnOfTuple([table<record{}>|error, table<record{}>|error] t) returns (int|string?)[] {
    table<record{}>|error x; table<record{}>|error y;
    [x, y] = t;
    (int|string?)[] returnArray = [];
    returnArray[0] = getTableCountValColumn(x);
    returnArray[1] = getTableCountValColumn(y);
    return returnArray;
}

function getTableCountValColumn(table<record {}>|error? result) returns int|string {
    int count = -1;
    if (result is table<record {}>) {
        while (result.hasNext()) {
            var rs = result.getNext();
            if (rs is Result) {
                count = rs.val;
            }
        }
        return count;
    } else if (result is error) {
        return <string> result.detail().message;
    } else {
        return -1;
    }
}
