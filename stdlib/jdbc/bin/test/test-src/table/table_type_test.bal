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

import ballerina/io;
import ballerina/time;
import ballerina/xmlutils;
import ballerina/jsonutils;
import ballerinax/java.jdbc;

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
    byte[] | () BLOB_TYPE;
    string? CLOB_TYPE;
    byte[] | () BINARY_TYPE;
};

type TestTypeData record {
    int i;
    int[] iA;
    int l;
    int[] lA;
    float f;
    float[] fA;
    float d;
    boolean b;
    string s;
    float[] dA;
    boolean[] bA;
    string[] sA;
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

function testToJson(string jdbcURL) returns @tainted json {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var result = testDB->select("SELECT int_type, long_type, float_type, double_type," +
                  "boolean_type, string_type from DataTable WHERE row_id = 1", ());
    json retVal = getJsonConversionResult(result);
    checkpanic testDB.stop();
    return retVal;
}

function testToJsonComplexTypes(string jdbcURL) returns @tainted json {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var result = testDB->select("SELECT blob_type,clob_type,binary_type from ComplexTypes where row_id = 1", ());
    json retVal = getJsonConversionResult(result);
    checkpanic testDB.stop();
    return retVal;
}

function testToJsonComplexTypesNil(string jdbcURL) returns @tainted json {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var result = testDB->select("SELECT blob_type,clob_type,binary_type from ComplexTypes where row_id = 2", ());
    json retVal = getJsonConversionResult(result);
    checkpanic testDB.stop();
    return retVal;
}

function testToXml(string jdbcURL) returns xml {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var result = testDB->select("SELECT int_type, long_type, float_type, double_type," +
                   "boolean_type, string_type from DataTable WHERE row_id = 1", ());
    xml retVal = getXMLConversionResult(result);
    checkpanic testDB.stop();
    return retVal;
}

function testToXmlComplexTypes(string jdbcURL) returns xml {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var result = testDB->select("SELECT blob_type,clob_type,binary_type from ComplexTypes where row_id = 1", ());

    xml retVal = getXMLConversionResult(result);
    checkpanic testDB.stop();
    return retVal;
}

function testToXmlComplexTypesNil(string jdbcURL) returns xml {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var result = testDB->select("SELECT blob_type,clob_type,binary_type from ComplexTypes where row_id = 2", ());

    xml retVal = getXMLConversionResult(result);
    checkpanic testDB.stop();
    return retVal;
}

function testToXmlMultipleConsume(string jdbcURL) returns xml {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var result = testDB->select("SELECT int_type, long_type, float_type, double_type," +
        "boolean_type, string_type from DataTable WHERE row_id = 1", ());

    xml retVal = getXMLConversionResult(result);
    checkpanic testDB.stop();
    return retVal;
}

function testToXmlWithAdd(string jdbcURL) returns xml {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var tableOrError1 = testDB->select("SELECT int_type from DataTable WHERE row_id = 1", ());
    xml result1 = getXMLConversionResult(tableOrError1);

    var tableOrError2 = testDB->select("SELECT int_type from DataTable WHERE row_id = 1", ());
    xml result2 = getXMLConversionResult(tableOrError2);

    xml result = result1 + result2;

    var dt3 = testDB->select("SELECT int_type from DataTable WHERE row_id = 1", ());
    if (dt3 is table<record {}>) {
        dt3.close();
    }
    checkpanic testDB.stop();
    return result;
}

function testToJsonMultipleConsume(string jdbcURL) returns @tainted json {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var tableOrError = testDB->select("SELECT int_type, long_type, float_type, double_type," +
        "boolean_type, string_type from DataTable WHERE row_id = 1", ());

    json result = getJsonConversionResult(tableOrError);
    checkpanic testDB.stop();
    return result;
}

function toXmlComplex(string jdbcURL) returns xml {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });
    var tableOrError = testDB->select("SELECT int_type, int_array, long_type, long_array, float_type," +
                    "float_array, double_type, boolean_type, string_type, decimal_type, double_array, boolean_array," +
                    "string_array from MixTypes where row_id =1", ());

    xml convertedVal = getXMLConversionResult(tableOrError);
    checkpanic testDB.stop();
    return convertedVal;
}

function testToXmlComplexWithStructDef(string jdbcURL) returns xml {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var tableOrError = testDB->select("SELECT int_type, int_array, long_type, long_array, float_type," +
                    "float_array, double_type, boolean_type, string_type, double_array, boolean_array, string_array " +
                    "from MixTypes where row_id =1", TestTypeData);

    xml convertedVal = getXMLConversionResult(tableOrError);
    checkpanic testDB.stop();
    return convertedVal;
}

function testToJsonComplex(string jdbcURL) returns @tainted json {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });
    var tableOrError = testDB->select("SELECT int_type, int_array, long_type, long_array, float_type," +
        "float_array, double_type, boolean_type, string_type, decimal_type, double_array, boolean_array, string_array " +
        "from MixTypes where row_id =1", ());

    json convertedVal = getJsonConversionResult(tableOrError);
    checkpanic testDB.stop();

    return convertedVal;
}

function testToJsonComplexWithStructDef(string jdbcURL) returns @tainted json {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var tableOrError = testDB->select("SELECT int_type, int_array, long_type, long_array, float_type," +
                    "float_array, double_type, boolean_type, string_type, double_array, boolean_array, string_array " +
                    "from MixTypes where row_id =1", TestTypeData);
    json ret = getJsonConversionResult(tableOrError);
    checkpanic testDB.stop();
    return ret;
}

function testJsonWithNull(string jdbcURL) returns @tainted json {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });
    var tableOrError = testDB->select("SELECT int_type, long_type, float_type, double_type," +
                  "boolean_type, string_type from DataTable WHERE row_id = 2", ());
    json convertedVal = getJsonConversionResult(tableOrError);
    checkpanic testDB.stop();
    return convertedVal;
}

function testXmlWithNull(string jdbcURL) returns xml {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });
    var tableOrError = testDB->select("SELECT int_type, long_type, float_type, double_type," +
                   "boolean_type, string_type from DataTable WHERE row_id = 2", ());
    xml ret = getXMLConversionResult(tableOrError);
    checkpanic testDB.stop();
    return ret;
}

function testToXmlWithinTransaction(string jdbcURL) returns [string, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    int returnValue = -1;
    string resultXml = "";
    transaction {
        var dt = testDB->select("SELECT int_type, long_type from DataTable WHERE row_id = 1", ());
        if (dt is table<record {}>) {
            var result = xmlutils:fromTable(dt);
            resultXml = io:sprintf("%s", result);
            returnValue = 0;
        }
    }
    checkpanic testDB.stop();
    return [resultXml, returnValue];
}

function testToJsonWithinTransaction(string jdbcURL) returns [string, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    int returnValue = -1;
    string result = "";
    transaction {
        var dt = testDB->select("SELECT int_type, long_type from DataTable WHERE row_id = 1", ());
        if (dt is table<record {}>) {
            var j = jsonutils:fromTable(dt);
            result = io:sprintf("%s", j.toJsonString());
            returnValue = 0;
        }
    }
    checkpanic testDB.stop();
    return [result, returnValue];
}

function testGetPrimitiveTypes(string jdbcURL) returns @tainted [int, int, float, float, boolean, string, decimal] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    int i = -1;
    int l = -1;
    float f = -1;
    float d = -1;
    boolean b = false;
    string s = "";
    decimal dec = -1;
    var dtRet = testDB->select("SELECT int_type, long_type, float_type, double_type," +
              "boolean_type, string_type, decimal_type from DataTable WHERE row_id = 1", ResultPrimitive);
    if (dtRet is table<ResultPrimitive>) {
        while (dtRet.hasNext()) {
            var rs = dtRet.getNext();
            if (rs is ResultPrimitive) {
                i = rs.INT_TYPE;
                l = rs.LONG_TYPE;
                f = rs.FLOAT_TYPE;
                d = rs.DOUBLE_TYPE;
                b = rs.BOOLEAN_TYPE;
                s = rs.STRING_TYPE;
                dec = rs.DECIMAL_TYPE;
            }
        }
    }
    checkpanic testDB.stop();
    return [i, l, f, d, b, s, dec];
}

function testGetComplexTypes(string jdbcURL) returns @tainted [byte[], string, byte[]] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });
    byte[] blobData = [];
    string clob = "";
    byte[] binaryData = [];
    var tableOrError = testDB->select("SELECT blob_type,clob_type,binary_type from ComplexTypes where row_id = 1",
    ResultObject);
    if (tableOrError is table<ResultObject>) {
        while (tableOrError.hasNext()) {
            var rs = tableOrError.getNext();
            if (rs is ResultObject) {
                blobData = rs.BLOB_TYPE;
                clob = rs.CLOB_TYPE;
                binaryData = rs.BINARY_TYPE;
            }
        }
    }
    checkpanic testDB.stop();
    return [blobData, clob, binaryData];
}

function testArrayData(string jdbcURL) returns @tainted [int[], int[], decimal[], string[], boolean[]] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var tableOrError = testDB->select("SELECT int_array, long_array, float_array, boolean_array," +
              "string_array from ArrayTypes where row_id = 1", ResultMap);
    int[] int_arr = [];
    int[] long_arr = [];
    decimal[] float_arr = [];
    string[] string_arr = [];
    boolean[] boolean_arr = [];
    if (tableOrError is table<ResultMap>) {
        while (tableOrError.hasNext()) {
            var rs = tableOrError.getNext();
            if (rs is ResultMap) {
                int_arr = rs.INT_ARRAY;
                long_arr = rs.LONG_ARRAY;
                float_arr = rs.FLOAT_ARRAY;
                boolean_arr = rs.BOOLEAN_ARRAY;
                string_arr = rs.STRING_ARRAY;
            }
        }
    }
    checkpanic testDB.stop();
    return [int_arr, long_arr, float_arr, string_arr, boolean_arr];
}

function testArrayDataInsertAndPrint(string jdbcURL) returns @tainted [int, int, int, int, int, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    int[] dataint = [1, 2, 3];
    decimal[] datafloat = [33.4, 55.4];
    string[] datastring = ["hello", "world"];
    boolean[] databoolean = [true, false, false, true, true];

    jdbc:Parameter paraID = {sqlType: jdbc:TYPE_INTEGER, value: 4};
    jdbc:Parameter paraInt = {sqlType: jdbc:TYPE_ARRAY, value: dataint};
    jdbc:Parameter paraLong = {sqlType: jdbc:TYPE_ARRAY, value: dataint};
    jdbc:Parameter paraFloat = {sqlType: jdbc:TYPE_ARRAY, value: datafloat};
    jdbc:Parameter paraString = {sqlType: jdbc:TYPE_ARRAY, value: datastring};
    jdbc:Parameter paraBool = {sqlType: jdbc:TYPE_ARRAY, value: databoolean};

    int intArrLen = -1;
    int longArrLen = -1;
    int floatArrLen = -1;
    int boolArrLen = -1;
    int strArrLen = -1;

    var updateRet = testDB->update("insert into ArrayTypes(row_id, int_array, long_array, float_array," +
                                "string_array, boolean_array) values (?,?,?,?,?,?)",
    paraID, paraInt, paraLong, paraFloat, paraString, paraBool);
    int updatedCount = -1;
    if (updateRet is jdbc:UpdateResult) {
        updatedCount = updateRet.updatedRowCount;
    } else {
        error e = updateRet;
        io:println(<string>e.detail()["message"]);
    }
    var dtRet = testDB->select("SELECT int_array, long_array, float_array, boolean_array, string_array " +
                                 "from ArrayTypes where row_id = 4", ResultMap);
    if (dtRet is table<ResultMap>) {
        while (dtRet.hasNext()) {
            var rs = dtRet.getNext();
            if (rs is ResultMap) {
                io:println(rs.INT_ARRAY);
                intArrLen = rs.INT_ARRAY.length();
                io:println(rs.LONG_ARRAY);
                longArrLen = rs.LONG_ARRAY.length();
                io:println(rs.FLOAT_ARRAY);
                floatArrLen = rs.FLOAT_ARRAY.length();
                io:println(rs.BOOLEAN_ARRAY);
                boolArrLen = rs.BOOLEAN_ARRAY.length();
                io:println(rs.STRING_ARRAY);
                strArrLen = rs.STRING_ARRAY.length();
            }
        }
    }
    checkpanic testDB.stop();
    return [updatedCount, intArrLen, longArrLen, floatArrLen, boolArrLen, strArrLen];
}

function testDateTime(int datein, int timein, int timestampin, string jdbcURL) returns @tainted [string, string, string, string] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });
    string date = "";
    string time = "";
    string timestamp = "";
    string datetime = "";

    jdbc:Parameter para0 = {sqlType: jdbc:TYPE_INTEGER, value: 1};
    jdbc:Parameter para1 = {sqlType: jdbc:TYPE_DATE, value: datein};
    jdbc:Parameter para2 = {sqlType: jdbc:TYPE_TIME, value: timein};
    jdbc:Parameter para3 = {sqlType: jdbc:TYPE_TIMESTAMP, value: timestampin};
    jdbc:Parameter para4 = {sqlType: jdbc:TYPE_DATETIME, value: timestampin};

    _ = checkpanic testDB->update("Insert into DateTimeTypes " +
        "(row_id, date_type, time_type, timestamp_type, datetime_type) values (?,?,?,?,?)",
    para0, para1, para2, para3, para4);

    var selectRet = testDB->select("SELECT date_type, time_type, timestamp_type, datetime_type " +
                "from DateTimeTypes where row_id = 1", ResultDates);
    if (selectRet is table<ResultDates>) {
        while (selectRet.hasNext()) {
            var rs = selectRet.getNext();
            if (rs is ResultDates) {
                time = rs.TIME_TYPE;
                date = rs.DATE_TYPE;
                timestamp = rs.TIMESTAMP_TYPE;
                datetime = rs.DATETIME_TYPE;
            }
        }
    }
    checkpanic testDB.stop();
    return [date, time, timestamp, datetime];
}

function testDateTimeAsTimeStruct(string jdbcURL) returns @tainted [int, int, int, int, int, int, int, int] | error {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    int dateInserted = -1;
    int dateRetrieved = -1;
    int timeInserted = -1;
    int timeRetrieved = -1;
    int timestampInserted = -1;
    int timestampRetrieved = -1;
    int datetimeInserted = -1;
    int datetimeRetrieved = -1;

    time:Time dateStruct = check time:createTime(2017, 5, 23, 0, 0, 0, 0, "");

    time:TimeZone zoneValue = {id: "UTC"};
    time:Time timeStruct = {time: 51323000, zone: zoneValue};

    time:Time timestampStruct = check time:createTime(2017, 1, 25, 16, 12, 23, 0, "UTC");
    time:Time datetimeStruct = check time:createTime(2017, 1, 31, 16, 12, 23, 332, "UTC");
    dateInserted = dateStruct.time;
    timeInserted = timeStruct.time;
    timestampInserted = timestampStruct.time;
    datetimeInserted = datetimeStruct.time;

    jdbc:Parameter para0 = {sqlType: jdbc:TYPE_INTEGER, value: 31};
    jdbc:Parameter para1 = {sqlType: jdbc:TYPE_DATE, value: dateStruct};
    jdbc:Parameter para2 = {sqlType: jdbc:TYPE_TIME, value: timeStruct};
    jdbc:Parameter para3 = {sqlType: jdbc:TYPE_TIMESTAMP, value: timestampStruct};
    jdbc:Parameter para4 = {sqlType: jdbc:TYPE_DATETIME, value: datetimeStruct};

    _ = checkpanic testDB->update("Insert into DateTimeTypes " +
        "(row_id, date_type, time_type, timestamp_type, datetime_type) values (?,?,?,?,?)",
    para0, para1, para2, para3, para4);

    var selectRet = testDB->select("SELECT date_type, time_type, timestamp_type, datetime_type " +
                "from DateTimeTypes where row_id = 31", ResultDatesStruct);
    if (selectRet is table<ResultDatesStruct>) {
        while (selectRet.hasNext()) {
            var rs = selectRet.getNext();
            if (rs is ResultDatesStruct) {
                dateRetrieved = rs.DATE_TYPE.time;
                timeRetrieved = rs.TIME_TYPE.time;
                timestampRetrieved = rs.TIMESTAMP_TYPE.time;
                datetimeRetrieved = rs.DATETIME_TYPE.time;
            }
        }
    }
    checkpanic testDB.stop();
    return [dateInserted, dateRetrieved, timeInserted, timeRetrieved, timestampInserted, timestampRetrieved,
    datetimeInserted, datetimeRetrieved];
}

function testDateTimeInt(int datein, int timein, int timestampin, string jdbcURL) returns @tainted [int, int, int, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    jdbc:Parameter para0 = {sqlType: jdbc:TYPE_INTEGER, value: 32};
    jdbc:Parameter para1 = {sqlType: jdbc:TYPE_DATE, value: datein};
    jdbc:Parameter para2 = {sqlType: jdbc:TYPE_TIME, value: timein};
    jdbc:Parameter para3 = {sqlType: jdbc:TYPE_TIMESTAMP, value: timestampin};
    jdbc:Parameter para4 = {sqlType: jdbc:TYPE_DATETIME, value: timestampin};

    int date = -1;
    int time = -1;
    int timestamp = -1;
    int datetime = -1;

    _ = checkpanic testDB->update("Insert into DateTimeTypes " +
        "(row_id, date_type, time_type, timestamp_type, datetime_type) values (?,?,?,?,?)",
    para0, para1, para2, para3, para4);

    var selectRet = testDB->select("SELECT date_type, time_type, timestamp_type, datetime_type " +
                "from DateTimeTypes where row_id = 32", ResultDatesInt);

    if (selectRet is table<ResultDatesInt>) {
        while (selectRet.hasNext()) {
            var rs = selectRet.getNext();
            if (rs is ResultDatesInt) {
                time = rs.TIME_TYPE;
                date = rs.DATE_TYPE;
                timestamp = rs.TIMESTAMP_TYPE;
                datetime = rs.DATETIME_TYPE;
            }
        }
    }
    checkpanic testDB.stop();
    return [date, time, timestamp, datetime];
}

function testBlobData(string jdbcURL) returns @tainted byte[] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });
    byte[] blobData = [];
    var selectRet = testDB->select("SELECT blob_type from ComplexTypes where row_id = 1", ResultBlob);
    if (selectRet is table<ResultBlob>) {
        while (selectRet.hasNext()) {
            var rs = selectRet.getNext();
            if (rs is ResultBlob) {
                blobData = rs.BLOB_TYPE;
            }
        }
    }
    checkpanic testDB.stop();
    return blobData;
}

function testColumnAlias(string jdbcURL) returns @tainted [int, int, float, float, boolean, string, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    int i = -1;
    int l = -1;
    float f = -1;
    float d = -1;
    boolean b = false;
    string s = "";
    int i2 = -1;
    var selectRet = testDB->select("SELECT dt1.int_type, dt1.long_type, dt1.float_type," +
           "dt1.double_type,dt1.boolean_type, dt1.string_type,dt2.int_type as dt2int_type from DataTable dt1 " +
           "left join DataTableRep dt2 on dt1.row_id = dt2.row_id WHERE dt1.row_id = 1;", ResultSetTestAlias);
    if (selectRet is table<ResultSetTestAlias>) {
        while (selectRet.hasNext()) {
            var rs = selectRet.getNext();
            if (rs is ResultSetTestAlias) {
                i = rs.INT_TYPE;
                l = rs.LONG_TYPE;
                f = rs.FLOAT_TYPE;
                d = rs.DOUBLE_TYPE;
                b = rs.BOOLEAN_TYPE;
                s = rs.STRING_TYPE;
                i2 = rs.DT2INT_TYPE;
            }
        }
    }
    checkpanic testDB.stop();
    return [i, l, f, d, b, s, i2];
}

function testBlobInsert(string jdbcURL) returns int {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });
    var selectRet = testDB->select("SELECT blob_type from ComplexTypes where row_id = 1", ResultBlob);
    byte[] blobData = [];
    if (selectRet is table<ResultBlob>) {
        while (selectRet.hasNext()) {
            var rs = selectRet.getNext();
            if (rs is ResultBlob) {
                blobData = rs.BLOB_TYPE;
            }
        }
    }
    jdbc:Parameter para0 = {sqlType: jdbc:TYPE_INTEGER, value: 10};
    jdbc:Parameter para1 = {sqlType: jdbc:TYPE_BLOB, value: blobData};
    var insertCountRet = testDB->update("Insert into ComplexTypes (row_id, blob_type) values (?,?)", para0, para1);
    int insertCount = insertCountRet is jdbc:UpdateResult ? insertCountRet.updatedRowCount : -1;

    checkpanic testDB.stop();
    return insertCount;
}

function testTableAutoClose(string jdbcURL) returns @tainted [int, json] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    int i = -1;
    string test = "";
    var selectRet = testDB->select("SELECT int_type from DataTable WHERE row_id = 1", ResultPrimitiveInt);

    if (selectRet is table<ResultPrimitiveInt>) {
        while (selectRet.hasNext()) {
            var rs = selectRet.getNext();
            if (rs is ResultPrimitiveInt) {
                i = rs.INT_TYPE;
            }
        }
    }
    var selectRet2 = testDB->select("SELECT int_type, long_type, float_type, double_type," +
              "boolean_type, string_type from DataTable WHERE row_id = 1", ());

    json jsonData = getJsonConversionResult(selectRet2);

    var selectRet3 = testDB->select("SELECT int_type, long_type, float_type, double_type," +
              "boolean_type, string_type from DataTable WHERE row_id = 1", ());
    if (selectRet3 is table<record {}>) {
        selectRet3.close();
    }

    checkpanic testDB.stop();
    return [i, jsonData];
}

function testTableManualClose(string jdbcURL) returns @tainted int {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });
    var selectRet = testDB->select("SELECT int_type from DataTable", ResultPrimitiveInt);
    int i = 0;
    if (selectRet is table<ResultPrimitiveInt>) {
        while (selectRet.hasNext()) {
            var rs = selectRet.getNext();
            if (rs is ResultPrimitiveInt) {
                int ret = rs.INT_TYPE;
                i = i + 1;
                if (i == 1) {
                    break;
                }
            } else {
                checkpanic testDB.stop();
                return -1;
            }
        }
        selectRet.close();
    } else {
        checkpanic testDB.stop();
        return -2;
    }

    int data = -4;
    var selectRet2 = testDB->select("SELECT int_type from DataTable WHERE row_id = 1", ResultPrimitiveInt);

    if (selectRet2 is table<ResultPrimitiveInt>) {
        while (selectRet2.hasNext()) {
            var rs2 = selectRet2.getNext();
            if (rs2 is ResultPrimitiveInt) {
                data = rs2.INT_TYPE;
            } else {
                checkpanic testDB.stop();
                return -2;
            }
        }
        selectRet2.close();
    } else {
        checkpanic testDB.stop();
        return -3;
    }
    checkpanic testDB.stop();
    return data;
}

function testCloseConnectionPool(string jdbcURL) returns int {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var selectRet = testDB->select("SELECT COUNT(*) FROM INFORMATION_SCHEMA.SESSIONS", ResultCount);
    int retVal;
    if (selectRet is table<ResultCount>) {
        while (selectRet.hasNext()) {
            var rs = selectRet.getNext();
            if (rs is ResultCount) {
                retVal = rs.COUNTVAL;
            } else {
                retVal = -1;
            }
        }
    } else {
        retVal = -2;
    }
    checkpanic testDB.stop();
    return retVal;
}

function testTablePrintAndPrintln(string jdbcURL) {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });
    var selectRet = testDB->select("SELECT int_type, long_type, float_type, double_type," +
        "boolean_type, string_type from DataTable WHERE row_id = 1", ());
    if (selectRet is table<record {}>) {
        io:println(selectRet);
        io:print(selectRet);
        selectRet.close();
    } else {
        error e = selectRet;
        io:print(<string>e.reason());
    }
    checkpanic testDB.stop();
}

function testMultipleRows(string jdbcURL) returns @tainted [int, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var selectRet = testDB->select("SELECT int_type from DataTableRep", ResultPrimitiveInt);

    ResultPrimitiveInt rs1 = {INT_TYPE: -1};
    ResultPrimitiveInt rs2 = {INT_TYPE: -1};
    int i = 0;
    if (selectRet is table<ResultPrimitiveInt>) {
        while (selectRet.hasNext()) {
            if (i == 0) {
                rs1 = <ResultPrimitiveInt>selectRet.getNext();
            } else {
                rs2 = <ResultPrimitiveInt>selectRet.getNext();
            }
            i = i + 1;
        }
    }
    checkpanic testDB.stop();
    return [rs1.INT_TYPE, rs2.INT_TYPE];
}

function testMultipleRowsWithoutLoop(string jdbcURL) returns @tainted [int, int, int, int, string, string] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    //Iterate the whole result
    var selectRet = testDB->select("SELECT int_type from DataTableRep order by int_type desc", ResultPrimitiveInt);

    int i1 = 0;
    int i2 = 0;
    int i3 = 0;
    int i4 = 0;
    string st1;
    string st2;
    if (selectRet is table<ResultPrimitiveInt>) {
        while (selectRet.hasNext()) {
            var rs = selectRet.getNext();
            if (rs is ResultPrimitiveInt) {
                i1 = rs.INT_TYPE;
            }
        }
    }

    //Pick the first row only
    var selectRet2 = testDB->select("SELECT int_type from DataTableRep order by int_type desc", ResultPrimitiveInt);

    if (selectRet2 is table<ResultPrimitiveInt>) {
        if (selectRet2.hasNext()) {
            var rs = selectRet2.getNext();
            if (rs is ResultPrimitiveInt) {
                i2 = rs.INT_TYPE;
            }
        }
        selectRet2.close();
    }

    //Pick all the rows without checking
    var selectRet3 = testDB->select("SELECT int_type from DataTableRep order by int_type desc", ResultPrimitiveInt);

    if (selectRet3 is table<ResultPrimitiveInt>) {
        var rs1 = selectRet3.getNext();
        if (rs1 is ResultPrimitiveInt) {
            i3 = rs1.INT_TYPE;
        }

        var rs2 = selectRet3.getNext();
        if (rs2 is ResultPrimitiveInt) {
            i4 = rs2.INT_TYPE;
        }
        selectRet3.close();
    }

    //Pick the first row by checking and next row without checking
    string s1 = "";
    var selectRet4 = testDB->select("SELECT int_type from DataTableRep order by int_type desc", ResultPrimitiveInt);

    if (selectRet4 is table<ResultPrimitiveInt>) {
        if (selectRet4.hasNext()) {
            var rs = selectRet4.getNext();
            if (rs is ResultPrimitiveInt) {
                s1 = s1 + rs.INT_TYPE.toString();
            }
            var rs2 = selectRet4.getNext();
            if (rs2 is ResultPrimitiveInt) {
                s1 = s1 + "_" + rs2.INT_TYPE.toString();
            }
            if (selectRet4.hasNext()) {
                s1 = s1 + "_" + "HAS";
            } else {
                s1 = s1 + "_" + "NOT";
            }
        }
    }

    //Pick the first row without checking, then check and no fetch, and finally fetch row by checking
    string s2 = "";
    var selectRet5 = testDB->select("SELECT int_type from DataTableRep order by int_type desc", ResultPrimitiveInt);

    if (selectRet5 is table<ResultPrimitiveInt>) {
        var rs = selectRet5.getNext();
        if (rs is ResultPrimitiveInt) {
            s2 = s2 + rs.INT_TYPE.toString();
        }
        if (selectRet5.hasNext()) {
            s2 = s2 + "_" + "HAS";
        } else {
            s2 = s2 + "_" + "NO";
        }
        if (selectRet5.hasNext()) {
            s2 = s2 + "_" + "HAS";
        } else {
            s2 = s2 + "_" + "NO";
        }
        if (selectRet5.hasNext()) {
            var rs2 = selectRet5.getNext();
            if (rs2 is ResultPrimitiveInt) {
                s2 = s2 + "_" + rs2.INT_TYPE.toString();
            }
        }
        if (selectRet5.hasNext()) {
            s2 = s2 + "_" + "HAS";
        } else {
            s2 = s2 + "_" + "NO";
        }
        if (selectRet5.hasNext()) {
            s2 = s2 + "_" + "HAS";
        } else {
            s2 = s2 + "_" + "NO";
        }
    }
    checkpanic testDB.stop();
    return [i1, i2, i3, i4, s1, s2];
}

function testHasNextWithoutConsume(string jdbcURL) returns [boolean, boolean, boolean] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var selectRet = testDB->select("SELECT int_type from DataTableRep order by int_type desc", ResultPrimitiveInt);

    boolean b1 = false;
    boolean b2 = false;
    boolean b3 = false;

    if (selectRet is table<ResultPrimitiveInt>) {
        if (selectRet.hasNext()) {
            b1 = true;
        }
        if (selectRet.hasNext()) {
            b2 = true;
        }
        if (selectRet.hasNext()) {
            b3 = true;
        }
        selectRet.close();
    }
    checkpanic testDB.stop();
    return [b1, b2, b3];
}

function testGetFloatTypes(string jdbcURL) returns @tainted [float, float, decimal, decimal] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var selectRet = testDB->select("SELECT float_type, double_type," +
                  "numeric_type, decimal_type from FloatTable WHERE row_id = 1", ResultSetFloat);

    float f = 0;
    float d = 0;
    decimal num = 0;
    decimal dec = 0;

    if (selectRet is table<ResultSetFloat>) {
        while (selectRet.hasNext()) {
            var rs = selectRet.getNext();
            if (rs is ResultSetFloat) {
                f = rs.FLOAT_TYPE;
                d = rs.DOUBLE_TYPE;
                num = rs.NUMERIC_TYPE;
                dec = rs.DECIMAL_TYPE;
            }
        }
    }
    checkpanic testDB.stop();
    return [f, d, num, dec];
}

function testSignedIntMaxMinValues(string jdbcURL) returns @tainted [int, int, int, string, string, string] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    string insertSQL = "INSERT INTO IntegerTypes(id,tinyIntData, smallIntData, intData, bigIntData) VALUES (?,?, ?,?,?)"
    ;
    string selectSQL = "SELECT id,tinyIntData,smallIntData,intData,bigIntData FROM IntegerTypes";

    int maxInsert = -1;
    int minInsert = -1;
    int nullInsert = -1;
    string jsonStr = "";
    string xmlStr = "";
    string str = "";

    //Insert signed max
    jdbc:Parameter para1 = {sqlType: jdbc:TYPE_INTEGER, value: 1};
    jdbc:Parameter para2 = {sqlType: jdbc:TYPE_TINYINT, value: 127};
    jdbc:Parameter para3 = {sqlType: jdbc:TYPE_SMALLINT, value: 32767};
    jdbc:Parameter para4 = {sqlType: jdbc:TYPE_INTEGER, value: 2147483647};
    jdbc:Parameter para5 = {sqlType: jdbc:TYPE_BIGINT, value: 9223372036854775807};
    var updateRet1 = testDB->update(insertSQL, para1, para2, para3, para4, para5);
    maxInsert = updateRet1 is jdbc:UpdateResult ? updateRet1.updatedRowCount : maxInsert;

    //Insert signed min
    para1 = {sqlType: jdbc:TYPE_INTEGER, value: 2};
    para2 = {sqlType: jdbc:TYPE_TINYINT, value: -128};
    para3 = {sqlType: jdbc:TYPE_SMALLINT, value: -32768};
    para4 = {sqlType: jdbc:TYPE_INTEGER, value: -2147483648};
    para5 = {sqlType: jdbc:TYPE_BIGINT, value: -9223372036854775808};
    var updateRet2 = testDB->update(insertSQL, para1, para2, para3, para4, para5);
    minInsert = updateRet2 is jdbc:UpdateResult ? updateRet2.updatedRowCount : minInsert;

    //Insert null
    para1 = {sqlType: jdbc:TYPE_INTEGER, value: 3};
    para2 = {sqlType: jdbc:TYPE_TINYINT, value: ()};
    para3 = {sqlType: jdbc:TYPE_SMALLINT, value: ()};
    para4 = {sqlType: jdbc:TYPE_INTEGER, value: ()};
    para5 = {sqlType: jdbc:TYPE_BIGINT, value: ()};
    var updateRet3 = testDB->update(insertSQL, para1, para2, para3, para4, para5);
    nullInsert = updateRet3 is jdbc:UpdateResult ? updateRet3.updatedRowCount : nullInsert;

    var dtRet = testDB->select(selectSQL, ());

    if (dtRet is table<record {}>) {
        json j = getJsonConversionResult(dtRet);
        jsonStr = io:sprintf("%s", j.toJsonString());
    }

    var dtRet2 = testDB->select(selectSQL, ());

    if (dtRet2 is table<record {}>) {
        xml x = getXMLConversionResult(dtRet2);
        xmlStr = io:sprintf("%s", x);
    }
    var dtRet3 = testDB->select(selectSQL, ResultSignedInt);

    if (dtRet3 is table<ResultSignedInt>) {
        str = "";
        while (dtRet3.hasNext()) {
            var result = dtRet3.getNext();
            if (result is ResultSignedInt) {
                var tinyIntData = result.TINYINTDATA;
                var smallIntData = result.SMALLINTDATA;
                var intData = result.INTDATA;
                var bigIntData = result.BIGINTDATA;
                str = str + result.ID.toString() + "|" + (tinyIntData is int ? tinyIntData.toString() : "-1") + "|" +
                (smallIntData is int ? smallIntData.toString() : "-1") + "|" +
                (intData is int ? intData.toString() : "-1") + "|" +
                (bigIntData is int ? bigIntData.toString() : "-1") + "#";
            }
        }
    }
    checkpanic testDB.stop();
    return [maxInsert, minInsert, nullInsert, jsonStr, xmlStr, str];
}

function testComplexTypeInsertAndRetrieval(string jdbcURL) returns @tainted [int, int, string, string, string, byte[][]] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    string insertSQL = "INSERT INTO ComplexTypes(row_id, blob_type, clob_type, binary_type) VALUES (?,?,?,?)";
    string selectSQL =
    "SELECT row_id, blob_type, clob_type, binary_type FROM ComplexTypes where row_id = 100 or row_id = 200";
    string text = "Sample Text";
    byte[] content = text.toBytes();

    int retDataInsert;
    int retNullInsert;
    string jsonStr;
    string xmlStr;
    string str;

    //Insert data
    jdbc:Parameter para1 = {sqlType: jdbc:TYPE_INTEGER, value: 100};
    jdbc:Parameter para2 = {sqlType: jdbc:TYPE_BLOB, value: content};
    jdbc:Parameter para3 = {sqlType: jdbc:TYPE_CLOB, value: text};
    jdbc:Parameter para4 = {sqlType: jdbc:TYPE_BINARY, value: content};
    var updateRet1 = testDB->update(insertSQL, para1, para2, para3, para4);
    retDataInsert = updateRet1 is jdbc:UpdateResult ? updateRet1.updatedRowCount : retDataInsert;
    //Insert null values
    para1 = {sqlType: jdbc:TYPE_INTEGER, value: 200};
    para2 = {sqlType: jdbc:TYPE_BLOB, value: ()};
    para3 = {sqlType: jdbc:TYPE_CLOB, value: ()};
    para4 = {sqlType: jdbc:TYPE_BINARY, value: ()};
    var updateRet2 = testDB->update(insertSQL, para1, para2, para3, para4);
    retNullInsert = updateRet2 is jdbc:UpdateResult ? updateRet2.updatedRowCount : retNullInsert;

    var selectRet = testDB->select(selectSQL, ());

    json j = getJsonConversionResult(selectRet);
    jsonStr = io:sprintf("%s", j.toJsonString());

    var selectRet2 = testDB->select(selectSQL, ());

    xml x = getXMLConversionResult(selectRet2);
    xmlStr = io:sprintf("%s", x);

    var selectRet3 = testDB->select(selectSQL, ResultComplexTypes);

    str = "";
    byte[][] expected = [];
    int i = 0;

    if (selectRet3 is table<ResultComplexTypes>) {
        while (selectRet3.hasNext()) {
            var result = selectRet3.getNext();
            if (result is ResultComplexTypes) {
                string blobType;
                expected[i] = result.BLOB_TYPE ?: [];
                blobType = result.BLOB_TYPE is () ? "nil" : "nonNil";
                var clobType = result.CLOB_TYPE;
                str = str + result.ROW_ID.toString() + "|" + blobType.toString() + "|" +
                (clobType is string ? clobType : "nil") + "|";
                i += 1;
            }
        }
    }
    checkpanic testDB.stop();
    return [retDataInsert, retNullInsert, jsonStr, xmlStr, str, expected];
}

function testJsonXMLConversionwithDuplicateColumnNames(string jdbcURL) returns @tainted [json, xml] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });
    var selectRet = testDB->select("SELECT dt1.row_id, dt1.int_type, dt2.row_id, dt2.int_type from DataTable dt1 left " +
            "join DataTableRep dt2 on dt1.row_id = dt2.row_id WHERE dt1.row_id = 1", ());
    json j = getJsonConversionResult(selectRet);

    var selectRet2 = testDB->select("SELECT dt1.row_id, dt1.int_type, dt2.row_id, dt2.int_type from DataTable dt1 left " +
            "join DataTableRep dt2 on dt1.row_id = dt2.row_id WHERE dt1.row_id = 1", ());
    xml x = getXMLConversionResult(selectRet2);

    checkpanic testDB.stop();

    return [j, x];
}

function testStructFieldNotMatchingColumnName(string jdbcURL) returns @tainted [int, int, int, int, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var selectRet = testDB->select("SELECT count(*) from DataTable WHERE row_id = 1", ResultCount);

    int countAll = -1;
    int i1 = -1;
    int i2 = -1;
    int i3 = -1;
    int i4 = -1;
    if (selectRet is table<ResultCount>) {
        while (selectRet.hasNext()) {
            var rs = selectRet.getNext();
            if (rs is ResultCount) {
                countAll = rs.COUNTVAL;
            }
        }
    }

    var selectRet2 = testDB->select("SELECT dt1.row_id, dt1.int_type, dt2.row_id, dt2.int_type from DataTable dt1 left " +
            "join DataTableRep dt2 on dt1.row_id = dt2.row_id WHERE dt1.row_id = 1", ResultTest);

    if (selectRet2 is table<ResultTest>) {
        while (selectRet2.hasNext()) {
            var rs = selectRet2.getNext();
            if (rs is ResultTest) {
                i1 = rs.t1Row;
                i2 = rs.t1Int;
                i3 = rs.t2Row;
                i4 = rs.t2Int;
            }
        }
    }
    checkpanic testDB.stop();
    return [countAll, i1, i2, i3, i4];
}

function testGetPrimitiveTypesWithForEach(string jdbcURL) returns @tainted [int, int, float, float, boolean, string, decimal] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var selectRet = testDB->select("SELECT int_type, long_type, float_type, double_type," +
              "boolean_type, string_type, decimal_type from DataTable WHERE row_id = 1", ResultPrimitive);

    int i = -1;
    int l = -1;
    float f = -1;
    float d = -1;
    boolean b = false;
    string s = "";
    decimal dec = -1;
    if (selectRet is table<ResultPrimitive>) {
        foreach var x in selectRet {
            i = x.INT_TYPE;
            l = x.LONG_TYPE;
            f = x.FLOAT_TYPE;
            d = x.DOUBLE_TYPE;
            b = x.BOOLEAN_TYPE;
            s = x.STRING_TYPE;
            dec = x.DECIMAL_TYPE;
        }
    }
    checkpanic testDB.stop();
    return [i, l, f, d, b, s, dec];
}

function testMultipleRowsWithForEach(string jdbcURL) returns @tainted [int, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var selectRet = testDB->select("SELECT int_type from DataTableRep", ResultPrimitiveInt);

    ResultPrimitiveInt rs1 = {INT_TYPE: -1};
    ResultPrimitiveInt rs2 = {INT_TYPE: -1};
    if (selectRet is table<ResultPrimitiveInt>) {
        int i = 0;
        foreach var x in selectRet {
            if (i == 0) {
                rs1 = x;
            } else {
                rs2 = x;
            }
            i = i + 1;
        }
    }
    checkpanic testDB.stop();
    return [rs1.INT_TYPE, rs2.INT_TYPE];
}

function testGetPrimitiveTypesWithWhileLoopAndConstructFrom(string jdbcURL) returns @tainted [int, int, float, float, boolean, string, decimal] {
    jdbc:Client testDB = new({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    });

    var selectRet = testDB->select("SELECT int_type, long_type, float_type, double_type," +
              "boolean_type, string_type, decimal_type from DataTable WHERE row_id = 1", ResultPrimitive);

    int i = -1;
    int l = -1;
    float f = -1;
    float d = -1;
    boolean b = false;
    string s = "";
    decimal dec = -1;
    if (selectRet is table<ResultPrimitive>) {
        while(selectRet.hasNext()) {
            ResultPrimitive rs = checkpanic ResultPrimitive.constructFrom(selectRet.getNext());
            i = rs.INT_TYPE;
            l = rs.LONG_TYPE;
            f = rs.FLOAT_TYPE;
            d = rs.DOUBLE_TYPE;
            b = rs.BOOLEAN_TYPE;
            s = rs.STRING_TYPE;
            dec = rs.DECIMAL_TYPE;
        }
    }
    checkpanic testDB.stop();
    return [i, l, f, d, b, s, dec];
}

function testTableAddInvalid(string jdbcURL) returns @tainted string {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });
    var selectRet = testDB->select("SELECT int_type from DataTableRep", ResultPrimitiveInt);
    string s = "";
    ResultPrimitiveInt row = {INT_TYPE: 443};
    if (selectRet is table<ResultPrimitiveInt>) {
        var ret = trap selectRet.add(row);
        if (ret is error) {
            s = <string>ret.detail()["message"];
        } else {
            s = "nil";
        }
        selectRet.close();
    }
    checkpanic testDB.stop();
    return s;
}

function testTableRemoveInvalid(string jdbcURL) returns @tainted string {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });
    var selectRet = testDB->select("SELECT int_type from DataTableRep", ResultPrimitiveInt);
    string s = "";
    ResultPrimitiveInt row = {INT_TYPE: 443};
    if (selectRet is table<ResultPrimitiveInt>) {
        var ret = trap selectRet.remove(isDelete);
        if (ret is int) {
            s = checkpanic string.constructFrom(ret);
        } else {
            error e = ret;
            s = <string>e.detail()["message"];
        }
        selectRet.close();
    }
    checkpanic testDB.stop();
    return s;
}

function tableGetNextInvalid(string jdbcURL) returns @tainted string {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });
    var selectRet = testDB->select("SELECT * from DataTable WHERE row_id = 1", ());
    string retVal = "";
    if (selectRet is table<record {}>) {
        selectRet.close();
        var ret = trap selectRet.getNext();
        if (ret is error) {
            retVal = <string>ret.detail()["message"];
        }
    }
    checkpanic testDB.stop();
    return retVal;
}

function isDelete(ResultPrimitiveInt p) returns boolean {
    return p.INT_TYPE < 2000;
}

function testToJsonAndAccessFromMiddle(string jdbcURL) returns @tainted [json, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });
    var selectRet = testDB->select("SELECT int_type, long_type, float_type, double_type," +
                  "boolean_type, string_type from DataTable", ());
    json result = getJsonConversionResult(selectRet);
    json[] jArray = <json[]>result;
    json j = jArray[1];
    checkpanic testDB.stop();
    return [result, jArray.length()];
}

function testToJsonAndIterate(string jdbcURL) returns @tainted [json, int] | error {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });
    var selectRet = testDB->select("SELECT int_type, long_type, float_type, double_type," +
                  "boolean_type, string_type from DataTable", ());
    json result = getJsonConversionResult(selectRet);
    json[] j = <json[]>result;
    checkpanic testDB.stop();
    return [j, j.length()];
}

function testToJsonAndSetAsChildElement(string jdbcURL) returns @tainted json {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });
    var selectRet = testDB->select("SELECT int_type, long_type, float_type, double_type," +
                  "boolean_type, string_type from DataTable", ());
    json result = getJsonConversionResult(selectRet);
    json j = {status: "SUCCESS", resp: {value: result}};
    checkpanic testDB.stop();
    return j;
}

function testToJsonAndLengthof(string jdbcURL) returns @tainted [int, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });
    var selectRet = testDB->select("SELECT int_type, long_type, float_type, double_type," +
                  "boolean_type, string_type from DataTable", ());

    json result = getJsonConversionResult(selectRet);
    json[] jArray = checkpanic json[].constructFrom(result);

    // get the length before accessing
    int beforeLen = jArray.length();

    // get the length after accessing
    json j = jArray[0];
    int afterLen = jArray.length();
    checkpanic testDB.stop();

    return [beforeLen, afterLen];
}

function getJsonConversionResult(table<record {}> | error tableOrError) returns json {
    json retVal = {};
    if (tableOrError is table<record {}>) {
        var jsonConversionResult = jsonutils:fromTable(tableOrError);
        // Converting to string to make sure the json is built before returning.
        string data = io:sprintf("%s", jsonConversionResult);
        retVal = jsonConversionResult;
    } else {
        retVal = {"Error": <string>tableOrError.detail()["message"]};
    }
    return retVal;
}

function getXMLConversionResult(table<record {}> | error tableOrError) returns xml {
    xml retVal = xml `<Error/>`;
    if (tableOrError is table<record {}>) {
        var xmlConversionResult = xmlutils:fromTable(tableOrError);
        // Converting to string to make sure the xml is built before returning.
        _ = io:sprintf("%s", xmlConversionResult);
        retVal = xmlConversionResult;
    } else {
        string errorXML = <string>tableOrError.detail()["message"];
        retVal = xml `<Error>{{errorXML}}</Error>`;
    }
    return retVal;
}

function testSelectQueryWithCursorTable(string jdbcURL) returns error? {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    table<IntData> t1 = check testDB->select("SELECT int_type from DataTable WHERE row_id = 1", IntData);
    error? e = trap testSelectQueryWithCursorTableHelper(t1);
    t1.close();
    checkpanic testDB.stop();
    return e;
}

function testSelectQueryWithCursorTableHelper(table<IntData> t1) {
    table<IntData> t1Copy = from t1
    select *;
}

function testJoinQueryWithCursorTable(string jdbcURL) returns error? {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 2}
    });

    table<IntData> t1 = check testDB->select("SELECT int_type from DataTable WHERE row_id = 1", IntData);
    table<IntData> t2 = check testDB->select("SELECT int_type from DataTable WHERE row_id = 1", IntData);

    error? e = trap testJoinQueryWithCursorTableHelper(t1, t2);
    t1.close();
    t2.close();
    checkpanic testDB.stop();
    return e;
}

function testTypeCheckingConstrainedCursorTableWithClosedConstraint(string jdbcURL) returns @tainted [int, int, float,
    float, boolean, string] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    int i = -1;
    int l = -1;
    float f = -1;
    float d = -1;
    boolean b = false;
    string s = "";
    var dtRet = testDB->select("SELECT int_type, long_type, float_type, double_type," +
                   "boolean_type, string_type from DataTable WHERE row_id = 1", ResultClosed);
    if (dtRet is table<ResultClosed>) {
        while (dtRet.hasNext()) {
            var rs = dtRet.getNext();
            if (rs is ResultClosed) {
                i = rs.INT_TYPE;
                l = rs.LONG_TYPE;
                f = rs.FLOAT_TYPE;
                d = rs.DOUBLE_TYPE;
                b = rs.BOOLEAN_TYPE;
                s = rs.STRING_TYPE;
            }
        }
    }
    checkpanic testDB.stop();
    return [i, l, f, d, b, s];
}

function testJoinQueryWithCursorTableHelper(table<IntData> t1, table<IntData> t2) {
    table<IntData> joinedTable = from t1 as table1
    join t2 as table2 on
    table1.int_type == table2.int_type
    select table1.int_type as int_type;
}

function testAssignStringValueToJsonField(string jdbcURL) returns @tainted json {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var result = testDB->select("SELECT string_type from DataTable WHERE row_id = 1", ResultJson);
    json val = "";
    if (result is table<ResultJson>) {
        foreach var rs in result {
            val = rs.STRING_TYPE;
        }
    }
    checkpanic testDB.stop();
    return val;
}

type Order record {
    int id;
    string name;
};

function testForEachInTableWithStmt(string jdbcURL) returns @tainted [int, int, float, string] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var dt = testDB->select("SELECT * from Person where id = 1", Person);

    int id = -1;
    int age = -1;
    float salary = -1;
    string name = "";

    if (dt is table<Person>) {
        foreach var x in dt {
            id = x.id;
            age = x.age;
            salary = x.salary;
            name = x.name;
        }
    }
    checkpanic testDB.stop();
    return [id, age, salary, name];
}

function testForEachInTableWithIndex(string jdbcURL) returns @tainted [string, string] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var dt = testDB->select("SELECT * from Person where id < 10 order by id", Person);

    string indexStr = "";
    string idStr = "";
    if (dt is table<Person>) {
        int i = 0;
        foreach var x in dt {
            indexStr = indexStr + "," + i.toString();
            idStr = idStr + "," + x.id.toString();
            i += 1;
        }
    }
    checkpanic testDB.stop();
    return [idStr, indexStr];
}

function testForEachInTable(string jdbcURL) returns [int, int, float, string] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var dt = testDB->select("SELECT * from Person where id = 1", Person);

    int idValue = -1;
    int ageValue = -1;
    float salValue = -1.0;
    string nameValue = "";

    if (dt is table<Person>) {
        foreach Person p in dt {
            idValue = <@untainted>p.id;
            ageValue = <@untainted>p.age;
            salValue = <@untainted>p.salary;
            nameValue = <@untainted>p.name;
        }
    }
    int id = idValue;
    int age = ageValue;
    float salary = salValue;
    string name = nameValue;
    checkpanic testDB.stop();
    return [id, age, salary, name];
}
