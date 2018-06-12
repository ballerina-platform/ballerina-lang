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
import ballerina/time;

type NonNillableInt {
    int val;
};

type NonNillableLong {
    int val;
};

type NonNillableFloat {
    float val;
};

type NonNillableDouble {
    float val;
};

type NonNillableBoolean {
    boolean val;
};

type NonNillableString {
    string val;
};

type NonNillableNumeric {
    float val;
};

type NonNillableTinyInt {
    int val;
};

type NonNillableSmallInt {
    int val;
};

type NonNillableDecimal {
    float val;
};

type NonNillableReal {
    float val;
};

type NonNillableClob {
    string val;
};

type NonNillableBlob {
    blob val;
};

type NonNillableBinary {
    blob val;
};

type NonNillableDate {
    time:Time val;
};

type NonNillableTime {
    time:Time val;
};

type NonNillableDateTime {
    time:Time val;
};

type NonNillableTimeStamp {
    time:Time val;
};

type InvalidUnion {
    (int|string) val;
};

type InvalidUnionArrayElement {
    (int|string)[] val,
};

type InvalidUnionArray {
    int[]|string val,
};

type InvalidUnionArray2 {
    string?[] val,
};

type ResultMap {
    int[] INT_ARRAY,
    int[] LONG_ARRAY,
    float[] FLOAT_ARRAY,
    boolean[] BOOLEAN_ARRAY,
    string[] STRING_ARRAY,
};

type ResultMapNonNillableTypeNillableElements {
    int?[] INT_ARRAY,
    int?[] LONG_ARRAY,
    float?[] FLOAT_ARRAY,
    boolean?[] BOOLEAN_ARRAY,
    string?[] STRING_ARRAY,
};

type ResultMapNillable {
    int?[]? INT_ARRAY,
    int?[]? LONG_ARRAY,
    float?[]? FLOAT_ARRAY,
    boolean?[]? BOOLEAN_ARRAY,
    string?[]? STRING_ARRAY,
};

type ResultMapNillableTypeNonNillableElements {
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

function testAssignNilToNonNillableField(string field, typedesc recordType) {
    endpoint jdbc:Client testDB {
        url: "jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: { maximumPoolSize: 1 }
    };

    table dt = check testDB->select("SELECT " + field + " from DataTypeTableNillable where row_id=2", recordType);

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
    endpoint jdbc:Client testDB {
        url: "jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: { maximumPoolSize: 1 }
    };

    table dt = check testDB->select("SELECT " + field + " from DataTypeTableNillable where row_id=1", InvalidUnion);

    try {
        while (dt.hasNext()) {
            var rs = <InvalidUnion>dt.getNext();
        }
    } finally {
        testDB.stop();
    }
}

function testAssignArrayToInvalidField(typedesc invalidType, int id) {
    endpoint jdbc:Client testDB {
        url: "jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
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
    endpoint jdbc:Client testDB {
        url: "jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
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
