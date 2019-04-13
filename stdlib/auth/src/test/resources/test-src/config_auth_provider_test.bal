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

import ballerina/auth;
import ballerina/encoding;

function testCreateConfigAuthProvider() returns auth:ConfigAuthStoreProvider {
    auth:ConfigAuthStoreProvider configAuthStoreProvider = new;
    return configAuthStoreProvider;
}

function testAuthenticationOfNonExistingUser() returns boolean|error {
    auth:ConfigAuthStoreProvider configAuthStoreProvider = new;
    string usernameAndPassword = "amila:abc";
    string credential = encoding:encodeBase64(usernameAndPassword.toByteArray("UTF-8"));
    return configAuthStoreProvider.authenticate(credential);
}

function testAuthenticationOfNonExistingPassword() returns boolean|error {
    auth:ConfigAuthStoreProvider configAuthStoreProvider = new;
    string usernameAndPassword = "isuru:xxy";
    string credential = encoding:encodeBase64(usernameAndPassword.toByteArray("UTF-8"));
    return configAuthStoreProvider.authenticate(credential);
}

function testAuthentication() returns boolean|error {
    auth:ConfigAuthStoreProvider configAuthStoreProvider = new;
    string usernameAndPassword = "isuru:xxx";
    string credential = encoding:encodeBase64(usernameAndPassword.toByteArray("UTF-8"));
    return configAuthStoreProvider.authenticate(credential);
}

function testAuthenticationWithEmptyUsername() returns boolean|error {
    auth:ConfigAuthStoreProvider configAuthStoreProvider = new;
    string usernameAndPassword = ":xxx";
    string credential = encoding:encodeBase64(usernameAndPassword.toByteArray("UTF-8"));
    return configAuthStoreProvider.authenticate(credential);
}

function testAuthenticationWithEmptyPassword() returns boolean|error {
    auth:ConfigAuthStoreProvider configAuthStoreProvider = new;
    string usernameAndPassword = "isuru:";
    string credential = encoding:encodeBase64(usernameAndPassword.toByteArray("UTF-8"));
    return configAuthStoreProvider.authenticate(credential);
}

function testAuthenticationWithEmptyPasswordAndInvalidUsername() returns boolean|error {
    auth:ConfigAuthStoreProvider configAuthStoreProvider = new;
    string usernameAndPassword = "invalid:";
    string credential = encoding:encodeBase64(usernameAndPassword.toByteArray("UTF-8"));
    return configAuthStoreProvider.authenticate(credential);
}

function testAuthenticationWithEmptyUsernameAndEmptyPassword() returns boolean|error {
    auth:ConfigAuthStoreProvider configAuthStoreProvider = new;
    string usernameAndPassword = ":";
    string credential = encoding:encodeBase64(usernameAndPassword.toByteArray("UTF-8"));
    return configAuthStoreProvider.authenticate(credential);
}

function testAuthenticationSha256() returns boolean|error {
    auth:ConfigAuthStoreProvider configAuthStoreProvider = new;
    string usernameAndPassword = "hashedSha256:xxx";
    string credential = encoding:encodeBase64(usernameAndPassword.toByteArray("UTF-8"));
    return configAuthStoreProvider.authenticate(credential);
}

function testAuthenticationSha384() returns boolean|error {
    auth:ConfigAuthStoreProvider configAuthStoreProvider = new;
    string usernameAndPassword = "hashedSha384:xxx";
    string credential = encoding:encodeBase64(usernameAndPassword.toByteArray("UTF-8"));
    return configAuthStoreProvider.authenticate(credential);
}

function testAuthenticationSha512() returns boolean|error {
    auth:ConfigAuthStoreProvider configAuthStoreProvider = new;
    string usernameAndPassword = "hashedSha512:xxx";
    string credential = encoding:encodeBase64(usernameAndPassword.toByteArray("UTF-8"));
    return configAuthStoreProvider.authenticate(credential);
}

function testAuthenticationPlain() returns boolean|error {
    auth:ConfigAuthStoreProvider configAuthStoreProvider = new;
    string usernameAndPassword = "plain:plainpassword";
    string credential = encoding:encodeBase64(usernameAndPassword.toByteArray("UTF-8"));
    return configAuthStoreProvider.authenticate(credential);
}

function testAuthenticationSha512Negative() returns boolean|error {
    auth:ConfigAuthStoreProvider configAuthStoreProvider = new;
    string usernameAndPassword = "hashedSha512:xxx ";
    string credential = encoding:encodeBase64(usernameAndPassword.toByteArray("UTF-8"));
    return configAuthStoreProvider.authenticate(credential);
}

function testAuthenticationPlainNegative() returns boolean|error {
    auth:ConfigAuthStoreProvider configAuthStoreProvider = new;
    string usernameAndPassword = "plain:plainpassword ";
    string credential = encoding:encodeBase64(usernameAndPassword.toByteArray("UTF-8"));
    return configAuthStoreProvider.authenticate(credential);
}
