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

public type Result record {
    int val;
};

string host = "localhost";
string user = "test";
string password = "test123";
string database = "TEST_SQL_CONN_POOL";
int port = 3305;
mysql:Options options= {
    connectTimeoutInSeconds: 3
};

function testGlobalConnectionPoolSingleDestination() returns @tainted (int | error)[] | error? {
    return drainGlobalPool(database);
}

function drainGlobalPool(string database) returns @tainted (int | error)[]| error? {
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
