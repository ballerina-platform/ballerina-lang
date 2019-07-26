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

import ballerinax/java.jdbc;

type Result record {
    string STRING_TYPE;
    int INT_TYPE;
    int LONG_TYPE;
    float FLOAT_TYPE;
    float DOUBLE_TYPE;
    boolean BOOLEAN_TYPE;
};

type ResultBlobWrongOrder record {
    int INT_TYPE;
    byte[] BLOB_TYPE;
};

type ResultBlobCorrectOrderWrongType record {
    int[] BLOB_TYPE;
    int INT_TYPE;
};

function testWrongOrderInt() returns @tainted error? {
    return testWrongOrder("SELECT int_type, long_type, float_type, boolean_type, string_type, double_type from DataTable
    WHERE row_id = 1");
}

function testWrongOrderString() returns @tainted error? {
    return testWrongOrder("SELECT int_type, long_type, float_type, boolean_type, string_type, double_type from DataTable
    WHERE row_id = 1");
}

function testWrongOrderBoolean() returns @tainted error? {
    return testWrongOrder("SELECT boolean_type, long_type, float_type, string_type, int_type, double_type from DataTable
    WHERE row_id = 1");
}

function testWrongOrderDouble() returns @tainted error? {
    return testWrongOrder("SELECT double_type, long_type, float_type, string_type, int_type, boolean_type from DataTable
    WHERE row_id = 1");
}

function testWrongOrderFloat() returns @tainted error? {
    return testWrongOrder("SELECT double_type, long_type, float_type, string_type, int_type, boolean_type from DataTable
    WHERE row_id = 1");
}

function testWrongOrderLong() returns @tainted error? {
    return testWrongOrder("SELECT boolean_type, string_type, float_type, long_type, int_type, double_type from DataTable
    WHERE row_id = 1");
}

function testWrongOrderBlobWrongOrder() returns @tainted error? {
    jdbc:Client testDB = new({
        url: "jdbc:h2:file:./target/tempdb/TEST_DATA_TABLE_H2",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    });

    var selectRet = testDB->select("SELECT blob_type, row_id from ComplexTypes WHERE row_id = 1", ResultBlobWrongOrder);
    error? retVal = ();

    if (selectRet is table<ResultBlobWrongOrder>) {
        while (selectRet.hasNext()) {
            var rs = trap selectRet.getNext();
            if (rs is error) {
                retVal = rs;
                break;
            }
        }
        selectRet.close();
    }
    checkpanic testDB.stop();
    return retVal;
}

function testWrongOrderBlobCorrectOrderWrongType() returns @tainted error? {
    jdbc:Client testDB = new({
        url: "jdbc:h2:file:./target/tempdb/TEST_DATA_TABLE_H2",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    });

    var selectRet = testDB->select("SELECT blob_type, row_id from ComplexTypes WHERE row_id = 1",
        ResultBlobCorrectOrderWrongType);
    error? retVal = ();

    if (selectRet is table<ResultBlobCorrectOrderWrongType>) {
        while (selectRet.hasNext()) {
            var rs = trap selectRet.getNext();
            if (rs is error) {
                retVal = rs;
                break;
            }
        }
        selectRet.close();
    }
    checkpanic testDB.stop();
    return retVal;
}

function testGreaterNoOfParams() returns @tainted error? {
    jdbc:Client testDB = new({
        url: "jdbc:h2:file:./target/tempdb/TEST_DATA_TABLE_H2",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    });

    var selectRet = testDB->select("SELECT boolean_type from DataTable WHERE row_id = 1", Result);
    error? retVal = ();

    if (selectRet is table<Result>) {
        while (selectRet.hasNext()) {
            var rs = trap selectRet.getNext();
            if (rs is error) {
                retVal = rs;
                break;
            }
        }
        selectRet.close();
    }
    checkpanic testDB.stop();
    return retVal;
}

function testLowerNoOfParams() returns @tainted error? {
    jdbc:Client testDB = new({
        url: "jdbc:h2:file:./target/tempdb/TEST_DATA_TABLE_H2",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    });

    var selectRet = testDB->select("SELECT boolean_type, boolean_type, string_type, float_type, long_type, int_type,
        double_type from DataTable WHERE row_id = 1", Result);
    error? retVal = ();

    if (selectRet is table<Result>) {
        while (selectRet.hasNext()) {
            var rs = trap selectRet.getNext();
            if (rs is error) {
                retVal = rs;
                break;
            }
        }
        selectRet.close();
    }
    checkpanic testDB.stop();
    return retVal;
}

function testWrongOrder(string queryStr) returns @tainted error? {
    jdbc:Client testDB = new({
        url: "jdbc:h2:file:./target/tempdb/TEST_DATA_TABLE_H2",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    });

    var selectRet = testDB->select(queryStr, Result);
    error? retVal = ();

    if (selectRet is table<Result>) {
        while (selectRet.hasNext()) {
            var rs = trap selectRet.getNext();
            if (rs is error) {
                retVal = rs;
                break;
            }
        }
        selectRet.close();
    }
    checkpanic testDB.stop();
    return retVal;
}
