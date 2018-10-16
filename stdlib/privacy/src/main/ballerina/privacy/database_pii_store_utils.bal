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

type PiiData record {
    string pii;
};

function buildInsertQuery (string tableName, string idColumn, string piiColumn) returns string {
    return "INSERT INTO `" + tableName + "` (`" + idColumn + "`,`" + piiColumn + "`) VALUES (?, ?)";
}

function buildSelectQuery (string tableName, string idColumn, string piiColumn) returns string {
    return "SELECT `" + piiColumn + "` FROM `" + tableName + "` WHERE `" + idColumn + "` = ?";
}

function buildDeleteQuery (string tableName, string idColumn) returns string {
    return "DELETE FROM `" + tableName + "` WHERE `" + idColumn + "` = ?";
}

function validateFieldName (string inputData) {
    if (inputData.contains("`")) {
        error validationError = {
            message: "invalid character found in '" + inputData + "'"
        };
        throw validationError;
    }
}

function processDeleteResult (string id, int|error queryResult) returns error? {
    match queryResult {
        int rowCount => {
            if (rowCount > 0) {
                return ();
            } else {
                error err = { message: "Identifier " + id + " is not found in PII store" };
                return err;
            }
        }
        error err => {
            return err;
        }
    }
}

function processInsertResult (string id, int|error queryResult) returns string|error {
    match queryResult {
        int rowCount => {
            if (rowCount > 0) {
                return id;
            } else {
                error err = { message: "Unable to insert PII with identifier " + id };
                return err;
            }
        }
        error err => {
            return err;
        }
    }
}

function processSelectResult(string id, table<PiiData>|error queryResult) returns string|error {
    match queryResult {
        table resultTable => {
            if (resultTable.hasNext()) {
                PiiData piiData = check <PiiData> resultTable.getNext();
                return piiData.pii;
            } else {
                error err = { message: "Identifier " + id + " is not found in PII store" };
                return err;
            }
        }
        error err => {
            return err;
        }
    }
}
