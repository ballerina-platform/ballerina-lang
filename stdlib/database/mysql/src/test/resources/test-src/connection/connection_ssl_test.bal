// Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied. See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/filepath;
import ballerina/mysql;

//TODO:Remove this and pass with functions.
//After fixing this https://github.com/ballerina-platform/ballerina-lang/issues/21259
string host = "localhost";
string user = "test";
string password = "test123";
string database = "SSL_CONNECT_DB";
int port = 3305;

string clientStorePath = check filepath:absolute("src/test/resources/keystore/client/client-keystore.p12");
string turstStorePath = check filepath:absolute("src/test/resources/keystore/client/trust-keystore.p12");

function testSSLVerifyCert() returns error? {
    mysql:Options options = {
        ssl: {
            mode: "VERIFY_CERT",
            clientCertKeystore: {
                path: clientStorePath,
                password: "changeit"
            },
            trustCertKeystore: {
                path: turstStorePath,
                password: "changeit"
            }
        }
    };
    mysql:Client dbClient = check new (user = user, password = password, database = database,
        port = port, options = options);
    return dbClient.close();
}

function testSSLPreferred() returns error? {
    mysql:Options options = {
        ssl: {
            mode: "PREFERRED",
            clientCertKeystore: {
                path: clientStorePath,
                password: "changeit"
            },
            trustCertKeystore: {
                path: turstStorePath,
                password: "changeit"
            }
        }
    };
    mysql:Client dbClient = check new (user = user, password = password, database = database,
        port = port, options = options);
    return dbClient.close();
}

function testSSLRequiredWithClientCert() returns error? {
    mysql:Options options = {
        ssl: {
            mode: "REQUIRED",
            clientCertKeystore: {
                path: clientStorePath,
                password: "changeit"
            }
        }
    };
    mysql:Client dbClient = check new (user = user, password = password, database = database,
        port = port, options = options);
    return dbClient.close();
}

function testSSLVerifyIdentity() returns error? {
    mysql:Options options = {
        ssl: {
            mode: "VERIFY_IDENTITY",
            clientCertKeystore: {
                path: clientStorePath,
                password: "changeit"
            },
            trustCertKeystore: {
                path: turstStorePath,
                password: "changeit"
            }
        }
    };
    mysql:Client dbClient = check new (user = user, password = password, database = database,
        port = port, options = options);
    return dbClient.close();
}
