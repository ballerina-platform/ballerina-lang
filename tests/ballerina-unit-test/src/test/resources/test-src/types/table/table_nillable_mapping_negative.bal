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

import ballerina/h2;
import ballerina/time;

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
    (int|string) val;
};

type InvalidUnionArrayElement record {
    (int|string)?[] val;
};

type InvalidUnionArray record {
    int[]|string val;
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

function testAssignNilToNonNillableInt() returns string {
    return testAssignNilToNonNillableField("int_type", NonNillableInt);
}

function testAssignNilToNonNillableLong() returns string {
    return testAssignNilToNonNillableField("long_type", NonNillableLong);
}

function testAssignNilToNonNillableFloat() returns string {
    return testAssignNilToNonNillableField("float_type", NonNillableFloat);
}

function testAssignNilToNonNillableDouble() returns string {
    return testAssignNilToNonNillableField("double_type", NonNillableDouble);
}

function testAssignNilToNonNillableBoolean() returns string {
    return testAssignNilToNonNillableField("boolean_type", NonNillableBoolean);
}

function testAssignNilToNonNillableString() returns string {
    return testAssignNilToNonNillableField("string_type", NonNillableString);
}

function testAssignNilToNonNillableNumeric() returns string {
    return testAssignNilToNonNillableField("numeric_type", NonNillableNumeric);
}

function testAssignNilToNonNillableTinyInt() returns string {
    return testAssignNilToNonNillableField("tinyint_type", NonNillableTinyInt);
}

function testAssignNilToNonNillableSmallint() returns string {
    return testAssignNilToNonNillableField("smallint_type", NonNillableSmallInt);
}

function testAssignNilToNonNillableDecimal() returns string {
    return testAssignNilToNonNillableField("decimal_type", NonNillableDecimal);
}

function testAssignNilToNonNillableReal() returns string {
    return testAssignNilToNonNillableField("real_type", NonNillableReal);
}

function testAssignNilToNonNillableClob() returns string {
    return testAssignNilToNonNillableField("clob_type", NonNillableClob);
}

function testAssignNilToNonNillableBlob() returns string {
    return testAssignNilToNonNillableField("blob_type", NonNillableBlob);
}

function testAssignNilToNonNillableBinary() returns string {
    return testAssignNilToNonNillableField("binary_type", NonNillableBinary);
}

function testAssignNilToNonNillableDate() returns string {
    return testAssignNilToNonNillableField("date_type", NonNillableDate);
}

function testAssignNilToNonNillableTime() returns string {
    return testAssignNilToNonNillableField("time_type", NonNillableTime);
}

function testAssignNilToNonNillableDateTime() returns string {
    return testAssignNilToNonNillableField("datetime_type", NonNillableDateTime);
}

function testAssignNilToNonNillableTimeStamp() returns string {
    return testAssignNilToNonNillableField("timestamp_type", NonNillableTimeStamp);
}

function testAssignNilToNonNillableField(string field, typedesc recordType) returns string {
    h2:Client testDB = new({
        path: "./target/tempdb/",
        name: "TEST_DATA_TABLE_H2",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
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
                errorMessage = <string>ret.reason();
            }
        }
    }
    testDB.stop();
    return errorMessage;
}

function testAssignToInvalidUnionInt() returns string {
    return testAssignToInvalidUnionField("int_type");
}

function testAssignToInvalidUnionLong() returns string {
    return testAssignToInvalidUnionField("long_type");
}

function testAssignToInvalidUnionFloat() returns string {
    return testAssignToInvalidUnionField("float_type");
}

function testAssignToInvalidUnionDouble() returns string {
    return testAssignToInvalidUnionField("double_type");
}

function testAssignToInvalidUnionBoolean() returns string {
    return testAssignToInvalidUnionField("boolean_type");
}

function testAssignToInvalidUnionString() returns string {
    return testAssignToInvalidUnionField("string_type");
}

function testAssignToInvalidUnionNumeric() returns string {
    return testAssignToInvalidUnionField("numeric_type");
}

function testAssignToInvalidUnionTinyInt() returns string {
    return testAssignToInvalidUnionField("tinyint_type");
}

function testAssignToInvalidUnionSmallint() returns string {
    return testAssignToInvalidUnionField("smallint_type");
}

function testAssignToInvalidUnionDecimal() returns string {
    return testAssignToInvalidUnionField("decimal_type");
}

function testAssignToInvalidUnionReal() returns string {
    return testAssignToInvalidUnionField("real_type");
}

function testAssignToInvalidUnionClob() returns string {
    return testAssignToInvalidUnionField("clob_type");
}

function testAssignToInvalidUnionBlob() returns string {
    return testAssignToInvalidUnionField("blob_type");
}

function testAssignToInvalidUnionBinary() returns string {
    return testAssignToInvalidUnionField("binary_type");
}

function testAssignToInvalidUnionDate() returns string {
    return testAssignToInvalidUnionField("date_type");
}

function testAssignToInvalidUnionTime() returns string {
    return testAssignToInvalidUnionField("time_type");
}

function testAssignToInvalidUnionDateTime() returns string {
    return testAssignToInvalidUnionField("datetime_type");
}

function testAssignToInvalidUnionTimeStamp() returns string {
    return testAssignToInvalidUnionField("timestamp_type");
}

function testAssignNullArrayToNonNillableWithNonNillableElements() returns string {
    h2:Client testDB = new({
        path: "./target/tempdb/",
        name: "TEST_DATA_TABLE_H2",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    });

    var dt = testDB->select("SELECT int_array, long_array, float_array, boolean_array,
              string_array from ArrayTypes where row_id = ?", ResultMap, 3);
    string errorMessage = "";
    if (dt is table<ResultMap>) {
        while (dt.hasNext()) {
            var ret = trap dt.getNext();
            if (ret is error) {
                errorMessage = ret.reason();
            }
        }
    }
    testDB.stop();
    return errorMessage;
}

function testAssignNullArrayToNonNillableTypeWithNillableElements() returns string {
    h2:Client testDB = new({
        path: "./target/tempdb/",
        name: "TEST_DATA_TABLE_H2",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    });

    var dt = testDB->select("SELECT int_array, long_array, float_array, boolean_array,
              string_array from ArrayTypes where row_id = ?", ResultMapNonNillableTypeNillableElements, 3);
    string errorMessage = "";
    if (dt is table<ResultMapNonNillableTypeNillableElements>) {
        while (dt.hasNext()) {
            var ret = trap dt.getNext();
            if (ret is error) {
                errorMessage = ret.reason();
            }
        }
    }
    testDB.stop();
    return errorMessage;
}

function testAssignNullElementArrayToNonNillableTypeWithNonNillableElements() returns string {
    h2:Client testDB = new({
        path: "./target/tempdb/",
        name: "TEST_DATA_TABLE_H2",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    });

    var dt = testDB->select("SELECT int_array, long_array, float_array, boolean_array,
              string_array from ArrayTypes where row_id = ?", ResultMap, 2);
    string errorMessage = "";
    if (dt is table<ResultMap>) {
        while (dt.hasNext()) {
            var ret = trap dt.getNext();
            if (ret is error) {
                errorMessage = ret.reason();
            }
        }
    }
    testDB.stop();
    return errorMessage;
}

function testAssignNullElementArrayToNillableTypeWithNonNillableElements() returns string {
    h2:Client testDB = new({
        path: "./target/tempdb/",
        name: "TEST_DATA_TABLE_H2",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    });

    var dt = testDB->select("SELECT int_array, long_array, float_array, boolean_array,
              string_array from ArrayTypes where row_id = ?", ResultMapNillableTypeNonNillableElements, 2);
    string errorMessage = "";
    if (dt is table<ResultMapNillableTypeNonNillableElements>) {
        while (dt.hasNext()) {
            var ret = trap dt.getNext();
            if (ret is error) {
                errorMessage = ret.reason();
            }
        }
    }
    testDB.stop();
    return errorMessage;
}

function testAssignInvalidUnionArray() returns string {
    h2:Client testDB = new({
        path: "./target/tempdb/",
        name: "TEST_DATA_TABLE_H2",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    });
    var dt = testDB->select("SELECT int_array from ArrayTypes where row_id = 1", InvalidUnionArray);
    string message = "";
    if (dt is table<InvalidUnionArray>) {
        while (dt.hasNext()) {
            var ret = trap dt.getNext();
            if (ret is error) {
                message = <string>ret.reason();
            }
        }
    }
    testDB.stop();
    return message;
}

function testAssignInvalidUnionArrayElement() returns string {
    h2:Client testDB = new({
        path: "./target/tempdb/",
        name: "TEST_DATA_TABLE_H2",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    });
    var dt = testDB->select("SELECT int_array from ArrayTypes where row_id = 1", InvalidUnionArrayElement);
    string message = "";
    if (dt is table<InvalidUnionArrayElement>) {
        while (dt.hasNext()) {
            var ret = trap dt.getNext();
            if (ret is error) {
                message = <string>ret.reason();
            }
        }
    }
    testDB.stop();
    return message;
}

function testAssignInvalidUnionArray2() returns string {
    h2:Client testDB = new({
        path: "./target/tempdb/",
        name: "TEST_DATA_TABLE_H2",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    });
    var dt = testDB->select("SELECT int_array from ArrayTypes where row_id = 1", InvalidUnionArray2);
    string message = "";
    if (dt is table<InvalidUnionArray2>) {
        while (dt.hasNext()) {
            var ret = trap dt.getNext();
            if (ret is error) {
                message = <string>ret.reason();
            }
        }
    }
    testDB.stop();
    return message;
}

function testAssignToInvalidUnionField(string field) returns string {
    h2:Client testDB = new({
        path: "./target/tempdb/",
        name: "TEST_DATA_TABLE_H2",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
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
                errorMessage = ret.reason();
            }
        }
    }
    testDB.stop();
    return errorMessage;
}
