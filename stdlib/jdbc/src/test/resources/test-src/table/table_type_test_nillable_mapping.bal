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

import ballerina/time;
import ballerinax/java.jdbc;

type ResultDatesWithNillableStringType record {
    string? DATE_TYPE;
    string? TIME_TYPE;
    string? TIMESTAMP_TYPE;
    string? DATETIME_TYPE;
};

type ResultDatesWithTimeType record {
    time:Time DATE_TYPE;
    time:Time TIME_TYPE;
    time:Time TIMESTAMP_TYPE;
    time:Time DATETIME_TYPE;
};

type ResultDatesWithNillableTimeType record {
    time:Time? DATE_TYPE;
    time:Time? TIME_TYPE;
    time:Time? TIMESTAMP_TYPE;
    time:Time? DATETIME_TYPE;
};

type ResultDatesWithIntType record {
    int DATE_TYPE;
    int TIME_TYPE;
    int TIMESTAMP_TYPE;
    int DATETIME_TYPE;
};

type ResultDatesWithNillableIntType record {
    int? DATE_TYPE;
    int? TIME_TYPE;
    int? TIMESTAMP_TYPE;
    int? DATETIME_TYPE;
};

type NillableDataTypesAll record {
    int? int_type;
    int? long_type;
    float? float_type;
    float? double_type;
    boolean? boolean_type;
    string? string_type;
    decimal? numeric_type;
    decimal? decimal_type;
    float? real_type;
    int? tinyint_type;
    int? smallint_type;
    string? clob_type;
    byte[]? binary_type;
    time:Time? date_type;
    time:Time? time_type;
    time:Time? datetime_type;
    time:Time? timestamp_type;
};

type NillableDataTypes record {
    int? int_type;
    int? long_type;
    float? float_type;
    float? double_type;
    boolean? boolean_type;
    string? string_type;
    decimal? numeric_type;
    decimal? decimal_type;
    float? real_type;
    int? tinyint_type;
    int? smallint_type;
    string? clob_type;
    byte[]? binary_type;
};

type NillableBlob record {
    byte[]? blob_type;
};

type ResultMapNonNillableTypeNillableElements record {
    int?[] INT_ARRAY;
    int?[] LONG_ARRAY;
    decimal?[] FLOAT_ARRAY;
    boolean?[] BOOLEAN_ARRAY;
    string?[] STRING_ARRAY;
};

type ResultMapNillable record {
    int?[]? INT_ARRAY;
    int?[]? LONG_ARRAY;
    decimal?[]? FLOAT_ARRAY;
    boolean?[]? BOOLEAN_ARRAY;
    string?[]? STRING_ARRAY;
};

type ResultMapNillableTypeNonNillableElements record {
    int[]? INT_ARRAY;
    int[]? LONG_ARRAY;
    decimal[]? FLOAT_ARRAY;
    boolean[]? BOOLEAN_ARRAY;
    string[]? STRING_ARRAY;
};

function testMappingToNillableTypeFields(string jdbcURL) returns @tainted [int?, int?, float?,
            float?, boolean?, string?, decimal?, decimal?, float?, int?, int?, string?, byte[]?] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var dt = testDB->select("SELECT int_type, long_type, float_type, double_type," +
        "boolean_type, string_type, numeric_type, decimal_type, real_type, tinyint_type, smallint_type, clob_type," +
        "binary_type from DataTypeTableNillable where row_id=1", NillableDataTypes);

    int? int_type = ();
    int? long_type = ();
    float? float_type = ();
    float? double_type = ();
    boolean? boolean_type = ();
    string? string_type = ();
    decimal? numeric_type = ();
    decimal? decimal_type = ();
    float? real_type = ();
    int? tinyint_type = ();
    int? smallint_type = ();
    string? clob_type = ();
    byte[]? binary_type = ();

    if (dt is table<NillableDataTypes>) {
        while (dt.hasNext()) {
            var rs = <NillableDataTypes>dt.getNext();
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
            binary_type = rs.binary_type;
        }
    }
    checkpanic testDB.stop();
    return [int_type, long_type, float_type, double_type,
    boolean_type, string_type,
    numeric_type, decimal_type, real_type, tinyint_type, smallint_type, clob_type, binary_type];
}

function testMappingToNillableTypeFieldsBlob(string jdbcURL) returns @tainted byte[]? {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });
    byte[]? blob_type = ();
    var dt = testDB->select("SELECT blob_type from DataTypeTableNillableBlob where row_id=3", NillableBlob);
    if (dt is table<NillableBlob>) {
        while (dt.hasNext()) {
            var rs = dt.getNext();
            if (rs is NillableBlob) {
                blob_type = rs.blob_type;
            }
        }
    }
    checkpanic testDB.stop();
    return blob_type;
}

function testMappingDatesToNillableTimeType(string jdbcURL) returns @tainted [int, int, int, int, int, int, int, int] | error {
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

    jdbc:Parameter para0 = {sqlType: jdbc:TYPE_INTEGER, value: 150};
    jdbc:Parameter para1 = {sqlType: jdbc:TYPE_DATE, value: dateStruct};
    jdbc:Parameter para2 = {sqlType: jdbc:TYPE_TIME, value: timeStruct};
    jdbc:Parameter para3 = {sqlType: jdbc:TYPE_TIMESTAMP, value: timestampStruct};
    jdbc:Parameter para4 = {sqlType: jdbc:TYPE_DATETIME, value: datetimeStruct};

    _ = checkpanic testDB->update("Insert into DateTimeTypes " +
        "(row_id, date_type, time_type, timestamp_type, datetime_type) values (?,?,?,?,?)",
        para0, para1, para2, para3, para4);
    var dt = testDB->select("SELECT date_type, time_type, timestamp_type, datetime_type " +
                "from DateTimeTypes where row_id = 150", ResultDatesWithNillableTimeType);

    if (dt is table<ResultDatesWithNillableTimeType>) {
        while (dt.hasNext()) {
            var rs = dt.getNext();
            if (rs is ResultDatesWithNillableTimeType) {
                dateRetrieved = getTimeIntFromTimeRecord(rs.DATE_TYPE);
                timeRetrieved = getTimeIntFromTimeRecord(rs.TIME_TYPE);
                timestampRetrieved = getTimeIntFromTimeRecord(rs.TIMESTAMP_TYPE);
                datetimeRetrieved = getTimeIntFromTimeRecord(rs.DATETIME_TYPE);
            }
        }
    }
    checkpanic testDB.stop();
    return [dateInserted, dateRetrieved, timeInserted, timeRetrieved, timestampInserted, timestampRetrieved,
    datetimeInserted, datetimeRetrieved];
}

function getTimeIntFromTimeRecord(time:Time? timeRec) returns int {
    if (timeRec is time:Time) {
        return timeRec.time;
    }
    return -1;
}

function testMappingDatesToNillableIntType(int datein, int timein, int timestampin, string jdbcURL) returns
    @tainted [int, int, int, int] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    jdbc:Parameter para0 = {sqlType: jdbc:TYPE_INTEGER, value: 151};
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

    var dt = testDB->select("SELECT date_type, time_type, timestamp_type, " +
    "datetime_type from DateTimeTypes where row_id = 151", ResultDatesWithNillableIntType);

    if (dt is table<ResultDatesWithNillableIntType>) {
        while (dt.hasNext()) {
            var rs = dt.getNext();
            if (rs is ResultDatesWithNillableIntType) {
                time = rs.TIME_TYPE ?: -1;
                date = rs.DATE_TYPE ?: -1;
                timestamp = rs.TIMESTAMP_TYPE ?: -1;
                datetime = rs.DATETIME_TYPE ?: -1;
            }
        }
    }
    checkpanic testDB.stop();
    return [date, time, timestamp, datetime];
}

function testMappingDatesToNillableStringType(int datein, int timein, int timestampin, string jdbcURL) returns
    @tainted [string, string, string, string] {
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

    jdbc:Parameter para0 = {sqlType: jdbc:TYPE_INTEGER, value: 152};
    jdbc:Parameter para1 = {sqlType: jdbc:TYPE_DATE, value: datein};
    jdbc:Parameter para2 = {sqlType: jdbc:TYPE_TIME, value: timein};
    jdbc:Parameter para3 = {sqlType: jdbc:TYPE_TIMESTAMP, value: timestampin};
    jdbc:Parameter para4 = {sqlType: jdbc:TYPE_DATETIME, value: timestampin};

    _ = checkpanic testDB->update("Insert into DateTimeTypes " +
        "(row_id, date_type, time_type, timestamp_type, datetime_type) values (?,?,?,?,?)",
        para0, para1, para2, para3, para4);

    var dt = testDB->select("SELECT date_type, time_type, timestamp_type, datetime_type " +
                "from DateTimeTypes where row_id = 152", ResultDatesWithNillableStringType);

    if (dt is table<ResultDatesWithNillableStringType>) {
        while (dt.hasNext()) {
            var rs = dt.getNext();
            if (rs is ResultDatesWithNillableStringType) {
                time = rs.TIME_TYPE ?: "nil";
                date = rs.DATE_TYPE ?: "nil";
                timestamp = rs.TIMESTAMP_TYPE ?: "nil";
                datetime = rs.DATETIME_TYPE ?: "nil";
            }
        }
    }
    checkpanic testDB.stop();
    return [date, time, timestamp, datetime];
}

function testMappingNullToNillableTypes(string jdbcURL) returns @tainted [int?, int?, float?,
    float?, boolean?, string?, decimal?, decimal?, float?, int?, int?, string?, byte[]?, time:Time?, time:Time?,
    time:Time?, time:Time?] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });
    var dt = testDB->select("SELECT int_type, long_type, float_type, double_type," +
    "boolean_type, string_type, numeric_type, decimal_type, real_type, tinyint_type, smallint_type, clob_type," +
    "binary_type, date_type, time_type, datetime_type, timestamp_type from DataTypeTableNillable where " +
    "row_id=2", NillableDataTypesAll);

    int? int_type = ();
    int? long_type = ();
    float? float_type = ();
    float? double_type = ();
    boolean? boolean_type = ();
    string? string_type = ();
    decimal? numeric_type = ();
    decimal? decimal_type = ();
    float? real_type = ();
    int? tinyint_type = ();
    int? smallint_type = ();
    string? clob_type = ();
    byte[]? binary_type = ();
    time:Time? date_type = ();
    time:Time? time_type = ();
    time:Time? datetime_type = ();
    time:Time? timestamp_type = ();

    if (dt is table<NillableDataTypesAll>) {
        while (dt.hasNext()) {
            var rs = dt.getNext();
            if (rs is NillableDataTypesAll) {
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
                binary_type = rs.binary_type;
                date_type = rs.date_type;
                time_type = rs.time_type;
                datetime_type = rs.datetime_type;
                timestamp_type = rs.timestamp_type;
            }
        }
    }
    checkpanic testDB.stop();
    return [int_type, long_type, float_type, double_type, boolean_type, string_type, numeric_type, decimal_type,
    real_type, tinyint_type, smallint_type, clob_type, binary_type, date_type, time_type, datetime_type,
    timestamp_type];
}

function testMappingNullToNillableTypesBlob(string jdbcURL) returns @tainted byte[]? {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });
    var dt = testDB->select("SELECT blob_type from DataTypeTableNillableBlob where row_id=4", NillableBlob);

    byte[]? blob_type = ();

    if (dt is table<NillableBlob>) {
        while (dt.hasNext()) {
            var rs = dt.getNext();
            if (rs is NillableBlob) {
                blob_type = rs.blob_type;
            }
        }
    }
    checkpanic testDB.stop();
    return blob_type;
}

function testMapArrayToNonNillableTypeWithNillableElementType(string jdbcURL) returns
    @tainted [int?[], int?[], decimal?[], string?[], boolean?[]] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var dt = testDB->select("SELECT int_array, long_array, float_array, boolean_array," +
              "string_array from ArrayTypes where row_id = 1", ResultMapNonNillableTypeNillableElements);

    int?[] int_arr = [];
    int?[] long_arr = [];
    decimal?[] float_arr = [];
    string?[] string_arr = [];
    boolean?[] boolean_arr = [];

    if (dt is table<ResultMapNonNillableTypeNillableElements>) {
        while (dt.hasNext()) {
            var rs = dt.getNext();
            if (rs is ResultMapNonNillableTypeNillableElements) {
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

function testMapArrayToNillableTypeWithNillableElementType(string jdbcURL) returns
    @tainted [int?[]?, int?[]?, decimal?[]?, string?[]?, boolean?[]?] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var dt = testDB->select("SELECT int_array, long_array, float_array, boolean_array," +
              "string_array from ArrayTypes where row_id = 1", ResultMapNillable);

    int?[]? int_arr = ();
    int?[]? long_arr = ();
    decimal?[]? float_arr = ();
    string?[]? string_arr = ();
    boolean?[]? boolean_arr = ();

    if (dt is table<ResultMapNillable>) {
        while (dt.hasNext()) {
            var rs = dt.getNext();
            if (rs is ResultMapNillable) {
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

function testMapArrayToNillableTypeWithNonNillableElementType(string jdbcURL) returns
    @tainted [int[]?, int[]?, decimal[]?, string[]?, boolean[]?] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });
    var dt = testDB->select("SELECT int_array, long_array, float_array, boolean_array," +
              "string_array from ArrayTypes where row_id = 1", ResultMapNillableTypeNonNillableElements);

    int[]? int_arr = ();
    int[]? long_arr = ();
    decimal[]? float_arr = ();
    string[]? string_arr = ();
    boolean[]? boolean_arr = ();

    if (dt is table<ResultMapNillableTypeNonNillableElements>) {
        while (dt.hasNext()) {
            var rs = dt.getNext();
            if (rs is ResultMapNillableTypeNonNillableElements) {
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

function testMapNillIncludedArrayNonNillableTypeWithNillableElementType(string jdbcURL) returns
    @tainted [int?[], int?[], decimal?[], string?[], boolean?[]] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var dt = testDB->select("SELECT int_array, long_array, float_array, boolean_array," +
              "string_array from ArrayTypes where row_id = 2", ResultMapNonNillableTypeNillableElements);

    int?[] int_arr = [];
    int?[] long_arr = [];
    decimal?[] float_arr = [];
    string?[] string_arr = [];
    boolean?[] boolean_arr = [];

    if (dt is table<ResultMapNonNillableTypeNillableElements>) {
        while (dt.hasNext()) {
            var rs = dt.getNext();
            if (rs is ResultMapNonNillableTypeNillableElements) {
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

function testMapNillIncludedArrayNillableTypeWithNillableElementType(string jdbcURL) returns
    @tainted [int?[]?, int?[]?, decimal?[]?, string?[]?, boolean?[]?] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var dt = testDB->select("SELECT int_array, long_array, float_array, boolean_array," +
              "string_array from ArrayTypes where row_id = 2", ResultMapNillable);

    int?[]? int_arr = ();
    int?[]? long_arr = ();
    decimal?[]? float_arr = ();
    string?[]? string_arr = ();
    boolean?[]? boolean_arr = ();

    if (dt is table<ResultMapNillable>) {
        while (dt.hasNext()) {
            var rs = dt.getNext();
            if (rs is ResultMapNillable) {
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

function testMapNilArrayToNillableTypeWithNonNillableElementTypes(string jdbcURL) returns
    @tainted [int[]?, int[]?, decimal[]?, string[]?, boolean[]?] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var dt = testDB->select("SELECT int_array, long_array, float_array, boolean_array," +
              "string_array from ArrayTypes where row_id = 3", ResultMapNillableTypeNonNillableElements);

    int[]? int_arr = ();
    int[]? long_arr = ();
    decimal[]? float_arr = ();
    string[]? string_arr = ();
    boolean[]? boolean_arr = ();

    if (dt is table<ResultMapNillableTypeNonNillableElements>) {
        while (dt.hasNext()) {
            var rs = dt.getNext();
            if (rs is ResultMapNillableTypeNonNillableElements) {
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

function testMapNilArrayToNillableTypeWithNillableElementTypes(string jdbcURL) returns
    @tainted [int?[]?, int?[]?, decimal?[]?, string?[]?, boolean?[]?] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var dt = testDB->select("SELECT int_array, long_array, float_array, boolean_array," +
              "string_array from ArrayTypes where row_id = 3", ResultMapNillable);

    int?[]? int_arr = ();
    int?[]? long_arr = ();
    decimal?[]? float_arr = ();
    string?[]? string_arr = ();
    boolean?[]? boolean_arr = ();

    if (dt is table<ResultMapNillable>) {
        while (dt.hasNext()) {
            var rs = dt.getNext();
            if (rs is ResultMapNillable) {
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

function testMapNillElementsOnlyArray(string jdbcURL) returns
    @tainted [int?[], int?[], decimal?[], string?[], boolean?[]] {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var dt = testDB->select("SELECT int_array, long_array, float_array, boolean_array," +
              "string_array from ArrayTypes where row_id = 5", ResultMapNonNillableTypeNillableElements);

    int?[] int_arr = [];
    int?[] long_arr = [];
    decimal?[] float_arr = [];
    string?[] string_arr = [];
    boolean?[] boolean_arr = [];

    if (dt is table<ResultMapNonNillableTypeNillableElements>) {
        while (dt.hasNext()) {
            var rs = dt.getNext();
            if (rs is ResultMapNonNillableTypeNillableElements) {
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
