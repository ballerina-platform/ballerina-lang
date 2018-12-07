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

import ballerina/sql;
import ballerina/h2;
import ballerina/io;

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

function testWrongOrderInt() {
    testWrongOrder("SELECT int_type, long_type, float_type, boolean_type, string_type, double_type from DataTable WHERE row_id = 1");
}

function testWrongOrderString() {
    testWrongOrder("SELECT int_type, long_type, float_type, boolean_type, string_type, double_type from DataTable WHERE row_id = 1");
}

function testWrongOrderBoolean() {
    testWrongOrder("SELECT boolean_type, long_type, float_type, string_type, int_type, double_type from DataTable WHERE row_id = 1");
}

function testWrongOrderDouble() {
    testWrongOrder("SELECT double_type, long_type, float_type, string_type, int_type, boolean_type from DataTable WHERE row_id = 1");
}

function testWrongOrderFloat() {
    testWrongOrder("SELECT double_type, long_type, float_type, string_type, int_type, boolean_type from DataTable WHERE row_id = 1");
}

function testWrongOrderLong() {
    testWrongOrder("SELECT boolean_type, string_type, float_type, long_type, int_type, double_type from DataTable WHERE row_id = 1");
}

function testWrongOrderBlobWrongOrder() {
    h2:Client testDB = new({
            path: "./target/tempdb/",
            name: "TEST_DATA_TABLE_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    var selectRet = testDB->select("SELECT blob_type, row_id from ComplexTypes WHERE row_id = 1", ResultBlobWrongOrder);

    if (selectRet is table<ResultBlobWrongOrder>) {
        while (selectRet.hasNext()) {
            var rs = selectRet.getNext();
        }
    }
    testDB.stop();
}

function testWrongOrderBlobCorrectOrderWrongType() {
    h2:Client testDB = new({
            path: "./target/tempdb/",
            name: "TEST_DATA_TABLE_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    var selectRet = testDB->select("SELECT blob_type, row_id from ComplexTypes WHERE row_id = 1",
        ResultBlobCorrectOrderWrongType);

    if (selectRet is table<ResultBlobCorrectOrderWrongType>) {
        while (selectRet.hasNext()) {
            var rs = selectRet.getNext();
        }
    }
    testDB.stop();
}

function testGreaterNoOfParams() {
    h2:Client testDB = new({
            path: "./target/tempdb/",
            name: "TEST_DATA_TABLE_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    var selectRet = testDB->select("SELECT boolean_type from DataTable WHERE row_id = 1", Result);

    if (selectRet is table<Result>) {
        while (selectRet.hasNext()) {
            var rs = selectRet.getNext();
        }
    }
    testDB.stop();
}

function testLowerNoOfParams() {
    h2:Client testDB = new({
            path: "./target/tempdb/",
            name: "TEST_DATA_TABLE_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    var selectRet = testDB->select("SELECT boolean_type, boolean_type, string_type, float_type, long_type, int_type,
        double_type from DataTable WHERE row_id = 1", Result);

    if (selectRet is table<Result>) {
        while (selectRet.hasNext()) {
            var rs = selectRet.getNext();
        }
    }
    testDB.stop();
}

function testWrongOrder(string queryStr) {
    h2:Client testDB = new({
            path: "./target/tempdb/",
            name: "TEST_DATA_TABLE_H2",
            username: "SA",
            password: "",
            poolOptions: { maximumPoolSize: 1 }
        });

    var selectRet = testDB->select(queryStr, Result);

    if (selectRet is table<Result>) {
        while (selectRet.hasNext()) {
            var rs = selectRet.getNext();
        }
    }
    testDB.stop();
}
