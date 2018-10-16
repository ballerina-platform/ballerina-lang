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
import ballerina/sql;
import ballerina/system;

public type H2PiiStore object {
    public h2:Client clientEndpoint;
    public string tableName;
    public string idColumn;
    public string piiColumn;

    public new (clientEndpoint, tableName, idColumn, piiColumn) {
        validateFieldName(tableName);
        validateFieldName(idColumn);
        validateFieldName(piiColumn);
    }

    public function pseudonymize (string pii) returns string|error {
        endpoint h2:Client client = clientEndpoint;
        string dbQuery = buildInsertQuery(tableName, idColumn, piiColumn);
        string id = system:uuid();
        sql:Parameter paramId = {sqlType: sql:TYPE_VARCHAR, value: id};
        sql:Parameter paramPii = {sqlType: sql:TYPE_VARCHAR, value: pii};
        var queryResult = client->update(dbQuery, paramId, paramPii);
        return processInsertResult(id, queryResult);
    }

    public function depseudonymize (string id) returns string|error {
        endpoint h2:Client client = clientEndpoint;
        string dbQuery = buildSelectQuery(tableName, idColumn, piiColumn);
        sql:Parameter paramId = {sqlType: sql:TYPE_VARCHAR, value: id};
        var queryResult = client->select(dbQuery, PiiData, paramId);
        return processSelectResult(id, queryResult);
    }

    public function delete (string id) returns error? {
        endpoint h2:Client client = clientEndpoint;
        string dbQuery = buildDeleteQuery(tableName, idColumn);
        sql:Parameter paramId = {sqlType: sql:TYPE_VARCHAR, value: id};
        var queryResult = client->update(dbQuery, paramId);
        return processDeleteResult(id, queryResult);
    }
};
