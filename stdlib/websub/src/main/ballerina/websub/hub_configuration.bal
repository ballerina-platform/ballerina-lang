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

import ballerina/config;
import ballerina/http;
import ballerina/log;

const string BASE_PATH = "/websub";
const string HUB_PATH = "/hub";

const string DEFAULT_HOST = "0.0.0.0";
const int DEFAULT_LEASE_SECONDS_VALUE = 86400; //one day
const string DEFAULT_SIGNATURE_METHOD = "SHA256";

//TODO: Fix persistence configs, H2?
const string DEFAULT_DB_DIRECTORY = "/tmp/websubdb";
const string DEFAULT_DB_NAME = "HUB_DB";
const string DEFAULT_DB_USERNAME = "sa";
const string DEFAULT_DB_PASSWORD = "";

@readonly string hubHost = DEFAULT_HOST;
@readonly int hubPort = 0;
@readonly int hubLeaseSeconds = DEFAULT_LEASE_SECONDS_VALUE;
@readonly string hubSignatureMethod = DEFAULT_SIGNATURE_METHOD;
@readonly boolean hubRemotePublishingEnabled = false;
@readonly RemotePublishMode hubRemotePublishMode = PUBLISH_MODE_DIRECT;
@readonly boolean hubTopicRegistrationRequired = false;
@readonly string hubPublicUrl = "";

@final boolean hubPersistenceEnabled = config:getAsBoolean("b7a.websub.hub.enablepersistence");
@final string hubDatabaseDirectory = config:getAsString("b7a.websub.hub.db.directory", default = DEFAULT_DB_DIRECTORY);
@final string hubDatabaseName = config:getAsString("b7a.websub.hub.db.name", default = DEFAULT_DB_NAME);
@final string hubDatabaseUsername = config:getAsString("b7a.websub.hub.db.username", default = DEFAULT_DB_USERNAME);
@final string hubDatabasePassword = config:getAsString("b7a.websub.hub.db.password", default = DEFAULT_DB_PASSWORD);
//TODO:add pool options

@readonly boolean hubSslEnabled = false;
@readonly http:ServiceSecureSocket? hubServiceSecureSocket = ();
@readonly http:SecureSocket? hubClientSecureSocket = ();

# Function to bind and start the Ballerina WebSub Hub service.
#
# + return - The `http:Listener` to which the service is bound
function startHubService() returns http:Listener {
    http:Listener hubServiceEP = new;
    hubServiceEP.init({
            host: hubHost,
            port: hubPort,
            secureSocket: hubServiceSecureSocket
    });
    hubServiceEP.register(hubService);
    hubServiceEP.start();
    return hubServiceEP;
}

# Function to retrieve the URL for the Ballerina WebSub Hub, to which potential subscribers need to send
# subscription/unsubscription requests.
#
# + return - The WebSub Hub's URL
function getHubUrl() returns string {
    return hubServiceSecureSocket is http:ServiceSecureSocket ? ("https://localhost:" + hubPort + BASE_PATH + HUB_PATH)
                : ("http://localhost:" + hubPort + BASE_PATH + HUB_PATH);
}

# Function to retrieve if persistence is enabled for the Hub.
#
# + return - True if persistence is enabled, false if not
function isHubPersistenceEnabled() returns boolean {
    return hubPersistenceEnabled;
}

# Function to retrieve if topics need to be registered at the Hub prior to publishing/subscribing.
#
# + return - True if persistence is enabled, false if not
function isHubTopicRegistrationRequired() returns boolean {
    return hubTopicRegistrationRequired;
}

function getServiceSecureSocketConfig(http:ServiceSecureSocket? currentServiceSecureSocket) returns
                                                                                          http:ServiceSecureSocket? {
    if (!hubSslEnabled) {
        return;
    }

    string keyStoreFilePath = config:getAsString("b7a.websub.hub.ssl.key_store.file_path");
    string keyStorePassword = config:getAsString("b7a.websub.hub.ssl.key_store.password");

    if (keyStoreFilePath == "") {
        if (currentServiceSecureSocket is http:ServiceSecureSocket) {
            return currentServiceSecureSocket;
        } else {
            keyStoreFilePath = "${ballerina.home}/bre/security/ballerinaKeystore.p12";
            keyStorePassword = "ballerina";
        }
    }

    http:ServiceSecureSocket newServiceSecureSocket = {
        keyStore:{
            path:keyStoreFilePath, password:keyStorePassword
        }
    };
    return newServiceSecureSocket;
}

function getSecureSocketConfig(http:SecureSocket? currentSecureSocket) returns http:SecureSocket? {
    string trustStoreFilePath;
    string trustStorePassword;

    if (!hubSslEnabled) {
        trustStoreFilePath = config:getAsString("b7a.websub.hub.ssl.trust_store.file_path");
        trustStorePassword = config:getAsString("b7a.websub.hub.ssl.trust_store.password");

        if (trustStoreFilePath == "") {
            return currentSecureSocket;
        }
        http:SecureSocket newSecureSocket = {
            trustStore:{
                path:trustStoreFilePath, password:trustStorePassword
            },
            verifyHostname:false
        };
        return newSecureSocket;
    }

    trustStoreFilePath = config:getAsString("b7a.websub.hub.ssl.trust_store.file_path");
    trustStorePassword = config:getAsString("b7a.websub.hub.ssl.trust_store.password");

    if (trustStoreFilePath == "") {
        if (currentSecureSocket is http:SecureSocket) {
            return currentSecureSocket;
        } else {
            trustStoreFilePath = "${ballerina.home}/bre/security/ballerinaTruststore.p12";
            trustStorePassword = "ballerina";
        }
    }

    http:SecureSocket newSecureSocket = {
        trustStore:{
            path:trustStoreFilePath, password:trustStorePassword
        },
        verifyHostname:false
    };
    return newSecureSocket;
}
