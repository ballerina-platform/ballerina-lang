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
import mockclient;
import ballerina/sql;

type ResultSetTestAlias record {
    int INT_TYPE;
    int LONG_TYPE;
    float FLOAT_TYPE;
    float DOUBLE_TYPE;
    boolean BOOLEAN_TYPE;
    string STRING_TYPE;
    int DT2INT_TYPE;
};

type ResultMap record {
    int[] INT_ARRAY;
    int[] LONG_ARRAY;
    decimal[] FLOAT_ARRAY;
    boolean[] BOOLEAN_ARRAY;
    string[] STRING_ARRAY;
};

type ResultDates record {
    string DATE_TYPE;
    string TIME_TYPE;
    string TIMESTAMP_TYPE;
    string DATETIME_TYPE;
};

type TestTypeData record {
    int int_type;
    int[] int_array;
    int long_type;
    int[] long_array;
    float float_type;
    float[] float_array;
    float double_type;
    boolean boolean_type;
    string string_type;
    float[] double_array;
    string[] string_array;
    boolean[] boolean_array;
};

function testGetPrimitiveTypes(string url, string user, string password) returns @tainted record {}|error? {
    mockclient:Client dbClient = check new (url = url, user = user, password = password);
    stream<record{}, error> streamData = dbClient->query("SELECT int_type, long_type, float_type, double_type,"
        + "boolean_type, string_type from DataTable WHERE row_id = 1");
    record {|record {} value;|}? data = check streamData.next();
    check streamData.close();
    record {}? value = data?.value;
    check dbClient.close();
    return value;
}

function testToJson(string url, string user, string password) returns @tainted json|error {
    mockclient:Client dbClient = check new (url = url, user = user, password = password);
  stream<record{}, error> streamData = dbClient->query("SELECT int_type, long_type, float_type, double_type,"
        + "boolean_type, string_type from DataTable WHERE row_id = 1");
    record {|record {} value;|}? data = check streamData.next();
    check streamData.close();
    record {}? value = data?.value;
    json|error retVal = value.cloneWithType(json);
    check dbClient.close();
    return retVal;
}

function testToJsonComplexTypes(string url, string user, string password) returns @tainted record {}|error? {
    mockclient:Client dbClient = check new (url = url, user = user, password = password);
    stream<record{}, error> streamData = dbClient->query("SELECT blob_type,clob_type,binary_type from" +
        " ComplexTypes where row_id = 1");
    record {|record {} value;|}? data = check streamData.next();
    check streamData.close();
    record {}? value = data?.value;
    check dbClient.close();
    return value;
}

function testComplexTypesNil(string url, string user, string password) returns @tainted record {}|error? {
    mockclient:Client dbClient = check new (url = url, user = user, password = password);
    stream<record{}, error> streamData = dbClient->query("SELECT blob_type,clob_type,binary_type from " +
        " ComplexTypes where row_id = 2");
    record {|record {} value;|}? data = check streamData.next();
    check streamData.close();
    record {}? value = data?.value;
    check dbClient.close();
    return value;
}

function testArrayRetrieval(string url, string user, string password)
returns @tainted record {}|error? {
    mockclient:Client dbClient = check new (url = url, user = user, password = password);
    stream<record{}, error> streamData = dbClient->query("SELECT int_type, int_array, long_type, long_array, " +
        "float_type, float_array, double_type, boolean_type, string_type, decimal_type, double_array, boolean_array," +
        "string_array from MixTypes where row_id =1");
    record {|record {} value;|}? data = check streamData.next();
    check streamData.close();
    record {}? value = data?.value;
    check dbClient.close();
    return value;
}

function testComplexWithStructDef(string url, string user, string password)
returns @tainted record {}|error? {
    mockclient:Client dbClient = check new (url = url, user = user, password = password);
        stream<record{}, error> streamData = dbClient->query("SELECT int_type, int_array, long_type, long_array, "
        + "float_type, float_array, double_type, boolean_type, string_type, double_array, boolean_array, string_array "
        + "from MixTypes where row_id =1", TestTypeData);
    record {|record {} value;|}? data = check streamData.next();
    check streamData.close();
    record {}? value = data?.value;
    check dbClient.close();
    return value;
}

function testMultipleRecoredRetrieval(string url, string user, string password)
returns @tainted ResultMap[]|error? {
    mockclient:Client dbClient = check new (url = url, user = user, password = password);
    stream<record{}, error> streamData = dbClient->query("SELECT int_array, long_array, float_array, boolean_array," +
        "string_array from ArrayTypes", ResultMap);
    ResultMap[] recordMap = [];
    error? e = streamData.forEach(function (record {} value) {
        if (value is ResultMap) {
            recordMap[recordMap.length()] = value;
        }
    });
    if (e is error) {
        return e;
    }
    check dbClient.close();
    return recordMap;
}

function testDateTime(string url, string user, string password) returns @tainted record {}|error? {
    mockclient:Client dbClient = check new (url = url, user = user, password = password);
    string insertQuery = string `Insert into DateTimeTypes (row_id, date_type, time_type, timestamp_type, datetime_type)
     values (1,'2017-05-23','14:15:23','2017-01-25 16:33:55','2017-01-25 16:33:55')`;
    sql:ExecuteResult? result = check dbClient->execute(insertQuery);
    stream<record{}, error> queryResult = dbClient->query("SELECT date_type, time_type, timestamp_type, datetime_type"
        + " from DateTimeTypes where row_id = 1", ResultDates);
    record {|record {} value;|}? data = check queryResult.next();
    record {}? value = data?.value;
    check dbClient.close();
    return value;
}

function testColumnAlias(string url, string user, string password) returns @tainted ResultSetTestAlias[]|error? {
    mockclient:Client dbClient = check new (url = url, user = user, password = password);
    stream<record{}, error> queryResult = dbClient->query("SELECT dt1.int_type, dt1.long_type, dt1.float_type," +
        "dt1.double_type,dt1.boolean_type, dt1.string_type,dt2.int_type as dt2int_type from DataTable dt1 " +
        "left join DataTableRep dt2 on dt1.row_id = dt2.row_id WHERE dt1.row_id = 1;", ResultSetTestAlias);
    ResultSetTestAlias[] recordMap = [];
    error? e = queryResult.forEach(function (record {} value) {
        if (value is ResultSetTestAlias) {
            recordMap[recordMap.length()] = value;
        }
    });
    if (e is error) {
        return e;
    }
    check dbClient.close();
    return recordMap;
}

function testQueryRowId(string url, string user, string password)
returns @tainted record{}[]|error? {
    mockclient:Client dbClient = check new (url = url, user = user, password = password);
    sql:ExecuteResult? result = check dbClient->execute("SET DATABASE SQL SYNTAX ORA TRUE");
    stream<record{}, error> streamData = dbClient->query("SELECT ROWNUM, int_array, long_array, float_array, boolean_array," +
        "string_array from ArrayTypes");
    record{}[] recordMap = [];
    error? e = streamData.forEach(function (record {} value) {
        recordMap[recordMap.length()] = value;
    });
    if (e is error) {
        return e;
    }
    check dbClient.close();
    return recordMap;
}
