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
package ballerina.websub.hub;

import ballerina/config;
import ballerina/http;
import ballerina/log;
import ballerina/websub;

@final string BASE_PATH = "/websub";
@final string HUB_PATH = "/hub";

@final string DEFAULT_HOST = "localhost";

@final int DEFAULT_PORT = 9292;
@final int DEFAULT_LEASE_SECONDS_VALUE = 86400000; //one day
@final string DEFAULT_SIGNATURE_METHOD = "SHA256";

@final string DEFAULT_DB_USERNAME = "ballerina";
@final string DEFAULT_DB_PASSWORD = "ballerina";

@final string hubHost = config:getAsString("hub.host", default=DEFAULT_HOST);
@final int hubPort = config:getAsInt("hub.port", default=DEFAULT_PORT);
@final int hubLeaseSeconds = config:getAsInt("hub.lease_seconds", default=DEFAULT_LEASE_SECONDS_VALUE);
@final string hubSignatureMethod = config:getAsString("hub.signature_method", default=DEFAULT_SIGNATURE_METHOD);
@final boolean hubRemotePublishingEnabled = config:getAsBoolean("hub.remote_publishing.enabled");
@final string hubRemotePublishingMode = config:getAsString("hub.remote_publishing.mode",
                                                                        default=websub:REMOTE_PUBLISHING_MODE_DIRECT);
@final boolean hubTopicRegistrationRequired = config:getAsBoolean("hub.topic_registration.required", default=true);
@final string hubPublicUrl = config:getAsString("hub.url", default=getHubUrl());

@final boolean hubPersistenceEnabled = config:getAsBoolean("hub.persistence.enabled");
@final string hubDatabaseUrl = config:getAsString("hub.db.url", default=DEFAULT_HOST);
@final string hubDatabaseUsername = config:getAsString("hub.db.username", default=DEFAULT_DB_USERNAME);
@final string hubDatabasePassword = config:getAsString("hub.db.password", default=DEFAULT_DB_PASSWORD);
//TODO:add pool options

@final boolean hubSslEnabled = config:getAsBoolean("hub.ssl.enabled", default=true);
http:ServiceSecureSocket? serviceSecureSocket = getServiceSecureSocketConfig();
http:SecureSocket? secureSocket = getSecureSocketConfig();

@Description {value:"Function to retrieve the URL for the Ballerina WebSub Hub, to which potential subscribers need to
                    send subscription/unsubscription requests."}
@Return {value:"The WebSub Hub's URL"}
function getHubUrl () returns (string) {
    match (serviceSecureSocket) {
        http:ServiceSecureSocket => { return "https://" + hubHost + ":" + hubPort + BASE_PATH + HUB_PATH; }
        () => { return "http://" + hubHost + ":" + hubPort + BASE_PATH + HUB_PATH; }
    }
}

@Description {value:"Function to retrieve if persistence is enabled for the Hub."}
@Return {value:"True if persistence is enabled, false if not"}
function isHubPersistenceEnabled () returns (boolean) {
    return hubPersistenceEnabled;
}

@Description {value:"Function to retrieve if topics need to be registered at the Hub prior to publishing/subscribing."}
@Return {value:"True if persistence is enabled, false if not"}
function isHubTopicRegistrationRequired () returns (boolean) {
    return hubTopicRegistrationRequired;
}

function getServiceSecureSocketConfig() returns (http:ServiceSecureSocket | ()) {
    if (!hubSslEnabled) {
        return;
    }

    string keyStoreFilePath = config:getAsString("hub.ssl.key_store.file_path",
                                                default="${ballerina.home}/bre/security/ballerinaKeystore.p12");
    string keyStorePassword = config:getAsString("hub.ssl.key_store.password", default="ballerina");
    http:ServiceSecureSocket serviceSecureSocket =
                                    { keyStore: { filePath: keyStoreFilePath, password: keyStorePassword } };
    return serviceSecureSocket;
}

function getSecureSocketConfig() returns (http:SecureSocket | ()) {
    string trustStoreFilePath;
    string trustStorePassword;

    if (!hubSslEnabled) {
        trustStoreFilePath = config:getAsString("hub.ssl.trust_store.file_path");
        if (trustStoreFilePath == "") {
            return;
        }
        trustStorePassword = config:getAsString("hub.ssl.trust_store.password");
        if (trustStorePassword == "") {
            log:printWarn("Ignoring trust store file since password is not specified.");
            return;
        }
        http:SecureSocket secureSocket = {
            trustStore: { filePath: trustStoreFilePath, password: trustStorePassword},
            verifyHostname: false
        };
        return secureSocket;
    }

    trustStoreFilePath = config:getAsString("hub.ssl.trust_store.file_path",
                                                    default="${ballerina.home}/bre/security/ballerinaTruststore.p12");
    trustStorePassword = config:getAsString("hub.ssl.trust_store.password", default="ballerina");
    http:SecureSocket secureSocket = { trustStore: { filePath: trustStoreFilePath, password: trustStorePassword},
                                        verifyHostname: false};
    return secureSocket;
}
