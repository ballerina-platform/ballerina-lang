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
import ballerina/privacy;

endpoint h2:Client testDB {
    path: "./target/H2PIIStore/",
    name: "TestDBH2",
    username: "SA",
    password: "",
    poolOptions: { maximumPoolSize: 1 }
};

@final string TABLE_NAME = "PII_STORE";
@final string ID_CLOUMN = "id";
@final string PII_COLUMN = "pii";

function pseudonymizePii (string pii) returns string|error {
    privacy:H2PiiStore piiStore = new(testDB, TABLE_NAME, ID_CLOUMN, PII_COLUMN);
    return privacy:pseudonymize(piiStore, pii);
}

function depseudonymizePii (string id) returns string|error {
    privacy:H2PiiStore piiStore = new(testDB, TABLE_NAME, ID_CLOUMN, PII_COLUMN);
    return privacy:depseudonymize(piiStore, id);
}

function deletePii (string id) returns error? {
    privacy:H2PiiStore piiStore = new(testDB, TABLE_NAME, ID_CLOUMN, PII_COLUMN);
    return privacy:delete(piiStore, id);
}

function pseudonymizePiiWithEmptyTableName (string pii) returns string|error {
    privacy:H2PiiStore piiStore = new(testDB, "", ID_CLOUMN, PII_COLUMN);
    return privacy:pseudonymize(piiStore, pii);
}
