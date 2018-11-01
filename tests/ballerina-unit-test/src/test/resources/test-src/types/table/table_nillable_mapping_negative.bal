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
    (int|string)[] val;
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

function testAssignNilToNonNillableInt() {
    testAssignNilToNonNillableField("int_type", NonNillableInt);
}

function testAssignNilToNonNillableLong() {
    testAssignNilToNonNillableField("long_type", NonNillableLong);
}

function testAssignNilToNonNillableFloat() {
    testAssignNilToNonNillableField("float_type", NonNillableFloat);
}

function testAssignNilToNonNillableDouble() {
    testAssignNilToNonNillableField("double_type", NonNillableDouble);
}

function testAssignNilToNonNillableBoolean() {
    testAssignNilToNonNillableField("boolean_type", NonNillableBoolean);
}

function testAssignNilToNonNillableString() {
    testAssignNilToNonNillableField("string_type", NonNillableString);
}

function testAssignNilToNonNillableNumeric() {
    testAssignNilToNonNillableField("numeric_type", NonNillableNumeric);
}

function testAssignNilToNonNillableTinyInt() {
    testAssignNilToNonNillableField("tinyint_type", NonNillableTinyInt);
}

function testAssignNilToNonNillableSmallint() {
    testAssignNilToNonNillableField("smallint_type", NonNillableSmallInt);
}

function testAssignNilToNonNillableDecimal() {
    testAssignNilToNonNillableField("decimal_type", NonNillableDecimal);
}

function testAssignNilToNonNillableReal() {
    testAssignNilToNonNillableField("real_type", NonNillableReal);
}

function testAssignNilToNonNillableClob() {
    testAssignNilToNonNillableField("clob_type", NonNillableClob);
}

function testAssignNilToNonNillableBlob() {
    testAssignNilToNonNillableField("blob_type", NonNillableBlob);
}

function testAssignNilToNonNillableBinary() {
    testAssignNilToNonNillableField("binary_type", NonNillableBinary);
}

function testAssignNilToNonNillableDate() {
    testAssignNilToNonNillableField("date_type", NonNillableDate);
}

function testAssignNilToNonNillableTime() {
    testAssignNilToNonNillableField("time_type", NonNillableTime);
}

function testAssignNilToNonNillableDateTime() {
    testAssignNilToNonNillableField("datetime_type", NonNillableDateTime);
}

function testAssignNilToNonNillableTimeStamp() {
    testAssignNilToNonNillableField("timestamp_type", NonNillableTimeStamp);
}

function testAssignNilToNonNillableField(string field, typedesc
    recordType) {
    endpoint h2:Client testDB {
        path: "./target/tempdb/",
        name: "TEST_DATA_TABLE_H2",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    };

    string dbTable;
    int rowId;

    if (field == "blob_type") {
        dbTable = "DataTypeTableNillableBlob";
        rowId = 4;
    } else {
        dbTable = "DataTypeTableNillable";
        rowId = 2;
    }

    table dt = check testDB->select("SELECT " + field + " from " + dbTable + " where row_id=?", recordType, rowId);

    try {
        while (dt.hasNext()) {
            var rs = dt.getNext();
        }
    } finally {
        testDB.stop();
    }
}

function testAssignToInvalidUnionInt() {
    testAssignToInvalidUnionField("int_type");
}

function testAssignToInvalidUnionLong() {
    testAssignToInvalidUnionField("long_type");
}

function testAssignToInvalidUnionFloat() {
    testAssignToInvalidUnionField("float_type");
}

function testAssignToInvalidUnionDouble() {
    testAssignToInvalidUnionField("double_type");
}

function testAssignToInvalidUnionBoolean() {
    testAssignToInvalidUnionField("boolean_type");
}

function testAssignToInvalidUnionString() {
    testAssignToInvalidUnionField("string_type");
}

function testAssignToInvalidUnionNumeric() {
    testAssignToInvalidUnionField("numeric_type");
}

function testAssignToInvalidUnionTinyInt() {
    testAssignToInvalidUnionField("tinyint_type");
}

function testAssignToInvalidUnionSmallint() {
    testAssignToInvalidUnionField("smallint_type");
}

function testAssignToInvalidUnionDecimal() {
    testAssignToInvalidUnionField("decimal_type");
}

function testAssignToInvalidUnionReal() {
    testAssignToInvalidUnionField("real_type");
}

function testAssignToInvalidUnionClob() {
    testAssignToInvalidUnionField("clob_type");
}

function testAssignToInvalidUnionBlob() {
    testAssignToInvalidUnionField("blob_type");
}

function testAssignToInvalidUnionBinary() {
    testAssignToInvalidUnionField("binary_type");
}

function testAssignToInvalidUnionDate() {
    testAssignToInvalidUnionField("date_type");
}

function testAssignToInvalidUnionTime() {
    testAssignToInvalidUnionField("time_type");
}

function testAssignToInvalidUnionDateTime() {
    testAssignToInvalidUnionField("datetime_type");
}

function testAssignToInvalidUnionTimeStamp() {
    testAssignToInvalidUnionField("timestamp_type");
}

function testAssignNullArrayToNonNillableWithNonNillableElements() {
    testAssignArrayToInvalidField(ResultMap, 3);
}

function testAssignNullArrayToNonNillableTypeWithNillableElements() {
    testAssignArrayToInvalidField(ResultMapNonNillableTypeNillableElements, 3);
}

function testAssignNullElementArrayToNonNillableTypeWithNonNillableElements() {
    testAssignArrayToInvalidField(ResultMap, 2);
}

function testAssignNullElementArrayToNillableTypeWithNonNillableElements() {
    testAssignArrayToInvalidField(ResultMapNillableTypeNonNillableElements, 2);
}

function testAssignInvalidUnionArray() {
    testInvalidUnionForArrays(InvalidUnionArray);
}

function testAssignInvalidUnionArrayElement() {
    testInvalidUnionForArrays(InvalidUnionArrayElement);
}

function testAssignInvalidUnionArray2() {
    testInvalidUnionForArrays(InvalidUnionArray2);
}

function testAssignToInvalidUnionField(string field) {
    endpoint h2:Client testDB {
        path: "./target/tempdb/",
        name: "TEST_DATA_TABLE_H2",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    };

    string dbTable;
    int rowId;

    if (field == "blob_type") {
        dbTable = "DataTypeTableNillableBlob";
        rowId = 3;
    } else {
        dbTable = "DataTypeTableNillable";
        rowId = 1;
    }

    table dt = check testDB->select("SELECT " + field + " from " + dbTable + " where row_id=?", InvalidUnion, rowId);

    try {
        while (dt.hasNext()) {
            var rs = <InvalidUnion>dt.getNext();
        }
    } finally {
        testDB.stop();
    }
}

function testAssignArrayToInvalidField(typedesc invalidType, int id) {
    endpoint h2:Client testDB {
        path: "./target/tempdb/",
        name: "TEST_DATA_TABLE_H2",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    };

    table dt = check testDB->select("SELECT int_array, long_array, float_array, boolean_array,
              string_array from ArrayTypes where row_id = ?", invalidType, id);

    try {
        while (dt.hasNext()) {
            var rs = dt.getNext();
        }
    } finally {
        testDB.stop();
    }
}

function testInvalidUnionForArrays(typedesc invalidUnion) {
    endpoint h2:Client testDB {
        path: "./target/tempdb/",
        name: "TEST_DATA_TABLE_H2",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    };

    table dt = check testDB->select("SELECT int_array from ArrayTypes where row_id = 1", invalidUnion);

    try {
        while (dt.hasNext()) {
            var rs = dt.getNext();
        }
    } finally {
        testDB.stop();
    }
}
