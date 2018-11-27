// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/mysql;
import ballerina/io;

mysql:Client mysqlDB = new({
    host: "localhost",
    port: 3306,
    name: "ballerinademo",
    username: "demouser",
    password: "password@123",
    dbOptions: { useSSL: false}
});

public function main(string... args) returns error? {
    string s1 = args[0];
    io:println(s1);
    table<any> t1 = check mysqlDB->select("SELECT id, age, name from employee where name = " + s1, ());
    return;
}
