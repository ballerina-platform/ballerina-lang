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
string database = "TEST_SQL_CALL_QUERY";
int port = 3305;

type StringData record {
    string varchar_type;
    string charmax_type;
    string char_type;
    string charactermax_type;
    string character_type;
    string nvarcharmax_type;
};

type StringDataSingle record {
    string varchar_type;
};

function testCallWithStringTypes() returns @tainted record {}|error? {
    mysql:Client dbClient = check new (host, user, password, database, port);
    var ret = check dbClient->call("{call InsertStringData(2,'test1', 'test2', 'c', 'test3', 'd', 'test4')};");

    string sqlQuery = "SELECT varchar_type, charmax_type, char_type, charactermax_type, character_type," +
                   "nvarcharmax_type from StringTypes where id = 2";
    return queryMySQLClient(dbClient, sqlQuery);
}

function testCallWithStringTypesInParams() returns @tainted record {}|error? {
    mysql:Client dbClient = check new (host, user, password, database, port);
    string varcharType = "test1";
    string charmaxType = "test2";
    string charType = "c";
    string charactermaxType = "test3";
    string characterType = "d";
    string nvarcharmaxType = "test4";

    var ret = check dbClient->call(`{call InsertStringData(3, ${varcharType}, ${charmaxType}, ${charType},
                            ${charactermaxType}, ${characterType}, ${nvarcharmaxType})}`);

    string sqlQuery = "SELECT varchar_type, charmax_type, char_type, charactermax_type, character_type," +
                   "nvarcharmax_type from StringTypes where id = 2";
    return queryMySQLClient(dbClient, sqlQuery);

}

function testCallWithStringTypesReturnsData() returns @tainted record {}|error? {
    mysql:Client dbClient = check new (host, user, password, database, port);
    sql:ProcedureCallResult ret = check dbClient->call("{call SelectStringData()}", [StringData]);
    stream<record{}, sql:Error>? qResult = ret.queryResult;
    if (qResult is ()) {
        check dbClient.close();
        return error("Result set cannot be nil!");
    } else {
        record {|record {} value;|}? data = check qResult.next();
        check qResult.close();
        record {}? value = data?.value;
        check ret.close();
        check dbClient.close();
        return value;
    }
}

function testCallWithStringTypesReturnsDataMultiple() returns @tainted [record {}|error?, record {}|error?]|error {
    mysql:Client dbClient = check new (host, user, password, database, port);
    sql:ProcedureCallResult ret = check dbClient->call("{call SelectStringDataMultiple()}", [StringData, StringDataSingle]);

    record {}? result1;
    stream<record{}, sql:Error>? qResult = ret.queryResult;
    if (qResult is ()) {
        check dbClient.close();
        return [error("Result set cannot be nil!"), ()];
    } else {
        record {|record {} value;|}? data = check qResult.next();
        check qResult.close();
        result1 = data?.value;
    }

    var nextResult = check ret.getNextQueryResult();
    if (!nextResult) {
        return [result1, error("Only 1 result set returned!")];
    }

    qResult = ret.queryResult;
    if (qResult is ()) {
        check dbClient.close();
        return [result1, error("Only 1 result set returned!")];
    } else {
        record {|record {} value;|}? data = check qResult.next();
        check qResult.close();
        record {}? result2 = data?.value;
        check ret.close();
        check dbClient.close();
        return [result1, result2];
    }
}

function testCallWithStringTypesOutParams() returns [string, string, string, string, string, string]|error {
    mysql:Client dbClient = check new (host, user, password, database, port);

    sql:IntegerValue paraID = new(1);
    sql:OutParameter paraVarchar = new;
    sql:OutParameter paraCharmax = new;
    sql:OutParameter paraChar = new;
    sql:OutParameter paraCharactermax = new;
    sql:OutParameter paraCharacter = new;
    sql:OutParameter paraNvarcharmax = new;

    sql:ProcedureCallResult ret = check dbClient->call(`{call SelectStringDataWithOutParams(${paraID}, ${paraVarchar}, ${paraCharmax}, ${paraChar}, ${paraCharactermax}, ${paraCharacter}, ${paraNvarcharmax})}`);
    check ret.close();
    checkpanic dbClient.close();

    string paraVarcharVal = check paraVarchar.get(string);
    string paraCharmaxVal = check paraCharmax.get(string);
    string paraCharVal = check paraChar.get(string);
    string paraCharactermaxVal = check paraCharactermax.get(string);
    string paraCharacterVal = check paraCharacter.get(string);
    string paraNvarcharmaxVal = check paraNvarcharmax.get(string);

    return [ paraVarcharVal,
             paraCharmaxVal,
             paraCharVal,
             paraCharactermaxVal,
             paraCharacterVal,
             paraNvarcharmaxVal];
}

function testCallWithNumericTypesOutParams() returns [int, int, int, int, boolean,
 decimal, decimal, float, float, float]| error {
    mysql:Client dbClient = check new (host, user, password, database, port);

    sql:IntegerValue paraID = new(1);
    sql:OutParameter paraInt = new;
    sql:OutParameter paraBigInt = new;
    sql:OutParameter paraSmallInt = new;
    sql:OutParameter paraTinyInt = new;
    sql:OutParameter paraBit = new;
    sql:OutParameter paraDecimal = new;
    sql:OutParameter paraNumeric = new;
    sql:OutParameter paraFloat = new;
    sql:OutParameter paraReal = new;
    sql:OutParameter paraDouble = new;

    var ret = dbClient->call(`{call SelectNumericDataWithOutParams(${paraID}, ${paraInt}, ${paraBigInt}, ${paraSmallInt}, ${paraTinyInt}, ${paraBit}, ${paraDecimal}, ${paraNumeric}, ${paraFloat}, ${paraReal}, ${paraDouble})}`);
    checkpanic dbClient.close();
    return [
        check paraInt.get(int), 
        check paraBigInt.get(int), 
        check paraSmallInt.get(int), 
        check paraTinyInt.get(int), 
        check paraBit.get(boolean),
        check paraDecimal.get(decimal), 
        check paraNumeric.get(decimal), 
        check paraFloat.get(float), 
        check paraReal.get(float), 
        check paraDouble.get(float)];
}

function queryMySQLClient(mysql:Client dbClient, @untainted string|sql:ParameterizedQuery sqlQuery)
returns @tainted record {}|error? {
    stream<record{}, error> streamData = dbClient->query(sqlQuery);
    record {|record {} value;|}? data = check streamData.next();
    check streamData.close();
    record {}? value = data?.value;
    check dbClient.close();
    return value;
}
