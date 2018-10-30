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

function testAssignNilToNonNillableInt(string jdbcUrl, string userName, string password) {
    testAssignNilToNonNillableField(jdbcUrl, userName, password, "int_type", NonNillableInt);
}

function testAssignNilToNonNillableLong(string jdbcUrl, string userName, string password) {
    testAssignNilToNonNillableField(jdbcUrl, userName, password, "long_type", NonNillableLong);
}

function testAssignNilToNonNillableFloat(string jdbcUrl, string userName, string password) {
    testAssignNilToNonNillableField(jdbcUrl, userName, password, "float_type", NonNillableFloat);
}

function testAssignNilToNonNillableDouble(string jdbcUrl, string userName, string password) {
    testAssignNilToNonNillableField(jdbcUrl, userName, password, "double_type", NonNillableDouble);
}

function testAssignNilToNonNillableBoolean(string jdbcUrl, string userName, string password) {
    testAssignNilToNonNillableField(jdbcUrl, userName, password, "boolean_type", NonNillableBoolean);
}

function testAssignNilToNonNillableString(string jdbcUrl, string userName, string password) {
    testAssignNilToNonNillableField(jdbcUrl, userName, password, "string_type", NonNillableString);
}

function testAssignNilToNonNillableNumeric(string jdbcUrl, string userName, string password) {
    testAssignNilToNonNillableField(jdbcUrl, userName, password, "numeric_type", NonNillableNumeric);
}

function testAssignNilToNonNillableTinyInt(string jdbcUrl, string userName, string password) {
    testAssignNilToNonNillableField(jdbcUrl, userName, password, "tinyint_type", NonNillableTinyInt);
}

function testAssignNilToNonNillableSmallint(string jdbcUrl, string userName, string password) {
    testAssignNilToNonNillableField(jdbcUrl, userName, password, "smallint_type", NonNillableSmallInt);
}

function testAssignNilToNonNillableDecimal(string jdbcUrl, string userName, string password) {
    testAssignNilToNonNillableField(jdbcUrl, userName, password, "decimal_type", NonNillableDecimal);
}

function testAssignNilToNonNillableReal(string jdbcUrl, string userName, string password) {
    testAssignNilToNonNillableField(jdbcUrl, userName, password, "real_type", NonNillableReal);
}

function testAssignNilToNonNillableClob(string jdbcUrl, string userName, string password) {
    testAssignNilToNonNillableField(jdbcUrl, userName, password, "clob_type", NonNillableClob);
}

function testAssignNilToNonNillableBlob(string jdbcUrl, string userName, string password) {
    testAssignNilToNonNillableField(jdbcUrl, userName, password, "blob_type", NonNillableBlob);
}

function testAssignNilToNonNillableBinary(string jdbcUrl, string userName, string password) {
    testAssignNilToNonNillableField(jdbcUrl, userName, password, "binary_type", NonNillableBinary);
}

function testAssignNilToNonNillableDate(string jdbcUrl, string userName, string password) {
    testAssignNilToNonNillableField(jdbcUrl, userName, password, "date_type", NonNillableDate);
}

function testAssignNilToNonNillableTime(string jdbcUrl, string userName, string password) {
    testAssignNilToNonNillableField(jdbcUrl, userName, password, "time_type", NonNillableTime);
}

function testAssignNilToNonNillableDateTime(string jdbcUrl, string userName, string password) {
    testAssignNilToNonNillableField(jdbcUrl, userName, password, "datetime_type", NonNillableDateTime);
}

function testAssignNilToNonNillableTimeStamp(string jdbcUrl, string userName, string password) {
    testAssignNilToNonNillableField(jdbcUrl, userName, password, "timestamp_type", NonNillableTimeStamp);
}

function testAssignNilToNonNillableField(string jdbcUrl, string userName, string password, string field, typedesc
    recordType) {
    endpoint jdbc:Client testDB {
        url: jdbcUrl,
        username: userName,
        password: password,
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

function testAssignToInvalidUnionInt(string jdbcUrl, string userName, string password) {
    testAssignToInvalidUnionField(jdbcUrl, userName, password, "int_type");
}

function testAssignToInvalidUnionLong(string jdbcUrl, string userName, string password) {
    testAssignToInvalidUnionField(jdbcUrl, userName, password, "long_type");
}

function testAssignToInvalidUnionFloat(string jdbcUrl, string userName, string password) {
    testAssignToInvalidUnionField(jdbcUrl, userName, password, "float_type");
}

function testAssignToInvalidUnionDouble(string jdbcUrl, string userName, string password) {
    testAssignToInvalidUnionField(jdbcUrl, userName, password, "double_type");
}

function testAssignToInvalidUnionBoolean(string jdbcUrl, string userName, string password) {
    testAssignToInvalidUnionField(jdbcUrl, userName, password, "boolean_type");
}

function testAssignToInvalidUnionString(string jdbcUrl, string userName, string password) {
    testAssignToInvalidUnionField(jdbcUrl, userName, password, "string_type");
}

function testAssignToInvalidUnionNumeric(string jdbcUrl, string userName, string password) {
    testAssignToInvalidUnionField(jdbcUrl, userName, password, "numeric_type");
}

function testAssignToInvalidUnionTinyInt(string jdbcUrl, string userName, string password) {
    testAssignToInvalidUnionField(jdbcUrl, userName, password, "tinyint_type");
}

function testAssignToInvalidUnionSmallint(string jdbcUrl, string userName, string password) {
    testAssignToInvalidUnionField(jdbcUrl, userName, password, "smallint_type");
}

function testAssignToInvalidUnionDecimal(string jdbcUrl, string userName, string password) {
    testAssignToInvalidUnionField(jdbcUrl, userName, password, "decimal_type");
}

function testAssignToInvalidUnionReal(string jdbcUrl, string userName, string password) {
    testAssignToInvalidUnionField(jdbcUrl, userName, password, "real_type");
}

function testAssignToInvalidUnionClob(string jdbcUrl, string userName, string password) {
    testAssignToInvalidUnionField(jdbcUrl, userName, password, "clob_type");
}

function testAssignToInvalidUnionBlob(string jdbcUrl, string userName, string password) {
    testAssignToInvalidUnionField(jdbcUrl, userName, password, "blob_type");
}

function testAssignToInvalidUnionBinary(string jdbcUrl, string userName, string password) {
    testAssignToInvalidUnionField(jdbcUrl, userName, password, "binary_type");
}

function testAssignToInvalidUnionDate(string jdbcUrl, string userName, string password) {
    testAssignToInvalidUnionField(jdbcUrl, userName, password, "date_type");
}

function testAssignToInvalidUnionTime(string jdbcUrl, string userName, string password) {
    testAssignToInvalidUnionField(jdbcUrl, userName, password, "time_type");
}

function testAssignToInvalidUnionDateTime(string jdbcUrl, string userName, string password) {
    testAssignToInvalidUnionField(jdbcUrl, userName, password, "datetime_type");
}

function testAssignToInvalidUnionTimeStamp(string jdbcUrl, string userName, string password) {
    testAssignToInvalidUnionField(jdbcUrl, userName, password, "timestamp_type");
}

function testAssignNullArrayToNonNillableWithNonNillableElements(string jdbcUrl, string userName, string password) {
    testAssignArrayToInvalidField(jdbcUrl, userName, password, ResultMap, 3);
}

function testAssignNullArrayToNonNillableTypeWithNillableElements(string jdbcUrl, string userName, string password) {
    testAssignArrayToInvalidField(jdbcUrl, userName, password, ResultMapNonNillableTypeNillableElements, 3);
}

function testAssignNullElementArrayToNonNillableTypeWithNonNillableElements(string jdbcUrl, string userName, string
    password) {
    testAssignArrayToInvalidField(jdbcUrl, userName, password, ResultMap, 2);
}

function testAssignNullElementArrayToNillableTypeWithNonNillableElements(string jdbcUrl, string userName, string
    password) {
    testAssignArrayToInvalidField(jdbcUrl, userName, password, ResultMapNillableTypeNonNillableElements, 2);
}

function testAssignInvalidUnionArray(string jdbcUrl, string userName, string password) {
    testInvalidUnionForArrays(jdbcUrl, userName, password, InvalidUnionArray);
}

function testAssignInvalidUnionArrayElement(string jdbcUrl, string userName, string password) {
    testInvalidUnionForArrays(jdbcUrl, userName, password, InvalidUnionArrayElement);
}

function testAssignInvalidUnionArray2(string jdbcUrl, string userName, string password) {
    testInvalidUnionForArrays(jdbcUrl, userName, password, InvalidUnionArray2);
}

function testAssignToInvalidUnionField(string jdbcUrl, string userName, string password, string field) {
    endpoint jdbc:Client testDB {
        url: jdbcUrl,
        username: userName,
        password: password,
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

function testAssignArrayToInvalidField(string jdbcUrl, string userName, string password, typedesc invalidType, int id) {
    endpoint jdbc:Client testDB {
        url: jdbcUrl,
        username: userName,
        password: password,
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

function testInvalidUnionForArrays(string jdbcUrl, string userName, string password, typedesc invalidUnion) {
    endpoint jdbc:Client testDB {
        url: jdbcUrl,
        username: userName,
        password: password,
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
