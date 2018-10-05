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

@final string BASE_PATH = "/websub";
@final string HUB_PATH = "/hub";

@final int DEFAULT_LEASE_SECONDS_VALUE = 86400; //one day
@final string DEFAULT_SIGNATURE_METHOD = "SHA256";

//TODO: Fix persistence configs, H2?
@final string DEFAULT_DB_URL = "jdbc:mysql://localhost:3306/subscriptionsdb";
@final string DEFAULT_DB_USERNAME = "ballerina";
@final string DEFAULT_DB_PASSWORD = "ballerina";

@readonly int hubPort;
@readonly int hubLeaseSeconds;
@readonly string hubSignatureMethod;
@readonly boolean hubRemotePublishingEnabled;
@readonly RemotePublishMode hubRemotePublishMode = PUBLISH_MODE_DIRECT;
@readonly boolean hubTopicRegistrationRequired;
@readonly string hubPublicUrl;

@final boolean hubPersistenceEnabled = config:getAsBoolean("b7a.websub.hub.enablepersistence");
@final string hubDatabaseUrl = config:getAsString("b7a.websub.hub.db.url", default = DEFAULT_DB_URL);
@final string hubDatabaseUsername = config:getAsString("b7a.websub.hub.db.username", default = DEFAULT_DB_USERNAME);
@final string hubDatabasePassword = config:getAsString("b7a.websub.hub.db.password", default = DEFAULT_DB_PASSWORD);
//TODO:add pool options

@readonly boolean hubSslEnabled;
@readonly http:ServiceSecureSocket? hubServiceSecureSocket = ();
@readonly http:SecureSocket? hubClientSecureSocket = ();

documentation {
    Function to bind and start the Ballerina WebSub Hub service.
}
function startHubService() returns http:Listener {
    http:Listener hubServiceEP = new;
    hubServiceEP.init({
            port:hubPort,
            secureSocket:hubServiceSecureSocket
    });
    hubServiceEP.register(hubService);
    hubServiceEP.start();
    return hubServiceEP;
}

documentation {
    Function to retrieve the URL for the Ballerina WebSub Hub, to which potential subscribers need to send
    subscription/unsubscription requests.

    R{{}} The WebSub Hub's URL
}
function getHubUrl() returns string {
    match (hubServiceSecureSocket) {
        http:ServiceSecureSocket => { return "https://localhost:" + hubPort + BASE_PATH + HUB_PATH; }
        () => { return "http://localhost:" + hubPort + BASE_PATH + HUB_PATH; }
    }
}

documentation {
    Function to retrieve if persistence is enabled for the Hub.

    R{{}} True if persistence is enabled, false if not
}
function isHubPersistenceEnabled() returns boolean {
    return hubPersistenceEnabled;
}

documentation {
    Function to retrieve if topics need to be registered at the Hub prior to publishing/subscribing.

    R{{}} True if persistence is enabled, false if not
}
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
        match (currentServiceSecureSocket) {
            http:ServiceSecureSocket serviceSecureSocketAsParam => return serviceSecureSocketAsParam;
            () => {
                keyStoreFilePath = "${ballerina.home}/bre/security/ballerinaKeystore.p12";
                keyStorePassword = "ballerina";
            }
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
        match (currentSecureSocket) {
            http:SecureSocket secureSocketAsParam => return secureSocketAsParam;
            () => {
                trustStoreFilePath = "${ballerina.home}/bre/security/ballerinaTruststore.p12";
                trustStorePassword = "ballerina";
            }
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
