// Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

string host = "localhost";
string user = "test";
string password = "test123";
string database = "TEST_SQL_COMPLEX_QUERY";
int port = 3305;


function testQuery() returns record{}|error? {
    mysql:Client dbClient = check new (host, user, password, database, port);
    stream<record{}, error> streamData = dbClient->query("SELECT INT_TYPE, LONG_TYPE, FLOAT_TYPE, DOUBLE_TYPE, BOOLEAN_TYPE, STRING_TYPE from DataTable WHERE row_id = 1");
    record{| record{} value; |}? data =  check streamData.next();
    check streamData.close();
    record{}? value = data?.value;
    check dbClient.close();
    return value;
}
