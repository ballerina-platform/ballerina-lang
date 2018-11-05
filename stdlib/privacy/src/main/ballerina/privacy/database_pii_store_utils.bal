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

import ballerina/io;

# Represents personally identifiable information (PII) stored in a database
#
# + pii - personally identifiable information
type PiiData record {
    string pii;
    !...
};

# Build insert query based on the table name and column names
#
# + tableName - table name used to store PII
# + idColumn - column name used to store pseudonymized identifier
# + piiColumn - column name used to store PII
# + return - insert query
function buildInsertQuery (string tableName, string idColumn, string piiColumn) returns string {
    return io:sprintf("INSERT INTO %s (%s, %s) VALUES (?, ?)", tableName, idColumn, piiColumn);
}

# Build select query based on the table name and column names
#
# + tableName - table name used to store PII
# + idColumn - column name used to store pseudonymized identifier
# + piiColumn - column name used to store PII
# + return - select query
function buildSelectQuery (string tableName, string idColumn, string piiColumn) returns string {
    return io:sprintf("SELECT %s FROM %s WHERE %s = ?", piiColumn, tableName, idColumn);
}

# Build delete query based on the table name and column names
#
# + tableName - table name used to store PII
# + idColumn - column name used to store pseudonymized identifier
# + return - delete query
function buildDeleteQuery (string tableName, string idColumn) returns string {
    return io:sprintf("DELETE FROM %s WHERE %s = ?", tableName, idColumn);
}

# Validate the table name and column names and throw errors if validation errors are present
#
# + tableName - table name used to store PII
# + idColumn - column name used to store pseudonymized identifier
# + piiColumn - column name used to store PII
function validateFieldName (string tableName, string idColumn, string piiColumn) {
    if (tableName == "") {
        error err = { message: "Table name is required" };
        throw err;
    }
    if (idColumn == "") {
        error err = { message: "ID column name is required" };
        throw err;
    }
    if (piiColumn == "") {
        error err = { message: "PII column name is required" };
        throw err;
    }
}

# Process results of the insert query
#
# + id - pseudonymized identifier getting inserted
# + queryResult - results of the insert query
# + return - pseudonymized identifier if insert was successful, error if insert failed
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

# Process results of the select query
#
# + id - pseudonymized identifier getting selected
# + queryResult - results of the select query
# + return - personally identifiable information (PII) if select was successful, error if select failed
function processSelectResult(string id, table<PiiData>|error queryResult) returns string|error {
    match queryResult {
        table resultTable => {
            if (resultTable.hasNext()) {
                PiiData piiData = check <PiiData> resultTable.getNext();
                resultTable.close();
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

# Process results of the delete query
#
# + id - pseudonymized identifier getting deleted
# + queryResult - results of the delete query
# + return - nil if deletion was successful, error if deletion failed
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
