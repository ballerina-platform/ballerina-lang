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
string database = "TEST_SQL_EXCUTE_QUERY";
int port = 3305;

function testCreateTable() returns sql:ExecuteResult|sql:Error? {
    mysql:Client dbClient = check new (host, user, password, database, port);
    sql:ExecuteResult? result = check dbClient->execute("CREATE TABLE TestCreateTable(studentID int, LastName varchar(255))");
    check dbClient.close();
    return result;
}

function testInsertTable() returns sql:ExecuteResult|sql:Error? {
    mysql:Client dbClient = check new (host, user, password, database, port);
    sql:ExecuteResult? result = check dbClient->execute("Insert into NumericTypes (int_type) values (20)");
    check dbClient.close();
    return result;
}

function testInsertTableWithoutGeneratedKeys() returns sql:ExecuteResult|sql:Error? {
    mysql:Client dbClient = check new (host, user, password, database, port);
    sql:ExecuteResult? result = check dbClient->execute("Insert into StringTypes (id, varchar_type) values (20, 'test')");
    check dbClient.close();
    return result;
}

function testInsertTableWithGeneratedKeys() returns sql:ExecuteResult|sql:Error? {
    mysql:Client dbClient = check new (host, user, password, database, port);
    sql:ExecuteResult? result = check dbClient->execute("insert into NumericTypes (int_type) values (21)");
    check dbClient.close();
    return result;
}

type NumericType record {
    int id;
    int? int_type;
    int? bigint_type;
    int? smallint_type;
    int? tinyint_type;
    boolean? bit_type;
    decimal? decimal_type;
    decimal? numeric_type;
    float? float_type;
    float? real_type;
};

function testInsertAndSelectTableWithGeneratedKeys() returns [sql:ExecuteResult?, NumericType?]|error? {
    mysql:Client dbClient = check new (host, user, password, database, port);
    sql:ExecuteResult? result = check dbClient->execute("insert into NumericTypes (int_type) values (31)");

    NumericType? insertedData = ();
    if (result is sql:ExecuteResult) {
        string|int? insertedId = result.lastInsertId;
        if (insertedId is int) {
            string query = string `SELECT * from NumericTypes where id = ${insertedId}`;
            stream<record{}, error> queryResult = dbClient->query(query, NumericType);

            stream<NumericType, sql:Error> streamData = <stream<NumericType, sql:Error>>queryResult;
            record {|NumericType value;|}? data = check streamData.next();
            check streamData.close();
            insertedData = data?.value;
        }
    }

    check dbClient.close();

    return [result, insertedData];
}

function testInsertWithAllNilAndSelectTableWithGeneratedKeys() returns [sql:ExecuteResult?, NumericType?]|error? {
    mysql:Client dbClient = check new (host, user, password, database, port);
    sql:ExecuteResult? result = check dbClient->execute("Insert into NumericTypes (int_type, bigint_type, "
        + "smallint_type, tinyint_type, bit_type, decimal_type, numeric_type, float_type, real_type) "
        + "values (null,null,null,null,null,null,null,null,null)");

    NumericType? insertedData = ();
    if (result is sql:ExecuteResult) {
        string|int? insertedId = result.lastInsertId;
        if (insertedId is int) {
            string query = string `SELECT * from NumericTypes where id = ${insertedId}`;
            stream<record{}, error> queryResult = dbClient->query(query, NumericType);

            stream<NumericType, sql:Error> streamData = <stream<NumericType, sql:Error>>queryResult;
            record {|NumericType value;|}? data = check streamData.next();
            check streamData.close();
            insertedData = data?.value;
        }
    }

    check dbClient.close();

    return [result, insertedData];
}

type StringData record {
    int id;
    string varchar_type;
    string charmax_type;
    string char_type;
    string charactermax_type;
    string character_type;
    string nvarcharmax_type;
    string longvarchar_type;
    string clob_type;
};

function testInsertWithStringAndSelectTable() returns [sql:ExecuteResult?, StringData?]|error? {
    mysql:Client dbClient = check new (host, user, password, database, port);
    int intIDVal = 25;
    string insertQuery = string `Insert into StringTypes (id, varchar_type, charmax_type, char_type, charactermax_type, character_type, nvarcharmax_type, longvarchar_type, clob_type) values (${intIDVal},'str1','str2','s','str4','s','str6','str7','str8')`;
    sql:ExecuteResult? result = check dbClient->execute(insertQuery);

    StringData? insertedData = ();
    string query = string `SELECT * from StringTypes where id = ${intIDVal}`;
    stream<record{}, error> queryResult = dbClient->query(query, StringData);
    stream<StringData, sql:Error> streamData = <stream<StringData, sql:Error>>queryResult;
    record {|StringData value;|}? data = check streamData.next();
    check streamData.close();
    insertedData = data?.value;

    check dbClient.close();

    return [result, insertedData];
}

function testInsertWithEmptyStringAndSelectTable() returns [sql:ExecuteResult?, StringData?]|error? {
    mysql:Client dbClient = check new (host, user, password, database, port);
    int intIDVal = 35;
    string insertQuery = string `Insert into StringTypes (id, varchar_type, charmax_type, char_type, charactermax_type, character_type, nvarcharmax_type, longvarchar_type, clob_type) values (${intIDVal},'','','','','','','','')`;
    sql:ExecuteResult? result = check dbClient->execute(insertQuery);

    StringData? insertedData = ();
    string query = string `SELECT * from StringTypes where id = ${intIDVal}`;
    stream<record{}, error> queryResult = dbClient->query(query, StringData);
    stream<StringData, sql:Error> streamData = <stream<StringData, sql:Error>>queryResult;
    record {|StringData value;|}? data = check streamData.next();
    check streamData.close();
    insertedData = data?.value;

    check dbClient.close();

    return [result, insertedData];
}

type StringNilData record {
    int id;
    string? varchar_type;
    string? charmax_type;
    string? char_type;
    string? charactermax_type;
    string? character_type;
    string? nvarcharmax_type;
    string? longvarchar_type;
    string? clob_type;
};

function testInsertWithNilStringAndSelectTable() returns [sql:ExecuteResult?, StringNilData?]|error? {
    mysql:Client dbClient = check new (host, user, password, database, port);
    int intIDVal = 45;
    string insertQuery = string `Insert into StringTypes (id, varchar_type, charmax_type, char_type, charactermax_type, character_type, nvarcharmax_type, longvarchar_type, clob_type) values (${intIDVal},null,null,null,null,null,null,null,null)`;
    sql:ExecuteResult? result = check dbClient->execute(insertQuery);

    StringNilData? insertedData = ();
    string query = string `SELECT * from StringTypes where id = ${intIDVal}`;
    stream<record{}, error> queryResult = dbClient->query(query, StringNilData);
    stream<StringNilData, sql:Error> streamData = <stream<StringNilData, sql:Error>>queryResult;
    record {|StringNilData value;|}? data = check streamData.next();
    check streamData.close();
    insertedData = data?.value;

    check dbClient.close();
    return [result, insertedData];
}

function testInsertTableWithDatabaseError() returns sql:ExecuteResult|sql:Error? {
    mysql:Client dbClient = check new (host, user, password, database, port);
    sql:ExecuteResult? result = check dbClient->execute("Insert into NumericTypesNonExistTable (int_type) values (20)");
    check dbClient.close();
    return result;
}

function testInsertTableWithDataTypeError() returns sql:ExecuteResult|sql:Error? {
    mysql:Client dbClient = check new (host, user, password, database, port);
    sql:ExecuteResult? result = check dbClient->execute("Insert into NumericTypes (int_type) values ('This is wrong type')");
    check dbClient.close();
    return result;
}

type ResultCount record {
    int countVal;
};

function testUdateData() returns [sql:ExecuteResult?, ResultCount?]|error? {
    mysql:Client dbClient = check new (host, user, password, database, port);
    sql:ExecuteResult? result = check dbClient->execute("Update NumericTypes set int_type = 11 where int_type = 10");
    ResultCount? resultCount = ();

    stream<record{}, error> queryResult = dbClient->query("SELECT count(*) as countval from NumericTypes where int_type = 11", ResultCount);
    stream<ResultCount, sql:Error> streamData = <stream<ResultCount, sql:Error>>queryResult;
    record {|ResultCount value;|}? data = check streamData.next();
    check streamData.close();
    resultCount = data?.value;

    check dbClient.close();
    return [result, resultCount];
}


