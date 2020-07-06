// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/java.jdbc;

string xaDatasourceName = "org.h2.jdbcx.JdbcDataSource";

type XAResultCount record {
    int COUNTVAL;
};

function testXATransactionSuccess(string jdbcURL1, string jdbcURL2, string user, string password)
returns @tainted [int, int]|error? {
    jdbc:Client dbClient1 = check new (url = jdbcURL1, user = user, password = password,
    connectionPool = {maxOpenConnections: 1});
    jdbc:Client dbClient2 = check new (url = jdbcURL2, user = user, password = password,
    connectionPool = {maxOpenConnections: 1});

    transaction {
        var e1 = check dbClient1->execute("insert into Customers (customerId, name, creditLimit, country) " +
                                "values (1, 'Anne', 1000, 'UK')");
        var e2 = check dbClient2->execute("insert into Salary (id, value ) values (1, 1000)");
        check commit;
    }

    int count1 = check getCustomerCount(dbClient1, "1");
    int count2 = check getSalaryCount(dbClient2, "1");

    check dbClient1.close();
    check dbClient2.close();
    return [count1, count2];
}

function testXATransactionSuccessWithDataSource(string jdbcURL1, string jdbcURL2, string user, string password)
returns @tainted [int, int]|error? {
    jdbc:Client dbClient1 = check new (url = jdbcURL1, user = user, password = password,
    options = {datasourceName: xaDatasourceName});
    jdbc:Client dbClient2 = check new (url = jdbcURL2, user = user, password = password,
    options = {datasourceName: xaDatasourceName});
    
    transaction {
        var e1 = check dbClient1->execute("insert into Customers (customerId, name, creditLimit, country) " +
                                "values (10, 'Anne', 1000, 'UK')");
        var e2 = check dbClient2->execute("insert into Salary (id, value ) values (10, 1000)");
        check commit;
    }

    int count1 = check getCustomerCount(dbClient1, "10");
    int count2 = check getSalaryCount(dbClient2, "10");

    check dbClient1.close();
    check dbClient2.close();
    return [count1, count2];
}

function getCustomerCount(jdbc:Client dbClient, string id) returns @tainted int|error{
    stream<XAResultCount, error> streamData = <stream<XAResultCount, error>> dbClient->query("Select COUNT(*) as " +
        "countval from Customers where customerId = "+ id, XAResultCount);
    return getResult(streamData);
}

function getSalaryCount(jdbc:Client dbClient, string id) returns @tainted int|error{
    stream<XAResultCount, error> streamData =
    <stream<XAResultCount, error>> dbClient->query("Select COUNT(*) as countval " +
    "from Salary where id = "+ id, XAResultCount);
    return getResult(streamData);
}

function getResult(stream<XAResultCount, error> streamData) returns int|error{
    record {|XAResultCount value;|}? data = check streamData.next();
    check streamData.close();
    XAResultCount? value = data?.value;
    if(value is XAResultCount){
       return value.COUNTVAL;
    }
    return 0;
}
