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

import ballerina/jdbc;
import ballerina/sql;
import ballerina/time;

type ResultDatesWithNillableStringType {
    string? DATE_TYPE,
    string? TIME_TYPE,
    string? TIMESTAMP_TYPE,
    string? DATETIME_TYPE,
};

type ResultDatesWithTimeType {
    time:Time DATE_TYPE,
    time:Time TIME_TYPE,
    time:Time TIMESTAMP_TYPE,
    time:Time DATETIME_TYPE,
};

type ResultDatesWithNillableTimeType {
    time:Time? DATE_TYPE;
    time:Time? TIME_TYPE;
    time:Time? TIMESTAMP_TYPE;
    time:Time? DATETIME_TYPE;
};

type ResultDatesWithIntType {
    int DATE_TYPE,
    int TIME_TYPE,
    int TIMESTAMP_TYPE,
    int DATETIME_TYPE,
};

type ResultDatesWithNillableIntType {
    int? DATE_TYPE,
    int? TIME_TYPE,
    int? TIMESTAMP_TYPE,
    int? DATETIME_TYPE,
};

type NillableDataTypes {
    int? int_type,
    int? long_type,
    float? float_type,
    float? double_type,
    boolean? boolean_type,
    string? string_type,
    float? numeric_type,
    float? decimal_type,
    float? real_type,
    int? tinyint_type,
    int? smallint_type,
    string? clob_type,
    blob? blob_type,
    blob? binary_type,
    time:Time? date_type,
    time:Time? time_type,
    time:Time? datetime_type,
    time:Time? timestamp_type,
};

type ResultMapNonNillableTypeNillableElements {
    int?[] INT_ARRAY;
    int?[] LONG_ARRAY;
    float?[] FLOAT_ARRAY;
    boolean?[] BOOLEAN_ARRAY;
    string?[] STRING_ARRAY;
};

type ResultMapNillable {
    int?[]? INT_ARRAY;
    int?[]? LONG_ARRAY;
    float?[]? FLOAT_ARRAY;
    boolean?[]? BOOLEAN_ARRAY;
    string?[]? STRING_ARRAY;
};

type ResultMapNillableTypeNonNillableElements {
    int[]? INT_ARRAY;
    int[]? LONG_ARRAY;
    float[]? FLOAT_ARRAY;
    boolean[]? BOOLEAN_ARRAY;
    string[]? STRING_ARRAY;
};

function testMappingToNillableTypeFields() returns (int?, int?, float?, float?, boolean?, string?, float?, float?,
        float?, int?, int?, string?, blob?, blob?) {
    endpoint jdbc:Client testDB {
        url: "jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: { maximumPoolSize: 1 }
    };

    table<NillableDataTypes> dt = check testDB->select("SELECT int_type, long_type, float_type, double_type,
    boolean_type, string_type, numeric_type, decimal_type, real_type, tinyint_type, smallint_type, clob_type,
    blob_type, binary_type from DataTypeTableNillable where
    row_id=1", NillableDataTypes);

    int? int_type;
    int? long_type;
    float? float_type;
    float? double_type;
    boolean? boolean_type;
    string? string_type;
    float? numeric_type;
    float? decimal_type;
    float? real_type;
    int? tinyint_type;
    int? smallint_type;
    string? clob_type;
    blob? blob_type;
    blob? binary_type;

    while (dt.hasNext()) {
        NillableDataTypes rs = check <NillableDataTypes>dt.getNext();
        int_type = rs.int_type;
        long_type = rs.long_type;
        float_type = rs.float_type;
        double_type = rs.double_type;
        boolean_type = rs.boolean_type;
        string_type = rs.string_type;
        numeric_type = rs.numeric_type;
        decimal_type = rs.decimal_type;
        real_type = rs.real_type;
        tinyint_type = rs.tinyint_type;
        smallint_type = rs.smallint_type;
        clob_type = rs.clob_type;
        blob_type = rs.blob_type;
        binary_type = rs.binary_type;
    }
    testDB.stop();
    return (int_type, long_type, float_type, double_type,
    boolean_type, string_type,
    numeric_type, decimal_type, real_type, tinyint_type, smallint_type, clob_type, blob_type, binary_type);
}

function testMappingDatesToNillableTimeType() returns (int, int, int, int, int, int, int, int) {
    endpoint jdbc:Client testDB {
        url: "jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: { maximumPoolSize: 1 }
    };

    int dateInserted;
    int dateRetrieved;
    int timeInserted;
    int timeRetrieved;
    int timestampInserted;
    int timestampRetrieved;
    int datetimeInserted;
    int datetimeRetrieved;

    time:Time dateStruct = time:createTime(2017, 5, 23, 0, 0, 0, 0, "");

    time:Timezone zoneValue = { zoneId: "UTC" };
    time:Time timeStruct = new(51323000, zoneValue);

    time:Time timestampStruct = time:createTime(2017, 1, 25, 16, 12, 23, 0, "UTC");
    time:Time datetimeStruct = time:createTime(2017, 1, 31, 16, 12, 23, 332, "UTC");
    dateInserted = dateStruct.time;
    timeInserted = timeStruct.time;
    timestampInserted = timestampStruct.time;
    datetimeInserted = datetimeStruct.time;

    sql:Parameter para0 = { sqlType: sql:TYPE_INTEGER, value: 150 };
    sql:Parameter para1 = { sqlType: sql:TYPE_DATE, value: dateStruct };
    sql:Parameter para2 = { sqlType: sql:TYPE_TIME, value: timeStruct };
    sql:Parameter para3 = { sqlType: sql:TYPE_TIMESTAMP, value: timestampStruct };
    sql:Parameter para4 = { sqlType: sql:TYPE_DATETIME, value: datetimeStruct };

    int count = check testDB->update("Insert into DateTimeTypes
        (row_id, date_type, time_type, timestamp_type, datetime_type) values (?,?,?,?,?)",
        para0, para1, para2, para3, para4);

    table dt = check testDB->select("SELECT date_type, time_type, timestamp_type, datetime_type
                from DateTimeTypes where row_id = 150", ResultDatesWithNillableTimeType);

    while (dt.hasNext()) {
        ResultDatesWithNillableTimeType rs = check <ResultDatesWithNillableTimeType>dt.getNext();
        dateRetrieved = rs.DATE_TYPE.time but { () => -1 };
        timeRetrieved = rs.TIME_TYPE.time but { () => -1 };
        timestampRetrieved = rs.TIMESTAMP_TYPE.time but { () => -1 };
        datetimeRetrieved = rs.DATETIME_TYPE.time but { () => -1 };
    }
    testDB.stop();
    return (dateInserted, dateRetrieved, timeInserted, timeRetrieved, timestampInserted, timestampRetrieved,
    datetimeInserted, datetimeRetrieved);
}

function testMappingDatesToNillableIntType(int datein, int timein, int timestampin) returns (int, int, int, int) {
    endpoint jdbc:Client testDB {
        url: "jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: { maximumPoolSize: 1 }
    };

    sql:Parameter para0 = { sqlType: sql:TYPE_INTEGER, value: 151 };
    sql:Parameter para1 = { sqlType: sql:TYPE_DATE, value: datein };
    sql:Parameter para2 = { sqlType: sql:TYPE_TIME, value: timein };
    sql:Parameter para3 = { sqlType: sql:TYPE_TIMESTAMP, value: timestampin };
    sql:Parameter para4 = { sqlType: sql:TYPE_DATETIME, value: timestampin };

    int date;
    int time;
    int timestamp;
    int datetime;

    int countt = check testDB->update("Insert into DateTimeTypes
        (row_id, date_type, time_type, timestamp_type, datetime_type) values (?,?,?,?,?)",
        para0, para1, para2, para3, para4);

    table<ResultDatesWithNillableIntType> dt = check testDB->select("SELECT date_type, time_type, timestamp_type,
    datetime_type from DateTimeTypes where row_id = 151", ResultDatesWithNillableIntType);

    while (dt.hasNext()) {
        ResultDatesWithNillableIntType rs = check <ResultDatesWithNillableIntType>dt.getNext();
        time = rs.TIME_TYPE but { () => -1 };
        date = rs.DATE_TYPE but { () => -1 };
        timestamp = rs.TIMESTAMP_TYPE but { () => -1 };
        datetime = rs.DATETIME_TYPE but { () => -1 };
    }
    testDB.stop();
    return (date, time, timestamp, datetime);
}

function testMappingDatesToNillableStringType(int datein, int timein, int timestampin) returns (string, string, string,
        string) {
    endpoint jdbc:Client testDB {
        url: "jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: { maximumPoolSize: 1 }
    };
    string date;
    string time;
    string timestamp;
    string datetime;

    sql:Parameter para0 = { sqlType: sql:TYPE_INTEGER, value: 152 };
    sql:Parameter para1 = { sqlType: sql:TYPE_DATE, value: datein };
    sql:Parameter para2 = { sqlType: sql:TYPE_TIME, value: timein };
    sql:Parameter para3 = { sqlType: sql:TYPE_TIMESTAMP, value: timestampin };
    sql:Parameter para4 = { sqlType: sql:TYPE_DATETIME, value: timestampin };

    int countRet = check testDB->update("Insert into DateTimeTypes
        (row_id, date_type, time_type, timestamp_type, datetime_type) values (?,?,?,?,?)",
        para0, para1, para2, para3, para4);

    table dt = check testDB->select("SELECT date_type, time_type, timestamp_type, datetime_type
                from DateTimeTypes where row_id = 152", ResultDatesWithNillableStringType);

    while (dt.hasNext()) {
        ResultDatesWithNillableStringType rs = check <ResultDatesWithNillableStringType>dt.getNext();
        time = rs.TIME_TYPE but { () => "nil" };
        date = rs.DATE_TYPE but { () => "nil" };
        timestamp = rs.TIMESTAMP_TYPE but { () => "nil" };
        datetime = rs.DATETIME_TYPE but { () => "nil" };
    }
    testDB.stop();
    return (date, time, timestamp, datetime);
}

function testMappingNullToNillableTypes() returns (int?, int?, float?, float?, boolean?, string?, float?, float?,
        float?, int?, int?, string?, blob?, blob?, time:Time?, time:Time?, time:Time?, time:Time?) {
    endpoint jdbc:Client testDB {
        url: "jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: { maximumPoolSize: 1 }
    };
    table<NillableDataTypes> dt = check testDB->select("SELECT int_type, long_type, float_type, double_type,
    boolean_type, string_type, numeric_type, decimal_type, real_type, tinyint_type, smallint_type, clob_type,
    blob_type, binary_type, date_type, time_type, datetime_type, timestamp_type from DataTypeTableNillable where
    row_id=2", NillableDataTypes);

    int? int_type;
    int? long_type;
    float? float_type;
    float? double_type;
    boolean? boolean_type;
    string? string_type;
    float? numeric_type;
    float? decimal_type;
    float? real_type;
    int? tinyint_type;
    int? smallint_type;
    string? clob_type;
    blob? blob_type;
    blob? binary_type;
    time:Time? date_type;
    time:Time? time_type;
    time:Time? datetime_type;
    time:Time? timestamp_type;

    while (dt.hasNext()) {
        NillableDataTypes rs = check <NillableDataTypes>dt.getNext();
        int_type = rs.int_type;
        long_type = rs.long_type;
        float_type = rs.float_type;
        double_type = rs.double_type;
        boolean_type = rs.boolean_type;
        string_type = rs.string_type;
        numeric_type = rs.numeric_type;
        decimal_type = rs.decimal_type;
        real_type = rs.real_type;
        tinyint_type = rs.tinyint_type;
        smallint_type = rs.smallint_type;
        clob_type = rs.clob_type;
        blob_type = rs.blob_type;
        binary_type = rs.binary_type;
        date_type = rs.date_type;
        time_type = rs.time_type;
        datetime_type = rs.datetime_type;
        timestamp_type = rs.timestamp_type;
    }
    testDB.stop();
    return (int_type, long_type, float_type, double_type, boolean_type, string_type, numeric_type, decimal_type,
    real_type, tinyint_type, smallint_type, clob_type, blob_type, binary_type, date_type, time_type, datetime_type,
    timestamp_type);
}

function testMapArrayToNonNillableTypeWithNillableElementType() returns (int?[], int?[], float?[], string?[],
            boolean?[]) {
    endpoint jdbc:Client testDB {
        url: "jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: { maximumPoolSize: 1 }
    };

    table dt = check testDB->select("SELECT int_array, long_array, float_array, boolean_array,
              string_array from ArrayTypes where row_id = 1", ResultMapNonNillableTypeNillableElements);

    int?[] int_arr;
    int?[] long_arr;
    float?[] float_arr;
    string?[] string_arr;
    boolean?[] boolean_arr;

    while (dt.hasNext()) {
        ResultMapNonNillableTypeNillableElements rs = check <ResultMapNonNillableTypeNillableElements>dt.getNext();
        int_arr = rs.INT_ARRAY;
        long_arr = rs.LONG_ARRAY;
        float_arr = rs.FLOAT_ARRAY;
        boolean_arr = rs.BOOLEAN_ARRAY;
        string_arr = rs.STRING_ARRAY;
    }
    testDB.stop();
    return (int_arr, long_arr, float_arr, string_arr, boolean_arr);
}

function testMapArrayToNillableTypeWithNillableElementType() returns (int?[]?, int?[]?, float?[]?, string?[]?,
            boolean?[]?) {
    endpoint jdbc:Client testDB {
        url: "jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: { maximumPoolSize: 1 }
    };

    table dt = check testDB->select("SELECT int_array, long_array, float_array, boolean_array,
              string_array from ArrayTypes where row_id = 1", ResultMapNillable);

    int?[]? int_arr;
    int?[]? long_arr;
    float?[]? float_arr;
    string?[]? string_arr;
    boolean?[]? boolean_arr;

    while (dt.hasNext()) {
        ResultMapNillable rs = check <ResultMapNillable>dt.getNext();
        int_arr = rs.INT_ARRAY;
        long_arr = rs.LONG_ARRAY;
        float_arr = rs.FLOAT_ARRAY;
        boolean_arr = rs.BOOLEAN_ARRAY;
        string_arr = rs.STRING_ARRAY;
    }
    testDB.stop();
    return (int_arr, long_arr, float_arr, string_arr, boolean_arr);
}

function testMapArrayToNillableTypeWithNonNillableElementType() returns (int[]?, int[]?, float[]?, string[]?,
            boolean[]?) {
    endpoint jdbc:Client testDB {
        url: "jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: { maximumPoolSize: 1 }
    };

    table dt = check testDB->select("SELECT int_array, long_array, float_array, boolean_array,
              string_array from ArrayTypes where row_id = 1", ResultMapNillableTypeNonNillableElements);

    int[]? int_arr;
    int[]? long_arr;
    float[]? float_arr;
    string[]? string_arr;
    boolean[]? boolean_arr;

    while (dt.hasNext()) {
        ResultMapNillableTypeNonNillableElements rs = check <ResultMapNillableTypeNonNillableElements>dt.getNext();
        int_arr = rs.INT_ARRAY;
        long_arr = rs.LONG_ARRAY;
        float_arr = rs.FLOAT_ARRAY;
        boolean_arr = rs.BOOLEAN_ARRAY;
        string_arr = rs.STRING_ARRAY;
    }
    testDB.stop();
    return (int_arr, long_arr, float_arr, string_arr, boolean_arr);
}

function testMapNillIncludedArrayNonNillableTypeWithNillableElementType() returns (int?[], int?[], float?[], string?[],
            boolean?[]) {
    endpoint jdbc:Client testDB {
        url: "jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: { maximumPoolSize: 1 }
    };

    table dt = check testDB->select("SELECT int_array, long_array, float_array, boolean_array,
              string_array from ArrayTypes where row_id = 2", ResultMapNonNillableTypeNillableElements);

    int?[] int_arr;
    int?[] long_arr;
    float?[] float_arr;
    string?[] string_arr;
    boolean?[] boolean_arr;

    while (dt.hasNext()) {
        ResultMapNonNillableTypeNillableElements rs = check <ResultMapNonNillableTypeNillableElements>dt.getNext();
        int_arr = rs.INT_ARRAY;
        long_arr = rs.LONG_ARRAY;
        float_arr = rs.FLOAT_ARRAY;
        boolean_arr = rs.BOOLEAN_ARRAY;
        string_arr = rs.STRING_ARRAY;
    }
    testDB.stop();
    return (int_arr, long_arr, float_arr, string_arr, boolean_arr);
}

function testMapNillIncludedArrayNillableTypeWithNillableElementType() returns (int?[]?, int?[]?, float?[]?, string?[]?,
            boolean?[]?) {
    endpoint jdbc:Client testDB {
        url: "jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: { maximumPoolSize: 1 }
    };

    table dt = check testDB->select("SELECT int_array, long_array, float_array, boolean_array,
              string_array from ArrayTypes where row_id = 2", ResultMapNillable);

    int?[]? int_arr;
    int?[]? long_arr;
    float?[]? float_arr;
    string?[]? string_arr;
    boolean?[]? boolean_arr;

    while (dt.hasNext()) {
        ResultMapNillable rs = check <ResultMapNillable>dt.getNext();
        int_arr = rs.INT_ARRAY;
        long_arr = rs.LONG_ARRAY;
        float_arr = rs.FLOAT_ARRAY;
        boolean_arr = rs.BOOLEAN_ARRAY;
        string_arr = rs.STRING_ARRAY;
    }
    testDB.stop();
    return (int_arr, long_arr, float_arr, string_arr, boolean_arr);
}

function testMapNilArrayToNillableTypeWithNonNillableElementTypes() returns (int[]?, int[]?, float[]?, string[]?,
            boolean[]?) {
    endpoint jdbc:Client testDB {
        url: "jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: { maximumPoolSize: 1 }
    };

    table dt = check testDB->select("SELECT int_array, long_array, float_array, boolean_array,
              string_array from ArrayTypes where row_id = 3", ResultMapNillableTypeNonNillableElements);

    int[]? int_arr;
    int[]? long_arr;
    float[]? float_arr;
    string[]? string_arr;
    boolean[]? boolean_arr;

    while (dt.hasNext()) {
        ResultMapNillableTypeNonNillableElements rs = check <ResultMapNillableTypeNonNillableElements>dt.getNext();
        int_arr = rs.INT_ARRAY;
        long_arr = rs.LONG_ARRAY;
        float_arr = rs.FLOAT_ARRAY;
        boolean_arr = rs.BOOLEAN_ARRAY;
        string_arr = rs.STRING_ARRAY;
    }
    testDB.stop();
    return (int_arr, long_arr, float_arr, string_arr, boolean_arr);
}

function testMapNilArrayToNillableTypeWithNillableElementTypes() returns (int?[]?, int?[]?, float?[]?, string?[]?,
            boolean?[]?) {
    endpoint jdbc:Client testDB {
        url: "jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: { maximumPoolSize: 1 }
    };

    table dt = check testDB->select("SELECT int_array, long_array, float_array, boolean_array,
              string_array from ArrayTypes where row_id = 3", ResultMapNillable);

    int?[]? int_arr;
    int?[]? long_arr;
    float?[]? float_arr;
    string?[]? string_arr;
    boolean?[]? boolean_arr;

    while (dt.hasNext()) {
        ResultMapNillable rs = check <ResultMapNillable>dt.getNext();
        int_arr = rs.INT_ARRAY;
        long_arr = rs.LONG_ARRAY;
        float_arr = rs.FLOAT_ARRAY;
        boolean_arr = rs.BOOLEAN_ARRAY;
        string_arr = rs.STRING_ARRAY;
    }
    testDB.stop();
    return (int_arr, long_arr, float_arr, string_arr, boolean_arr);
}

function testMapNillElementsOnlyArray() returns (int?[], int?[], float?[], string?[], boolean?[]) {
    endpoint jdbc:Client testDB {
        url: "jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: { maximumPoolSize: 1 }
    };

    table dt = check testDB->select("SELECT int_array, long_array, float_array, boolean_array,
              string_array from ArrayTypes where row_id = 5", ResultMapNonNillableTypeNillableElements);

    int?[] int_arr;
    int?[] long_arr;
    float?[] float_arr;
    string?[] string_arr;
    boolean?[] boolean_arr;

    while (dt.hasNext()) {
        ResultMapNonNillableTypeNillableElements rs = check <ResultMapNonNillableTypeNillableElements>dt.getNext();
        int_arr = rs.INT_ARRAY;
        long_arr = rs.LONG_ARRAY;
        float_arr = rs.FLOAT_ARRAY;
        boolean_arr = rs.BOOLEAN_ARRAY;
        string_arr = rs.STRING_ARRAY;
    }
    testDB.stop();
    return (int_arr, long_arr, float_arr, string_arr, boolean_arr);
}
