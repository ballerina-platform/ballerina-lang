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

import ballerina/mysql;
import ballerina/sql;

public type Result record {
    int val;
};

string host = "localhost";
string user = "test";
string password = "test123";
int port = 3305;
mysql:Options options= {
    connectTimeoutInSeconds: 1
};

function testGlobalConnectionPoolSingleDestination(string database) returns @tainted (int | error)[] | error {
    return drainGlobalPool(database);
}

function drainGlobalPool(string database) returns @tainted (int | error)[]| error {
    mysql:Client dbClient1 = check new (host, user, password, database, port, options);
    mysql:Client dbClient2 = check new (host, user, password, database, port, options);
    mysql:Client dbClient3 = check new (host, user, password, database, port, options);
    mysql:Client dbClient4 = check new (host, user, password, database, port, options);
    mysql:Client dbClient5 = check new (host, user, password, database, port, options);

    stream<record{}, error>[] resultArray = [];

    resultArray[0] = dbClient1->query("select count(*) as val from Customers where registrationID = 1", Result);
    resultArray[1] = dbClient1->query("select count(*) as val from Customers where registrationID = 2", Result);

     resultArray[2] = dbClient2->query("select count(*) as val from Customers where registrationID = 1", Result);
     resultArray[3] = dbClient2->query("select count(*) as val from Customers where registrationID = 1", Result);

     resultArray[4] = dbClient3->query("select count(*) as val from Customers where registrationID = 2", Result);
     resultArray[5] = dbClient3->query("select count(*) as val from Customers where registrationID = 2", Result);

     resultArray[6] = dbClient4->query("select count(*) as val from Customers where registrationID = 1", Result);
     resultArray[7] = dbClient4->query("select count(*) as val from Customers where registrationID = 1", Result);

     resultArray[8] = dbClient5->query("select count(*) as val from Customers where registrationID = 1", Result);
     resultArray[9] = dbClient5->query("select count(*) as val from Customers where registrationID = 1", Result);

     resultArray[10] = dbClient5->query("select count(*) as val from Customers where registrationID = 1", Result);

    (int | error)[] returnArray = [];
    int i = 0;
    // Connections will be released here as we fully consume the data in the following conversion function calls
    foreach var x in resultArray {
        returnArray[i] = getReturnValue(x);
        i += 1;
    }
    // All 5 clients are supposed to use the same pool. Default maximum no of connections is 10.
    // Since each select operation hold up one connection each, the last select operation should
    // return an error
    return returnArray;
}

function testGlobalConnectionPoolsMultipleDestinations(string database1, string database2) returns
    @tainted [(int | error)[], (int | error)[]] | error {
    var errorFromFristDestination = check drainGlobalPool(database1);
    var errorFromSecondDestination = check drainGlobalPool(database2);
    return [errorFromFristDestination, errorFromSecondDestination];
}

function testGlobalConnectionPoolSingleDestinationConcurrent(string database) returns @tainted (int | error)[][] | error {
    worker w1 returns [stream<record{}, error>, stream<record{}, error>] | error {
        return testGlobalConnectionPoolConcurrentHelper1(database);
    }

    worker w2 returns [stream<record{}, error>, stream<record{}, error>] | error {
        return testGlobalConnectionPoolConcurrentHelper1(database);
    }

    worker w3 returns [stream<record{}, error>, stream<record{}, error>] | error {
        return testGlobalConnectionPoolConcurrentHelper1(database);
    }

    worker w4 returns [stream<record{}, error>, stream<record{}, error>] | error {
        return testGlobalConnectionPoolConcurrentHelper1(database);
    }

    record {
        [stream<record{}, error>, stream<record{}, error>] | error w1;
        [stream<record{}, error>, stream<record{}, error>] | error w2;
        [stream<record{}, error>, stream<record{}, error>] | error w3;
        [stream<record{}, error>, stream<record{}, error>] | error w4;
    } results = wait { w1, w2, w3, w4 };

    var result2 = check testGlobalConnectionPoolConcurrentHelper2(database);

    (int | error)[][] returnArray = [];
    // Connections will be released here as we fully consume the data in the following conversion function calls
    returnArray[0] = check getCombinedReturnValue(results.w1);
    returnArray[1] = check getCombinedReturnValue(results.w2);
    returnArray[2] = check getCombinedReturnValue(results.w3);
    returnArray[3] = check getCombinedReturnValue(results.w4);
    returnArray[4] =  result2;

    // All 5 clients are supposed to use the same pool. Default maximum no of connections is 10.
    // Since each select operation hold up one connection each, the last select operation should
    // return an error
    return returnArray;
}

function testLocalSharedConnectionPoolConfigSingleDestination(string database) returns @tainted (int | error)[]| error {
    sql:ConnectionPool pool = {maxOpenConnections: 5};
    mysql:Client dbClient1 = check new (host, user, password, database, port, options, pool);
    mysql:Client dbClient2 = check new (host, user, password, database, port, options, pool);
    mysql:Client dbClient3 = check new (host, user, password, database, port, options, pool);
    mysql:Client dbClient4 = check new (host, user, password, database, port, options, pool);
    mysql:Client dbClient5 = check new (host, user, password, database, port, options, pool);
    
    (stream<record{}, error>)[] resultArray = [];
    resultArray[0] = dbClient1->query("select count(*) as val from Customers where registrationID = 1", Result);
    resultArray[1] = dbClient2->query("select count(*) as val from Customers where registrationID = 1", Result);
    resultArray[2] = dbClient3->query("select count(*) as val from Customers where registrationID = 2", Result);
    resultArray[3] = dbClient4->query("select count(*) as val from Customers where registrationID = 1", Result);
    resultArray[4] = dbClient5->query("select count(*) as val from Customers where registrationID = 1", Result);
    resultArray[5] = dbClient5->query("select count(*) as val from Customers where registrationID = 2", Result);

    (int | error)[] returnArray = [];
    int i = 0;
    // Connections will be released here as we fully consume the data in the following conversion function calls
    foreach var x in resultArray {
        returnArray[i] = getReturnValue(x);
        i += 1;
    }

    check dbClient1.close();
    check dbClient2.close();
    check dbClient3.close();
    check dbClient4.close();
    check dbClient5.close();

    // All 5 clients are supposed to use the same pool created with the configurations given by the
    // custom pool options. Since each select operation holds up one connection each, the last select
    // operation should return an error
    return returnArray;
}

function testLocalSharedConnectionPoolConfigDifferentDbOptions(string database) returns @tainted (int | error)[] | error {
    sql:ConnectionPool pool = {maxOpenConnections: 3};
    mysql:Client dbClient1 = check new (host, user, password, database, port, {connectTimeoutInSeconds: 2, socketTimeoutInSeconds:10}, pool);
    mysql:Client dbClient2 = check new (host, user, password, database, port, {socketTimeoutInSeconds:10, connectTimeoutInSeconds: 2}, pool);
    mysql:Client dbClient3 = check new (host, user, password, database, port, {connectTimeoutInSeconds: 2, socketTimeoutInSeconds:10}, pool);
    mysql:Client dbClient4 = check new (host, user, password, database, port, {connectTimeoutInSeconds: 1}, pool);
    mysql:Client dbClient5 = check new (host, user, password, database, port, {connectTimeoutInSeconds: 1}, pool);
    mysql:Client dbClient6 = check new (host, user, password, database, port, {connectTimeoutInSeconds: 1}, pool);

    stream<record {} , error>[] resultArray = [];
    resultArray[0] = dbClient1->query("select count(*) as val from Customers where registrationID = 1", Result);
    resultArray[1] = dbClient2->query("select count(*) as val from Customers where registrationID = 1", Result);
    resultArray[2] = dbClient3->query("select count(*) as val from Customers where registrationID = 2", Result);
    resultArray[3] = dbClient3->query("select count(*) as val from Customers where registrationID = 1", Result);

    resultArray[4] = dbClient4->query("select count(*) as val from Customers where registrationID = 1", Result);
    resultArray[5] = dbClient5->query("select count(*) as val from Customers where registrationID = 2", Result);
    resultArray[6] = dbClient6->query("select count(*) as val from Customers where registrationID = 2", Result);
    resultArray[7] = dbClient6->query("select count(*) as val from Customers where registrationID = 1", Result);

    (int | error)[] returnArray = [];
    int i = 0;
    // Connections will be released here as we fully consume the data in the following conversion function calls
    foreach var x in resultArray {
        returnArray[i] = getReturnValue(x);
        i += 1;
    }

    check dbClient1.close();
    check dbClient2.close();
    check dbClient3.close();
    check dbClient4.close();
    check dbClient5.close();
    check dbClient6.close();

    // Since max pool size is 3, the last select function call going through each pool should fail.
    return returnArray;
}


function testLocalSharedConnectionPoolConfigMultipleDestinations(string database1, string database2) returns @tainted (int | error)[] | error {
    sql:ConnectionPool pool = {maxOpenConnections: 3};
    mysql:Client dbClient1 = check new (host, user, password, database1, port, options, pool);
    mysql:Client dbClient2 = check new (host, user, password, database1, port, options, pool);
    mysql:Client dbClient3 = check new (host, user, password, database1, port, options, pool);
    mysql:Client dbClient4 = check new (host, user, password, database2, port, options, pool);
    mysql:Client dbClient5 = check new (host, user, password, database2, port, options, pool);
    mysql:Client dbClient6 = check new (host, user, password, database2, port, options, pool);

    stream<record {} , error>[] resultArray = [];
    resultArray[0] = dbClient1->query("select count(*) as val from Customers where registrationID = 1", Result);
    resultArray[1] = dbClient2->query("select count(*) as val from Customers where registrationID = 1", Result);
    resultArray[2] = dbClient3->query("select count(*) as val from Customers where registrationID = 2", Result);
    resultArray[3] = dbClient3->query("select count(*) as val from Customers where registrationID = 1", Result);

    resultArray[4] = dbClient4->query("select count(*) as val from Customers where registrationID = 1", Result);
    resultArray[5] = dbClient5->query("select count(*) as val from Customers where registrationID = 2", Result);
    resultArray[6] = dbClient6->query("select count(*) as val from Customers where registrationID = 2", Result);
    resultArray[7] = dbClient6->query("select count(*) as val from Customers where registrationID = 1", Result);

    (int | error)[] returnArray = [];
    int i = 0;
    // Connections will be released here as we fully consume the data in the following conversion function calls
    foreach var x in resultArray {
        returnArray[i] = getReturnValue(x);
        i += 1;
    }

    check dbClient1.close();
    check dbClient2.close();
    check dbClient3.close();
    check dbClient4.close();
    check dbClient5.close();
    check dbClient6.close();

    // Since max pool size is 3, the last select function call going through each pool should fail.
    return returnArray;
}

function testGlobalConnectionPoolConcurrentHelper1(string database) returns
            @tainted [stream<record{}, error>, stream<record{}, error>] | error {
    mysql:Client dbClient = check new (host, user, password, database, port, options);
    var dt1 = dbClient->query("select count(*) as val from Customers where registrationID = 1", Result);
    var dt2 = dbClient->query("select count(*) as val from Customers where registrationID = 2", Result);
    return [dt1, dt2];
}

function testGlobalConnectionPoolConcurrentHelper2(string database) returns @tainted (int | error)[] | error {
    mysql:Client dbClient = check new (host, user, password, database, port, options);
    (int | error)[] returnArray = [];
    var dt1 = dbClient->query("select count(*) as val from Customers where registrationID = 1", Result);
    var dt2 = dbClient->query("select count(*) as val from Customers where registrationID = 2", Result);
    var dt3 = dbClient->query("select count(*) as val from Customers where registrationID = 1", Result);
    // Connections will be released here as we fully consume the data in the following conversion function calls
    returnArray[0] = getReturnValue(dt1);
    returnArray[1] = getReturnValue(dt2);
    returnArray[2] = getReturnValue(dt3);

    return returnArray;
}

function getCombinedReturnValue([stream<record{}, error>, stream<record{}, error>] | error queryResult) returns
                        (int | error)[] | error {
    if(queryResult is error){
       return queryResult;
    } else {
        stream<record{}, error> x;
        stream<record{}, error> y;
        [x, y] = queryResult;
        (int | error)[] returnArray = [];
        returnArray[0] = getReturnValue(x);
        returnArray[1] = getReturnValue(y);
        return returnArray;
    }
}

function getReturnValue(stream<record{}, error> queryResult) returns int | error {
    int count = -1;
    record{| record{} value; |}? data = check queryResult.next();
    if(data is record{| record{} value; |})  {
       record{} value = data.value;
       if(value is Result) {
          count = value.val;
       }
    }
    check queryResult.close();
    return count;
}
