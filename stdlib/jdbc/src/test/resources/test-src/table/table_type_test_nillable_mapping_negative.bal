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

type NonNillableInt record {
    int val;
};

type NonNillableLong record {
    int val;
};

type NonNillableFloat record {
    float val;
};

type NonNillableDouble record {
    float val;
};

type NonNillableBoolean record {
    boolean val;
};

type NonNillableString record {
    string val;
};

type NonNillableNumeric record {
    float val;
};

type NonNillableTinyInt record {
    int val;
};

type NonNillableSmallInt record {
    int val;
};

type NonNillableDecimal record {
    float val;
};

type NonNillableReal record {
    float val;
};

type NonNillableClob record {
    string val;
};

type NonNillableBlob record {
    byte[] val;
};

type NonNillableBinary record {
    byte[] val;
};

type NonNillableDate record {
    time:Time val;
};

type NonNillableTime record {
    time:Time val;
};

type NonNillableDateTime record {
    time:Time val;
};

type NonNillableTimeStamp record {
    time:Time val;
};

type InvalidUnion record {
    (int | string) val;
};

type InvalidUnionArrayElement record {
    (int | string)?[] val;
};

type InvalidUnionArray record {
    int[] | string val;
};

type InvalidUnionArray2 record {
    string?[] val;
};

type ResultMap record {
    int[] INT_ARRAY;
    int[] LONG_ARRAY;
    float[] FLOAT_ARRAY;
    boolean[] BOOLEAN_ARRAY;
    string[] STRING_ARRAY;
};

type ResultMapNonNillableTypeNillableElements record {
    int?[] INT_ARRAY;
    int?[] LONG_ARRAY;
    float?[] FLOAT_ARRAY;
    boolean?[] BOOLEAN_ARRAY;
    string?[] STRING_ARRAY;
};

type ResultMapNillable record {
    int?[]? INT_ARRAY;
    int?[]? LONG_ARRAY;
    float?[]? FLOAT_ARRAY;
    boolean?[]? BOOLEAN_ARRAY;
    string?[]? STRING_ARRAY;
};

type ResultMapNillableTypeNonNillableElements record {
    int[]? INT_ARRAY;
    int[]? LONG_ARRAY;
    float[]? FLOAT_ARRAY;
    boolean[]? BOOLEAN_ARRAY;
    string[]? STRING_ARRAY;
};

function testAssignNilToNonNillableInt(string jdbcURL) returns @tainted string {
    return testAssignNilToNonNillableField("int_type", NonNillableInt, jdbcURL);
}

function testAssignNilToNonNillableLong(string jdbcURL) returns @tainted string {
    return testAssignNilToNonNillableField("long_type", NonNillableLong, jdbcURL);
}

function testAssignNilToNonNillableFloat(string jdbcURL) returns @tainted string {
    return testAssignNilToNonNillableField("float_type", NonNillableFloat, jdbcURL);
}

function testAssignNilToNonNillableDouble(string jdbcURL) returns @tainted string {
    return testAssignNilToNonNillableField("double_type", NonNillableDouble, jdbcURL);
}

function testAssignNilToNonNillableBoolean(string jdbcURL) returns @tainted string {
    return testAssignNilToNonNillableField("boolean_type", NonNillableBoolean, jdbcURL);
}

function testAssignNilToNonNillableString(string jdbcURL) returns @tainted string {
    return testAssignNilToNonNillableField("string_type", NonNillableString, jdbcURL);
}

function testAssignNilToNonNillableNumeric(string jdbcURL) returns @tainted string {
    return testAssignNilToNonNillableField("numeric_type", NonNillableNumeric, jdbcURL);
}

function testAssignNilToNonNillableTinyInt(string jdbcURL) returns @tainted string {
    return testAssignNilToNonNillableField("tinyint_type", NonNillableTinyInt, jdbcURL);
}

function testAssignNilToNonNillableSmallint(string jdbcURL) returns @tainted string {
    return testAssignNilToNonNillableField("smallint_type", NonNillableSmallInt, jdbcURL);
}

function testAssignNilToNonNillableDecimal(string jdbcURL) returns @tainted string {
    return testAssignNilToNonNillableField("decimal_type", NonNillableDecimal, jdbcURL);
}

function testAssignNilToNonNillableReal(string jdbcURL) returns @tainted string {
    return testAssignNilToNonNillableField("real_type", NonNillableReal, jdbcURL);
}

function testAssignNilToNonNillableClob(string jdbcURL) returns @tainted string {
    return testAssignNilToNonNillableField("clob_type", NonNillableClob, jdbcURL);
}

function testAssignNilToNonNillableBlob(string jdbcURL) returns @tainted string {
    return testAssignNilToNonNillableField("blob_type", NonNillableBlob, jdbcURL);
}

function testAssignNilToNonNillableBinary(string jdbcURL) returns @tainted string {
    return testAssignNilToNonNillableField("binary_type", NonNillableBinary, jdbcURL);
}

function testAssignNilToNonNillableDate(string jdbcURL) returns @tainted string {
    return testAssignNilToNonNillableField("date_type", NonNillableDate, jdbcURL);
}

function testAssignNilToNonNillableTime(string jdbcURL) returns @tainted string {
    return testAssignNilToNonNillableField("time_type", NonNillableTime, jdbcURL);
}

function testAssignNilToNonNillableDateTime(string jdbcURL) returns @tainted string {
    return testAssignNilToNonNillableField("datetime_type", NonNillableDateTime, jdbcURL);
}

function testAssignNilToNonNillableTimeStamp(string jdbcURL) returns @tainted string {
    return testAssignNilToNonNillableField("timestamp_type", NonNillableTimeStamp, jdbcURL);
}

function testAssignNilToNonNillableField(string field, typedesc<record {}> recordType, string jdbcURL) returns
    @tainted string {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });
    string dbTable;
    int rowId;
    if (field == "blob_type") {
        dbTable = "DataTypeTableNillableBlob";
        rowId = 4;
    } else {
        dbTable = "DataTypeTableNillable";
        rowId = 2;
    }
    var dt = testDB->select("SELECT " + field + " from " + dbTable + " where row_id=?", recordType, rowId);
    string errorMessage = "";
    if (dt is table<record {}>) {
        while (dt.hasNext()) {
            var ret = trap dt.getNext();
            if (ret is error) {
                errorMessage = <string>ret.detail()["message"];
            }
        }
    }
    checkpanic testDB.stop();
    return errorMessage;
}

function testAssignToInvalidUnionInt(string jdbcURL) returns @tainted string {
    return testAssignToInvalidUnionField("int_type", jdbcURL);
}

function testAssignToInvalidUnionLong(string jdbcURL) returns @tainted string {
    return testAssignToInvalidUnionField("long_type", jdbcURL);
}

function testAssignToInvalidUnionFloat(string jdbcURL) returns @tainted string {
    return testAssignToInvalidUnionField("float_type", jdbcURL);
}

function testAssignToInvalidUnionDouble(string jdbcURL) returns @tainted string {
    return testAssignToInvalidUnionField("double_type", jdbcURL);
}

function testAssignToInvalidUnionBoolean(string jdbcURL) returns @tainted string {
    return testAssignToInvalidUnionField("boolean_type", jdbcURL);
}

function testAssignToInvalidUnionString(string jdbcURL) returns @tainted string {
    return testAssignToInvalidUnionField("string_type", jdbcURL);
}

function testAssignToInvalidUnionNumeric(string jdbcURL) returns @tainted string {
    return testAssignToInvalidUnionField("numeric_type", jdbcURL);
}

function testAssignToInvalidUnionTinyInt(string jdbcURL) returns @tainted string {
    return testAssignToInvalidUnionField("tinyint_type", jdbcURL);
}

function testAssignToInvalidUnionSmallint(string jdbcURL) returns @tainted string {
    return testAssignToInvalidUnionField("smallint_type", jdbcURL);
}

function testAssignToInvalidUnionDecimal(string jdbcURL) returns @tainted string {
    return testAssignToInvalidUnionField("decimal_type", jdbcURL);
}

function testAssignToInvalidUnionReal(string jdbcURL) returns @tainted string {
    return testAssignToInvalidUnionField("real_type", jdbcURL);
}

function testAssignToInvalidUnionClob(string jdbcURL) returns @tainted string {
    return testAssignToInvalidUnionField("clob_type", jdbcURL);
}

function testAssignToInvalidUnionBlob(string jdbcURL) returns @tainted string {
    return testAssignToInvalidUnionField("blob_type", jdbcURL);
}

function testAssignToInvalidUnionBinary(string jdbcURL) returns @tainted string {
    return testAssignToInvalidUnionField("binary_type", jdbcURL);
}

function testAssignToInvalidUnionDate(string jdbcURL) returns @tainted string {
    return testAssignToInvalidUnionField("date_type", jdbcURL);
}

function testAssignToInvalidUnionTime(string jdbcURL) returns @tainted string {
    return testAssignToInvalidUnionField("time_type", jdbcURL);
}

function testAssignToInvalidUnionDateTime(string jdbcURL) returns @tainted string {
    return testAssignToInvalidUnionField("datetime_type", jdbcURL);
}

function testAssignToInvalidUnionTimeStamp(string jdbcURL) returns @tainted string {
    return testAssignToInvalidUnionField("timestamp_type", jdbcURL);
}

function testAssignNullArrayToNonNillableWithNonNillableElements(string jdbcURL) returns @tainted string {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var dt = testDB->select("SELECT int_array, long_array, float_array, boolean_array," +
              "string_array from ArrayTypes where row_id = ?", ResultMap, 3);
    string errorMessage = "";
    if (dt is table<ResultMap>) {
        while (dt.hasNext()) {
            var ret = trap dt.getNext();
            if (ret is error) {
                errorMessage = <string>ret.detail()["message"];
            }
        }
    }
    checkpanic testDB.stop();
    return errorMessage;
}

function testAssignNullArrayToNonNillableTypeWithNillableElements(string jdbcURL) returns @tainted string {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var dt = testDB->select("SELECT int_array, long_array, float_array, boolean_array," +
              "string_array from ArrayTypes where row_id = ?", ResultMapNonNillableTypeNillableElements, 3);
    string errorMessage = "";
    if (dt is table<ResultMapNonNillableTypeNillableElements>) {
        while (dt.hasNext()) {
            var ret = trap dt.getNext();
            if (ret is error) {
                errorMessage = <string>ret.detail()["message"];
            }
        }
    }
    checkpanic testDB.stop();
    return errorMessage;
}

function testAssignNullElementArrayToNonNillableTypeWithNonNillableElements(string jdbcURL) returns @tainted string {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var dt = testDB->select("SELECT int_array, long_array, float_array, boolean_array," +
              "string_array from ArrayTypes where row_id = ?", ResultMap, 2);
    string errorMessage = "";
    if (dt is table<ResultMap>) {
        while (dt.hasNext()) {
            var ret = trap dt.getNext();
            if (ret is error) {
                errorMessage = <string>ret.detail()["message"];
            }
        }
    }
    checkpanic testDB.stop();
    return errorMessage;
}

function testAssignNullElementArrayToNillableTypeWithNonNillableElements(string jdbcURL) returns @tainted string {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    var dt = testDB->select("SELECT int_array, long_array, float_array, boolean_array," +
              "string_array from ArrayTypes where row_id = ?", ResultMapNillableTypeNonNillableElements, 2);
    string errorMessage = "";
    if (dt is table<ResultMapNillableTypeNonNillableElements>) {
        while (dt.hasNext()) {
            var ret = trap dt.getNext();
            if (ret is error) {
                errorMessage = <string>ret.detail()["message"];
            }
        }
    }
    checkpanic testDB.stop();
    return errorMessage;
}

function testAssignInvalidUnionArray(string jdbcURL) returns @tainted string {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });
    var dt = testDB->select("SELECT int_array from ArrayTypes where row_id = 1", InvalidUnionArray);
    string message = "";
    if (dt is table<InvalidUnionArray>) {
        while (dt.hasNext()) {
            var ret = trap dt.getNext();
            if (ret is error) {
                message = <string>ret.detail()["message"];
            }
        }
    }
    checkpanic testDB.stop();
    return message;
}

function testAssignInvalidUnionArrayElement(string jdbcURL) returns @tainted string {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });
    var dt = testDB->select("SELECT int_array from ArrayTypes where row_id = 1", InvalidUnionArrayElement);
    string message = "";
    if (dt is table<InvalidUnionArrayElement>) {
        while (dt.hasNext()) {
            var ret = trap dt.getNext();
            if (ret is error) {
                message = <string>ret.detail()["message"];
            }
        }
    }
    checkpanic testDB.stop();
    return message;
}

function testAssignInvalidUnionArray2(string jdbcURL) returns @tainted string {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });
    var dt = testDB->select("SELECT int_array from ArrayTypes where row_id = 1", InvalidUnionArray2);
    string message = "";
    if (dt is table<InvalidUnionArray2>) {
        while (dt.hasNext()) {
            var ret = trap dt.getNext();
            if (ret is error) {
                message = <string>ret.detail()["message"];
            }
        }
    }
    checkpanic testDB.stop();
    return message;
}

function testAssignToInvalidUnionField(string field, string jdbcURL) returns @tainted string {
    jdbc:Client testDB = new ({
        url: jdbcURL,
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize: 1}
    });

    string dbTable;
    int rowId;

    if (field == "blob_type") {
        dbTable = "DataTypeTableNillableBlob";
        rowId = 3;
    } else {
        dbTable = "DataTypeTableNillable";
        rowId = 1;
    }

    var dt = testDB->select("SELECT " + field + " from " + dbTable + " where row_id=?", InvalidUnion, rowId);
    string errorMessage = "";
    if (dt is table<InvalidUnion>) {
        while (dt.hasNext()) {
            var ret = trap <InvalidUnion>dt.getNext();
            if (ret is error) {
                errorMessage = <string>ret.detail()["message"];
            }
        }
    }
    checkpanic testDB.stop();
    return errorMessage;
}
