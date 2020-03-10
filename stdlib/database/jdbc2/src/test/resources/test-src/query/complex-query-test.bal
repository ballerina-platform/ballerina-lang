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
import ballerina/java.jdbc;
import ballerina/time;

type ResultPrimitive record {
    int INT_TYPE;
    int LONG_TYPE;
    float FLOAT_TYPE;
    float DOUBLE_TYPE;
    boolean BOOLEAN_TYPE;
    string STRING_TYPE;
    decimal DECIMAL_TYPE;
};

type ResultJson record {
    json STRING_TYPE;
};

type ResultSetTestAlias record {
    int INT_TYPE;
    int LONG_TYPE;
    float FLOAT_TYPE;
    float DOUBLE_TYPE;
    boolean BOOLEAN_TYPE;
    string STRING_TYPE;
    int DT2INT_TYPE;
};

type ResultClosed record {|
    int INT_TYPE;
    int LONG_TYPE;
    float FLOAT_TYPE;
    float DOUBLE_TYPE;
    boolean BOOLEAN_TYPE;
    string STRING_TYPE;
|};

type ResultObject record {
    byte[] BLOB_TYPE;
    string CLOB_TYPE;
    byte[] BINARY_TYPE;
};

type ResultMap record {
    int[] INT_ARRAY;
    int[] LONG_ARRAY;
    decimal[] FLOAT_ARRAY;
    boolean[] BOOLEAN_ARRAY;
    string[] STRING_ARRAY;
};

type ResultBlob record {
    byte[] BLOB_TYPE;
};

type ResultDates record {
    string DATE_TYPE;
    string TIME_TYPE;
    string TIMESTAMP_TYPE;
    string DATETIME_TYPE;
};

type ResultDatesStruct record {
    time:Time DATE_TYPE;
    time:Time TIME_TYPE;
    time:Time TIMESTAMP_TYPE;
    time:Time DATETIME_TYPE;
};

type ResultDatesInt record {
    int DATE_TYPE;
    int TIME_TYPE;
    int TIMESTAMP_TYPE;
    int DATETIME_TYPE;
};

type ResultSetFloat record {
    float FLOAT_TYPE;
    float DOUBLE_TYPE;
    decimal NUMERIC_TYPE;
    decimal DECIMAL_TYPE;
};

type ResultPrimitiveInt record {
    int INT_TYPE;
};

type ResultCount record {
    int COUNTVAL;
};

type ResultTest record {
    int t1Row;
    int t1Int;
    int t2Row;
    int t2Int;
};

type ResultSignedInt record {
    int ID;
    int? TINYINTDATA;
    int? SMALLINTDATA;
    int? INTDATA;
    int? BIGINTDATA;
};

type ResultComplexTypes record {
    int ROW_ID;
    byte[]|() BLOB_TYPE;
    string? CLOB_TYPE;
    byte[]|() BINARY_TYPE;
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

type IntData record {
    int int_type;
};

type Person record {
    int id;
    int age;
    float salary;
    string name;
};

function testGetPrimitiveTypes(string jdbcURL, string user, string password) returns record {}|error? {
    jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    stream<record{}, error> streamData = dbClient->query("SELECT int_type, long_type, float_type, double_type,"
        + "boolean_type, string_type from DataTable WHERE row_id = 1");
    record {|record {} value;|}? data = check streamData.next();
    check streamData.close();
    record {}? value = data?.value;
    check dbClient.close();
    return value;
}

function testToJson(string jdbcURL, string user, string password) returns @tainted json|error {
    jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
  stream<record{}, error> streamData = dbClient->query("SELECT int_type, long_type, float_type, double_type,"
        + "boolean_type, string_type from DataTable WHERE row_id = 1");
    record {|record {} value;|}? data = check streamData.next();
    check streamData.close();
    record {}? value = data?.value;
    json|error retVal = json.constructFrom(value);
    check dbClient.close();
    return retVal;
}

function testToJsonComplexTypes(string jdbcURL, string user, string password) returns record {}|error? {
    jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    stream<record{}, error> streamData = dbClient->query("SELECT blob_type,clob_type,binary_type from" +
        " ComplexTypes where row_id = 1");
    record {|record {} value;|}? data = check streamData.next();
    check streamData.close();
    record {}? value = data?.value;
    check dbClient.close();
    return value;
}

function testComplexTypesNil(string jdbcURL, string user, string password) returns record {}|error? {
    jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    stream<record{}, error> streamData = dbClient->query("SELECT blob_type,clob_type,binary_type from " +
        " ComplexTypes where row_id = 2");
    record {|record {} value;|}? data = check streamData.next();
    check streamData.close();
    record {}? value = data?.value;
    check dbClient.close();
    return value;
}

function testArrayRetrieval(string jdbcURL, string user, string password)
returns record {}|error? {
    jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    stream<record{}, error> streamData = dbClient->query("SELECT int_type, int_array, long_type, long_array, " +
        "float_type, float_array, double_type, boolean_type, string_type, decimal_type, double_array, boolean_array," +
        "string_array from MixTypes where row_id =1");
    record {|record {} value;|}? data = check streamData.next();
    check streamData.close();
    record {}? value = data?.value;
    check dbClient.close();
    return value;
}

function testComplexWithStructDef(string jdbcURL, string user, string password)
returns record {}|error? {
    jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
        stream<record{}, error> streamData = dbClient->query("SELECT int_type, int_array, long_type, long_array, "
        + "float_type, float_array, double_type, boolean_type, string_type, double_array, boolean_array, string_array "
        + "from MixTypes where row_id =1", TestTypeData);
    record {|record {} value;|}? data = check streamData.next();
    check streamData.close();
    record {}? value = data?.value;
    check dbClient.close();
    return value;
}

function testMultipleRecoredRetrieval(string jdbcURL, string user, string password)
returns @tainted ResultMap[]|error? {
    jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
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

