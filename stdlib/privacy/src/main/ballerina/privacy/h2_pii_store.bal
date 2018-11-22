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
import ballerina/system;

# Represents personally identifiable information (PII) storage mechanisum based on H2 database
#
# + clientEndpoint - reference to H2 database client endpoint
# + tableName - table name used to store PII
# + idColumn - column name used to store pseudonymized identifier
# + piiColumn - column name used to store PII
public type H2PiiStore object {
    public h2:Client clientEndpoint;
    public string tableName;
    public string idColumn;
    public string piiColumn;

    # Create personally identifiable information (PII) storage mechanisum based on H2 database
    #
    # + clientEndpoint - reference to H2 database client endpoint
    # + tableName - table name used to store PII
    # + idColumn - column name used to store pseudonymized identifier
    # + piiColumn - column name used to store PII
    public new (clientEndpoint, tableName, idColumn, piiColumn) {
        validateFieldName(tableName, idColumn, piiColumn);
    }

    # Pseudonymize personally identifiable information (PII) and store PII and the pseudonymized identifier
    #
    # + pii - PII to be pseudonymized
    # + return - 36 characters long UUID if storage operation was successful, error if storage operation failed
    public function pseudonymize (string pii) returns string|error {
        endpoint h2:Client client = self.clientEndpoint;
        string dbQuery = buildInsertQuery(self.tableName, self.idColumn, self.piiColumn);
        string id = system:uuid();
        var queryResult = client->update(dbQuery, id, pii);
        return processInsertResult(id, queryResult);
    }

    # Depseudonymize the identifier by retrieving the personally identifiable information (PII)
    #
    # + id - pseudonymized identifier to be depseudonymize
    # + return - PII if retrieval was successful, error if retrieval failed
    public function depseudonymize (string id) returns string|error {
        endpoint h2:Client client = self.clientEndpoint;
        string dbQuery = buildSelectQuery(self.tableName, self.idColumn, self.piiColumn);
        var queryResult = client->select(dbQuery, PiiData, id);
        return processSelectResult(id, queryResult);
    }

    # Delete personally identifiable information (PII)
    #
    # + id - pseudonymized identifier to be deleted
    # + return - nil if retrieval was successful, error if retrieval failed
    public function delete (string id) returns error? {
        endpoint h2:Client client = self.clientEndpoint;
        string dbQuery = buildDeleteQuery(self.tableName, self.idColumn);
        var queryResult = client->update(dbQuery, id);
        return processDeleteResult(id, queryResult);
    }

};
