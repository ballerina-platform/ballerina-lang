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
import ballerina/sql;

string host = "localhost";
string user = "test";
string password = "test123";
string database = "TEST_SQL_COMPLEX_QUERY";
int port = 3305;

type ResultSetTestAlias record {
    int INT_TYPE;
    int LONG_TYPE;
    float FLOAT_TYPE;
    float DOUBLE_TYPE;
    boolean BOOLEAN_TYPE;
    string STRING_TYPE;
    int DT2INT_TYPE;
};

type ResultDates record {
    string DATE_TYPE;
    string TIME_TYPE;
    string TIMESTAMP_TYPE;
    string DATETIME_TYPE;
};

function testGetPrimitiveTypes() returns @tainted record{}|error? {
    mysql:Client dbClient = check new (host, user, password, database, port);
    stream<record{}, error> streamData = dbClient->query("SELECT INT_TYPE, LONG_TYPE, FLOAT_TYPE, DOUBLE_TYPE,"
        + " BOOLEAN_TYPE, STRING_TYPE from DataTable WHERE ROW_ID = 1");
    record {|record {} value;|}? data = check streamData.next();
    check streamData.close();
    record {}? value = data?.value;
    check dbClient.close();
    return value;
}

function testToJson() returns @tainted json|error {
    mysql:Client dbClient = check new (host, user, password, database, port);
    stream<record{}, error> streamData = dbClient->query("SELECT INT_TYPE, LONG_TYPE, FLOAT_TYPE, DOUBLE_TYPE,"
        + "BOOLEAN_TYPE, STRING_TYPE from DataTable WHERE ROW_ID = 1");
    record {|record {} value;|}? data = check streamData.next();
    check streamData.close();
    record {}? value = data?.value;
    json|error retVal = value.cloneWithType(json);
    check dbClient.close();
    return retVal;
}

function testToJsonComplexTypes() returns @tainted record {}|error? {
    mysql:Client dbClient = check new (host, user, password, database, port);
    stream<record{}, error> streamData = dbClient->query("SELECT BLOB_TYPE,CLOB_TYPE,BINARY_TYPE from" +
        " ComplexTypes where ROW_ID = 1");
    record {|record {} value;|}? data = check streamData.next();
    check streamData.close();
    record {}? value = data?.value;
    check dbClient.close();
    return value;
}

function testComplexTypesNil() returns @tainted record {}|error? {
    mysql:Client dbClient = check new (host, user, password, database, port);
    stream<record{}, error> streamData = dbClient->query("SELECT BLOB_TYPE,CLOB_TYPE,BINARY_TYPE from " +
        " ComplexTypes where ROW_ID = 2");
    record {|record {} value;|}? data = check streamData.next();
    check streamData.close();
    record {}? value = data?.value;
    check dbClient.close();
    return value;
}

function testDateTime() returns @tainted record{}|error? {
    mysql:Client dbClient = check new (host, user, password, database, port);
    string insertQuery = string `Insert into DateTimeTypes (ROW_ID, DATE_TYPE, TIME_TYPE, TIMESTAMP_TYPE, DATETIME_TYPE)
     values (1,'2017-05-23','14:15:23','2017-01-25 16:33:55','2017-01-25 16:33:55')`;
    sql:ExecuteResult? result = check dbClient->execute(insertQuery);
    stream<record{}, error> queryResult = dbClient->query("SELECT DATE_TYPE, TIME_TYPE, TIMESTAMP_TYPE, DATETIME_TYPE"
       + " from DateTimeTypes where ROW_ID = 1", ResultDates);
    record{| record{} value; |}? data =  check queryResult.next();
    record{}? value = data?.value;
    check dbClient.close();
    return value;
}

function testColumnAlias() returns @tainted ResultSetTestAlias[]|error? {
    mysql:Client dbClient = check new (host, user, password, database, port);
    stream<record{}, error> queryResult = dbClient->query("SELECT dt1.INT_TYPE, dt1.LONG_TYPE, dt1.FLOAT_TYPE," +
           "dt1.DOUBLE_TYPE,dt1.BOOLEAN_TYPE, dt1.STRING_TYPE,dt2.INT_TYPE as dt2INT_TYPE from DataTable dt1 " +
           "left join DataTableRep dt2 on dt1.ROW_ID = dt2.ROW_ID WHERE dt1.ROW_ID = 1;", ResultSetTestAlias);
    ResultSetTestAlias[] recordMap = [];
    error? e = queryResult.forEach(function (record{} value) {
        if (value is ResultSetTestAlias) {
            recordMap[recordMap.length()] = value;
            }
        });
    if(e is error) {
        return e;
        }
    check dbClient.close();
    return recordMap;
}
